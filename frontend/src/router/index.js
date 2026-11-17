import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由表
 * ------------------------------------------------------------------
 * 前台为公开路由（访客免登录），/admin 下均为受保护路由，
 * meta.roles 与 config/menu.js 保持一致，守卫中做二次校验。
 */
const routes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/front/HomeView.vue'), meta: { title: '首页' } },
      { path: 'search', name: 'search', component: () => import('@/views/front/SearchView.vue'), meta: { title: '藏品检索' } },
      { path: 'relic/:id', name: 'relic-detail', component: () => import('@/views/front/DetailView.vue'), meta: { title: '藏品详情' } },
      { path: 'dashboard', name: 'dashboard', component: () => import('@/views/front/DashboardView.vue'), meta: { title: '数据看板' } },
      { path: 'scan', name: 'scan', component: () => import('@/views/front/ScanView.vue'), meta: { title: '扫码溯源' } }
    ]
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/admin/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/workbench' },
      { path: 'workbench', name: 'workbench', component: () => import('@/views/admin/WorkbenchView.vue'), meta: { title: '系统工作台', roles: [1, 2, 3] } },
      { path: 'collect', name: 'collect', component: () => import('@/views/admin/CollectView.vue'), meta: { title: '藏品征集', roles: [2] } },
      { path: 'catalog', name: 'catalog', component: () => import('@/views/admin/CatalogView.vue'), meta: { title: '编目建档', roles: [2] } },
      { path: 'archive', name: 'archive', component: () => import('@/views/admin/ArchiveView.vue'), meta: { title: '藏品档案', roles: [1, 2] } },
      { path: 'stock', name: 'stock', component: () => import('@/views/admin/StockView.vue'), meta: { title: '出入库管理', roles: [2] } },
      { path: 'warehouse', name: 'warehouse', component: () => import('@/views/admin/WarehouseView.vue'), meta: { title: '库房管理', roles: [2] } },
      { path: 'repair', name: 'repair', component: () => import('@/views/admin/RepairView.vue'), meta: { title: '修复管理', roles: [2] } },
      { path: 'loan', name: 'loan', component: () => import('@/views/admin/LoanView.vue'), meta: { title: '展览借调', roles: [2] } },
      { path: 'cancel', name: 'cancel', component: () => import('@/views/admin/CancelView.vue'), meta: { title: '注销管理', roles: [1, 2] } },
      { path: 'audit', name: 'audit', component: () => import('@/views/admin/AuditView.vue'), meta: { title: '审核工作台', roles: [3] } },
      { path: 'stats', name: 'stats', component: () => import('@/views/admin/StatsView.vue'), meta: { title: '统计分析', roles: [1] } },
      { path: 'user', name: 'user', component: () => import('@/views/admin/UserView.vue'), meta: { title: '用户管理', roles: [1] } },
      { path: 'dict', name: 'dict', component: () => import('@/views/admin/DictView.vue'), meta: { title: '数据字典', roles: [1] } },
      { path: 'param', name: 'param', component: () => import('@/views/admin/ParamView.vue'), meta: { title: '系统参数', roles: [1] } },
      { path: 'log', name: 'log', component: () => import('@/views/admin/LogView.vue'), meta: { title: '操作日志', roles: [1] } }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue'), meta: { title: '页面不存在' } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth) {
    await userStore.ensureUser()
    if (!userStore.isLogin) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    if (to.meta.roles && !userStore.hasRole(to.meta.roles)) {
      return { path: '/admin/workbench', query: { denied: to.path } }
    }
  }

  // 已登录用户访问登录页时直接进入后台，除非显式要求切换角色
  if (to.name === 'login' && userStore.isLogin && !to.query.switch) {
    return { path: '/admin/workbench' }
  }

  return true
})

router.afterEach((to) => {
  const base = import.meta.env.VITE_APP_TITLE || '文物藏品管理与溯源系统'
  document.title = to.meta.title ? `${to.meta.title} · ${base}` : base
})

export default router
