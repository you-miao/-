<template>
  <div class="page-card">
    <div class="search-bar">
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width:140px">
        <el-option label="待审核" :value="0" />
        <el-option label="已上架" :value="1" />
        <el-option label="已下架" :value="2" />
        <el-option label="已驳回" :value="5" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索商品标题" clearable style="width:200px" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <el-image :src="imgUrl(row.coverImg)" style="width:50px;height:50px;border-radius:4px" fit="cover">
            <template #error><div style="width:50px;height:50px;background:#f5f7fa;display:flex;align-items:center;justify-content:center;font-size:12px;color:#c0c4cc;border-radius:4px">无图</div></template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column label="发布者" width="100">
        <template #default="{ row }">{{ row.publisherName }}</template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.productType === 1 ? '' : 'success'" size="small">{{ row.productType === 1 ? '出售' : '交换' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="90">
        <template #default="{ row }">{{ row.productType === 1 ? '¥' + row.price : '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <button v-if="row.status === 0" class="act-btn act-approve" @click="handleAudit(row.id, 1)">通过</button>
            <button v-if="row.status === 0" class="act-btn act-reject" @click="handleReject(row.id)">驳回</button>
            <button v-if="row.status === 1" class="act-btn act-off" @click="handleAudit(row.id, 3)">下架</button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div style="display:flex;justify-content:center;margin-top:16px">
      <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" layout="total, prev, pager, next" @change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminProducts, audit } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: 0, keyword: '' })

function imgUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}

const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已上架', type: 'success' },
  2: { label: '已下架', type: 'info' },
  3: { label: '已交换', type: '' },
  4: { label: '已售出', type: '' },
  5: { label: '已驳回', type: 'danger' }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminProducts(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleAudit(id, action) {
  const labels = { 1: '通过', 3: '下架' }
  await ElMessageBox.confirm(`确认${labels[action]}该商品？`, '确认')
  await audit({ targetId: id, targetType: 1, auditAction: action })
  ElMessage.success('操作成功')
  loadData()
}

async function handleReject(id) {
  const { value } = await ElMessageBox.prompt('请输入驳回理由', '驳回', { inputPlaceholder: '理由' })
  await audit({ targetId: id, targetType: 1, auditAction: 2, reason: value })
  ElMessage.success('已驳回')
  loadData()
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;
}
.action-btns {
  display: flex;
  gap: 8px;
}
.act-btn {
  padding: 4px 14px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid;
  transition: all 0.2s;
  background: #fff;
  line-height: 1.6;
}
.act-approve {
  color: #1d6e3f;
  border-color: #c6e6d5;
  background: #f0f9f4;
  &:hover { background: #e0f2e9; border-color: #8dcfab; }
}
.act-reject {
  color: #9b1c1c;
  border-color: #f0c6c6;
  background: #fdf2f2;
  &:hover { background: #fce4e4; border-color: #e8a0a0; }
}
.act-off {
  color: #6b5900;
  border-color: #e8deb5;
  background: #fdfaf0;
  &:hover { background: #faf4de; border-color: #d4c67e; }
}
</style>
