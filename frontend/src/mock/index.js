/**
 * 内置 mock 服务（演示开关：VITE_USE_MOCK）
 * ------------------------------------------------------------------
 * 目标：不依赖后端即可完整演示所有业务流程；接口路径、请求参数、
 * 响应结构（{ code, message, data }）与真实 Spring Boot 接口保持一致，
 * 关闭 mock 后前端代码无需改动。
 *
 * 数据结构见 ./db.js；所有写操作会同步写入 localStorage，刷新不丢失，
 * 需要恢复初始数据时执行 localStorage.removeItem('relic_mock_db_v3')。
 */
import { createDatabase } from './db'
import { formatDate, now } from '@/utils/format'

const STORAGE_KEY = 'relic_mock_db_v3'
const TOKEN_PREFIX = 'mock-token-'

/* ------------------------------------------------------------------ */
/* 数据库装载与持久化                                                   */
/* ------------------------------------------------------------------ */
function loadDatabase() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (parsed && parsed.version === 3) return parsed
    }
  } catch (error) {
    console.warn('[mock] 本地数据损坏，已重置为初始数据', error)
  }
  return createDatabase()
}

let db = loadDatabase()

function persist() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(db))
  } catch (error) {
    console.warn('[mock] 本地存储写入失败（数据仅在内存中生效）', error)
  }
}

/** 恢复初始演示数据 */
export function resetMockDatabase() {
  db = createDatabase()
  persist()
}

/* ------------------------------------------------------------------ */
/* 通用工具                                                            */
/* ------------------------------------------------------------------ */
const ok = (data = null, message = '操作成功') => ({ code: 200, message, data })
const fail = (message, code = 500) => ({ code, message, data: null })

const nextId = (prefix) => `${prefix}${Date.now().toString(36)}${Math.random().toString(36).slice(2, 10)}`
const contains = (source, keyword) => String(source ?? '').toLowerCase().includes(String(keyword).toLowerCase())
const inRange = (value, begin, end) => {
  if (!begin && !end) return true
  const date = String(value ?? '').slice(0, 10)
  if (begin && date < begin) return false
  if (end && date > end) return false
  return true
}

function paginate(list, query = {}) {
  const pageNum = Number(query.pageNum || 1)
  const pageSize = Number(query.pageSize || 10)
  const start = (pageNum - 1) * pageSize
  return {
    list: list.slice(start, start + pageSize),
    total: list.length,
    pageNum,
    pageSize
  }
}

function categoryName(id) {
  return db.categories.find((c) => c.id === id)?.categoryName || '未分类'
}
function warehouseName(id) {
  return db.warehouses.find((w) => w.id === id)?.warehouseName || '—'
}
function positionName(id) {
  const pos = db.positions.find((p) => p.id === id)
  return pos ? `${pos.positionCode}` : '—'
}
function relicOf(id) {
  return db.relics.find((r) => r.id === id)
}

/** 写操作日志（对应 sys_operate_log） */
function pushLog(module, operateType, operateContent, userName = '当前用户') {
  const user = db.users.find((u) => u.realName === userName)
  db.operateLogs.unshift({
    id: nextId('lg'),
    operateUserId: user?.id || 'usr0000000000000000000000000001',
    operateUserName: userName,
    module,
    operateType,
    operateContent,
    operateTime: now(),
    createTime: now(),
    isDeleted: 0
  })
}

/** 写溯源记录 */
function pushTrace(relicId, traceType, content, operatorName = '系统') {
  db.traces.push({
    id: nextId('tr'),
    fkRelicId: relicId,
    traceType,
    fkBizId: null,
    traceTime: now(),
    operatorName,
    traceContent: content,
    createUserName: operatorName,
    createTime: now(),
    isDeleted: 0
  })
}

/* ------------------------------------------------------------------ */
/* 装饰器：把外键翻译成前端直接可用的字段                                */
/* ------------------------------------------------------------------ */
function decorateRelic(relic) {
  return {
    ...relic,
    categoryName: categoryName(relic.fkCategoryId),
    warehouseName: warehouseName(relic.fkWarehouseId),
    positionName: positionName(relic.fkPositionId),
    attachmentCount: db.attachments.filter((a) => a.fkRelicId === relic.id && !a.isDeleted).length,
    traceCount: db.traces.filter((t) => t.fkRelicId === relic.id && !t.isDeleted).length
  }
}

function decorateCollection(item) {
  return {
    ...item,
    categoryName: categoryName(item.fkCategoryId),
    relicCode: db.relics.find((r) => r.fkCollectionApplyId === item.id)?.relicCode || null
  }
}

function decorateStock(item) {
  const relic = relicOf(item.fkRelicId)
  return {
    ...item,
    relicName: relic?.relicName || '—',
    relicCode: relic?.relicCode || '—',
    fromWarehouseName: item.fkFromWarehouseId ? warehouseName(item.fkFromWarehouseId) : '—',
    toWarehouseName: item.fkToWarehouseId ? warehouseName(item.fkToWarehouseId) : '—'
  }
}

function decorateRepair(item) {
  const relic = relicOf(item.fkRelicId)
  return {
    ...item,
    relicName: relic?.relicName || '—',
    relicCode: relic?.relicCode || '—',
    categoryName: relic ? categoryName(relic.fkCategoryId) : '—'
  }
}

function decorateLoan(item) {
  const relic = relicOf(item.fkRelicId)
  return {
    ...item,
    relicName: relic?.relicName || '—',
    relicCode: relic?.relicCode || '—',
    overdueDays: item.planReturnTime && item.loanStatus === 2
      ? Math.max(0, Math.round((Date.now() - new Date(item.planReturnTime.replace(/-/g, '/')).getTime()) / 86400000))
      : 0
  }
}

function decorateCancel(item) {
  const relic = relicOf(item.fkRelicId)
  return {
    ...item,
    relicName: relic?.relicName || '—',
    relicCode: relic?.relicCode || '—',
    categoryName: relic ? categoryName(relic.fkCategoryId) : '—'
  }
}

function decorateTrace(trace) {
  const relic = relicOf(trace.fkRelicId)
  return { ...trace, relicName: relic?.relicName || '—', relicCode: relic?.relicCode || '—' }
}

/* ------------------------------------------------------------------ */
/* 登录态                                                              */
/* ------------------------------------------------------------------ */
function currentUser(params = {}) {
  const token = params.__token || ''
  if (!token.startsWith(TOKEN_PREFIX)) return null
  const id = token.slice(TOKEN_PREFIX.length)
  return db.users.find((u) => u.id === id && u.isEnabled === 1 && !u.isDeleted) || null
}

const safeUser = (user) => {
  const { loginPassword, ...rest } = user
  return rest
}

/* ------------------------------------------------------------------ */
/* 业务处理：认证                                                      */
/* ------------------------------------------------------------------ */
function login(ctx) {
  const { loginName, loginPassword } = ctx.body || {}
  if (!loginName || !loginPassword) return fail('请输入用户名和密码')
  const user = db.users.find((u) => u.loginName === loginName && !u.isDeleted)
  if (!user) return fail('用户名或密码错误')
  // 真实后端使用 BCrypt 校验，演示环境直接比对明文
  if (user.loginPassword !== loginPassword) return fail('用户名或密码错误')
  if (user.isEnabled !== 1) return fail('账号已被停用，请联系系统管理员')

  user.lastLoginTime = now()
  persist()
  pushLog('登录认证', '登录', `${user.realName} 登录系统`, user.realName)
  persist()
  return ok({ token: `${TOKEN_PREFIX}${user.id}`, user: safeUser(user) }, '登录成功')
}

function profile(ctx) {
  const user = currentUser(ctx)
  if (!user) return fail('登录状态已失效，请重新登录', 401)
  return ok(safeUser(user))
}

/* ------------------------------------------------------------------ */
/* 系统管理：用户                                                      */
/* ------------------------------------------------------------------ */
function listUsers(ctx) {
  const { keyword = '', roleType = '', isEnabled = '', pageNum, pageSize } = ctx.query
  const list = db.users
    .filter((u) => !u.isDeleted)
    .filter((u) => (roleType === '' ? true : u.roleType === Number(roleType)))
    .filter((u) => (isEnabled === '' ? true : u.isEnabled === Number(isEnabled)))
    .filter((u) => (!keyword ? true : contains(u.loginName, keyword) || contains(u.realName, keyword) || contains(u.phone, keyword)))
    .sort((a, b) => (a.createTime < b.createTime ? 1 : -1))
    .map(safeUser)
  return ok(paginate(list, { pageNum, pageSize }))
}

