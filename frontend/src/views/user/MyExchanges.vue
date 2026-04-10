<template>
  <div class="page-card">
    <h2 class="page-title">我的交换</h2>
    <el-tabs v-model="role" @tab-change="loadData">
      <el-tab-pane label="我发起的" name="applicant" />
      <el-tab-pane label="我收到的" name="owner" />
    </el-tabs>
    <div class="search-bar">
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width:130px" @change="loadData">
        <el-option label="待处理" :value="0" />
        <el-option label="已同意" :value="1" />
        <el-option label="已拒绝" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
    </div>

    <div class="exchange-list" v-loading="loading">
      <div v-for="row in list" :key="row.id" class="exchange-card">
        <div class="exchange-card-header">
          <el-tag :type="exStatusMap[row.status]?.type" size="small">{{ exStatusMap[row.status]?.label }}</el-tag>
          <span class="exchange-time">{{ row.createTime }}</span>
          <span class="exchange-user">{{ role === 'applicant' ? '物主：' + row.ownerName : '申请人：' + row.applicantName }}</span>
        </div>
        <div class="exchange-card-body">
          <!-- 目标商品 -->
          <div class="exchange-product-card" @click="$router.push(`/product/${row.productId}`)">
            <el-image :src="coverUrl(row.productCoverImg)" style="width:70px;height:70px;border-radius:6px;flex-shrink:0" fit="cover" />
            <div class="exchange-product-info">
              <div class="product-label">目标商品</div>
              <div class="product-name">{{ row.productTitle }}</div>
            </div>
          </div>

          <div class="exchange-arrow">
            <el-icon :size="22"><Right /></el-icon>
          </div>

          <!-- 交换物商品 -->
          <div class="exchange-product-card offer" v-if="row.offerProductId" @click="$router.push(`/product/${row.offerProductId}`)">
            <el-image :src="coverUrl(row.offerProductCoverImg)" style="width:70px;height:70px;border-radius:6px;flex-shrink:0" fit="cover" />
            <div class="exchange-product-info">
              <div class="product-label">交换物品</div>
              <div class="product-name">{{ row.offerProductTitle }}</div>
              <div class="product-meta" v-if="row.offerProductCategoryName">分类：{{ row.offerProductCategoryName }}</div>
              <div class="product-meta" v-if="row.offerProductPrice">估价：<span class="price-text">¥{{ row.offerProductPrice }}</span></div>
            </div>
          </div>
          <div class="exchange-product-card offer no-product" v-else>
            <div class="exchange-product-info">
              <div class="product-label">交换物品</div>
              <div class="product-name">{{ row.offerDesc || '未指定交换物' }}</div>
            </div>
          </div>
        </div>

        <div class="exchange-card-desc" v-if="row.offerDesc && row.offerProductId">
          <span class="desc-label">补充说明：</span>{{ row.offerDesc }}
        </div>
        <div class="exchange-card-desc" v-if="row.message">
          <span class="desc-label">留言：</span>{{ row.message }}
        </div>
        <div class="exchange-card-desc" v-if="row.status === 2 && row.rejectReason">
          <span class="desc-label" style="color:#f56c6c">拒绝理由：</span>{{ row.rejectReason }}
        </div>

        <div class="exchange-card-footer">
          <template v-if="role === 'owner' && row.status === 0">
            <el-button type="success" size="small" @click="handleAccept(row.id)">同意</el-button>
            <el-button type="danger" size="small" plain @click="handleReject(row.id)">拒绝</el-button>
          </template>
          <el-button v-if="row.status === 1" type="primary" size="small" @click="handleConfirm(row.id)">
            {{ getConfirmLabel(row) }}
          </el-button>
          <el-button v-if="role === 'applicant' && row.status <= 1" size="small" plain @click="handleCancelEx(row.id)">取消</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无交换记录" />
    </div>

    <div style="display:flex;justify-content:center;margin-top:16px">
      <el-pagination v-model:current-page="query.pageNum" :page-size="20" :total="total" layout="total, prev, pager, next" @change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyApply, getMyReceive, acceptExchange, rejectExchange, confirmExchange, cancelExchange } from '@/api/exchange'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Right } from '@element-plus/icons-vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const role = ref('applicant')
const query = reactive({ pageNum: 1, status: null })

const exStatusMap = {
  0: { label: '待处理', type: 'warning' },
  1: { label: '已同意', type: '' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'info' }
}

function coverUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}

function getConfirmLabel(row) {
  const isApplicant = role.value === 'applicant'
  const myConfirmed = isApplicant ? row.applicantConfirm === 1 : row.ownerConfirm === 1
  const otherConfirmed = isApplicant ? row.ownerConfirm === 1 : row.applicantConfirm === 1
  if (myConfirmed) return '等待对方确认'
  if (otherConfirmed) return '对方已确认，点击完成'
  return '确认完成'
}

async function loadData() {
  loading.value = true
  try {
    const fn = role.value === 'applicant' ? getMyApply : getMyReceive
    const res = await fn(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleAccept(id) {
  await ElMessageBox.confirm('同意该交换申请？同意后将自动拒绝该商品的其他申请。', '确认')
  await acceptExchange(id)
  ElMessage.success('已同意')
  loadData()
}

async function handleReject(id) {
  const { value } = await ElMessageBox.prompt('请输入拒绝理由', '拒绝', { inputPlaceholder: '选填' })
  await rejectExchange(id, value)
  ElMessage.success('已拒绝')
  loadData()
}

async function handleConfirm(id) {
  await ElMessageBox.confirm('确认交换已完成？双方都确认后交换将完成。', '确认')
  await confirmExchange(id)
  ElMessage.success('已确认')
  loadData()
}

async function handleCancelEx(id) {
  await ElMessageBox.confirm('确认取消交换？', '确认', { type: 'warning' })
  await cancelExchange(id)
  ElMessage.success('已取消')
  loadData()
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
.search-bar { margin-bottom: 16px; }
.exchange-list { display: flex; flex-direction: column; gap: 14px; min-height: 200px; }
.exchange-card {
  background: $bg-white;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px 20px;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
}
.exchange-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  .exchange-time { font-size: 13px; color: $text-secondary; margin-left: auto; }
  .exchange-user { font-size: 13px; color: $text-regular; }
}
.exchange-card-body {
  display: flex;
  align-items: center;
  gap: 12px;
}
.exchange-product-card {
  flex: 1;
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f8f9fb;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: #f0f2f5; }
  &.offer { background: #f0f9eb; border-color: #e1f3d8; }
  &.offer:hover { background: #e8f5e0; }
  &.no-product { cursor: default; background: #fafafa; border-style: dashed; &:hover { background: #fafafa; } }
}
.exchange-product-info {
  flex: 1;
  overflow: hidden;
  .product-label { font-size: 12px; color: $text-secondary; margin-bottom: 4px; }
  .product-name { font-size: 14px; font-weight: 600; color: $text-primary; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px; }
  .product-meta { font-size: 12px; color: $text-secondary; margin-bottom: 2px; }
  .price-text { color: #e74c3c; font-weight: 600; }
}
.exchange-arrow {
  flex-shrink: 0;
  color: $text-secondary;
  display: flex;
  align-items: center;
}
.exchange-card-desc {
  margin-top: 10px;
  font-size: 13px;
  color: $text-regular;
  .desc-label { color: $text-secondary; font-weight: 500; }
}
.exchange-card-footer {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
