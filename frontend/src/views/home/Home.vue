<template>
  <div class="home-page">
    <!-- 搜索区 -->
    <div class="search-section page-card">
      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索商品名称或描述..." size="large" clearable @keyup.enter="loadProducts" style="max-width:400px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="query.categoryId" placeholder="分类" clearable size="large" style="width:160px">
          <el-option-group v-for="cat in categories" :key="cat.id" :label="cat.name">
            <el-option :label="cat.name" :value="cat.id" />
            <el-option v-for="child in cat.children" :key="child.id" :label="child.name" :value="child.id" />
          </el-option-group>
        </el-select>
        <el-select v-model="query.productType" placeholder="类型" clearable size="large" style="width:120px">
          <el-option label="出售" :value="1" />
          <el-option label="交换" :value="2" />
        </el-select>
        <el-input v-model="query.campus" placeholder="校区" clearable size="large" style="width:140px" />
        <div class="price-range">
          <el-input-number v-model="query.minPrice" :min="0" :precision="0" :controls="false" placeholder="最低价" size="large" style="width:110px" />
          <span class="price-sep">-</span>
          <el-input-number v-model="query.maxPrice" :min="0" :precision="0" :controls="false" placeholder="最高价" size="large" style="width:110px" />
        </div>
        <el-button type="primary" size="large" @click="loadProducts">搜索</el-button>
        <el-button size="large" @click="resetQuery">重置</el-button>
      </div>
      <div class="tag-list">
        <span
          v-for="tag in tags"
          :key="tag.id"
          :class="['tag-chip', { active: query.tagId === tag.id }]"
          @click="handleTagClick(tag)"
        >{{ tag.name }}</span>
      </div>
    </div>

    <!-- 个性推荐 -->
    <div v-if="recommendProducts.length > 0 && !query.keyword && !query.tagId && !query.categoryId" class="recommend-section">
      <div class="section-header">
        <h3 class="section-title">猜你喜欢</h3>
        <span class="section-sub">根据你的浏览和收藏智能推荐</span>
      </div>
      <div class="product-grid recommend-grid">
        <ProductCard v-for="item in recommendProducts" :key="item.id" :item="item" />
      </div>
    </div>

    <!-- 全部商品 -->
    <div class="section-header" style="margin-top:20px">
      <h3 class="section-title">{{ query.tagId || query.keyword || query.categoryId ? '筛选结果' : '全部商品' }}</h3>
    </div>
    <div class="product-grid" v-loading="loading">
      <ProductCard v-for="item in products" :key="item.id" :item="item" />
    </div>
    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[12, 20, 40]"
        layout="total, sizes, prev, pager, next"
        @change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProductList, getCategoryTree, getAllTags, getRecommendProducts } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'

const loading = ref(false)
const products = ref([])
const total = ref(0)
const categories = ref([])
const tags = ref([])
const recommendProducts = ref([])

const query = reactive({
  pageNum: 1, pageSize: 12, keyword: '',
  categoryId: null, productType: null, campus: '', tagId: null,
  minPrice: undefined, maxPrice: undefined
})

async function loadProducts() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.tagId) delete params.tagId
    if (params.minPrice == null) delete params.minPrice
    if (params.maxPrice == null) delete params.maxPrice
    const res = await getProductList(params)
    products.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleTagClick(tag) {
  if (query.tagId === tag.id) {
    query.tagId = null
  } else {
    query.tagId = tag.id
  }
  query.pageNum = 1
  loadProducts()
}

function resetQuery() {
  Object.assign(query, { pageNum: 1, keyword: '', categoryId: null, productType: null, campus: '', tagId: null, minPrice: undefined, maxPrice: undefined })
  loadProducts()
}

async function loadRecommend() {
  try {
    const res = await getRecommendProducts({ pageNum: 1, pageSize: 8 })
    recommendProducts.value = res.data?.records || []
  } catch (e) {
    recommendProducts.value = []
  }
}

onMounted(async () => {
  loadProducts()
  loadRecommend()
  getCategoryTree().then(res => categories.value = res.data)
  getAllTags().then(res => tags.value = res.data)
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
.search-section { margin-bottom: 20px; }
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
.price-range {
  display: flex;
  align-items: center;
  gap: 4px;
  .price-sep { color: #999; font-size: 14px; }
}
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}
.tag-chip {
  display: inline-flex;
  align-items: center;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  background: #f0f2f5;
  color: $text-regular;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  user-select: none;
  letter-spacing: 0.5px;
  &:hover {
    background: #e8ecf1;
    color: $text-primary;
  }
  &.active {
    background: $primary-bg;
    color: $primary-color;
    border-color: $primary-color;
    font-weight: 500;
  }
}
.section-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 14px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: $text-primary;
  margin: 0;
}
.section-sub {
  font-size: 13px;
  color: $text-secondary;
}
.recommend-section {
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: $shadow-sm;
}
.recommend-grid {
  grid-template-columns: repeat(4, 1fr);
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 200px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
}
@media (max-width: 1024px) {
  .product-grid, .recommend-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .product-grid, .recommend-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
