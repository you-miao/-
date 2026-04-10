<template>
  <div class="page-card">
    <h2 class="page-title">我的收藏</h2>
    <div class="product-grid" v-loading="loading">
      <ProductCard v-for="item in list" :key="item.id" :item="item" />
    </div>
    <el-empty v-if="!loading && list.length === 0" description="暂无收藏" />
    <div style="display:flex;justify-content:center;margin-top:16px" v-if="total > 0">
      <el-pagination v-model:current-page="pageNum" :page-size="12" :total="total" layout="total, prev, pager, next" @change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyFavorites } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)

async function loadData() {
  loading.value = true
  try {
    const res = await getMyFavorites({ pageNum: pageNum.value, pageSize: 12 })
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 200px;
}
@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
