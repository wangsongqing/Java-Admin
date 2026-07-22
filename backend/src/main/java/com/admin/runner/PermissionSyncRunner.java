package com.admin.runner;

import com.admin.config.PermissionProperties;
import com.admin.entity.Permission;
import com.admin.enums.StatusEnum;
import com.admin.mapper.PermissionMapper;
import com.admin.mapper.RolePermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限数据同步器
 *
 * <p>应用启动时（Flyway 迁移完成后）自动执行，将 {@code permissions.yml} 中定义的权限
 * 同步到 {@code sys_permission} 表。</p>
 *
 * <p><b>幂等策略</b>：以 {@code code} 为业务唯一键。
 * <ul>
 *   <li>数据库不存在该 code → 插入</li>
 *   <li>数据库已存在该 code → 更新（name、type、parentId、icon 等字段）</li>
 *   <li>YAML 中不存在但数据库存在 → 由 {@code permissions.sync-delete} 配置决定是否级联删除</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionSyncRunner implements CommandLineRunner {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionProperties permissionProperties;

    @Override
    public void run(String... args) {
        List<PermissionProperties.PermissionDefinition> definitions = permissionProperties.getDefinitions();

        if (definitions == null || definitions.isEmpty()) {
            log.info("⚙️ permissions.yml 未定义任何权限，跳过同步");
            return;
        }

        log.info("========== 开始同步权限配置 ==========");
        log.info("📋 配置文件中共 {} 个权限定义", definitions.size());

        // 建立 code → parentId 映射，用于解析 parentCode
        Map<String, Long> codeToIdMap = new HashMap<>();

        // 收集 YAML 中所有有效的 code（用于后续判断哪些需要删除）
        Set<String> yamlCodes = new HashSet<>();

        // 第一轮：同步所有权限（插入或更新）
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        for (PermissionProperties.PermissionDefinition def : definitions) {
            // 校验必填字段
            if (def.getCode() == null || def.getCode().isBlank()) {
                log.warn("⚠️ 跳过一条缺少 code 的权限定义: {}", def);
                skipped++;
                continue;
            }
            if (def.getName() == null || def.getName().isBlank()) {
                log.warn("⚠️ 跳过一条缺少 name 的权限定义: code={}", def.getCode());
                skipped++;
                continue;
            }
            if (def.getType() == null || def.getType().isBlank()) {
                log.warn("⚠️ 跳过一条缺少 type 的权限定义: code={}", def.getCode());
                skipped++;
                continue;
            }

            yamlCodes.add(def.getCode());

            // 解析 parentId
            Long parentId = 0L;
            if (def.getParentCode() != null && !def.getParentCode().isBlank()) {
                parentId = codeToIdMap.getOrDefault(def.getParentCode(), 0L);
            }

            // 按 code 查询是否已存在
            Permission existing = permissionMapper.selectOne(
                    new LambdaQueryWrapper<Permission>().eq(Permission::getCode, def.getCode())
            );

            if (existing == null) {
                // 插入
                Permission permission = new Permission();
                permission.setParentId(parentId);
                permission.setCode(def.getCode());
                permission.setName(def.getName());
                permission.setType(def.getType());
                permission.setIcon(def.getIcon());
                permission.setPath(def.getPath());
                permission.setComponent(def.getComponent());
                permission.setSort(def.getSort() != null ? def.getSort() : 0);
                permission.setStatus(def.getStatus() != null ? def.getStatus() : StatusEnum.NORMAL.getCode());
                permissionMapper.insert(permission);

                // 记录新插入的 ID，供子权限引用
                codeToIdMap.put(def.getCode(), permission.getId());
                log.info("  ➕ 插入权限 [{}] {} (id={})", def.getCode(), def.getName(), permission.getId());
                inserted++;
            } else {
                // 更新
                boolean changed = false;

                if (!equalsLong(existing.getParentId(), parentId)) {
                    existing.setParentId(parentId);
                    changed = true;
                }
                if (!equalsStr(existing.getName(), def.getName())) {
                    existing.setName(def.getName());
                    changed = true;
                }
                if (!equalsStr(existing.getType(), def.getType())) {
                    existing.setType(def.getType());
                    changed = true;
                }
                if (!equalsStr(existing.getIcon(), def.getIcon())) {
                    existing.setIcon(def.getIcon());
                    changed = true;
                }
                if (!equalsStr(existing.getPath(), def.getPath())) {
                    existing.setPath(def.getPath());
                    changed = true;
                }
                if (!equalsStr(existing.getComponent(), def.getComponent())) {
                    existing.setComponent(def.getComponent());
                    changed = true;
                }
                if (!equalsInt(existing.getSort(), def.getSort())) {
                    existing.setSort(def.getSort() != null ? def.getSort() : 0);
                    changed = true;
                }
                if (!equalsInt(existing.getStatus(), def.getStatus())) {
                    existing.setStatus(def.getStatus() != null ? def.getStatus() : StatusEnum.NORMAL.getCode());
                    changed = true;
                }

                if (changed) {
                    permissionMapper.updateById(existing);
                    log.info("  🔄 更新权限 [{}] {} (id={})", def.getCode(), def.getName(), existing.getId());
                    updated++;
                }

                // 记录已有 ID，供子权限引用
                codeToIdMap.put(def.getCode(), existing.getId());
            }
        }

        log.info("---------- 增/改同步完成 ----------");
        log.info("   ✅ 插入: {}  🔄 更新: {}  ⏭️ 跳过: {}", inserted, updated, skipped);

        // 第二轮：级联删除（仅在 syncDelete=true 时执行）
        int deleted = 0;
        if (permissionProperties.isSyncDelete()) {
            deleted = performCascadeDelete(yamlCodes);
        } else {
            log.info("🔒 同步删除未开启（permissions.sync-delete=false），YAML 中移除的权限不会从数据库删除");
        }

        log.info("========== 权限同步全部完成 ==========");
        if (deleted > 0) {
            log.info("   🗑️ 级联删除: {}", deleted);
        }
    }

    /**
     * 级联删除 YAML 中已不存在的权限
     *
     * <p>删除顺序：先清除 sys_role_permission 关联，再删除 sys_permission 记录。
     * 父权限删除时，其所有子孙权限一并删除（级联）。</p>
     */
    private int performCascadeDelete(Set<String> yamlCodes) {
        // 查询数据库中所有未删除的权限
        List<Permission> allDbPermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
        );

        // 找出不在 YAML 中的权限
        List<Permission> orphanPermissions = allDbPermissions.stream()
                .filter(p -> !yamlCodes.contains(p.getCode()))
                .collect(Collectors.toList());

        if (orphanPermissions.isEmpty()) {
            log.info("🗑️ 同步删除：数据库与 YAML 一致，无需删除");
            return 0;
        }

        log.info("🗑️ 同步删除：发现 {} 个 YAML 中已不存在的权限，开始级联删除", orphanPermissions.size());

        // 构建 id → Permission 映射，方便查找子孙
        Map<Long, Permission> idToPermission = new HashMap<>();
        for (Permission p : allDbPermissions) {
            idToPermission.put(p.getId(), p);
        }

        // 对每个孤立权限，收集其所有子孙（递归）
        Set<Long> idsToDelete = new LinkedHashSet<>();
        for (Permission orphan : orphanPermissions) {
            collectDescendants(orphan.getId(), idToPermission, idsToDelete);
        }

        if (idsToDelete.isEmpty()) {
            return 0;
        }

        // 先删除角色-权限关联
        List<Long> deleteIdList = new ArrayList<>(idsToDelete);
        rolePermissionMapper.deleteByPermissionIds(deleteIdList);

        // 再删除权限记录
        permissionMapper.deleteBatchIds(deleteIdList);

        // 输出被删除的权限信息
        for (Long id : deleteIdList) {
            Permission p = idToPermission.get(id);
            if (p != null) {
                log.info("  🗑️ 已删除权限 [{}] {} (id={})", p.getCode(), p.getName(), p.getId());
            }
        }

        return deleteIdList.size();
    }

    /**
     * 递归收集某个权限及其所有子孙权限的 ID
     */
    private void collectDescendants(Long parentId, Map<Long, Permission> idToPermission, Set<Long> result) {
        if (!result.add(parentId)) {
            return; // 已处理过，防止循环引用
        }
        // 查找所有 parentId 等于当前 id 的子权限
        for (Permission p : idToPermission.values()) {
            if (parentId.equals(p.getParentId())) {
                collectDescendants(p.getId(), idToPermission, result);
            }
        }
    }

    // ---- 辅助比较方法 ----

    private boolean equalsStr(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private boolean equalsLong(Long a, Long b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private boolean equalsInt(Integer a, Integer b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
