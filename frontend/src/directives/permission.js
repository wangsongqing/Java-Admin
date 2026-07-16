import { useUserStore } from '@/store/user'

/**
 * 按钮级权限指令
 * 用法：
 *   v-permission="'system:user:add'"        → 有权限显示，无权限移除
 *   v-permission="['system:user:add', ...]"  → 拥有任一权限即显示
 */
export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()

    if (!value) return

    const codes = Array.isArray(value) ? value : [value]
    const hasPerm = codes.some((code) => userStore.hasPermission(code))

    if (!hasPerm) {
      // 移除 DOM 元素
      el.parentNode?.removeChild(el)
    }
  }
}
