import request from './request'

/**
 * 统计分析接口
 * ------------------------------------------------------------------
 * 后端分组统计统一以 { list: [{ ..., count }] } 形式返回，
 * 而图表组件需要 [{ name, value }]；趋势接口需要 { labels, inbound, outbound, trace }。
 * 这里集中做一次「接口 → 视图」的整形，避免每个页面各写一套 map，
 * 同时兼容后端直接返回数组的写法。
 */

/** 取出分组列表：兼容 { list: [] } 与裸数组两种返回 */
function groupList(payload) {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.list)) return payload.list
  return []
}

/**
 * 分组列表整形为图表数据
 * @param {*} payload 接口返回
 * @param {string[]} nameKeys 名称字段候选（按顺序取第一个有值的）
 */
function toPairs(payload, nameKeys = []) {
  return groupList(payload).map((item) => {
    const label = nameKeys
      .map((key) => item?.[key])
      .find((value) => value !== undefined && value !== null && value !== '')
    return {
      name: label ?? item?.name ?? '未分类',
      value: Number(item?.value ?? item?.count ?? 0)
    }
  })
}

/** 工作台 / 统计分析总览 */
export const getOverview = () => request.get('/stats/overview')

/** 分类构成 */
export async function getCategoryStats() {
  return toPairs(await request.get('/stats/category'), ['categoryName'])
}

/** 年代分布 */
export async function getEraStats() {
  return toPairs(await request.get('/stats/era'), ['era'])
}

/** 级别构成 */
export async function getLevelStats() {
  return toPairs(await request.get('/stats/level'), ['levelName'])
}

/** 库房分布 */
export async function getWarehouseStats() {
  return toPairs(await request.get('/stats/warehouse'), ['warehouseName'])
}

/** 近 n 个月出入库与溯源趋势 */
export async function getStockTrend(months = 6) {
  const payload = await request.get('/stats/stock-trend', { params: { months } })
  if (payload && Array.isArray(payload.labels)) return payload
  const rows = groupList(payload)
  return {
    labels: rows.map((row) => row.month ?? ''),
    inbound: rows.map((row) => Number(row.inCount ?? 0)),
    outbound: rows.map((row) => Number(row.outCount ?? 0)),
    trace: rows.map((row) => Number(row.traceCount ?? 0))
  }
}

/**
 * 前台公开统计（免登录）。
 * 返回公开藏品数、分类 / 年代 / 库房分布、累计扫码与最新流转。
 */
export async function getPublicStats() {
  const data = (await request.get('/stats/public')) || {}
  return {
    total: Number(data.total ?? data.totalRelics ?? 0),
    totalCategories: Number(data.totalCategories ?? 0),
    categories: Array.isArray(data.categories) ? data.categories : [],
    eras: Array.isArray(data.eras) ? data.eras : [],
    warehouses: Array.isArray(data.warehouses) ? data.warehouses : [],
    scanTotal: Number(data.scanTotal ?? 0),
    recentTraces: data.recentTraces || []
  }
}
