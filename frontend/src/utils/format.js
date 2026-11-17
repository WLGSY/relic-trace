/**
 * 通用格式化与取值工具
 */

/** 补零 */
const pad = (n) => String(n).padStart(2, '0')

/** 解析为 Date（兼容 "2026-10-28 14:32:00" 与 ISO） */
export function toDate(value) {
  if (!value) return null
  if (value instanceof Date) return value
  const normalized = String(value).replace(/-/g, '/')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

/** 日期：YYYY-MM-DD */
export function formatDate(value, fallback = '—') {
  const date = toDate(value)
  if (!date) return fallback
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

/** 日期时间：YYYY-MM-DD HH:mm */
export function formatDateTime(value, fallback = '—') {
  const date = toDate(value)
  if (!date) return fallback
  return `${formatDate(date)} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

/** 仅时间：HH:mm */
export function formatTime(value, fallback = '—') {
  const date = toDate(value)
  if (!date) return fallback
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`
}

/** 今天（YYYY-MM-DD），用于表单默认值 */
export function today() {
  return formatDate(new Date())
}

/** 当前时间（YYYY-MM-DD HH:mm:ss） */
export function now() {
  const d = new Date()
  return `${formatDate(d)} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/** 金额 / 数量千分位 */
export function formatNumber(value, fallback = '0') {
  if (value === null || value === undefined || value === '') return fallback
  const num = Number(value)
  if (Number.isNaN(num)) return fallback
  return num.toLocaleString('zh-CN')
}

/** 金额（元） */
export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '—'
  return `¥ ${Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

/** 姓名首字（头像占位） */
export function initials(name) {
  return name ? String(name).trim().charAt(0) : '·'
}

/** 从枚举对象中取中文标签 */
export function labelOf(map, value, fallback = '—') {
  if (value === null || value === undefined || value === '') return fallback
  return map?.[value]?.label ?? String(value)
}

/** 从枚举对象中取视觉语义（tone） */
export function toneOf(map, value) {
  return map?.[value]?.tone ?? 'ink'
}

/** 枚举对象转下拉选项 */
export function toOptions(map) {
  return Object.entries(map).map(([value, item]) => ({ value: Number(value), label: item.label }))
}

/** 两个日期相差天数（用于借调到期提醒） */
export function daysUntil(value) {
  const date = toDate(value)
  if (!date) return null
  const diff = date.setHours(0, 0, 0, 0) - new Date().setHours(0, 0, 0, 0)
  return Math.round(diff / 86400000)
}

/** 生成日期范围内的连续月份（用于统计图 X 轴） */
export function recentMonths(count = 6) {
  const list = []
  const base = new Date()
  for (let i = count - 1; i >= 0; i -= 1) {
    const d = new Date(base.getFullYear(), base.getMonth() - i, 1)
    list.push(`${d.getFullYear()}-${pad(d.getMonth() + 1)}`)
  }
  return list
}

/** 简易防抖，用于检索输入 */
export function debounce(fn, wait = 300) {
  let timer = null
  return function debounced(...args) {
    clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), wait)
  }
}
