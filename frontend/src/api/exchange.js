import request from '@/utils/request'

export function applyExchange(data) {
  return request.post('/exchange', data)
}

export function acceptExchange(id) {
  return request.put(`/exchange/accept/${id}`)
}

export function rejectExchange(id, reason) {
  return request.put(`/exchange/reject/${id}`, null, { params: { reason } })
}

export function confirmExchange(id) {
  return request.put(`/exchange/confirm/${id}`)
}

export function cancelExchange(id) {
  return request.put(`/exchange/cancel/${id}`)
}

export function getMyApply(params) {
  return request.get('/exchange/my/apply', { params })
}

export function getMyReceive(params) {
  return request.get('/exchange/my/receive', { params })
}

export function getExchangeDetail(id) {
  return request.get(`/exchange/${id}`)
}

export function sendMessage(exchangeId, content, msgType) {
  return request.post(`/exchange/message/${exchangeId}`, null, { params: { content, msgType } })
}

export function getMessages(exchangeId) {
  return request.get(`/exchange/message/${exchangeId}`)
}

export function getMyExchangeProducts() {
  return request.get('/exchange/my/products')
}
