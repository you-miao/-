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

// 3.1 用户获取自己的捐赠记录
export const getMyDonations = () => {
  return request.get('/donation/user/my-list')
}

// 4. 社团确认接收物资
export const receiveDonation = (orderId) => {
  return request.post(`/donation/charity/receive/${orderId}`)
}

// 5. 社团获取自己发布的募捐活动（含审核状态）
export const getCharityCampaigns = () => {
  return request.get('/donation/charity/campaigns')
}

// 6. 按活动获取订单
export const getCharityOrders = (params) => {
  return request.get('/donation/charity/orders', { params })
}

// 7. 社团驳回订单
export const rejectDonation = (orderId, reason) => {
  return request.post(`/donation/charity/reject/${orderId}`, null, {
    params: { reason }
  })
}

// 8. 下架活动
export const offlineCampaign = (campaignId) => {
  return request.put(`/donation/charity/campaign/offline/${campaignId}`)
}

// 9. 调整目标数量（delta 可正可负）
export const adjustCampaignTarget = (campaignId, delta) => {
  return request.put(`/donation/charity/campaign/target/${campaignId}`, null, {
    params: { delta }
  })
}

// 10. 发布爱心公示公告（图文）
export const publishAnnouncement = (data) => {
  return request.post('/donation/charity/announcement/publish', data)
}
