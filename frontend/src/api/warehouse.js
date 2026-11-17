import request from './request'

/** 库房（relic_warehouse） */
export const listWarehouses = () => request.get('/warehouses')
export const createWarehouse = (data) => request.post('/warehouses', data)
export const updateWarehouse = (id, data) => request.put(`/warehouses/${id}`, data)
export const deleteWarehouse = (id) => request.delete(`/warehouses/${id}`)

/** 仓位（relic_storage_position） */
export const listPositions = (params) => request.get('/positions', { params })
export const createPosition = (data) => request.post('/positions', data)
export const updatePosition = (id, data) => request.put(`/positions/${id}`, data)
export const deletePosition = (id) => request.delete(`/positions/${id}`)

/** 库存盘点（relic_stock_check） */
export const listStockChecks = (params) => request.get('/stock-checks', { params })
export const createStockCheck = (data) => request.post('/stock-checks', data)
export const finishStockCheck = (id, data) => request.put(`/stock-checks/${id}`, data)
