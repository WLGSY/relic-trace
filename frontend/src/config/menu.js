/**
 * 后台菜单配置
 * ------------------------------------------------------------------
 * 依据文档 1.2 项目范围与 2.2 用例图：按 role_type 渲染不同菜单。
 *   role_type 1 系统管理员 → 工作台、藏品档案、统计分析、系统管理
 *   role_type 2 录入员     → 工作台、征集、编目、档案、出入库、库房、修复、借调、注销
 *   role_type 3 审核员     → 工作台、审核工作台
 * roles 同时被 router 守卫使用，保证直接输入 URL 也无法越权访问。
 */
export const MENU_GROUPS = [
  {
    group: '业务管理',
    code: 'business',
    items: [
      { name: 'workbench', label: '系统工作台', path: '/admin/workbench', roles: [1, 2, 3] },
      { name: 'collect', label: '藏品征集', path: '/admin/collect', roles: [2] },
      { name: 'catalog', label: '编目建档', path: '/admin/catalog', roles: [2] },
      { name: 'archive', label: '藏品档案', path: '/admin/archive', roles: [1, 2] },
      { name: 'stock', label: '出入库管理', path: '/admin/stock', roles: [2] },
      { name: 'warehouse', label: '库房管理', path: '/admin/warehouse', roles: [2] },
      { name: 'repair', label: '修复管理', path: '/admin/repair', roles: [2] },
      { name: 'loan', label: '展览借调', path: '/admin/loan', roles: [2] },
      { name: 'cancel', label: '注销管理', path: '/admin/cancel', roles: [1, 2] }
    ]
  },
  {
    group: '审核与统计',
    code: 'audit',
    items: [
      { name: 'audit', label: '审核工作台', path: '/admin/audit', roles: [3] },
      { name: 'stats', label: '统计分析', path: '/admin/stats', roles: [1] }
    ]
  },
  {
    group: '系统管理',
    code: 'system',
    items: [
      { name: 'user', label: '用户管理', path: '/admin/user', roles: [1] },
      { name: 'dict', label: '数据字典', path: '/admin/dict', roles: [1] },
      { name: 'param', label: '系统参数', path: '/admin/param', roles: [1] },
      { name: 'log', label: '操作日志', path: '/admin/log', roles: [1] }
    ]
  }
]

/** 按角色过滤后的菜单（保持分组顺序，空分组不渲染） */
export function menuForRole(roleType) {
  return MENU_GROUPS
    .map((group) => ({ ...group, items: group.items.filter((item) => item.roles.includes(roleType)) }))
    .filter((group) => group.items.length > 0)
}
