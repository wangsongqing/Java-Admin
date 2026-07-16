import request from '@/utils/request'

/**
 * 查询角色列表
 */
export function getRoleList(params) {
  return request({
    url: '/roles/list',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询角色
 */
export function getRoleById(id) {
  return request({
    url: `/roles/${id}`,
    method: 'get'
  })
}

/**
 * 创建角色
 */
export function createRole(data) {
  return request({
    url: '/roles',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 */
export function updateRole(data) {
  return request({
    url: '/roles',
    method: 'put',
    data
  })
}

/**
 * 删除角色
 */
export function deleteRole(id) {
  return request({
    url: `/roles/${id}`,
    method: 'delete'
  })
}

/**
 * 查询角色拥有的权限ID列表
 */
export function getRolePermissions(id) {
  return request({
    url: `/roles/${id}/permissions`,
    method: 'get'
  })
}

/**
 * 分配权限
 */
export function assignRolePermissions(id, permissionIds) {
  return request({
    url: `/roles/${id}/permissions`,
    method: 'put',
    data: { permissionIds }
  })
}
