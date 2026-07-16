import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/user'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      // Sa-Token 不需要 Bearer 前缀，直接传 token 值
      config.headers.Authorization = userStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 200 表示成功
    if (res.code === 200) {
      return res
    }

    // 未登录或 Token 过期
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error(res.message || '登录已过期，请重新登录')
      return Promise.reject(new Error(res.message || '未登录'))
    }

    // 无权限（403）→ 仅提示，不跳转登录
    if (res.code === 403) {
      ElMessage.warning(res.message || '无权限操作')
      return Promise.reject(new Error(res.message || '无权限'))
    }

    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'

    if (status === 401) {
      // 未登录 → 跳登录页
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error(message || '登录已过期，请重新登录')
    } else if (status === 403) {
      // 无权限 → 仅提示
      ElMessage.warning(message || '无权限操作')
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default service
