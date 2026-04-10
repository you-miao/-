<template>
  <div class="dashboard">
    <div class="stat-cards">
      <div class="stat-card" v-for="item in statCards" :key="item.key">
        <div class="stat-icon" :style="{ background: item.bg }">
          <el-icon :size="28" color="#fff"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats[item.key] ?? '-' }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <div class="dashboard-row">
      <div class="page-card" style="flex:1">
        <h3 class="section-title">待处理事项</h3>
        <div class="pending-list">
          <div class="pending-item" @click="$router.push('/admin/products')">
            <el-icon color="#2b5ce6"><Goods /></el-icon>
            <span>待审核商品</span>
            <el-badge :value="stats.pendingAuditProducts || 0" type="danger" />
          </div>
          <div class="pending-item" @click="$router.push('/admin/comments')">
            <el-icon color="#34c759"><ChatDotRound /></el-icon>
            <span>待审核评论</span>
            <el-badge :value="stats.pendingAuditComments || 0" type="danger" />
          </div>
        </div>
      </div>
      <div class="page-card" style="flex:1">
        <h3 class="section-title">今日数据</h3>
        <div class="today-stats">
          <div class="today-item">
            <span class="tv">{{ stats.todayNewUsers ?? 0 }}</span>
            <span class="tl">新增用户</span>
          </div>
          <div class="today-item">
            <span class="tv">{{ stats.todayNewProducts ?? 0 }}</span>
            <span class="tl">新增商品</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboard } from '@/api/admin'

const stats = ref({})

const statCards = [
  { key: 'totalUsers', label: '总用户数', icon: 'User', bg: '#3b5998' },
  { key: 'totalProducts', label: '总商品数', icon: 'Goods', bg: '#4a6fa5' },
  { key: 'totalOrders', label: '交易订单', icon: 'ShoppingCart', bg: '#5a7fb5' },
  { key: 'totalExchanges', label: '交换完成', icon: 'Switch', bg: '#6889a5' }
]

onMounted(async () => {
  const res = await getDashboard()
  stats.value = res.data
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: $shadow-sm;
}
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-value { font-size: 28px; font-weight: 700; color: $text-primary; display: block; }
.stat-label { font-size: 13px; color: $text-secondary; }
.dashboard-row { display: flex; gap: 16px; }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.pending-list { display: flex; flex-direction: column; gap: 12px; }
.pending-item {
  display: flex; align-items: center; gap: 10px; padding: 12px; border-radius: $radius-md;
  cursor: pointer; transition: background 0.2s;
  &:hover { background: $bg-color; }
  span { flex: 1; font-size: 14px; }
}
.today-stats { display: flex; gap: 24px; }
.today-item { text-align: center; flex: 1; }
.tv { display: block; font-size: 32px; font-weight: 700; color: $primary-color; }
.tl { font-size: 13px; color: $text-secondary; }
</style>
