// 文件路径：frontend/src/api/donation.js
import request from '@/utils/request'

// 1. 获取公开的募捐活动列表
export const getCampaigns = () => {
  return request.get('/donation/public/campaigns')
}

// 2. 获取公开的捐赠公示列表
export const getAnnouncements = () => {
  return request.get('/donation/public/announcements')
}

// 3. 用户提交捐赠订单
export const submitDonation = (data) => {
  return request.post('/donation/user/submit', data)
}

// 4. 社团确认接收物资
export const receiveDonation = (orderId) => {
  return request.post(`/donation/charity/receive/${orderId}`)
}