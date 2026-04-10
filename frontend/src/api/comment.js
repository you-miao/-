import request from '@/utils/request'

export function addComment(data) {
  return request.post('/comment', data)
}

export function getComments(productId, params) {
  return request.get(`/comment/list/${productId}`, { params })
}

export function toggleLike(id) {
  return request.post(`/comment/like/${id}`)
}

export function deleteComment(id) {
  return request.delete(`/comment/${id}`)
}

export function reportComment(id, reason) {
  return request.post(`/comment/report/${id}`, null, { params: { reason } })
}
