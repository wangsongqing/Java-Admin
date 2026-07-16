import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 登录：保存 token、用户信息、角色和权限
  const setToken = (tokenValue, user) => {
    token.value = tokenValue
    userInfo.value = user
    roles.value = user?.roles || []
    permissions.value = user?.permissions || []
    localStorage.setItem('token', tokenValue)
    localStorage.setItem('userInfo', JSON.stringify(user))
    localStorage.setItem('roles', JSON.stringify(user?.roles || []))
    localStorage.setItem('permissions', JSON.stringify(user?.permissions || []))
  }

  // 判断是否拥有某个权限
  const hasPermission = (code) => {
    if (!code) return true
    return permissions.value.includes(code)
  }

  // 判断是否拥有某个角色
  const hasRole = (roleCode) => {
    if (!roleCode) return true
    return roles.value.includes(roleCode)
  }

  // 登出：清除数据
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    roles.value = []
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('roles')
    localStorage.removeItem('permissions')
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    setToken,
    hasPermission,
    hasRole,
    logout
  }
})
