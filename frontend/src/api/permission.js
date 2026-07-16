import request from '@/utils/request'

/**
 * 查询所有权限树
 */
export function getPermissionTree() {
  return request({
    url: '/permissions/tree',
    method: 'get'
  })
}
