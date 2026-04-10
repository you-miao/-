import request from '@/utils/request'

export function getProductList(params) {
  return request.get('/product/list', { params })
}

export function getMyProducts(params) {
  return request.get('/product/my', { params })
}

export function getProductDetail(id) {
  return request.get(`/product/${id}`)
}

export function publishProduct(data) {
  return request.post('/product', data)
}

export function updateProduct(data) {
  return request.put('/product', data)
}

export function offShelfProduct(id) {
  return request.put(`/product/off/${id}`)
}

export function toggleFavorite(id) {
  return request.post(`/product/favorite/${id}`)
}

export function getMyFavorites(params) {
  return request.get('/product/favorites', { params })
}

export function getCategoryTree() {
  return request.get('/category/tree')
}

export function getAllTags() {
  return request.get('/category/tags')
}

export function getRecommendProducts(params) {
  return request.get('/product/recommend', { params })
}
