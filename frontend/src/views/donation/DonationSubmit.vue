<template>
  <div class="submit-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h3>🎁 填写爱心捐赠单</h3>
          <span v-if="form.campaignTitle" class="campaign-tag">
            正在捐赠给：{{ form.campaignTitle }}
          </span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
        <el-form-item label="物品名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="例如：九成新高等数学教材、冬季羽绒服" />
        </el-form-item>

        <el-form-item label="物品照片" prop="itemImages">
          <el-upload
            action="#"
            list-type="picture-card"
            :http-request="customUpload"
            :on-remove="handleImgRemove"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/png 格式，最多上传 3 张真实照片</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="联系人" prop="donorName">
          <el-input v-model="form.donorName" placeholder="怎么称呼您？" />
        </el-form-item>

        <el-form-item label="联系电话" prop="donorPhone">
          <el-input v-model="form.donorPhone" placeholder="请输入您的手机号码" />
        </el-form-item>

        <el-form-item label="取件地址" prop="donorAddress">
          <el-input v-model="form.donorAddress" placeholder="例如：北区3栋楼下 / 校园菜鸟驿站门前" />
        </el-form-item>

        <el-form-item label="爱心寄语">
          <el-input 
            v-model="form.message" 
            type="textarea" 
            :rows="3" 
            placeholder="写下您对受捐者的祝福吧..." 
          />
        </el-form-item>

        <el-form-item>
          <el-button type="success" @click="submitDonation" :loading="submitting" style="width: 150px;">
            ❤️ 确认捐赠
          </el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  campaignId: null,
  campaignTitle: '',
  itemName: '',
  itemImages: '', // 存储图片URL，多张图用逗号隔开
  donorName: '',
  donorPhone: '',
  donorAddress: '',
  message: ''
})

const rules = {
  itemName: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  itemImages: [{ required: true, message: '请至少上传一张物品照片', trigger: 'change' }],
  donorName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  donorPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  donorAddress: [{ required: true, message: '请输入上门取件地址', trigger: 'blur' }]
}

// 💡 核心大招：强制接管上传逻辑，使用你封装的 request
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
      const imageUrl = res.data.url
      // 多张图片以逗号拼接
      if (!form.itemImages) {
        form.itemImages = imageUrl
      } else {
        form.itemImages += ',' + imageUrl
      }
      
      // 触发一下表单校验，消除红色的“请至少上传一张物品照片”提示
      if (formRef.value) {
        formRef.value.validateField('itemImages')
      }
      
      options.onSuccess(res) // 通知组件上传成功
    } else {
      ElMessage.error(res.message || '图片上传失败')
      options.onError(new Error(res.message))
    }
  } catch (error) {
    ElMessage.error('上传失败，请检查网络或图片尺寸')
    options.onError(error)
  }
}

// 图片移除处理 (保持你原本优秀的移除逻辑)
const handleImgRemove = (file) => {
  if (file.response && file.response.code === 200) {
    const urlToRemove = file.response.data.url
    // 将被删除的 url 从字符串中剔除掉
    form.itemImages = form.itemImages.split(',').filter(url => url !== urlToRemove).join(',')
  }
}

const submitDonation = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await request.post('/donation/user/submit', form)
        if (res.code === 200) {
          ElMessage.success('捐赠提交成功！感谢您的爱心！')
          router.push('/donation/hall')
        } else {
          ElMessage.error(res.message || '提交失败')
        }
      } catch (error) {
        ElMessage.error('网络错误，请稍后再试')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  if (route.query.campaignId) {
    form.campaignId = route.query.campaignId
    form.campaignTitle = route.query.title
  }
  // 自动填入当前登录用户的昵称和手机号作为默认联系方式
  if (userStore.userInfo) {
    form.donorName = userStore.userInfo.nickname || ''
    form.donorPhone = userStore.userInfo.phone || ''
  }
})
</script>

<style scoped>
.submit-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}
.form-card {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h3 {
  margin: 0;
  color: #303133;
}
.campaign-tag {
  background-color: #f0f9eb;
  color: #67c23a;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 13px;
}
</style>