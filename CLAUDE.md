# Java-Admin 项目文档

## 项目概述

Java-Admin 是一个前后端分离的后台管理系统，提供用户管理、角色管理、权限管理等基础 RBAC 能力。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.5 + Java 17
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.x（通过 Flyway 10.11.0 自动迁移建表）
- **权限认证**: Sa-Token 1.39.0（`sa-token-spring-boot3-starter`）
- **密码加密**: BCrypt（`spring-security-crypto`）
- **工具库**: Hutool 5.8.27、Lombok
- **API 前缀**: `/api`（`server.servlet.context-path`）

### 前端
- **框架**: Vue 3 + Vite 5
- **UI**: Element Plus 2.7（中文 locale）
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.3
- **HTTP**: Axios 1.6

## 目录结构

```
Java-Admin/
├── backend/                          # Spring Boot 后端
│   └── src/main/
│       ├── java/com/admin/
│       │   ├── AdminApplication.java # 启动类（@MapperScan("com.admin.mapper")）
│       │   ├── annotation/           # 自定义注解（已清空，原 NoAuth 已移除）
│       │   ├── common/               # 公共类（Result 统一响应）
│       │   ├── config/               # 配置类（Sa-Token、MyBatis-Plus、Flyway）
│       │   ├── controller/           # 控制器（Auth/User/Role/Permission）
│       │   ├── dto/                  # 数据传输对象
│       │   ├── entity/               # 实体类（User/Role/Permission/UserRole/RolePermission）
│       │   ├── exception/            # 异常处理（BusinessException + GlobalExceptionHandler）
│       │   ├── interceptor/          # 拦截器（已清空，原 JwtInterceptor 已移除）
│       │   ├── mapper/               # MyBatis Mapper 接口
│       │   └── service/              # 业务逻辑层
│       └── resources/
│           ├── db/migration/         # Flyway 迁移脚本（V1~V4）
│           ├── mapper/               # MyBatis XML（UserRoleMapper、RolePermissionMapper）
│           └── application.yml       # 主配置
└── frontend/                         # Vue 3 前端
    └── src/
        ├── api/                      # API 请求（auth/user/role/permission）
        ├── directives/               # 自定义指令（permission 按钮级权限）
        ├── router/                   # 路由配置（含权限守卫）
        ├── store/                    # Pinia 状态管理（user store）
        ├── utils/                    # 工具类（request axios 封装）
        ├── views/                    # 页面（login/layout/dashboard/user/role）
        └── main.js                   # 入口（注册 Pinia/Router/ElementPlus/指令）
```

## 数据库设计

### 核心表结构（Flyway 迁移）

| 表名 | 说明 | 迁移文件 |
|---|---|---|
| `sys_user` | 用户表 | `V1__Create_user_table.sql` |
| `sys_role` | 角色表 | `V3__Rbac_tables.sql` |
| `sys_permission` | 权限表（菜单/按钮/API 统一存储） | `V3__Rbac_tables.sql` |
| `sys_user_role` | 用户-角色关联表 | `V3__Rbac_tables.sql` |
| `sys_role_permission` | 角色-权限关联表 | `V3__Rbac_tables.sql` |

### 权限编码规范

- 菜单权限：`system`、`system:user`、`system:role`
- 按钮权限：`system:user:add`、`system:user:edit`、`system:user:delete`、`system:user:reset`、`system:user:status`、`system:role:add`、`system:role:edit`、`system:role:delete`、`system:role:assign`

### 初始数据

- **角色**: `ROLE_ADMIN`（超级管理员，全部权限）、`ROLE_USER`（普通用户，仅查看）
- **用户**: `admin/admin123`（管理员）、`test/123456`（普通用户）

## 权限体系（Sa-Token）

### 核心机制
- **认证**: `StpUtil.login(userId)` 登录，`StpUtil.logout()` 登出
- **鉴权注解**: `@SaCheckRole("ROLE_ADMIN")`、`@SaCheckPermission("system:user:add")`
- **权限加载**: `StpInterfaceImpl` 实现 `getPermissionList` 和 `getRoleList`，从数据库查询用户角色和权限
- **Token 传递**: 前端通过 `Authorization` 请求头传递 token（无 Bearer 前缀）

