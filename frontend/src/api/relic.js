import request from './request'

/** 藏品分类（relic_category） */
export const getCategories = () => request.get('/categories')

/** 后台：藏品档案分页查询 */
export const listRelics = (params) => request.get('/relics', { params })

/** 后台：藏品详情（含附件、二维码、溯源、征集来源） */
export const getRelic = (id) => request.get(`/relics/${id}`)

/** 前台：公开藏品详情（免登录，仅公开藏品可查） */
export const getPublicRelic = (id) => request.get(`/relics/public/${id}`)

/** 后台：编目建档 */
export const createRelic = (data) => request.post('/relics', data)

/** 后台：修改藏品档案 */
export const updateRelic = (id, data) => request.put(`/relics/${id}`, data)

/** 后台：逻辑删除 */
export const deleteRelic = (id) => request.delete(`/relics/${id}`)

/** 后台：批量逻辑删除 */
export const batchDeleteRelics = (ids) => request.post('/relics/batch-delete', { ids })

/** 溯源时间线 */
export const getRelicTrace = (id) => request.get(`/relics/${id}/trace`)

/** 扫码溯源：按藏品编号 / 二维码内容查询 */
export const getRelicByCode = (code) => request.get(`/relics/by-code/${encodeURIComponent(code)}`)

/** 后台：查询藏品溯源二维码（缺失时后端自动补建） */
export const getQrCode = (id) => request.get(`/relics/${id}/qrcode`)

/** 后台：重新生成某件藏品的溯源二维码 */
export const rebuildQrCode = (id) => request.post(`/relics/${id}/qrcode/rebuild`)

/** 后台：批量重建全部藏品二维码（存量补齐 / 更换扫码域名后刷新） */
export const rebuildAllQrCodes = () => request.post('/relics/qrcodes/rebuild')

/** 前台：精选藏品 */
export const getHighlights = () => request.get('/relics/public/highlights')

/** 前台：公开藏品检索 */
export const searchPublicRelics = (params) => request.get('/relics/public/search', { params })

/**
 * 后台：上传藏品附件（影像资料 / 修复照片 / 协议）
 * @param {string} id 藏品 id
 * @param {File} file 浏览器 File 对象
 * @param {number} fileType 附件类型，默认 1（藏品影像）
 */
export const uploadRelicAttachment = (id, file, fileType = 1) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('fileType', fileType)
  // 实例默认 Content-Type 为 application/json，此处显式置空，
  // 由浏览器自动生成 multipart 边界，否则 FormData 会被序列化成 JSON
  return request.post(`/relics/${id}/attachments`, formData, {
    headers: { 'Content-Type': undefined }
  })
}

/** 后台：删除藏品附件 */
export const deleteRelicAttachment = (attachmentId) => request.delete(`/relics/attachments/${attachmentId}`)
