import request from './request'

/** 展览借调（relic_exhibition_loan） */
export const listLoans = (params) => request.get('/loans', { params })
export const createLoan = (data) => request.post('/loans', data)
export const handoverLoan = (id) => request.post(`/loans/${id}/handover`)
export const returnLoan = (id, data) => request.post(`/loans/${id}/return`, data)
