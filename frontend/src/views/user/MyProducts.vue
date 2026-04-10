<template>
  <div class="page-card">
    <h2 class="page-title">我的发布</h2>
    <div class="search-bar">
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width:140px">
        <el-option label="待审核" :value="0" />
        <el-option label="已上架" :value="1" />
        <el-option label="已下架" :value="2" />
        <el-option label="已交换" :value="3" />
        <el-option label="已售出" :value="4" />
        <el-option label="已驳回" :value="5" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" @click="$router.push('/publish')" style="margin-left:auto">发布新商品</el-button>
    </div>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <el-image :src="imgUrl(row.coverImg)" style="width:50px;height:50px;border-radius:4px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.productType === 1 ? '' : 'success'" size="small">{{ row.productType === 1 ? '出售' : '交换' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="100">
        <template #default="{ row }">{{ row.productType === 1 ? '¥' + row.price : '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="70" />
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" link type="warning" @click="handleOff(row.id)">下架</el-button>
          <el-button link type="primary" @click="$router.push(`/product/${row.id}`)">查看</el-button>
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
import { getMyProducts, offShelfProduct } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: null })

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
    const res = await getMyProducts(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleOff(id) {
  await ElMessageBox.confirm('确定下架该商品？', '提示', { type: 'warning' })
  await offShelfProduct(id)
  ElMessage.success('已下架')
  loadData()
}

onMounted(() => loadData())
</script>
