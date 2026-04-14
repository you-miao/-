<template>
  <div class="donation-container">
    <div class="page-header">
      <h2 class="title">💚 校园爱心募捐大厅</h2>
      <p class="subtitle">让闲置的物品，成为他人眼里的光</p>
    </div>

    <el-row :gutter="20" v-if="campaigns.length > 0">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in campaigns" :key="item.id">
        <el-card shadow="hover" class="campaign-card">
          <img :src="formatImageUrl(item.coverImg) || defaultImg" class="cover-image" alt="公益封面" />
          <div class="card-content">
            <h3 class="item-title" :title="item.title">{{ item.title }}</h3>
            <p class="item-desc" :title="item.description">{{ item.description }}</p>
            
            <div class="progress-info">
              <span>已筹: <strong style="color: #67C23A;">{{ item.currentCount || 0 }}</strong> 件</span>
              <span>目标: {{ item.targetCount === 0 || !item.targetCount ? '不限量' : item.targetCount + '件' }}</span>
            </div>
            
            <el-progress 
              v-if="item.targetCount > 0"
              :percentage="calculatePercentage(item.currentCount, item.targetCount)" 
              :status="item.currentCount >= item.targetCount ? 'success' : ''"
              :stroke-width="8"
              style="margin-bottom: 15px;"
            />
            
            <el-button type="success" plain class="action-btn" @click="handleDonateClick(item)">
              我要捐赠
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty 
      v-else 
      description="当前暂无进行中的募捐活动，去后台发布一个试试吧！" 
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const campaigns = ref([])
// 默认封面图
const defaultImg = 'https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80'

// 💡 新增：解决图片裂开的终极拼接函数
const formatImageUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http')) return url; // 兼容默认的网图
  
  // 核心修复：同样加上 /api 路径前缀
  return 'http://localhost:8088/api' + (url.startsWith('/') ? '' : '/') + url;
}

const calculatePercentage = (current, target) => {
  if (!current) return 0
  if (!target) return 100
  const percent = Math.floor((current / target) * 100)
  return percent > 100 ? 100 : percent
}

const fetchCampaigns = async () => {
  try {
    const res = await request.get('/donation/public/campaigns')
    if (res.code === 200) {
      campaigns.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('无法连接后端接口，请检查 Spring Boot 是否启动')
  }
}

const handleDonateClick = (campaign) => {
  router.push({ 
    path: '/donation/submit', 
    query: { 
      campaignId: campaign.id,
      title: campaign.title 
    } 
  })
}

onMounted(() => {
  fetchCampaigns()
})
</script>

<style scoped>
.donation-container { max-width: 1200px; margin: 0 auto; padding: 20px 20px 60px; }
.page-header { text-align: center; margin-bottom: 40px; padding: 30px 0; background: linear-gradient(120deg, #f0f9eb 0%, #ffffff 100%); border-radius: 12px; }
.page-header .title { font-size: 32px; color: #67c23a; margin-bottom: 12px; letter-spacing: 2px; font-weight: 600; }
.page-header .subtitle { color: #909399; font-size: 16px; letter-spacing: 1px; }
.campaign-card { margin-bottom: 24px; border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); transition: all 0.3s ease; }
.campaign-card:hover { transform: translateY(-8px); box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1); }
.cover-image { width: 100%; height: 180px; object-fit: cover; display: block; border-bottom: 1px solid #f0f2f5; }
.card-content { padding: 16px; }
.item-title { margin: 0 0 8px 0; font-size: 18px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: bold; }
.item-desc { color: #606266; font-size: 14px; line-height: 1.6; height: 44px; overflow: hidden; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.progress-info { display: flex; justify-content: space-between; font-size: 13px; color: #909399; margin-bottom: 8px; padding-top: 10px; }
.action-btn { width: 100%; border-radius: 8px; font-weight: bold; }
</style>