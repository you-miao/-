import request from '@/utils/request'

export function getDashboard() {
  return request.get('/admin/dashboard')
}

export function audit(data) {
  return request.post('/admin/audit', data)
}

export function getAdminProducts(params) {
  return request.get('/admin/products', { params })
}

export function getAdminUsers(params) {
  return request.get('/admin/users', { params })
}

export function updateUserStatus(userId, status) {
  return request.put(`/admin/user/status/${userId}`, null, { params: { status } })
}

export function getAdminComments(params) {
  return request.get('/admin/comments', { params })
}
// ====== 捐赠活动审核 API ======

// 获取捐赠活动列表 (管理员后台)
export function getAdminDonationList(params) {
  return request({
    url: '/admin/donation/list',
    method: 'get',
    params
  })
}

// 提交审核结果 (重点：参数格式已完全对齐后端的 AuditDTO)
export function auditDonation(data) {
  return request({
    url: '/admin/donation/audit',
    method: 'post',
    data
  })
}
