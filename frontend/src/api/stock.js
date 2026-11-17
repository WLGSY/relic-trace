import request from './request'

/** 出入库记录（relic_stock_record） */
export const listStockRecords = (params) => request.get('/stock-records', { params })
export const createStockRecord = (data) => request.post('/stock-records', data)
export const updateStockRecord = (id, data) => request.put(`/stock-records/${id}`, data)
export const deleteStockRecord = (id) => request.delete(`/stock-records/${id}`)
