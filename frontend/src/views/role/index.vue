<template>
  <div class="role-page">
    <el-card shadow="never">
      <!-- 搜索栏 -->
      <el-form v-if="hasViewPermission" :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键字">
          <el-input
            v-model="queryForm.keyword"
            placeholder="角色名称/编码"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="toolbar">
        <el-button v-permission="'system:role:add'" type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>新增角色
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="hasViewPermission ? tableData : []" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-permission="'system:role:assign'" type="success" link size="small" @click="handleAssignPermission(row)">
              <el-icon><Key /></el-icon>分配权限
            </el-button>
            <el-button v-permission="'system:role:edit'" type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button v-permission="'system:role:delete'" type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 无查看权限提示 -->
    <el-empty
      v-if="!hasViewPermission"
      description="暂无查看角色权限，请联系管理员"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="如 ROLE_ADMIN" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        check-strictly
        :props="{ label: 'name', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleSavePermissions">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete, Edit, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  getRolePermissions,
  assignRolePermissions
} from '@/api/role'
import { getPermissionTree } from '@/api/permission'

const userStore = useUserStore()
const { hasPermission } = userStore

// 查看权限
const hasViewPermission = hasPermission('system:role:view')

// 查询
const queryForm = reactive({
  keyword: ''
})

const loading = ref(false)
const tableData = ref([])

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  remark: '',
  status: 1
})

const formRules = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^ROLE_[A-Z_]+$/, message: '角色编码必须以 ROLE_ 开头，大写字母', trigger: 'blur' }
  ],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

// 分配权限
const permissionDialogVisible = ref(false)
const permissionTree = ref([])
const selectedPermissionIds = ref([])
const currentRoleId = ref(null)
const assigning = ref(false)
const permissionTreeRef = ref(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (queryForm.keyword) params.keyword = queryForm.keyword
    const res = await getRoleList(params)
    tableData.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  queryForm.keyword = ''
  loadData()
}

// 新增
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  resetForm()
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 提交
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateRole(formData)
        ElMessage.success('更新成功')
      } else {
        await createRole(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      console.error(e)
    } finally {
      submitting.value = false
    }
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

// 分配权限
const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id

  // 加载权限树
  const res = await getPermissionTree()
  permissionTree.value = res.data

  // 加载当前角色已分配的权限
  const permRes = await getRolePermissions(row.id)

  permissionDialogVisible.value = true

  // 等 DOM 渲染完成后，主动设置勾选状态（default-checked-keys 只在首次挂载生效）
  await nextTick()
  permissionTreeRef.value?.setCheckedKeys(permRes.data)
}

// 保存权限分配
const handleSavePermissions = async () => {
  assigning.value = true
  try {
    // check-strictly 模式下，直接取完全勾选的节点
    const checkedIds = permissionTreeRef.value.getCheckedKeys()
    await assignRolePermissions(currentRoleId.value, checkedIds)
    ElMessage.success('分配成功')
    permissionDialogVisible.value = false
  } catch (e) {
    console.error(e)
  } finally {
    assigning.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    roleCode: '',
    roleName: '',
    remark: '',
    status: 1
  })
}

onMounted(() => {
  if (hasViewPermission) {
    loadData()
  }
})
</script>

<style lang="scss" scoped>
.role-page {
  .search-form {
    :deep(.el-form-item) {
      margin-right: 16px;
      margin-bottom: 0;
    }
  }

  .toolbar {
    margin: 16px 0;
  }
}
</style>
