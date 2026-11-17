import request from './request'

/** 注销申请（relic_cancel_apply） */
export const listCancels = (params) => request.get('/cancels', { params })
export const createCancel = (data) => request.post('/cancels', data)
export const archiveCancel = (id) => request.post(`/cancels/${id}/archive`)
