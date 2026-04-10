import request from '@/utils/request'

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

export function updatePassword(data) {
  return request.put('/user/password', data)
}

export function updateAvatar(avatarUrl) {
  return request.put('/user/avatar', avatarUrl, { headers: { 'Content-Type': 'text/plain' } })
}

export function getAddressList() {
  return request.get('/user/address')
}

export function saveAddress(data) {
  return request.post('/user/address', data)
}

export function deleteAddress(id) {
  return request.delete(`/user/address/${id}`)
}
