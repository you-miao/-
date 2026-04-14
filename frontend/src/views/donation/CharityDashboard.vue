<template>
  <div class="dashboard-container">
    <div class="header">
      <h2><el-icon><Monitor /></el-icon> 社团公益工作台</h2>
      <p>在这里管理全校的爱心物资与募捐活动</p>
    </div>

    <el-tabs v-model="activeTab" class="custom-tabs" type="border-card">
      
      <el-tab-pane name="orders">
        <template #label>
          <span class="tab-label"><el-icon><Box /></el-icon> 物资接收管理</span>
        </template>
        
        <el-table :data="orders" v-loading="loadingOrders" stripe style="width: 100%">
          <el-table-column prop="orderNo" label="捐赠单号" width="180" />
          <el-table-column prop="itemName" label="物品名称" min-width="150" />
          
          <el-table-column label="物品照片" width="100" align="center">
            <template #default="scope">
              <el-image 
                v-if="scope.row.itemImages"
                style="width: 50px; height: 50px; border-radius: 4px; border: 1px solid #ebeef5;"
                :src="formatImageUrl(scope.row.itemImages.split(',')[0])" 
                :preview-src-list="scope.row.itemImages.split(',').map(url => formatImageUrl(url))"
                preview-teleported
                fit="cover"
              />
              <span v-else style="color: #909399; font-size: 12px;">无图</span>
            </template>
          </el-table-column>

          <el-table-column prop="donorName" label="联系人" width="120" />
          <el-table-column prop="donorPhone" label="联系电话" width="150" />
          <el-table-column prop="donorAddress" label="取件地址" min-width="180" show-overflow-tooltip />
          
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'">
                {{ scope.row.status === 0 ? '待取件' : '已接收' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="scope">
              <el-button 
                v-if="scope.row.status === 0" 
                type="success" 
                size="small" 
                @click="handleReceive(scope.row)"
              >
                确认接收
              </el-button>
              <span v-else style="color: #909399; font-size: 13px;">处理完毕</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane name="publish">
        <template #label>
          <span class="tab-label"><el-icon><Promotion /></el-icon> 发布募捐活动</span>
        </template>
        
        <div class="form-wrapper">
          <el-form 
            ref="formRef" 
            :model="campaignForm" 
            :rules="rules" 
            label-width="120px"
            size="large"
          >
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="campaignForm.title" placeholder="例如：2026届计算机学院毕业季旧书漂流" />
            </el-form-item>

            <el-form-item label="目标筹集数量" prop="targetCount">
              <el-input-number v-model="campaignForm.targetCount" :min="0" :step="10" />
              <span style="margin-left: 15px; color: #909399; font-size: 13px;">
                (填 0 表示不限制数量)
              </span>
            </el-form-item>

            <el-form-item label="活动封面" prop="coverImg">
              <el-upload
                class="avatar-uploader"
                action="#"
                :http-request="customUpload"
                :show-file-list="false"
                accept="image/*"
              >
                <img v-if="campaignForm.coverImg" :src="formatImageUrl(campaignForm.coverImg)" class="preview-img" />
                <el-icon v-else class="uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>

            <el-form-item label="活动详情描述" prop="description">
              <el-input 
                v-model="campaignForm.description" 
                type="textarea" 
                :rows="5" 
                placeholder="请详细描述本次募捐的目的、用途和急需的物品类型..." 
              />
            </el-form-item>

            <el-form-item>
              <el-button type="success" @click="submitCampaign" :loading="publishing" style="width: 150px;">
                🚀 立即发布活动
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Monitor, Box, Promotion, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 💡 解决图片裂开的拼接函数
const formatImageUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  return 'http://localhost:8088' + (url.startsWith('/') ? '' : '/') + url;
}

const activeTab = ref('orders')

// ================= 物资接收管理逻辑 =================
const orders = ref([])
const loadingOrders = ref(false)

const fetchOrders = async () => {
  loadingOrders.value = true
  try {
    const res = await request.get('/donation/charity/orders')
    if (res.code === 200) {
      orders.value = res.data || []
    } else {
      ElMessage.warning(res.message || '获取订单失败')
    }
  } catch (error) {
    ElMessage.error('网络异常，无法获取订单列表')
  } finally {
    loadingOrders.value = false
  }
}

const handleReceive = (row) => {
  ElMessageBox.confirm(
    `确认已经收到 [${row.donorName}] 同学的 [${row.itemName}] 了吗？确认后状态将不可撤销。`,
    '接收确认',
    { confirmButtonText: '确认接收', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      const res = await request.post(`/donation/charity/receive/${row.id}`)
      if (res.code === 200) {
        ElMessage.success('操作成功！')
        fetchOrders() 
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      ElMessage.error('网络请求失败')
    }
  }).catch(() => {})
}

// ================= 发布募捐活动逻辑 =================
const formRef = ref(null)
const publishing = ref(false)

const campaignForm = reactive({
  title: '',
  targetCount: 0,
  coverImg: '',
  description: '',
  charityId: null // 解决发布存不进数据库的问题
})

const rules = {
  title: [{ required: true, message: '请填写活动标题', trigger: 'blur' }],
  coverImg: [{ required: true, message: '请上传活动封面图片', trigger: 'change' }],
  description: [{ required: true, message: '请填写活动描述', trigger: 'blur' }]
}

// 💡 按照你给的代码，完全复刻接管上传逻辑
const customUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const res = await request.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (res.code === 200) {
      campaignForm.coverImg = res.data.url
      ElMessage.success('封面上传成功')
      
      if (formRef.value) {
        formRef.value.validateField('coverImg')
      }
      options.onSuccess(res)
    } else {
      ElMessage.error(res.message || '图片上传失败')
      options.onError(new Error(res.message))
    }
  } catch (error) {
    ElMessage.error('上传失败，请检查网络或图片尺寸')
    options.onError(error)
  }
}

const submitCampaign = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      publishing.value = true
      try {
        // 自动填入社团ID
        campaignForm.charityId = userStore.userInfo?.id || 1;

        const res = await request.post('/donation/charity/campaign/publish', campaignForm)
        if (res.code === 200) {
          ElMessage.success('发布成功！请等待系统审核。')
          formRef.value.resetFields()
          campaignForm.coverImg = '' 
        } else {
          ElMessage.error(res.message || '发布失败')
        }
      } catch (error) {
        ElMessage.error('发布请求失败')
      } finally {
        publishing.value = false
      }
    }
  })
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.header {
  margin-bottom: 30px;
  border-left: 5px solid #67c23a;
  padding-left: 15px;
}
.header h2 {
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #303133;
}
.header p {
  margin: 0;
  color: #909399;
}
.tab-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: bold;
}
.form-wrapper {
  max-width: 800px;
  margin: 30px auto;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  transition: border-color 0.3s;
}
.avatar-uploader:hover {
  border-color: #67c23a;
}
.preview-img {
  width: 178px;
  height: 178px;
  object-fit: cover;
}
.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>