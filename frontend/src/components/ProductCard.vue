<template>
  <div class="product-card" @click="$router.push(`/product/${item.id}`)">
    <div class="card-img">
      <img :src="imgUrl(item.coverImg) || '/placeholder.png'" :alt="item.title" />
      <span v-if="item.productType === 2" class="type-badge exchange">交换</span>
      <span v-else class="type-badge sale">出售</span>
    </div>
    <div class="card-body">
      <h3 class="card-title">{{ item.title }}</h3>
      <div class="card-meta">
        <span v-if="item.productType === 1" class="price">¥{{ item.price }}</span>
        <span v-else class="price exchange-text">以物换物</span>
        <span class="campus">{{ item.campus }}</span>
      </div>
      <div class="card-footer">
        <span class="publisher">{{ item.publisherName || '匿名' }}</span>
        <span class="views"><el-icon><View /></el-icon> {{ item.viewCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ item: { type: Object, required: true } })

function imgUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
.product-card {
  background: $bg-white;
  border-radius: $radius-lg;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: $shadow-sm;
  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;
  }
}
.card-img {
  position: relative;
  width: 100%;
  padding-top: 75%;
  overflow: hidden;
  background: $bg-color;
  img {
    position: absolute;
    top: 0; left: 0;
    width: 100%; height: 100%;
    object-fit: cover;
  }
  .type-badge {
    position: absolute;
    top: 8px; left: 8px;
    padding: 2px 10px;
    border-radius: 4px;
    font-size: 12px;
    color: #fff;
    font-weight: 500;
    &.sale { background: $primary-color; }
    &.exchange { background: $success-color; }
  }
}
.card-body { padding: 12px; }
.card-title {
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}
.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  .price {
    font-size: 18px;
    font-weight: 700;
    color: #e74c3c;
  }
  .exchange-text { font-size: 14px; color: $success-color; }
  .campus {
    font-size: 12px;
    color: $text-secondary;
    background: $bg-color;
    padding: 2px 6px;
    border-radius: 3px;
  }
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: $text-secondary;
  .views {
    display: flex;
    align-items: center;
    gap: 2px;
  }
}
</style>
