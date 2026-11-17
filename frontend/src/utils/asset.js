/**
 * 静态资源地址处理
 * ------------------------------------------------------------------
 * 后端把 uploads 目录映射为 /uploads/**，而 context-path 为 /api，
 * 因此数据库里存的相对路径 /uploads/relics/xx.jpg 需要补上 /api 前缀。
 * 开发期由 Vite 代理 /api，生产期由 Nginx 反代，两种模式共用同一份数据。
 */
const API_BASE = import.meta.env.VITE_API_BASE || '/api'

/**
 * 把后端返回的相对路径转成浏览器可直接访问的地址。
 * 已是 http(s) 绝对地址、data URI 或 blob 时原样返回。
 *
 * @param {string} path 相对路径，如 /uploads/relics/TC-0001.jpg
 * @returns {string} 可直接用于 img src 的地址
 */
export function assetUrl(path) {
  if (!path) return ''
  if (/^(https?:)?\/\//.test(path) || path.startsWith('data:') || path.startsWith('blob:')) {
    return path
  }
  const base = API_BASE.replace(/\/$/, '')
  return path.startsWith('/') ? `${base}${path}` : `${base}/${path}`
}

/**
 * 附件路径转可访问地址。
 *
 * <p>后端附件表里存的是相对 uploads 的路径（如 relics/TC-0001.jpg），
 * 而封面字段返回的是已带 /uploads 前缀的路径，两种形态这里统一处理。</p>
 *
 * @param {string} path 附件相对路径或 /uploads 开头的路径
 * @returns {string} 可直接用于 img src 的地址
 */
export function uploadUrl(path) {
  if (!path) return ''
  if (/^(https?:)?\/\//.test(path) || path.startsWith('data:') || path.startsWith('blob:')) {
    return path
  }
  return assetUrl(path.startsWith('/') ? path : `/uploads/${path}`)
}
