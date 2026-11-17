import request from './request'

/** 审核工作台（sys_audit_record） */
export const listPending = (params) => request.get('/audits/pending', { params })
export const getPendingCount = () => request.get('/audits/pending-count')
export const listAuditRecords = (params) => request.get('/audits', { params })
export const submitAudit = (data) => request.post('/audits', data)
export const batchAudit = (items, auditOpinion) => request.post('/audits/batch', { items, auditOpinion })
