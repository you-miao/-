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
