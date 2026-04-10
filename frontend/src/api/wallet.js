import request from '@/utils/request'

export function getWalletBalance() {
  return request.get('/wallet/balance')
}

export function rechargeWallet(amount) {
  return request.post('/wallet/recharge', { amount })
}

export function getRechargeRecords(params) {
  return request.get('/wallet/recharge-records', { params })
}

export function getWalletTransactions(params) {
  return request.get('/wallet/transactions', { params })
}