### 前端权限控制
- **路由守卫**: `router.beforeEach` 检查 `meta.permission`，无权限跳首页
- **菜单显示**: `v-if="userStore.hasPermission('system:xxx')"` 控制菜单项显隐
- **按钮权限**: `v-permission="'system:user:add'"` 自定义指令，无权限移除 DOM
- **403 处理**: 响应拦截器中 403 仅提示不跳转登录（区别于 401）

## 关键约定

- 统一响应格式：`Result { code: 200/400/401/403/500, message, data }`
- 逻辑删除：`deleted` 字段（0 正常，1 删除），由 MyBatis-Plus `@TableLogic` 自动处理
- 时间填充：`createTime`（INSERT 填充）、`updateTime`（INSERT_UPDATE 填充）
- 分页：`pageNum` + `pageSize`，返回 `{ records, total }`
- 密码：BCrypt 加密，`passwordEncoder.matches()` 校验，`passwordEncoder.encode()` 加密

## 权限配置（permissions.yml）

权限通过 `backend/src/main/resources/permissions.yml` 集中定义，应用启动时由
`PermissionSyncRunner`（CommandLineRunner）自动同步到 `sys_permission` 表。

### 操作方式
1. 在 `permissions.yml` 的 `permissions.definitions` 列表中新增/修改/删除一条定义
2. 重启应用即可（Flyway 迁移完成后自动执行同步）

### 改名
直接修改 YAML 中的 `name`，重启后自动 `UPDATE`（按 `code` 匹配）。

### 删除
默认不会删除（安全模式）。如需自动级联删除：
1. 将 `application.yml` 中的 `permissions.sync-delete` 设为 `true`
2. 从 `permissions.yml` 中移除对应权限定义
3. 重启应用 → 该权限及其所有子孙权限会自动从数据库清除（先清 `sys_role_permission` 关联，再删 `sys_permission` 记录）

> ⚠️ `sync-delete: true` 表示 YAML 是权限的唯一来源。开启前请确认没有通过其他方式（如管理界面）手动配置权限，否则会被误删。

### 幂等策略
- 按 `code` 匹配：已存在 → 更新字段；不存在 → 插入
- `sync-delete: true` 时：YAML 中不存在但数据库存在的权限 → 级联删除

### 字段说明
| 字段 | 必填 | 说明 |
|---|---|---|
| `code` | ✅ | 权限编码，全局唯一，如 `system:user:add` |
| `name` | ✅ | 权限名称 |
| `type` | ✅ | `menu` / `button` / `api` |
| `parentCode` | ❌ | 父权限的 code，顶级菜单省略 |
| `icon` | ❌ | 菜单图标（Element Plus 图标名） |
| `path` | ❌ | 路由地址，如 `/users` |
| `component` | ❌ | 组件路径，如 `views/user/index` |
| `sort` | ❌ | 排序，默认 0 |
| `status` | ❌ | 1-正常 0-禁用，默认 1 |

### 相关文件
| 文件 | 职责 |
|---|---|
| `backend/src/main/resources/permissions.yml` | 权限定义配置 |
| `backend/src/main/java/com/admin/config/PermissionProperties.java` | YAML 绑定（`@ConfigurationProperties`） |
| `backend/src/main/java/com/admin/config/PermissionSyncRunner.java` | 启动同步逻辑（CommandLineRunner） |

## 常用命令

```bash
# 后端
cd backend
mvn spring-boot:run    # 启动（端口 8080，前缀 /api）
mvn compile            # 编译验证

# 前端
cd frontend
npm run dev            # 开发服务器
npm run build          # 生产构建
```

## 注意事项

- 数据库需提前创建 `java_admin` 库（utf8mb4），Flyway 会自动建表
- 修改 RBAC 表数据需通过 Flyway 迁移脚本（`backend/src/main/resources/db/migration/`）
- 新增接口若需鉴权，在 Controller 方法上添加 `@SaCheckRole` 或 `@SaCheckPermission` 注解
- 新增按钮权限需在 `sys_permission` 表添加对应记录，并给角色分配
