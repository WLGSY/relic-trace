import axios from 'axios'
import { ElMessage } from 'element-plus'
import { mockRequest } from '@/mock'

/**
 * Axios 实例与统一响应处理
 * ------------------------------------------------------------------
 * 后端约定：HTTP 200 返回 { code, message, data }
 *   code = 200 成功；401 登录失效；其余为业务异常
 * 拦截器统一返回 data 字段，因此 api 层函数直接拿到业务数据。
 */

const USE_MOCK = String(import.meta.env.VITE_USE_MOCK ?? 'false') === 'true'
const TOKEN_KEY = 'relic_token'
const USER_KEY = 'relic_user'

export const authStorage = {
  getToken: () => localStorage.getItem(TOKEN_KEY) || '',
  setToken: (token) => localStorage.setItem(TOKEN_KEY, token),
  getUser: () => {
    try {
      return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
    } catch {
      return null
    }
  },
  setUser: (user) => localStorage.setItem(USER_KEY, JSON.stringify(user)),
  clear: () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

/* mock 适配器：把请求交给内置 mock 服务，不产生真实网络请求 */
if (USE_MOCK) {
  service.defaults.adapter = async (config) => {
    const payload = {
      method: config.method,
      url: config.url,
      params: config.params,
      data: config.data,
      token: authStorage.getToken()
    }
    const envelope = await mockRequest(payload)
    return {
      data: envelope,
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
      request: {}
    }
  }
}

service.interceptors.request.use(
  (config) => {
    const token = authStorage.getToken()
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const envelope = response.data
    // 非标准响应（如文件流）直接返回
    if (!envelope || typeof envelope.code === 'undefined') return envelope

    if (envelope.code === 200) return envelope.data

    if (envelope.code === 401) {
      const onAdminPage = window.location.hash.startsWith('#/admin')
      const hadToken = !!authStorage.getToken()
      authStorage.clear()
      // 仅后台页面或已登录会话失效时才跳登录页；
      // 前台公开页（首页 / 检索 / 详情）遇到管理接口 401 时静默失败，避免整页被踢走
      if (onAdminPage || hadToken) {
        ElMessage.error(envelope.message || '登录状态已失效，请重新登录')
        const redirect = encodeURIComponent(window.location.hash.replace(/^#/, '') || '/admin/workbench')
        window.location.hash = `#/login?redirect=${redirect}`
      }
      return Promise.reject(new Error(envelope.message || '未登录'))
    }

    ElMessage.error(envelope.message || '操作失败')
    return Promise.reject(new Error(envelope.message || '操作失败'))
  },
  (error) => {
    const message = error.code === 'ECONNABORTED'
      ? '请求超时，请稍后重试'
      : error.response?.data?.message || error.message || '网络异常，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
