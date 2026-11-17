import request from './request'

/** 藏品征集申请（relic_collection_apply） */
export const listCollections = (params) => request.get('/collection-applies', { params })
export const getCollection = (id) => request.get(`/collection-applies/${id}`)
export const createCollection = (data) => request.post('/collection-applies', data)
export const updateCollection = (id, data) => request.put(`/collection-applies/${id}`, data)
export const submitCollection = (id) => request.post(`/collection-applies/${id}/submit`)
export const deleteCollection = (id) => request.delete(`/collection-applies/${id}`)
export const batchDeleteCollections = (ids) => request.post('/collection-applies/batch-delete', { ids })