function createUser(ctx) {
  const body = ctx.body || {}
  if (!body.loginName || !body.realName || !body.roleType) return fail('登录账号、真实姓名、角色类型为必填项')
  if (db.users.some((u) => u.loginName === body.loginName && !u.isDeleted)) return fail('登录账号已存在，请更换')
  const user = {
    id: nextId('usr'),
    loginName: body.loginName,
    loginPassword: body.loginPassword || '123456',
    realName: body.realName,
    roleType: Number(body.roleType),
    phone: body.phone || '',
    avatar: '',
    isEnabled: body.isEnabled ?? 1,
    lastLoginTime: null,
    createUserId: currentUser(ctx)?.id || 'system',
    createUserName: currentUser(ctx)?.realName || '系统管理员',
    createTime: now(),
    updateUserName: currentUser(ctx)?.realName || '系统管理员',
    updateTime: now(),
    isDeleted: 0,
    memo: body.memo || ''
  }
  db.users.unshift(user)
  persist()
  pushLog('用户管理', '新增', `新增用户「${user.realName}」（${user.loginName}）`, user.createUserName)
  persist()
  return ok(safeUser(user), '用户创建成功')
}

function updateUser(ctx) {
  const user = db.users.find((u) => u.id === ctx.params.id && !u.isDeleted)
  if (!user) return fail('用户不存在')
  const body = ctx.body || {}
  if (body.loginName && db.users.some((u) => u.loginName === body.loginName && u.id !== user.id && !u.isDeleted)) {
    return fail('登录账号已存在，请更换')
  }
  Object.assign(user, {
    loginName: body.loginName ?? user.loginName,
    realName: body.realName ?? user.realName,
    roleType: body.roleType !== undefined ? Number(body.roleType) : user.roleType,
    phone: body.phone ?? user.phone,
    isEnabled: body.isEnabled ?? user.isEnabled,
    memo: body.memo ?? user.memo,
    updateUserName: currentUser(ctx)?.realName || '系统管理员',
    updateTime: now()
  })
  persist()
  pushLog('用户管理', '修改', `修改用户「${user.realName}」信息`, user.updateUserName)
  persist()
  return ok(safeUser(user), '保存成功')
}

function deleteUser(ctx) {
  const user = db.users.find((u) => u.id === ctx.params.id && !u.isDeleted)
  if (!user) return fail('用户不存在')
  if (user.roleType === 1 && db.users.filter((u) => u.roleType === 1 && u.isEnabled === 1 && !u.isDeleted).length <= 1) {
    return fail('系统至少需保留一个启用的系统管理员')
  }
  user.isDeleted = 1
  persist()
  pushLog('用户管理', '删除', `删除用户「${user.realName}」`, currentUser(ctx)?.realName)
  persist()
  return ok(null, '已删除')
}

function resetPassword(ctx) {
  const user = db.users.find((u) => u.id === ctx.params.id && !u.isDeleted)
  if (!user) return fail('用户不存在')
  const password = ctx.body?.password
  if (!password || String(password).length < 6) return fail('新密码长度不得少于 6 位')
  user.loginPassword = password
  user.updateTime = now()
  persist()
  pushLog('用户管理', '修改', `重置用户「${user.realName}」登录密码`, currentUser(ctx)?.realName)
  persist()
  return ok(null, '密码已重置')
}

function toggleUser(ctx) {
  const user = db.users.find((u) => u.id === ctx.params.id && !u.isDeleted)
  if (!user) return fail('用户不存在')
  const enabled = Number(ctx.body?.isEnabled)
  if (![0, 1].includes(enabled)) return fail('状态值不合法')
  if (enabled === 0 && user.id === currentUser(ctx)?.id) return fail('不能停用当前登录账号')
  user.isEnabled = enabled
  user.updateTime = now()
  persist()
  pushLog('用户管理', '修改', `${enabled === 1 ? '启用' : '停用'}用户「${user.realName}」`, currentUser(ctx)?.realName)
  persist()
  return ok(null, enabled === 1 ? '账号已启用' : '账号已停用')
}

