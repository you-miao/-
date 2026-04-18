<template>
  <div class="page-card">
    <h2 class="page-title">我的订单</h2>
    <el-tabs v-model="role" @tab-change="loadData">
      <el-tab-pane label="我买到的" name="buyer" />
      <el-tab-pane label="我卖出的" name="seller" />
    </el-tabs>
    <div class="search-bar">
      <el-select v-model="query.status" placeholder="订单状态" clearable style="width:130px" @change="loadData">
        <el-option label="待付款" :value="0" />
        <el-option label="待收货" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已取消" :value="3" />
      </el-select>
    </div>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:10px">
            <el-image :src="imgUrl(row.productCoverImg)" style="width:50px;height:50px;border-radius:4px" fit="cover" />
            <span>{{ row.productTitle }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="orderStatusMap[row.status]?.type" size="small">{{ orderStatusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">查看详情</el-button>
          <el-button v-if="row.status === 0 && role === 'buyer'" link type="primary" @click="openPay(row.id)">付款</el-button>
          <el-button v-if="row.status === 0 && role === 'buyer'" link type="info" @click="handleCancel(row.id)">取消</el-button>
          <el-button v-if="row.status === 1 && role === 'buyer'" link type="success" @click="handleReceive(row.id)">确认收货</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="display:flex;justify-content:center;margin-top:16px">
      <el-pagination v-model:current-page="query.pageNum" :page-size="20" :total="total" layout="total, prev, pager, next" @change="loadData" />
    </div>

    <el-dialog v-model="payDialogVisible" title="选择支付方式" width="420px" @open="loadWalletBalance">
      <div class="pay-dialog-body">
        <p class="pay-balance">当前余额：<strong>¥{{ walletBalance }}</strong></p>
        <el-radio-group v-model="payMethod">
          <el-radio :label="1">线下支付（当面/协商付款，不从平台余额扣款）</el-radio>
          <el-radio :label="2">余额支付（从平台账户余额扣除订单金额）</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paySubmitting" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="订单详情" width="680px" destroy-on-close>
      <div v-loading="detailLoading">
        <el-descriptions v-if="detailOrder" :column="1" border>
          <el-descriptions-item label="订单号">{{ detailOrder.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ detailOrder.productTitle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ detailOrder.price ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="orderStatusMap[detailOrder.status]?.type" size="small">
              {{ orderStatusMap[detailOrder.status]?.label || '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="购买人">{{ detailOrder.buyerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="购买人ID">{{ detailOrder.buyerId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ detailOrder.contactInfo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="交付方式">{{ deliveryTypeText(detailOrder.deliveryType) }}</el-descriptions-item>
          <el-descriptions-item v-if="detailOrder.deliveryType === 2" label="收货人">{{ detailOrder.receiverName || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="detailOrder.deliveryType === 2" label="收货手机号">{{ detailOrder.receiverPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="detailOrder.deliveryType === 2" label="收货地址">{{ detailOrder.receiverAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item v-else label="取货地点">{{ detailOrder.pickupLocation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detailOrder.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detailOrder.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货时间">{{ detailOrder.receiveTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="取消原因">{{ detailOrder.cancelReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单备注">{{ detailOrder.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBuyOrders, getSellOrders, payOrder, confirmReceive, cancelOrder, getOrderDetail } from '@/api/order'
import { getWalletBalance } from '@/api/wallet'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const role = ref('buyer')

function imgUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}
const query = reactive({ pageNum: 1, status: null })

const orderStatusMap = {
  0: { label: '待付款', type: 'warning' },
  1: { label: '待收货', type: '' },
  2: { label: '已完成', type: 'success' },
  3: { label: '已取消', type: 'info' }
}

function deliveryTypeText(type) {
  if (type === 2) return '送货上门'
  return '线下自提'
}

async function loadData() {
  loading.value = true
  try {
    const fn = role.value === 'buyer' ? getBuyOrders : getSellOrders
    const res = await fn(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const payDialogVisible = ref(false)
const payOrderId = ref(null)
const payMethod = ref(1)
const walletBalance = ref('0.00')
const paySubmitting = ref(false)

async function loadWalletBalance() {
  try {
    const res = await getWalletBalance()
    walletBalance.value = Number(res.data ?? 0).toFixed(2)
  } catch {
    walletBalance.value = '0.00'
  }
}

function openPay(id) {
  payOrderId.value = id
  payMethod.value = 1
  payDialogVisible.value = true
}

async function confirmPay() {
  paySubmitting.value = true
  try {
    await payOrder(payOrderId.value, payMethod.value)
    ElMessage.success('付款成功')
    payDialogVisible.value = false
    loadData()
  } finally {
    paySubmitting.value = false
  }
}

async function handleReceive(id) {
  await ElMessageBox.confirm('确认收货？', '确认', { type: 'warning' })
  await confirmReceive(id)
  ElMessage.success('已确认收货')
  loadData()
}

async function handleCancel(id) {
  await ElMessageBox.confirm('确认取消订单？', '确认', { type: 'warning' })
  await cancelOrder(id, '用户主动取消')
  ElMessage.success('订单已取消')
  loadData()
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailOrder = ref(null)

async function openDetail(id) {
  detailVisible.value = true
  detailLoading.value = true
  detailOrder.value = null
  try {
    const res = await getOrderDetail(id)
    detailOrder.value = res.data
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.pay-dialog-body {
  line-height: 1.7;
}
.pay-balance {
  margin: 0 0 12px;
  font-size: 15px;
  color: #606266;
}
.pay-dialog-body :deep(.el-radio) {
  display: flex;
  align-items: flex-start;
  white-space: normal;
  height: auto;
  margin-bottom: 10px;
}
</style>
