import request from '@/utils/request'

export function createOrder(data) {
  return request.post('/order', data)
}

export function payOrder(id, payMethod = 1) {
  return request.put(`/order/pay/${id}`, { payMethod })
}

export function confirmReceive(id) {
  return request.put(`/order/receive/${id}`)
}

export function cancelOrder(id, reason) {
  return request.put(`/order/cancel/${id}`, null, { params: { reason } })
}

export function getBuyOrders(params) {
  return request.get('/order/buy', { params })
}

export function getSellOrders(params) {
  return request.get('/order/sell', { params })
}

export function getOrderDetail(id) {
  return request.get(`/order/${id}`)
}
