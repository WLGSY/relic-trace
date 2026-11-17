import request from './request'

/** 系统管理：用户 / 字典 / 参数 / 日志 */
export const listUsers = (params) => request.get('/users', { params })
export const createUser = (data) => request.post('/users', data)
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/users/${id}`)
export const resetUserPassword = (id, password) => request.put(`/users/${id}/password`, { password })
export const toggleUserStatus = (id, isEnabled) => request.put(`/users/${id}/status`, { isEnabled })

export const listDicts = (params) => request.get('/dicts', { params })
export const createDict = (data) => request.post('/dicts', data)
export const updateDict = (id, data) => request.put(`/dicts/${id}`, data)
export const deleteDict = (id) => request.delete(`/dicts/${id}`)

export const listConfigs = (params) => request.get('/configs', { params })
export const updateConfig = (id, data) => request.put(`/configs/${id}`, data)

export const listOperateLogs = (params) => request.get('/logs', { params })
export const listLogModules = () => request.get('/logs/modules')
