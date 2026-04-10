<template>
  <div class="page-card wallet-page">
    <h2 class="page-title">我的钱包</h2>

    <el-card class="balance-card" shadow="never">
      <div class="balance-row">
        <div>
          <div class="balance-label">账户余额（元）</div>
          <div class="balance-value">¥{{ balanceDisplay }}</div>
        </div>
        <div class="recharge-box">
          <el-input-number v-model="rechargeAmount" :min="0.01" :max="50000" :precision="2" :step="10" />
          <el-button type="primary" :loading="recharging" @click="doRecharge">模拟充值</el-button>
        </div>
      </div>
      <p class="hint">演示环境为模拟充值，金额将立即到账，可用于「余额支付」购买商品。</p>
    </el-card>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="资金流水" name="tx">
        <el-table :data="txList" v-loading="txLoading" stripe>
          <el-table-column label="时间" width="180">
            <template #default="{ row }">{{ formatTime(row.bizTime) }}</template>
          </el-table-column>
          <el-table-column label="类型" width="110">
            <template #default="{ row }">
              <el-tag v-if="row.type === 'recharge'" type="success" size="small">充值</el-tag>
              <el-tag v-else-if="row.type === 'pay'" type="warning" size="small">支出</el-tag>
              <el-tag v-else-if="row.type === 'income'" type="primary" size="small">售出</el-tag>
              <span v-else>{{ row.type }}</span>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="{ row }">
              <span :class="amountClass(row.amount)">{{ formatMoney(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="220" />
        </el-table>
        <div class="pager">
          <el-pagination
            v-model:current-page="txQuery.pageNum"
            :page-size="10"
            :total="txTotal"
            layout="total, prev, pager, next"
            @change="loadTransactions"
          />
        </div>
      </el-tab-pane>
      <el-tab-pane label="充值记录" name="rc">
        <el-table :data="rcList" v-loading="rcLoading" stripe>
          <el-table-column prop="rechargeNo" label="流水号" width="200" />
          <el-table-column label="金额" width="120">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.status === 1" type="success" size="small">成功</el-tag>
              <el-tag v-else type="info" size="small">其他</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rechargeTime" label="到账时间" width="180" />
        </el-table>
        <div class="pager">
          <el-pagination
            v-model:current-page="rcQuery.pageNum"
            :page-size="10"
            :total="rcTotal"
            layout="total, prev, pager, next"
            @change="loadRechargeRecords"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getWalletBalance, rechargeWallet, getRechargeRecords, getWalletTransactions } from '@/api/wallet'
import { ElMessage } from 'element-plus'

const balance = ref(0)
const balanceDisplay = computed(() => Number(balance.value || 0).toFixed(2))
const rechargeAmount = ref(10)
const recharging = ref(false)
const activeTab = ref('tx')

const txList = ref([])
const txTotal = ref(0)
const txLoading = ref(false)
const txQuery = reactive({ pageNum: 1 })

const rcList = ref([])
const rcTotal = ref(0)
const rcLoading = ref(false)
const rcQuery = reactive({ pageNum: 1 })

function amountClass(amt) {
  const n = Number(amt)
  if (n > 0) return 'amt-plus'
  if (n < 0) return 'amt-minus'
  return ''
}

function formatMoney(amt) {
  const n = Number(amt)
  if (n > 0) return '+' + n.toFixed(2)
  return n.toFixed(2)
}

function formatTime(t) {
  if (!t) return ''
  if (typeof t === 'string') return t.replace('T', ' ').slice(0, 19)
  return String(t)
}

async function refreshBalance() {
  const res = await getWalletBalance()
  balance.value = res.data
}

async function doRecharge() {
  recharging.value = true
  try {
    await rechargeWallet(rechargeAmount.value)
    ElMessage.success('充值成功')
    await refreshBalance()
    loadTransactions()
    loadRechargeRecords()
  } finally {
    recharging.value = false
  }
}

async function loadTransactions() {
  txLoading.value = true
  try {
    const res = await getWalletTransactions(txQuery)
    txList.value = res.data.records || []
    txTotal.value = res.data.total || 0
  } finally {
    txLoading.value = false
  }
}

async function loadRechargeRecords() {
  rcLoading.value = true
  try {
    const res = await getRechargeRecords(rcQuery)
    rcList.value = res.data.records || []
    rcTotal.value = res.data.total || 0
  } finally {
    rcLoading.value = false
  }
}

function onTabChange(name) {
  if (name === 'tx') loadTransactions()
  else loadRechargeRecords()
}

onMounted(async () => {
  await refreshBalance()
  loadTransactions()
})
</script>

<style lang="scss" scoped>
.wallet-page {
  max-width: 960px;
}
.balance-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
  border: 1px solid #e4eaf2;
}
.balance-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}
.balance-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}
.balance-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
.recharge-box {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hint {
  margin: 12px 0 0;
  font-size: 12px;
  color: #909399;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
.amt-plus {
  color: #67c23a;
  font-weight: 500;
}
.amt-minus {
  color: #e6a23c;
  font-weight: 500;
}
</style>
