import request from './request'

/** 修复申请（relic_repair_apply） */
export const listRepairs = (params) => request.get('/repairs', { params })
export const createRepair = (data) => request.post('/repairs', data)
export const processRepair = (id, data) => request.post(`/repairs/${id}/process`, data)
export const deleteRepair = (id) => request.delete(`/repairs/${id}`)