/* ------------------------------------------------------------------ */
/* 藏品征集                                                            */
/* ------------------------------------------------------------------ */
function listCollections(ctx) {
  const q = ctx.query
  const list = db.collectionApplies
    .filter((c) => !c.isDeleted)
    .filter((c) => (q.applyStatus === '' || q.applyStatus === undefined ? true : c.applyStatus === Number(q.applyStatus)))
    .filter((c) => (q.categoryId ? c.fkCategoryId === q.categoryId : true))
    .filter((c) => (q.era ? c.era === q.era : true))
    .filter((c) => (q.collectionWay ? c.collectionWay === q.collectionWay : true))
    .filter((c) => inRange(c.collectionTime, q.beginTime, q.endTime))
    .filter((c) => (!q.keyword ? true : contains(c.relicName, q.keyword) || contains(c.applyCode, q.keyword) || contains(c.collector, q.keyword)))
    .sort((a, b) => (a.createTime < b.createTime ? 1 : -1))
    .map(decorateCollection)
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

function getCollection(ctx) {
  const item = db.collectionApplies.find((c) => c.id === ctx.params.id)
  if (!item) return fail('征集申请不存在')
  return ok(decorateCollection(item))
}

function createCollection(ctx) {
  const body = ctx.body || {}
  if (!body.relicName || !body.fkCategoryId) return fail('藏品名称与藏品分类为必填项')
  const seq = 75 + db.collectionApplies.filter((c) => c.applyCode.startsWith(`C-${new Date().getFullYear()}`)).length
  const item = {
    id: nextId('col'),
    applyCode: `C-${new Date().getFullYear()}-${String(seq).padStart(3, '0')}`,
    batchNo: body.batchNo || `B-${new Date().getFullYear()}-${String(Math.floor(seq / 2)).padStart(3, '0')}`,
    relicName: body.relicName,
    fkCategoryId: body.fkCategoryId,
    era: body.era || '',
    texture: body.texture || '',
    specification: body.specification || '',
    relicLevel: body.relicLevel || null,
    collectionSource: body.collectionSource || '',
    collectionWay: body.collectionWay || '',
    collectionPrice: body.collectionPrice ?? 0,
    collectionDesc: body.collectionDesc || '',
    collector: body.collector || currentUser(ctx)?.realName || '张录入',
    collectionTime: body.collectionTime || formatDate(new Date()),
    applyStatus: body.submit ? 1 : 0,
    auditOpinion: '',
    createUserId: currentUser(ctx)?.id || '',
    createUserName: currentUser(ctx)?.realName || '张录入',
    createTime: now(),
    updateTime: now(),
    isDeleted: 0,
    memo: ''
  }
  db.collectionApplies.unshift(item)
  persist()
  pushLog('藏品征集', '新增', `${body.submit ? '新增并提交' : '保存草稿'}征集申请 ${item.applyCode}「${item.relicName}」`, item.createUserName)
  if (body.submit) pushTrace(item.id, 1, `征集申请 ${item.applyCode} 已提交审核。`, item.createUserName)
  persist()
  return ok(decorateCollection(item), body.submit ? '已提交审核' : '草稿已保存')
}

function updateCollection(ctx) {
  const item = db.collectionApplies.find((c) => c.id === ctx.params.id)
  if (!item) return fail('征集申请不存在')
  if (item.applyStatus === 1) return fail('已提交的征集信息不可直接修改，需审核驳回后修改')
  if (item.applyStatus === 2) return fail('审核通过的征集信息已用于编目，不可修改')
  Object.assign(item, ctx.body || {}, { updateTime: now(), auditOpinion: '' })
  persist()
  pushLog('藏品征集', '修改', `修改征集申请 ${item.applyCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(decorateCollection(item), '保存成功')
}

function submitCollection(ctx) {
  const item = db.collectionApplies.find((c) => c.id === ctx.params.id)
  if (!item) return fail('征集申请不存在')
  if (![0, 3].includes(item.applyStatus)) return fail('当前状态不允许提交审核')
  item.applyStatus = 1
  item.updateTime = now()
  persist()
  pushLog('藏品征集', '修改', `提交征集申请 ${item.applyCode} 审核`, currentUser(ctx)?.realName)
  return ok(null, '已提交审核')
}

function deleteCollection(ctx) {
  const item = db.collectionApplies.find((c) => c.id === ctx.params.id)
  if (!item) return fail('征集申请不存在')
  if (![0, 3].includes(item.applyStatus)) return fail('待审核或已通过的征集信息不可删除')
  item.isDeleted = 1
  persist()
  pushLog('藏品征集', '删除', `删除征集申请 ${item.applyCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(null, '已删除')
}

function batchDeleteCollection(ctx) {
  const ids = ctx.body?.ids || []
  const deletable = db.collectionApplies.filter((c) => ids.includes(c.id) && [0, 3].includes(c.applyStatus))
  deletable.forEach((c) => { c.isDeleted = 1 })
  persist()
  if (deletable.length) {
    pushLog('藏品征集', '删除', `批量删除 ${deletable.length} 条征集申请`, currentUser(ctx)?.realName)
    persist()
  }
  return ok({ deleted: deletable.length }, `已删除 ${deletable.length} 条记录`)
}

/* ------------------------------------------------------------------ */
/* 编目建档 / 藏品档案                                                  */
/* ------------------------------------------------------------------ */
function relicSort(list, sort) {
  if (sort === 'level') return list.sort((a, b) => a.relicLevel - b.relicLevel)
  if (sort === 'oldest') return list.sort((a, b) => (a.createTime > b.createTime ? 1 : -1))
  if (sort === 'views') return list.sort((a, b) => (b.scanCount || 0) - (a.scanCount || 0))
  return list.sort((a, b) => (a.relicCode < b.relicCode ? 1 : -1))
}

function filterRelics(q) {
  return db.relics
    .filter((r) => !r.isDeleted)
    .filter((r) => (q.isPublic === undefined || q.isPublic === '' ? true : r.isPublic === Number(q.isPublic)))
    .filter((r) => (q.stockStatus === undefined || q.stockStatus === '' ? true : r.stockStatus === Number(q.stockStatus)))
    .filter((r) => (q.relicLevel === undefined || q.relicLevel === '' ? true : r.relicLevel === Number(q.relicLevel)))
    .filter((r) => (q.categoryId ? r.fkCategoryId === q.categoryId : true))
    .filter((r) => (q.era ? r.era === q.era : true))
    .filter((r) => (!q.keyword ? true : contains(r.relicName, q.keyword) || contains(r.relicCode, q.keyword) || contains(r.era, q.keyword) || contains(r.texture, q.keyword)))
}

function listRelics(ctx) {
  const q = ctx.query
  const list = filterRelics(q).map((r) => {
    const decorated = decorateRelic(r)
    decorated.scanCount = db.qrCodes.find((x) => x.fkRelicId === r.id)?.scanCount || 0
    return decorated
  })
  relicSort(list, q.sort)
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

function getRelic(ctx) {
  const relic = db.relics.find((r) => r.id === ctx.params.id && !r.isDeleted)
  if (!relic) return fail('藏品不存在')
  return ok({
    ...decorateRelic(relic),
    attachments: db.attachments.filter((a) => a.fkRelicId === relic.id && !a.isDeleted),
    qrcode: db.qrCodes.find((q) => q.fkRelicId === relic.id) || null,
    traces: db.traces.filter((t) => t.fkRelicId === relic.id && !t.isDeleted).map(decorateTrace),
    collection: db.collectionApplies.find((c) => c.id === relic.fkCollectionApplyId) || null
  })
}

/** 编目建档：由征集申请生成藏品档案，自动生成藏品编号与溯源二维码 */
function createRelic(ctx) {
  const body = ctx.body || {}
  const apply = db.collectionApplies.find((c) => c.id === body.fkCollectionApplyId)
  if (!apply) return fail('请选择已通过审核的征集申请')
  if (apply.applyStatus !== 2) return fail('仅审核通过的征集申请可编目建档')
  if (db.relics.some((r) => r.fkCollectionApplyId === apply.id && !r.isDeleted)) return fail('该征集申请已完成编目，请勿重复建档')

  const prefix = db.configs.find((c) => c.configKey === 'relic_code.prefix')?.configValue || String(new Date().getFullYear())
  const seq = db.relics.length + 1
  const relicCode = body.relicCode || `${prefix}-${String(seq).padStart(4, '0')}`
  if (db.relics.some((r) => r.relicCode === relicCode)) return fail('藏品编号已存在')

  const relic = {
    id: nextId('rel'),
    relicCode,
    relicName: body.relicName || apply.relicName,
    fkCategoryId: body.fkCategoryId || apply.fkCategoryId,
    fkCollectionApplyId: apply.id,
    era: body.era ?? apply.era,
    texture: body.texture ?? apply.texture,
    specification: body.specification ?? apply.specification,
    relicLevel: body.relicLevel ?? apply.relicLevel,
    quantity: body.quantity || 1,
    integrityStatus: body.integrityStatus || '基本完好',
    fkWarehouseId: body.fkWarehouseId || null,
    fkPositionId: body.fkPositionId || null,
    stockStatus: 1,
    qrcodeContent: `relic://trace/${relicCode}`,
    isPublic: body.isPublic ?? 1,
    relicDesc: body.relicDesc ?? apply.collectionDesc,
    collectionWay: apply.collectionWay,
    collectionSource: apply.collectionSource,
    collectionPrice: apply.collectionPrice,
    storageTime: body.storageTime || formatDate(new Date()),
    cover: ['a', 'b', 'c', 'd', 'e', 'f'][db.relics.length % 6],
    createUserId: currentUser(ctx)?.id || '',
    createUserName: currentUser(ctx)?.realName || '张录入',
    createTime: now(),
    updateUserName: currentUser(ctx)?.realName || '张录入',
    updateTime: now(),
    isDeleted: 0,
    memo: ''
  }
  db.relics.unshift(relic)
  db.qrCodes.push({
    id: nextId('qr'),
    fkRelicId: relic.id,
    qrContent: relic.qrcodeContent,
    qrPath: `/files/qrcode/${relic.relicCode}.png`,
    generateUserName: relic.createUserName,
    generateTime: now(),
    scanCount: 0,
    lastScanTime: null
  })
  pushTrace(relic.id, 2, `编目建档完成，生成藏品编号 ${relic.relicCode} 与溯源二维码。`, relic.createUserName)
  persist()
  pushLog('编目建档', '新增', `编目建档 ${relic.relicCode}「${relic.relicName}」`, relic.createUserName)
  persist()
  return ok({ ...decorateRelic(relic), qrcode: db.qrCodes.find((q) => q.fkRelicId === relic.id) }, '编目建档成功')
}

function updateRelic(ctx) {
  const relic = db.relics.find((r) => r.id === ctx.params.id && !r.isDeleted)
  if (!relic) return fail('藏品不存在')
  const before = JSON.stringify({ name: relic.relicName, level: relic.relicLevel, status: relic.stockStatus })
  Object.assign(relic, ctx.body || {}, { updateTime: now(), updateUserName: currentUser(ctx)?.realName || '张录入' })
  const after = JSON.stringify({ name: relic.relicName, level: relic.relicLevel, status: relic.stockStatus })
  if (before !== after) {
    pushTrace(relic.id, 2, '藏品档案信息发生变更，已记录本次修改。', currentUser(ctx)?.realName || '张录入')
  }
  persist()
  pushLog('藏品档案', '修改', `修改藏品 ${relic.relicCode}「${relic.relicName}」档案`, currentUser(ctx)?.realName)
  persist()
  return ok(decorateRelic(relic), '保存成功')
}

function deleteRelic(ctx) {
  const relic = db.relics.find((r) => r.id === ctx.params.id && !r.isDeleted)
  if (!relic) return fail('藏品不存在')
  if (relic.stockStatus === 3) return fail('藏品处于借出状态，请先办理归还')
  relic.isDeleted = 1
  persist()
  pushLog('藏品档案', '删除', `逻辑删除藏品 ${relic.relicCode}「${relic.relicName}」`, currentUser(ctx)?.realName)
  persist()
  return ok(null, '已删除（逻辑删除，可在数据库中恢复）')
}

function batchDeleteRelic(ctx) {
  const ids = ctx.body?.ids || []
  const targets = db.relics.filter((r) => ids.includes(r.id) && !r.isDeleted && r.stockStatus !== 3)
  targets.forEach((r) => { r.isDeleted = 1 })
  persist()
  if (targets.length) {
    pushLog('藏品档案', '删除', `批量删除 ${targets.length} 件藏品档案`, currentUser(ctx)?.realName)
    persist()
  }
  return ok({ deleted: targets.length }, `已删除 ${targets.length} 件藏品`)
}

function getRelicByCode(ctx) {
  const code = decodeURIComponent(ctx.params.code || '').trim()
  const relic = db.relics.find((r) => (r.relicCode === code || r.qrcodeContent === code) && !r.isDeleted)
  if (!relic) return fail('二维码无效或对应藏品不存在', 404)
  return getRelic({ params: { id: relic.id } })
}

/** 前台：精选藏品（公开且未注销） */
function publicHighlights() {
  const list = db.relics
    .filter((r) => !r.isDeleted && r.isPublic === 1 && r.stockStatus !== 5)
    .slice(0, 6)
    .map((r) => {
      const decorated = decorateRelic(r)
      decorated.scanCount = db.qrCodes.find((q) => q.fkRelicId === r.id)?.scanCount || 0
      return decorated
    })
  return ok(list)
}

/** 前台：检索（关键词 / 分类 / 年代 / 级别 / 排序 / 分页） */
function publicSearch(ctx) {
  const q = { ...ctx.query, isPublic: 1 }
  const list = filterRelics(q)
    .filter((r) => r.stockStatus !== 5)
    .map((r) => {
      const decorated = decorateRelic(r)
      decorated.scanCount = db.qrCodes.find((x) => x.fkRelicId === r.id)?.scanCount || 0
      return decorated
    })
  relicSort(list, q.sort)
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

/* ------------------------------------------------------------------ */
/* 出入库                                                              */
/* ------------------------------------------------------------------ */
function listStocks(ctx) {
  const q = ctx.query
  const list = db.stockRecords
    .filter((s) => !s.isDeleted)
    .filter((s) => (q.recordType ? s.recordType === Number(q.recordType) : true))
    .filter((s) => (q.applyStatus === '' || q.applyStatus === undefined ? true : s.applyStatus === Number(q.applyStatus)))
    .filter((s) => (q.operatorName ? contains(s.operatorName, q.operatorName) : true))
    .filter((s) => inRange(s.operateTime, q.beginTime, q.endTime))
    .filter((s) => {
      if (!q.keyword) return true
      const relic = relicOf(s.fkRelicId)
      return contains(s.recordCode, q.keyword) || contains(relic?.relicName, q.keyword) || contains(relic?.relicCode, q.keyword)
    })
    .sort((a, b) => (a.operateTime < b.operateTime ? 1 : -1))
    .map(decorateStock)
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

function createStock(ctx) {
  const body = ctx.body || {}
  if (!body.fkRelicId || !body.recordType) return fail('请选择藏品与业务类型')
  const relic = relicOf(body.fkRelicId)
  if (!relic) return fail('藏品不存在')
  if (relic.stockStatus === 3) return fail('藏品当前处于借出状态，不允许该操作')
  if (body.recordType === 1 && relic.stockStatus === 1) return fail('藏品已在库，无需重复入库')

  const seq = db.stockRecords.length + 1
  const record = {
    id: nextId('sr'),
    recordCode: `S-${new Date().getFullYear()}-${String(seq).padStart(3, '0')}`,
    recordType: Number(body.recordType),
    fkRelicId: relic.id,
    sourceTarget: body.sourceTarget || '',
    fkFromWarehouseId: body.fkFromWarehouseId || null,
    fkToWarehouseId: body.fkToWarehouseId || null,
    operatorName: body.operatorName || currentUser(ctx)?.realName || '张录入',
    operateTime: body.operateTime || now(),
    operateReason: body.operateReason || '',
    applyStatus: 0,
    auditOpinion: '',
    createUserId: currentUser(ctx)?.id || '',
    createUserName: currentUser(ctx)?.realName || '张录入',
    createTime: now(),
    updateUserName: currentUser(ctx)?.realName || '张录入',
    updateTime: now(),
    isDeleted: 0
  }
  db.stockRecords.unshift(record)
  persist()
  pushLog('出入库管理', '新增', `登记${['', '入库', '出库', '移库'][record.recordType]}单 ${record.recordCode}`, record.operatorName)
  persist()
  return ok(decorateStock(record), '登记成功，等待审核')
}

function updateStock(ctx) {
  const record = db.stockRecords.find((s) => s.id === ctx.params.id && !s.isDeleted)
  if (!record) return fail('出入库记录不存在')
  if (record.applyStatus !== 0) return fail('已审核的记录不可修改')
  Object.assign(record, ctx.body || {}, { updateTime: now() })
  persist()
  pushLog('出入库管理', '修改', `修改出入库单 ${record.recordCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(decorateStock(record), '保存成功')
}

function deleteStock(ctx) {
  const record = db.stockRecords.find((s) => s.id === ctx.params.id && !s.isDeleted)
  if (!record) return fail('出入库记录不存在')
  if (record.applyStatus !== 0) return fail('已审核的记录不可删除')
  record.isDeleted = 1
  persist()
  pushLog('出入库管理', '删除', `删除出入库单 ${record.recordCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(null, '已删除')
}

/* ------------------------------------------------------------------ */
/* 库房 / 仓位 / 盘点                                                   */
/* ------------------------------------------------------------------ */
function listWarehouses() {
  const list = db.warehouses
    .filter((w) => !w.isDeleted)
    .map((w) => ({
      ...w,
      positionCount: db.positions.filter((p) => p.fkWarehouseId === w.id && !p.isDeleted).length,
      relicCount: db.relics.filter((r) => r.fkWarehouseId === w.id && !r.isDeleted && r.stockStatus !== 5).length
    }))
  return ok(list)
}

function saveWarehouse(ctx) {
  const body = ctx.body || {}
  if (!body.warehouseCode || !body.warehouseName) return fail('库房编号与名称为必填项')
  if (ctx.params.id) {
    const item = db.warehouses.find((w) => w.id === ctx.params.id)
    if (!item) return fail('库房不存在')
    Object.assign(item, body, { updateUserName: currentUser(ctx)?.realName, updateTime: now() })
    persist()
    pushLog('库房管理', '修改', `修改库房 ${item.warehouseName}`, currentUser(ctx)?.realName)
    return ok(item, '保存成功')
  }
  if (db.warehouses.some((w) => w.warehouseCode === body.warehouseCode && !w.isDeleted)) return fail('库房编号已存在')
  const item = {
    id: nextId('wh'),
    warehouseCode: body.warehouseCode,
    warehouseName: body.warehouseName,
    location: body.location || '',
    keeperName: body.keeperName || '',
    isEnabled: body.isEnabled ?? 1,
    createUserName: currentUser(ctx)?.realName,
    createTime: now(),
    memo: body.memo || '',
    isDeleted: 0
  }
  db.warehouses.push(item)
  persist()
  pushLog('库房管理', '新增', `新增库房 ${item.warehouseName}`, currentUser(ctx)?.realName)
  persist()
  return ok(item, '新增成功')
}

function deleteWarehouse(ctx) {
  const item = db.warehouses.find((w) => w.id === ctx.params.id)
  if (!item) return fail('库房不存在')
  if (db.relics.some((r) => r.fkWarehouseId === item.id && !r.isDeleted && r.stockStatus !== 5)) {
    return fail('库房内仍有藏品，无法删除')
  }
  item.isDeleted = 1
  db.positions.filter((p) => p.fkWarehouseId === item.id).forEach((p) => { p.isDeleted = 1 })
  persist()
  pushLog('库房管理', '删除', `删除库房 ${item.warehouseName}`, currentUser(ctx)?.realName)
  persist()
  return ok(null, '已删除')
}

function listPositions(ctx) {
  const q = ctx.query
  const list = db.positions
    .filter((p) => !p.isDeleted)
    .filter((p) => (q.warehouseId ? p.fkWarehouseId === q.warehouseId : true))
    .map((p) => ({
      ...p,
      warehouseName: warehouseName(p.fkWarehouseId),
      relicCount: db.relics.filter((r) => r.fkPositionId === p.id && !r.isDeleted && r.stockStatus !== 5).length
    }))
  return ok(list)
}

function savePosition(ctx) {
  const body = ctx.body || {}
  if (!body.positionCode || !body.fkWarehouseId) return fail('仓位编号与所属库房为必填项')
  if (ctx.params.id) {
    const item = db.positions.find((p) => p.id === ctx.params.id)
    if (!item) return fail('仓位不存在')
    Object.assign(item, body)
    persist()
    pushLog('库房管理', '修改', `修改仓位 ${item.positionCode}`, currentUser(ctx)?.realName)
    return ok(item, '保存成功')
  }
  if (db.positions.some((p) => p.fkWarehouseId === body.fkWarehouseId && p.positionCode === body.positionCode && !p.isDeleted)) {
    return fail('同一库房内仓位编号不可重复')
  }
  const item = {
    id: nextId('pos'),
    positionCode: body.positionCode,
    positionName: body.positionName || `${body.positionCode} 号柜`,
    fkWarehouseId: body.fkWarehouseId,
    isEnabled: body.isEnabled ?? 1,
    isDeleted: 0
  }
  db.positions.push(item)
  persist()
  pushLog('库房管理', '新增', `新增仓位 ${item.positionCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(item, '新增成功')
}

function deletePosition(ctx) {
  const item = db.positions.find((p) => p.id === ctx.params.id)
  if (!item) return fail('仓位不存在')
  if (db.relics.some((r) => r.fkPositionId === item.id && !r.isDeleted && r.stockStatus !== 5)) {
    return fail('仓位已被藏品占用，不允许删除')
  }
  item.isDeleted = 1
  persist()
  pushLog('库房管理', '删除', `删除仓位 ${item.positionCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(null, '已删除')
}

function listChecks(ctx) {
  const q = ctx.query
  const list = db.stockChecks
    .filter((c) => !c.isDeleted)
    .filter((c) => (q.warehouseId ? c.fkWarehouseId === q.warehouseId : true))
    .map((c) => ({ ...c, warehouseName: warehouseName(c.fkWarehouseId) }))
    .sort((a, b) => (a.checkTime < b.checkTime ? 1 : -1))
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

function createCheck(ctx) {
  const body = ctx.body || {}
  if (!body.fkWarehouseId) return fail('请选择盘点库房')
  const total = db.relics.filter((r) => r.fkWarehouseId === body.fkWarehouseId && !r.isDeleted && r.stockStatus !== 5).length
  const real = Number(body.realCount ?? total)
  const item = {
    id: nextId('sc'),
    checkCode: `K-${new Date().getFullYear()}-${String(db.stockChecks.length + 1).padStart(3, '0')}`,
    fkWarehouseId: body.fkWarehouseId,
    checkUserName: body.checkUserName || currentUser(ctx)?.realName || '王库房',
    checkTime: body.checkTime || formatDate(new Date()),
    totalCount: total,
    realCount: real,
    abnormalCount: Math.max(0, total - real),
    checkStatus: body.checkStatus || 2,
    checkResult: body.checkResult || (total === real ? '账实相符，无异常。' : `存在 ${Math.max(0, total - real)} 件差异，需复核。`),
    createUserName: currentUser(ctx)?.realName || '王库房',
    createTime: now(),
    isDeleted: 0
  }
  db.stockChecks.unshift(item)
  persist()
  pushLog('库房管理', '新增', `创建盘点单 ${item.checkCode}`, item.checkUserName)
  persist()
  return ok(item, '盘点任务已生成')
}

function finishCheck(ctx) {
  const item = db.stockChecks.find((c) => c.id === ctx.params.id)
  if (!item) return fail('盘点单不存在')
  Object.assign(item, ctx.body || {}, {
    checkStatus: 2,
    abnormalCount: Math.max(0, Number(item.totalCount) - Number(ctx.body?.realCount ?? item.realCount))
  })
  persist()
  pushLog('库房管理', '修改', `完成盘点单 ${item.checkCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(item, '盘点已完成')
}

/* ------------------------------------------------------------------ */
/* 修复                                                                */
/* ------------------------------------------------------------------ */
function listRepairs(ctx) {
  const q = ctx.query
  const list = db.repairs
    .filter((r) => !r.isDeleted)
    .filter((r) => (q.applyStatus === '' || q.applyStatus === undefined ? true : r.applyStatus === Number(q.applyStatus)))
    .filter((r) => (!q.keyword ? true : contains(decorateRepair(r).relicName, q.keyword) || contains(r.applyCode, q.keyword)))
    .sort((a, b) => (a.createTime < b.createTime ? 1 : -1))
    .map(decorateRepair)
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

function createRepair(ctx) {
  const body = ctx.body || {}
  if (!body.fkRelicId || !body.repairItem) return fail('请选择藏品并填写修复项目')
  const relic = relicOf(body.fkRelicId)
  if (!relic) return fail('藏品不存在')
  if ([3, 5].includes(relic.stockStatus)) return fail('藏品当前状态不允许发起修复')
  const item = {
    id: nextId('rp'),
    applyCode: `R-${new Date().getFullYear()}-${String(db.repairs.length + 1).padStart(3, '0')}`,
    fkRelicId: relic.id,
    repairItem: body.repairItem,
    repairUserName: body.repairUserName || '周修复',
    startTime: body.startTime || formatDate(new Date()),
    endTime: null,
    conditionBefore: body.conditionBefore || '',
    conditionAfter: '',
    applyStatus: 1,
    auditOpinion: '',
    createUserId: currentUser(ctx)?.id || '',
    createUserName: currentUser(ctx)?.realName || '张录入',
    createTime: now(),
    isDeleted: 0
  }
  db.repairs.unshift(item)
  persist()
  pushLog('修复管理', '新增', `提交修复申请 ${item.applyCode}`, item.createUserName)
  persist()
  return ok(decorateRepair(item), '修复申请已提交审核')
}

function processRepair(ctx) {
  const item = db.repairs.find((r) => r.id === ctx.params.id)
  if (!item) return fail('修复申请不存在')
  if (item.applyStatus !== 2) return fail('仅审核通过的修复申请可记录修复过程')
  Object.assign(item, {
    conditionAfter: ctx.body?.conditionAfter ?? item.conditionAfter,
    endTime: ctx.body?.endTime || formatDate(new Date())
  })
  const relic = relicOf(item.fkRelicId)
  if (relic) {
    relic.integrityStatus = ctx.body?.integrityStatus || relic.integrityStatus
    relic.stockStatus = ctx.body?.backToStock === false ? relic.stockStatus : 1
    pushTrace(relic.id, 6, `修复完成：${item.repairItem}，${item.conditionAfter || '状况改善'}。`, item.repairUserName)
  }
  persist()
  pushLog('修复管理', '修改', `记录修复过程 ${item.applyCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(decorateRepair(item), '修复记录已保存')
}

function deleteRepair(ctx) {
  const item = db.repairs.find((r) => r.id === ctx.params.id)
  if (!item) return fail('修复申请不存在')
  if (item.applyStatus === 2) return fail('审核通过的修复申请不可删除')
  item.isDeleted = 1
  persist()
  return ok(null, '已删除')
}

/* ------------------------------------------------------------------ */
/* 展览借调                                                            */
/* ------------------------------------------------------------------ */
function listLoans(ctx) {
  const q = ctx.query
  const list = db.loans
    .filter((l) => !l.isDeleted)
    .filter((l) => (q.loanStatus === '' || q.loanStatus === undefined ? true : l.loanStatus === Number(q.loanStatus)))
    .filter((l) => (!q.keyword ? true : contains(decorateLoan(l).relicName, q.keyword) || contains(l.loanCode, q.keyword) || contains(l.loanUnit, q.keyword)))
    .sort((a, b) => (a.createTime < b.createTime ? 1 : -1))
    .map(decorateLoan)
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

function createLoan(ctx) {
  const body = ctx.body || {}
  if (!body.fkRelicId || !body.loanUnit) return fail('请选择藏品并填写借调单位')
  const relic = relicOf(body.fkRelicId)
  if (!relic) return fail('藏品不存在')
  if ([3, 4, 5].includes(relic.stockStatus)) return fail('藏品当前状态不允许借调')
  const conflict = db.loans.some((l) => l.fkRelicId === relic.id && l.loanStatus === 2 && !l.isDeleted)
  if (conflict) return fail('该藏品存在进行中的借调，时间冲突')
  const item = {
    id: nextId('ln'),
    loanCode: `L-${new Date().getFullYear()}-${String(db.loans.length + 1).padStart(3, '0')}`,
    fkRelicId: relic.id,
    loanUnit: body.loanUnit,
    loanUserName: body.loanUserName || currentUser(ctx)?.realName || '张录入',
    loanTime: body.loanTime || formatDate(new Date()),
    planReturnTime: body.planReturnTime || formatDate(new Date()),
    actualReturnTime: null,
    loanStatus: 1,
    auditOpinion: '',
    agreementPath: null,
    createUserName: currentUser(ctx)?.realName || '张录入',
    createTime: now(),
    isDeleted: 0
  }
  db.loans.unshift(item)
  persist()
  pushLog('展览借调', '新增', `提交借调申请 ${item.loanCode}（${item.loanUnit}）`, item.createUserName)
  persist()
  return ok(decorateLoan(item), '借调申请已提交审核')
}

/** 出库交接：审核通过后由录入员办理，藏品状态转为借出 */
function handoverLoan(ctx) {
  const item = db.loans.find((l) => l.id === ctx.params.id)
  if (!item) return fail('借调记录不存在')
  if (item.loanStatus !== 1) return fail('仅待审核的借调可办理出库交接')
  const audit = db.auditRecords.find((a) => a.bizType === 4 && a.fkBizId === item.id && a.auditResult === 1)
  if (!audit) return fail('请先完成审核，通过后方可办理交接')
  item.loanStatus = 2
  item.agreementPath = `/files/loan/${item.loanCode}.pdf`
  const relic = relicOf(item.fkRelicId)
  if (relic) {
    relic.stockStatus = 3
    pushTrace(relic.id, 7, `借出交接完成：借调至${item.loanUnit}，预计 ${item.planReturnTime} 归还。`, item.loanUserName)
  }
  persist()
  pushLog('展览借调', '修改', `办理出库交接 ${item.loanCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(decorateLoan(item), '出库交接完成')
}

/** 归还接收 */
function returnLoan(ctx) {
  const item = db.loans.find((l) => l.id === ctx.params.id)
  if (!item) return fail('借调记录不存在')
  if (item.loanStatus !== 2) return fail('仅借出中的记录可办理归还')
  item.loanStatus = 3
  item.actualReturnTime = ctx.body?.actualReturnTime || formatDate(new Date())
  const relic = relicOf(item.fkRelicId)
  if (relic) {
    relic.stockStatus = 1
    pushTrace(relic.id, 8, `归还接收完成，验收${ctx.body?.inspection || '无新增损伤'}，恢复在库。`, item.loanUserName)
  }
  persist()
  pushLog('展览借调', '修改', `办理归还接收 ${item.loanCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(decorateLoan(item), '归还接收完成')
}

/* ------------------------------------------------------------------ */
/* 注销                                                                */
/* ------------------------------------------------------------------ */
function listCancels(ctx) {
  const q = ctx.query
  const list = db.cancels
    .filter((c) => !c.isDeleted)
    .filter((c) => (q.applyStatus === '' || q.applyStatus === undefined ? true : c.applyStatus === Number(q.applyStatus)))
    .filter((c) => (!q.keyword ? true : contains(decorateCancel(c).relicName, q.keyword) || contains(c.applyCode, q.keyword)))
    .sort((a, b) => (a.createTime < b.createTime ? 1 : -1))
    .map(decorateCancel)
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

function createCancel(ctx) {
  const body = ctx.body || {}
  if (!body.fkRelicId || !body.cancelReason) return fail('请选择藏品并填写注销原因')
  const relic = relicOf(body.fkRelicId)
  if (!relic) return fail('藏品不存在')
  if (relic.stockStatus === 3 || relic.stockStatus === 4) return fail('藏品存在未了结的借调或修复业务，不允许注销')
  if (db.cancels.some((c) => c.fkRelicId === relic.id && c.applyStatus === 0 && !c.isDeleted)) return fail('该藏品已有待审核的注销申请')
  const item = {
    id: nextId('cc'),
    applyCode: `X-${new Date().getFullYear()}-${String(db.cancels.length + 1).padStart(3, '0')}`,
    fkRelicId: relic.id,
    cancelReason: body.cancelReason,
    cancelUserName: body.cancelUserName || currentUser(ctx)?.realName || '张录入',
    applyStatus: 0,
    auditOpinion: '',
    archiveUserName: null,
    archiveTime: null,
    createUserName: currentUser(ctx)?.realName || '张录入',
    createTime: now(),
    isDeleted: 0
  }
  db.cancels.unshift(item)
  persist()
  pushLog('注销管理', '新增', `提交注销申请 ${item.applyCode}`, item.createUserName)
  persist()
  return ok(decorateCancel(item), '注销申请已提交审核')
}

/** 注销归档：审核通过后由系统管理员执行 */
function archiveCancel(ctx) {
  const item = db.cancels.find((c) => c.id === ctx.params.id)
  if (!item) return fail('注销申请不存在')
  if (item.applyStatus !== 1) return fail('仅审核通过的注销申请可执行归档')
  if (item.archiveTime) return fail('该申请已完成归档')
  item.archiveUserName = currentUser(ctx)?.realName || '系统管理员'
  item.archiveTime = now()
  const relic = relicOf(item.fkRelicId)
  if (relic) {
    relic.stockStatus = 5
    pushTrace(relic.id, 9, `注销归档完成：${item.cancelReason}`, item.archiveUserName)
  }
  persist()
  pushLog('注销管理', '修改', `执行注销归档 ${item.applyCode}`, item.archiveUserName)
  persist()
  return ok(decorateCancel(item), '已注销归档')
}

/* ------------------------------------------------------------------ */
/* 审核                                                                */
/* ------------------------------------------------------------------ */
/** 各类待审业务统一成同一结构，供审核工作台使用 */
function pendingList(bizType) {
  if (bizType === 1) {
    return db.collectionApplies
      .filter((c) => c.applyStatus === 1 && !c.isDeleted)
      .map((c) => ({
        bizType: 1, bizId: c.id, bizCode: c.applyCode, title: c.relicName,
        subtitle: `${c.era || '—'} · ${categoryName(c.fkCategoryId)} · ${c.collectionWay || '—'}`,
        submitUser: c.createUserName, submitTime: c.createTime,
        detail: c.collectionDesc, amount: c.collectionPrice
      }))
  }
  if (bizType === 2) {
    return db.stockRecords
      .filter((s) => s.applyStatus === 0 && !s.isDeleted)
      .map((s) => ({
        bizType: 2, bizId: s.id, bizCode: s.recordCode,
        title: `${['', '入库', '出库', '移库'][s.recordType]} · ${relicOf(s.fkRelicId)?.relicName || ''}`,
        subtitle: `${s.sourceTarget || '—'} · 经办人 ${s.operatorName}`,
        submitUser: s.createUserName, submitTime: s.createTime,
        detail: s.operateReason
      }))
  }
  if (bizType === 3) {
    return db.repairs
      .filter((r) => r.applyStatus === 1 && !r.isDeleted)
      .map((r) => ({
        bizType: 3, bizId: r.id, bizCode: r.applyCode,
        title: `修复 · ${relicOf(r.fkRelicId)?.relicName || ''}`,
        subtitle: `${r.repairItem} · 修复人 ${r.repairUserName}`,
        submitUser: r.createUserName, submitTime: r.createTime,
        detail: r.conditionBefore
      }))
  }
  if (bizType === 4) {
    return db.loans
      .filter((l) => l.loanStatus === 1 && !l.isDeleted)
      .map((l) => ({
        bizType: 4, bizId: l.id, bizCode: l.loanCode,
        title: `借调 · ${relicOf(l.fkRelicId)?.relicName || ''}`,
        subtitle: `${l.loanUnit} · ${l.loanTime} 至 ${l.planReturnTime}`,
        submitUser: l.createUserName, submitTime: l.createTime,
        detail: `借调单位：${l.loanUnit}，经办人：${l.loanUserName}`
      }))
  }
  return db.cancels
    .filter((c) => c.applyStatus === 0 && !c.isDeleted)
    .map((c) => ({
      bizType: 5, bizId: c.id, bizCode: c.applyCode,
      title: `注销 · ${relicOf(c.fkRelicId)?.relicName || ''}`,
      subtitle: `${categoryName(relicOf(c.fkRelicId)?.fkCategoryId)} · 经办人 ${c.cancelUserName}`,
      submitUser: c.createUserName, submitTime: c.createTime,
      detail: c.cancelReason
    }))
}

function listPending(ctx) {
  const { bizType, pageNum, pageSize } = ctx.query
  const types = bizType ? [Number(bizType)] : [1, 2, 3, 4, 5]
  const list = types.flatMap((t) => pendingList(t)).sort((a, b) => (a.submitTime < b.submitTime ? 1 : -1))
  return ok(paginate(list, { pageNum, pageSize: pageSize || 50 }))
}

function pendingCount() {
  const counts = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  Object.keys(counts).forEach((key) => { counts[key] = pendingList(Number(key)).length })
  return ok({ ...counts, total: Object.values(counts).reduce((a, b) => a + b, 0) })
}

/** 执行审核：更新业务状态 + 写审核记录 + 写溯源（审核通过的业务） */
function audit(ctx) {
  const { bizType, bizId, auditResult, auditOpinion } = ctx.body || {}
  const type = Number(bizType)
  const result = Number(auditResult)
  if (!type || !bizId || ![1, 2].includes(result)) return fail('审核参数不完整')
  if (result === 2 && !String(auditOpinion || '').trim()) return fail('驳回时必须填写审核意见')

  const auditor = currentUser(ctx)
  const opinion = auditOpinion || '同意。'
  let bizCode = ''
  let relicId = null
  let traceType = null
  let traceContent = ''

  if (type === 1) {
    const item = db.collectionApplies.find((c) => c.id === bizId)
    if (!item || item.applyStatus !== 1) return fail('该征集申请不在待审核状态')
    item.applyStatus = result === 1 ? 2 : 3
    item.auditOpinion = opinion
    item.updateTime = now()
    bizCode = item.applyCode
    traceType = 1
    traceContent = result === 1 ? `征集审核通过：${opinion}` : `征集审核驳回：${opinion}`
  } else if (type === 2) {
    const item = db.stockRecords.find((s) => s.id === bizId)
    if (!item || item.applyStatus !== 0) return fail('该出入库单不在待审核状态')
    item.applyStatus = result === 1 ? 1 : 2
    item.auditOpinion = opinion
    bizCode = item.recordCode
    relicId = item.fkRelicId
    if (result === 1) {
      const relic = relicOf(relicId)
      if (relic) {
        if (item.recordType === 1) {
          relic.stockStatus = 1
          if (item.fkToWarehouseId) relic.fkWarehouseId = item.fkToWarehouseId
        } else if (item.recordType === 2) {
          relic.stockStatus = 2
        } else {
          if (item.fkToWarehouseId) relic.fkWarehouseId = item.fkToWarehouseId
        }
        traceType = item.recordType === 1 ? 3 : item.recordType === 2 ? 4 : 5
        traceContent = `${['', '入库', '出库', '移库'][item.recordType]}审核通过：${item.sourceTarget}，${item.operateReason}`
        pushTrace(relic.id, traceType, traceContent, auditor?.realName || '李审核')
      }
    }
  } else if (type === 3) {
    const item = db.repairs.find((r) => r.id === bizId)
    if (!item || item.applyStatus !== 1) return fail('该修复申请不在待审核状态')
    item.applyStatus = result === 1 ? 2 : 3
    item.auditOpinion = opinion
    bizCode = item.applyCode
    relicId = item.fkRelicId
    if (result === 1) {
      const relic = relicOf(relicId)
      if (relic) {
        relic.stockStatus = 4
        pushTrace(relic.id, 6, `修复申请审核通过，开始修复：${item.repairItem}。`, auditor?.realName || '李审核')
      }
    }
  } else if (type === 4) {
    const item = db.loans.find((l) => l.id === bizId)
    if (!item || item.loanStatus !== 1) return fail('该借调申请不在待审核状态')
    item.loanStatus = result === 1 ? 1 : 4
    item.auditOpinion = opinion
    bizCode = item.loanCode
    relicId = item.fkRelicId
    if (result === 1) {
      const relic = relicOf(relicId)
      if (relic) pushTrace(relic.id, 7, `借调申请审核通过，借往${item.loanUnit}，等待办理出库交接。`, auditor?.realName || '李审核')
    }
  } else if (type === 5) {
    const item = db.cancels.find((c) => c.id === bizId)
    if (!item || item.applyStatus !== 0) return fail('该注销申请不在待审核状态')
    item.applyStatus = result === 1 ? 1 : 2
    item.auditOpinion = opinion
    bizCode = item.applyCode
    relicId = item.fkRelicId
  } else {
    return fail('未知的业务类型')
  }

  db.auditRecords.unshift({
    id: nextId('au'),
    bizType: type,
    fkBizId: bizId,
    bizCode,
    auditUserId: auditor?.id || '',
    auditUserName: auditor?.realName || '李审核',
    auditTime: now(),
    auditResult: result,
    auditOpinion: opinion,
    createTime: now(),
    isDeleted: 0
  })
  persist()
  pushLog('审核工作台', '审核', `审核 ${bizCode}，结果：${result === 1 ? '通过' : '驳回'}`, auditor?.realName || '李审核')
  persist()
  return ok({ bizType: type, bizId, auditResult: result }, result === 1 ? '审核通过' : '已驳回')
}

/** 批量审核（仅支持通过，对应"批量通过"扩展路径） */
function batchAudit(ctx) {
  const items = ctx.body?.items || []
  if (!items.length) return fail('请选择待审核记录')
  let success = 0
  const messages = []
  items.forEach((item) => {
    const res = audit({ body: { ...item, auditResult: 1, auditOpinion: ctx.body?.auditOpinion || '批量通过。' }, __token: ctx.__token })
    if (res.code === 200) success += 1
    else messages.push(res.message)
  })
  if (success === 0) return fail(messages[0] || '批量审核失败')
  return ok({ success }, `已通过 ${success} 条${messages.length ? `，${messages.length} 条未处理` : ''}`)
}

function listAudits(ctx) {
  const q = ctx.query
  const list = db.auditRecords
    .filter((a) => !a.isDeleted)
    .filter((a) => (q.bizType ? a.bizType === Number(q.bizType) : true))
    .filter((a) => (q.auditResult ? a.auditResult === Number(q.auditResult) : true))
    .filter((a) => (!q.keyword ? true : contains(a.bizCode, q.keyword) || contains(a.auditUserName, q.keyword)))
    .sort((a, b) => (a.auditTime < b.auditTime ? 1 : -1))
    .map((a) => ({ ...a, bizTypeName: ['', '征集审核', '出入库审核', '修复审核', '借调审核', '注销审核'][a.bizType] }))
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

/* ------------------------------------------------------------------ */
/* 统计                                                                */
/* ------------------------------------------------------------------ */
function statsOverview() {
  const alive = db.relics.filter((r) => !r.isDeleted)
  const pending = pendingCount().data.total
  const monthPrefix = now().slice(0, 7)
  const monthStock = db.stockRecords.filter((s) => s.operateTime.startsWith(monthPrefix) && !s.isDeleted).length
  const monthNew = alive.filter((r) => String(r.createTime).startsWith(monthPrefix)).length
  return ok({
    totalRelics: alive.length,
    inStock: alive.filter((r) => r.stockStatus === 1).length,
    outbound: alive.filter((r) => r.stockStatus === 2).length,
    loaned: alive.filter((r) => r.stockStatus === 3).length,
    repairing: alive.filter((r) => r.stockStatus === 4).length,
    cancelled: alive.filter((r) => r.stockStatus === 5).length,
    pendingAudits: pending,
    monthStock,
    monthNew,
    publicCount: alive.filter((r) => r.isPublic === 1).length,
    warehouseCount: db.warehouses.filter((w) => !w.isDeleted).length,
    scanTotal: db.qrCodes.reduce((sum, q) => sum + (q.scanCount || 0), 0),
    monthTrace: db.traces.filter((t) => t.traceTime.startsWith(monthPrefix) && !t.isDeleted).length
  })
}

function statsCategory() {
  const alive = db.relics.filter((r) => !r.isDeleted && r.stockStatus !== 5)
  return ok(db.categories.map((c) => ({
    name: c.categoryName,
    value: alive.filter((r) => r.fkCategoryId === c.id).length
  })))
}

function statsEra() {
  const alive = db.relics.filter((r) => !r.isDeleted && r.stockStatus !== 5)
  const map = new Map()
  alive.forEach((r) => map.set(r.era || '未知', (map.get(r.era || '未知') || 0) + 1))
  return ok([...map.entries()].map(([name, value]) => ({ name, value })))
}

function statsLevel() {
  const alive = db.relics.filter((r) => !r.isDeleted && r.stockStatus !== 5)
  return ok([
    { name: '一级', value: alive.filter((r) => r.relicLevel === 1).length },
    { name: '二级', value: alive.filter((r) => r.relicLevel === 2).length },
    { name: '三级', value: alive.filter((r) => r.relicLevel === 3).length },
    { name: '一般', value: alive.filter((r) => r.relicLevel === 4).length }
  ])
}

function statsWarehouse() {
  const alive = db.relics.filter((r) => !r.isDeleted && r.stockStatus !== 5)
  return ok(db.warehouses.filter((w) => !w.isDeleted).map((w) => ({
    name: w.warehouseName,
    value: alive.filter((r) => r.fkWarehouseId === w.id).length
  })))
}

function statsStockTrend(ctx) {
  const months = Number(ctx.query.months || 6)
  const labels = []
  const base = new Date()
  for (let i = months - 1; i >= 0; i -= 1) {
    const d = new Date(base.getFullYear(), base.getMonth() - i, 1)
    labels.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  return ok({
    labels,
    inbound: labels.map((m) => db.stockRecords.filter((s) => s.recordType === 1 && s.operateTime.startsWith(m) && !s.isDeleted).length),
    outbound: labels.map((m) => db.stockRecords.filter((s) => s.recordType === 2 && s.operateTime.startsWith(m) && !s.isDeleted).length),
    trace: labels.map((m) => db.traces.filter((t) => t.traceTime.startsWith(m) && !t.isDeleted).length)
  })
}

function statsPublic() {
  const alive = db.relics.filter((r) => !r.isDeleted && r.isPublic === 1 && r.stockStatus !== 5)
  return ok({
    total: alive.length,
    categories: db.categories.map((c) => ({ name: c.categoryName, value: alive.filter((r) => r.fkCategoryId === c.id).length })),
    eras: (() => {
      const map = new Map()
      alive.forEach((r) => map.set(r.era || '未知', (map.get(r.era || '未知') || 0) + 1))
      return [...map.entries()].map(([name, value]) => ({ name, value }))
    })(),
    warehouses: db.warehouses.filter((w) => !w.isDeleted).map((w) => ({
      name: w.warehouseName, value: alive.filter((r) => r.fkWarehouseId === w.id).length
    })),
    recentTraces: db.traces.filter((t) => !t.isDeleted).slice(-6).reverse().map(decorateTrace),
    scanTotal: db.qrCodes.reduce((sum, q) => sum + (q.scanCount || 0), 0)
  })
}

/* ------------------------------------------------------------------ */
/* 字典 / 参数 / 日志                                                   */
/* ------------------------------------------------------------------ */
function listDicts(ctx) {
  const { dictType = '', keyword = '' } = ctx.query
  const list = db.dicts
    .filter((d) => !d.isDeleted)
    .filter((d) => (dictType ? d.dictType === dictType : true))
    .filter((d) => (!keyword ? true : contains(d.dictLabel, keyword) || contains(d.dictCode, keyword)))
    .sort((a, b) => (a.dictType === b.dictType ? a.sortNo - b.sortNo : a.dictType < b.dictType ? -1 : 1))
  return ok(list)
}

function saveDict(ctx) {
  const body = ctx.body || {}
  if (!body.dictType || !body.dictCode || !body.dictLabel) return fail('字典类型、编码、名称均为必填项')
  if (ctx.params.id) {
    const item = db.dicts.find((d) => d.id === ctx.params.id)
    if (!item) return fail('字典项不存在')
    Object.assign(item, body, { updateTime: now() })
    persist()
    pushLog('数据字典', '修改', `修改字典项 ${item.dictType}/${item.dictCode}`, currentUser(ctx)?.realName)
    return ok(item, '保存成功')
  }
  if (db.dicts.some((d) => d.dictType === body.dictType && d.dictCode === body.dictCode && !d.isDeleted)) {
    return fail('同一字典类型下编码不可重复')
  }
  const item = {
    id: nextId('dict'),
    dictType: body.dictType,
    dictCode: body.dictCode,
    dictLabel: body.dictLabel,
    sortNo: Number(body.sortNo || db.dicts.filter((d) => d.dictType === body.dictType).length + 1),
    isEnabled: body.isEnabled ?? 1,
    createUserName: currentUser(ctx)?.realName,
    createTime: now(),
    updateTime: now(),
    remark: body.remark || '',
    isDeleted: 0
  }
  db.dicts.push(item)
  persist()
  pushLog('数据字典', '新增', `新增字典项 ${item.dictType}/${item.dictCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(item, '新增成功')
}

function deleteDict(ctx) {
  const item = db.dicts.find((d) => d.id === ctx.params.id)
  if (!item) return fail('字典项不存在')
  item.isDeleted = 1
  persist()
  pushLog('数据字典', '删除', `删除字典项 ${item.dictType}/${item.dictCode}`, currentUser(ctx)?.realName)
  persist()
  return ok(null, '已删除')
}

function listConfigs(ctx) {
  const { keyword = '' } = ctx.query
  const list = db.configs.filter((c) => !c.isDeleted).filter((c) => (!keyword ? true : contains(c.configName, keyword) || contains(c.configKey, keyword)))
  return ok(list)
}

function updateConfig(ctx) {
  const item = db.configs.find((c) => c.id === ctx.params.id)
  if (!item) return fail('参数不存在')
  if (!String(ctx.body?.configValue ?? '').trim()) return fail('参数值不能为空')
  item.configValue = ctx.body.configValue
  item.updateTime = now()
  persist()
  pushLog('系统参数', '修改', `参数 ${item.configKey} 调整为 ${item.configValue}`, currentUser(ctx)?.realName)
  persist()
  return ok(item, '保存成功')
}

function listLogs(ctx) {
  const q = ctx.query
  const list = db.operateLogs
    .filter((l) => !l.isDeleted)
    .filter((l) => (q.module ? contains(l.module, q.module) : true))
    .filter((l) => (q.operateType ? l.operateType === q.operateType : true))
    .filter((l) => (q.operateUserName ? contains(l.operateUserName, q.operateUserName) : true))
    .filter((l) => inRange(l.operateTime, q.beginTime, q.endTime))
    .filter((l) => (!q.keyword ? true : contains(l.operateContent, q.keyword) || contains(l.operateUserName, q.keyword)))
    .sort((a, b) => (a.operateTime < b.operateTime ? 1 : -1))
  return ok(paginate(list, { pageNum: q.pageNum, pageSize: q.pageSize }))
}

function modulesOfLogs() {
  return ok([...new Set(db.operateLogs.map((l) => l.module))])
}

/* ------------------------------------------------------------------ */
/* 路由表                                                              */
/* ------------------------------------------------------------------ */
const routes = [
  ['POST', /^\/auth\/login$/, login],
  ['POST', /^\/auth\/logout$/, () => ok(null, '已退出登录')],
  ['GET', /^\/auth\/profile$/, profile],

  ['GET', /^\/users$/, listUsers],
  ['POST', /^\/users$/, createUser],
  ['PUT', /^\/users\/([^/]+)\/password$/, resetPassword],
  ['PUT', /^\/users\/([^/]+)\/status$/, toggleUser],
  ['PUT', /^\/users\/([^/]+)$/, updateUser],
  ['DELETE', /^\/users\/([^/]+)$/, deleteUser],

  ['GET', /^\/categories$/, () => ok(db.categories)],

  ['GET', /^\/collection-applies$/, listCollections],
  ['POST', /^\/collection-applies$/, createCollection],
  ['POST', /^\/collection-applies\/batch-delete$/, batchDeleteCollection],
  ['POST', /^\/collection-applies\/([^/]+)\/submit$/, submitCollection],
  ['GET', /^\/collection-applies\/([^/]+)$/, getCollection],
  ['PUT', /^\/collection-applies\/([^/]+)$/, updateCollection],
  ['DELETE', /^\/collection-applies\/([^/]+)$/, deleteCollection],

  ['GET', /^\/relics$/, listRelics],
  ['POST', /^\/relics$/, createRelic],
  ['GET', /^\/relics\/public\/highlights$/, publicHighlights],
  ['GET', /^\/relics\/public\/search$/, publicSearch],
  ['GET', /^\/relics\/by-code\/([^/]+)$/, getRelicByCode],
  ['POST', /^\/relics\/batch-delete$/, batchDeleteRelic],
  ['GET', /^\/relics\/([^/]+)\/trace$/, (ctx) => {
    const list = db.traces.filter((t) => t.fkRelicId === ctx.params.id && !t.isDeleted).map(decorateTrace)
    return ok(list)
  }],
  ['GET', /^\/relics\/([^/]+)$/, getRelic],
  ['PUT', /^\/relics\/([^/]+)$/, updateRelic],
  ['DELETE', /^\/relics\/([^/]+)$/, deleteRelic],

  ['GET', /^\/stock-records$/, listStocks],
  ['POST', /^\/stock-records$/, createStock],
  ['PUT', /^\/stock-records\/([^/]+)$/, updateStock],
  ['DELETE', /^\/stock-records\/([^/]+)$/, deleteStock],

  ['GET', /^\/warehouses$/, listWarehouses],
  ['POST', /^\/warehouses$/, saveWarehouse],
  ['PUT', /^\/warehouses\/([^/]+)$/, saveWarehouse],
  ['DELETE', /^\/warehouses\/([^/]+)$/, deleteWarehouse],

  ['GET', /^\/positions$/, listPositions],
  ['POST', /^\/positions$/, savePosition],
  ['PUT', /^\/positions\/([^/]+)$/, savePosition],
  ['DELETE', /^\/positions\/([^/]+)$/, deletePosition],

  ['GET', /^\/stock-checks$/, listChecks],
  ['POST', /^\/stock-checks$/, createCheck],
  ['PUT', /^\/stock-checks\/([^/]+)$/, finishCheck],

  ['GET', /^\/repairs$/, listRepairs],
  ['POST', /^\/repairs$/, createRepair],
  ['POST', /^\/repairs\/([^/]+)\/process$/, processRepair],
  ['DELETE', /^\/repairs\/([^/]+)$/, deleteRepair],

  ['GET', /^\/loans$/, listLoans],
  ['POST', /^\/loans$/, createLoan],
  ['POST', /^\/loans\/([^/]+)\/handover$/, handoverLoan],
  ['POST', /^\/loans\/([^/]+)\/return$/, returnLoan],

  ['GET', /^\/cancels$/, listCancels],
  ['POST', /^\/cancels$/, createCancel],
  ['POST', /^\/cancels\/([^/]+)\/archive$/, archiveCancel],

  ['GET', /^\/audits\/pending$/, listPending],
  ['GET', /^\/audits\/pending-count$/, pendingCount],
  ['GET', /^\/audits$/, listAudits],
  ['POST', /^\/audits$/, audit],
  ['POST', /^\/audits\/batch$/, batchAudit],

  ['GET', /^\/stats\/overview$/, statsOverview],
  ['GET', /^\/stats\/category$/, statsCategory],
  ['GET', /^\/stats\/era$/, statsEra],
  ['GET', /^\/stats\/level$/, statsLevel],
  ['GET', /^\/stats\/warehouse$/, statsWarehouse],
  ['GET', /^\/stats\/stock-trend$/, statsStockTrend],
  ['GET', /^\/stats\/public$/, statsPublic],

  ['GET', /^\/dicts$/, listDicts],
  ['POST', /^\/dicts$/, saveDict],
  ['PUT', /^\/dicts\/([^/]+)$/, saveDict],
  ['DELETE', /^\/dicts\/([^/]+)$/, deleteDict],

  ['GET', /^\/configs$/, listConfigs],
  ['PUT', /^\/configs\/([^/]+)$/, updateConfig],

  ['GET', /^\/logs$/, listLogs],
  ['GET', /^\/logs\/modules$/, modulesOfLogs]
]

/**
 * mock 入口：由 axios 自定义 adapter 调用
 * @param {{method:string,url:string,params?:object,data?:any,token?:string}} request
 */
export async function mockRequest(request) {
  const method = String(request.method || 'GET').toUpperCase()
  const path = String(request.url || '').replace(/^\/api/, '').split('?')[0]
  const query = request.params || {}
  let body = request.data
  if (typeof body === 'string') {
    try { body = JSON.parse(body) } catch { body = {} }
  }

  // 人工延迟，模拟真实网络，便于观察 loading 态
  await new Promise((resolve) => setTimeout(resolve, 180 + Math.random() * 220))

  const route = routes.find(([m, pattern]) => m === method && pattern.test(path))
  if (!route) {
    console.warn('[mock] 未匹配的接口：', method, path)
    return fail(`接口不存在：${method} ${path}`, 404)
  }
  const match = path.match(route[1])
  const params = {}
  if (match) {
    // 现有路由最多一个路径参数：by-code 接口取 code，其余统一取 id
    const key = route[1].source.includes('by-code') ? 'code' : 'id'
    match.slice(1).forEach((value, index) => {
      params[index === 0 ? key : `p${index}`] = decodeURIComponent(value)
    })
  }

  try {
    return route[2]({
      params,
      query,
      body,
      __token: request.token || ''
    })
  } catch (error) {
    console.error('[mock] 处理异常：', error)
    return fail(error.message || '服务器内部错误')
  }
}

export { db }
