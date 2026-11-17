import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getProfile, login as loginApi, logout as logoutApi } from '@/api/auth'
import { authStorage } from '@/api/request'
import { ROLE_TYPE } from '@/utils/constants'
import { menuForRole } from '@/config/menu'

/**
 * 登录态与权限
 * ------------------------------------------------------------------
 * token / 用户信息持久化在 localStorage，刷新后自动恢复；
 * 路由守卫在跳转前调用 ensureUser()，无 token 或角色不符时重定向登录页。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(authStorage.getToken())
  const user = ref(authStorage.getUser())
  const menus = ref(user.value ? menuForRole(user.value.roleType) : [])

  const isLogin = computed(() => Boolean(token.value))
  const roleType = computed(() => user.value?.roleType || 0)
  const roleName = computed(() => ROLE_TYPE[roleType.value]?.label || '访客')
  const roleBadge = computed(() => ROLE_TYPE[roleType.value]?.badge || '')
  const realName = computed(() => user.value?.realName || '访客')

  function applyAuth(payload) {
    token.value = payload.token
    user.value = payload.user
    menus.value = menuForRole(payload.user.roleType)
    authStorage.setToken(payload.token)
    authStorage.setUser(payload.user)
  }

  async function login(form) {
    const payload = await loginApi(form)
    applyAuth(payload)
    return payload
  }

  /** 刷新页面后用 token 拉取最新用户信息；失败则清空登录态 */
  async function ensureUser() {
    if (!token.value) return null
    if (user.value) return user.value
    try {
      const profile = await getProfile()
      user.value = profile
      menus.value = menuForRole(profile.roleType)
      authStorage.setUser(profile)
      return profile
    } catch {
      clearAuth()
      return null
    }
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    menus.value = []
    authStorage.clear()
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      clearAuth()
    }
  }

  /** 是否具备某角色访问权限 */
  function hasRole(roles = []) {
    return roles.includes(roleType.value)
  }

  return {
    token, user, menus, isLogin, roleType, roleName, roleBadge, realName,
    login, logout, ensureUser, clearAuth, hasRole
  }
})
