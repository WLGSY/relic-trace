/**
 * 全局枚举常量
 * ------------------------------------------------------------------
 * 取值严格对应《数据表设计》中的字段注释：
 *   sys_user.role_type、relic_info.stock_status、
 *   relic_collection_apply.apply_status、relic_stock_record.record_type、
 *   relic_exhibition_loan.loan_status、relic_trace_record.trace_type、
 *   sys_audit_record.biz_type 等。
 * tone 用于 StatusTag 组件的视觉语义（金 / 朱砂 / 青玉 / 中性）。
 */

/** 角色类型 role_type */
export const ROLE_TYPE = {
  1: { label: '系统管理员', key: 'admin', badge: 'role-admin' },
  2: { label: '录入员', key: 'operator', badge: 'role-operator' },
  3: { label: '审核员', key: 'auditor', badge: 'role-auditor' }
}

/** 访客（免登录）在演示中的标记 */
export const GUEST_ROLE = 0

/** 藏品级别 relic_level */
export const RELIC_LEVEL = {
  1: { label: '一级', tone: 'cinnabar' },
  2: { label: '二级', tone: 'bronze' },
  3: { label: '三级', tone: 'ink' },
  4: { label: '一般', tone: 'ink' }
}

/** 库存状态 stock_status */
export const STOCK_STATUS = {
  1: { label: '在库', tone: 'jade' },
  2: { label: '出库', tone: 'bronze' },
  3: { label: '借出', tone: 'cinnabar' },
  4: { label: '修复中', tone: 'bronze' },
  5: { label: '已注销', tone: 'ink' }
}

/** 通用申请状态（征集 / 修复）apply_status：0 草稿 1 待审核 2 通过 3 驳回 */
export const APPLY_STATUS = {
  0: { label: '草稿', tone: 'ink' },
  1: { label: '待审核', tone: 'bronze' },
  2: { label: '审核通过', tone: 'jade' },
  3: { label: '审核驳回', tone: 'cinnabar' }
}

/** 出入库 / 注销申请状态：0 待审核 1 通过 2 驳回 */
export const SIMPLE_APPLY_STATUS = {
  0: { label: '待审核', tone: 'bronze' },
  1: { label: '审核通过', tone: 'jade' },
  2: { label: '审核驳回', tone: 'cinnabar' }
}

/** 出入库业务类型 record_type */
export const RECORD_TYPE = {
  1: { label: '入库', tone: 'jade' },
  2: { label: '出库', tone: 'cinnabar' },
  3: { label: '移库', tone: 'bronze' }
}

/** 借调状态 loan_status */
export const LOAN_STATUS = {
  1: { label: '待审核', tone: 'bronze' },
  2: { label: '借出中', tone: 'cinnabar' },
  3: { label: '已归还', tone: 'jade' },
  4: { label: '审核驳回', tone: 'ink' }
}

/** 盘点状态 check_status */
export const CHECK_STATUS = {
  1: { label: '盘点中', tone: 'bronze' },
  2: { label: '已完成', tone: 'jade' }
}

/** 溯源类型 trace_type */
export const TRACE_TYPE = {
  1: { label: '征集', tone: 'bronze' },
  2: { label: '入藏', tone: 'jade' },
  3: { label: '入库', tone: 'jade' },
  4: { label: '出库', tone: 'cinnabar' },
  5: { label: '移库', tone: 'bronze' },
  6: { label: '修复', tone: 'bronze' },
  7: { label: '借出', tone: 'cinnabar' },
  8: { label: '归还', tone: 'jade' },
  9: { label: '注销', tone: 'ink' }
}

/** 审核业务类型 sys_audit_record.biz_type */
export const AUDIT_BIZ_TYPE = {
  1: { label: '征集审核', path: 'collection' },
  2: { label: '出入库审核', path: 'stock' },
  3: { label: '修复审核', path: 'repair' },
  4: { label: '借调审核', path: 'loan' },
  5: { label: '注销审核', path: 'cancel' }
}

/** 审核结果 audit_result */
export const AUDIT_RESULT = {
  1: { label: '通过', tone: 'jade' },
  2: { label: '驳回', tone: 'cinnabar' }
}

/** 附件类型 file_type */
export const FILE_TYPE = {
  1: { label: '藏品影像' },
  2: { label: '修复前照片' },
  3: { label: '修复后照片' },
  4: { label: '借调协议' },
  5: { label: '其他' }
}

/* ------------------------------------------------------------------
 * 字典项兜底值
 * 真实环境中由 sys_dict 表返回（数据字典页可维护），
 * 这里保留一份默认值，保证接口不可用时页面仍可渲染。
 * ------------------------------------------------------------------ */
export const DICT_FALLBACK = {
  relic_level: Object.entries(RELIC_LEVEL).map(([code, v]) => ({ dictCode: code, dictLabel: v.label })),
  // 与 sys_dict 表保持一致（dict_code 为英文标识，不是序号）
  collection_way: [
    { dictCode: 'donate', dictLabel: '捐赠' },
    { dictCode: 'purchase', dictLabel: '购买' },
    { dictCode: 'transfer', dictLabel: '调拨' },
    { dictCode: 'borrow', dictLabel: '借展' }
  ],
  era: [
    { dictCode: '1', dictLabel: '新石器时代' },
    { dictCode: '2', dictLabel: '商周' },
    { dictCode: '3', dictLabel: '汉代' },
    { dictCode: '4', dictLabel: '唐代' },
    { dictCode: '5', dictLabel: '五代' },
    { dictCode: '6', dictLabel: '宋代' },
    { dictCode: '7', dictLabel: '明清' },
    { dictCode: '8', dictLabel: '近现代' }
  ],
  texture: [
    { dictCode: '1', dictLabel: '陶瓷' },
    { dictCode: '2', dictLabel: '青铜' },
    { dictCode: '3', dictLabel: '玉器' },
    { dictCode: '4', dictLabel: '书画' },
    { dictCode: '5', dictLabel: '织绣' },
    { dictCode: '6', dictLabel: '竹木' }
  ],
  integrity_status: [
    { dictCode: '1', dictLabel: '完好' },
    { dictCode: '2', dictLabel: '基本完好' },
    { dictCode: '3', dictLabel: '局部残损' },
    { dictCode: '4', dictLabel: '严重残损' }
  ],
  stock_status: Object.entries(STOCK_STATUS).map(([code, v]) => ({ dictCode: code, dictLabel: v.label }))
}

/** 藏品封面渐变（离线环境下代替真实图片，避免外链失效） */
export const COVER_KEYS = ['a', 'b', 'c', 'd', 'e', 'f']
