<template>
  <div class="publish-page">
    <div class="page-card">
      <h2 class="page-title">发布商品</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" size="large" style="max-width:700px">
        <el-form-item label="商品类型" prop="productType">
          <el-radio-group v-model="form.productType">
            <el-radio :label="1">出售</el-radio>
            <el-radio :label="2">交换</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-cascader v-model="categoryValue" :options="categories" :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }" placeholder="选择分类" clearable @change="v => form.categoryId = v" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述商品的详细信息" />
        </el-form-item>
        <el-form-item v-if="form.productType === 1" label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item v-if="form.productType === 2" label="交换条件">
          <el-input v-model="form.exchangeDesc" placeholder="描述希望交换的物品类型" />
        </el-form-item>
        <el-form-item label="成色">
          <el-slider v-model="form.quality" :min="1" :max="10" show-stops />
        </el-form-item>
        <el-form-item label="校区">
          <el-input v-model="form.campus" placeholder="所在校区" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contactInfo" placeholder="手机号或微信" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            :action="'/api/file/upload'"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :limit="9"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="标签">
          <el-checkbox-group v-model="form.tagIds">
            <el-checkbox v-for="tag in tags" :key="tag.id" :label="tag.id">{{ tag.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布商品</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { publishProduct, getCategoryTree, getAllTags } from '@/api/product'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)
const categories = ref([])
const tags = ref([])
const categoryValue = ref(null)

const uploadHeaders = computed(() => ({ Authorization: `Bearer ${userStore.token}` }))

const form = reactive({
  productType: 1, title: '', description: '', categoryId: null,
  price: 0, exchangeDesc: '', campus: '', contactInfo: '',
  quality: 8, imageUrls: [], tagIds: []
})

const rules = {
  productType: [{ required: true, message: '请选择类型' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类' }],
  price: [{ required: true, message: '请输入价格', type: 'number' }]
}

function handleUploadSuccess(res) {
  if (res.code === 200) form.imageUrls.push(res.data.url)
}
function handleRemove(file) {
  const url = file.response?.data?.url
  if (url) form.imageUrls = form.imageUrls.filter(u => u !== url)
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await publishProduct(form)
    ElMessage.success('发布成功，等待审核')
    router.push('/my-products')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  getCategoryTree().then(res => categories.value = res.data)
  getAllTags().then(res => tags.value = res.data)
})
</script>

<style lang="scss" scoped>
.publish-page { max-width: 800px; margin: 0 auto; }
</style>
