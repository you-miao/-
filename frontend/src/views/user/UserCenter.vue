<template>
  <div class="user-center">
    <div class="page-card">
      <h2 class="page-title">个人中心</h2>
      <div class="wallet-strip">
        <span class="wallet-balance">账户余额：<strong>¥{{ balanceDisplay }}</strong></span>
        <router-link to="/wallet">
          <el-button type="primary" link>钱包与充值</el-button>
        </router-link>
      </div>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <el-form ref="infoRef" :model="infoForm" :rules="infoRules" label-width="90px" style="max-width:500px" size="large">
            <el-form-item label="头像">
              <el-upload :action="'/api/file/upload'" :headers="uploadHeaders" :show-file-list="false" :on-success="handleAvatarSuccess" accept="image/*">
                <el-avatar :size="72" :src="imgUrl(infoForm.avatar)" icon="User" style="cursor:pointer" />
              </el-upload>
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="infoForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname"><el-input v-model.trim="infoForm.nickname" /></el-form-item>
            <el-form-item label="真实姓名" prop="realName"><el-input v-model.trim="infoForm.realName" /></el-form-item>
            <el-form-item label="学号" prop="studentNo"><el-input v-model.trim="infoForm.studentNo" /></el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="infoForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
                <el-radio :label="0">保密</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="手机号" prop="phone"><el-input v-model.trim="infoForm.phone" /></el-form-item>
            <el-form-item label="邮箱" prop="email"><el-input v-model.trim="infoForm.email" /></el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveInfo">保存修改</el-button>
              <el-button @click="resetInfo">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="90px" style="max-width:500px" size="large">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPwd">
              <el-input v-model="pwdForm.confirmPwd" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="收货地址" name="address">
          <el-button type="primary" style="margin-bottom:16px" @click="openAddressDialog(null)">添加地址</el-button>
          <el-table :data="sortedAddressList" :row-class-name="getAddressRowClass" stripe>
            <el-table-column prop="receiver" label="收货人" width="100" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column prop="campus" label="校区" width="120" />
            <el-table-column prop="detail" label="详细地址" />
            <el-table-column label="默认" width="70">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button v-if="!row.isDefault" link type="success" @click="handleSetDefaultAddress(row.id)">设为默认</el-button>
                <el-button link type="primary" @click="openAddressDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteAddress(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="addrDialogVisible" :title="addrForm.id ? '编辑地址' : '添加地址'" width="480px">
      <el-form ref="addrRef" :model="addrForm" :rules="addrRules" label-width="80px">
        <el-form-item label="收货人" prop="receiver"><el-input v-model.trim="addrForm.receiver" /></el-form-item>
        <el-form-item label="电话" prop="phone"><el-input v-model.trim="addrForm.phone" /></el-form-item>
        <el-form-item label="校区" prop="campus"><el-input v-model.trim="addrForm.campus" /></el-form-item>
        <el-form-item label="详细地址" prop="detail"><el-input v-model.trim="addrForm.detail" type="textarea" /></el-form-item>
        <el-form-item label="设为默认"><el-switch v-model="addrForm.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addrDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo, updatePassword, updateAvatar, getAddressList, saveAddress, setDefaultAddress, deleteAddress } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { resolveImageUrl } from '@/utils/image'

const userStore = useUserStore()
const activeTab = ref('info')
const balanceDisplay = ref('0.00')
const saving = ref(false)
const changingPwd = ref(false)
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${userStore.token}` }))
const infoRef = ref()
const infoSnapshot = ref({})

const infoForm = reactive({
  username: '',
  nickname: '',
  realName: '',
  gender: 0,
  phone: '',
  email: '',
  studentNo: '',
  avatar: ''
})
const infoRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d{6,20}$/, message: '学号需为6-20位数字', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ pattern: /^$|^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '邮箱格式不正确', trigger: 'blur' }]
}

function imgUrl(url) {
  return resolveImageUrl(url)
}
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPwd: '' })
const pwdRef = ref()
const addressList = ref([])
const sortedAddressList = computed(() => [...addressList.value].sort((a, b) => {
  if ((b.isDefault || 0) !== (a.isDefault || 0)) return (b.isDefault || 0) - (a.isDefault || 0)
  return (b.id || 0) - (a.id || 0)
}))
const addrDialogVisible = ref(false)
const addrRef = ref()
const addrForm = reactive({ id: null, receiver: '', phone: '', campus: '', detail: '', isDefault: 0 })
const addrRules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '联系电话格式不正确', trigger: 'blur' }
  ],
  campus: [{ required: true, message: '请输入校区', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码' }],
  newPassword: [{ required: true, message: '请输入新密码' }, { min: 6, message: '密码至少6位' }],
  confirmPwd: [{ required: true, message: '请确认密码' }, {
    validator: (r, v, cb) => v !== pwdForm.newPassword ? cb(new Error('两次密码不一致')) : cb()
  }]
}

async function loadUserInfo() {
  const res = await getUserInfo()
  Object.assign(infoForm, res.data)
  infoSnapshot.value = { ...res.data }
  const b = res.data?.balance
  balanceDisplay.value = b != null && b !== '' && !Number.isNaN(Number(b)) ? Number(b).toFixed(2) : '0.00'
}

async function saveInfo() {
  await infoRef.value.validate()
  saving.value = true
  try {
    const payload = {
      nickname: infoForm.nickname,
      realName: infoForm.realName,
      gender: infoForm.gender,
      phone: infoForm.phone || null,
      email: infoForm.email || null,
      studentNo: infoForm.studentNo
    }
    await updateUserInfo(payload)
    userStore.updateUserInfo({ nickname: infoForm.nickname, avatar: infoForm.avatar })
    infoSnapshot.value = { ...infoForm }
    ElMessage.success('保存成功')
  } finally { saving.value = false }
}

function resetInfo() {
  Object.assign(infoForm, infoSnapshot.value)
}

async function handleAvatarSuccess(res) {
  if (res.code === 200) {
    infoForm.avatar = res.data.url
    await updateAvatar(res.data.url)
    userStore.updateUserInfo({ avatar: res.data.url })
    ElMessage.success('头像已更新')
  }
}

async function changePassword() {
  await pwdRef.value.validate()
  changingPwd.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPwd: '' })
  } finally { changingPwd.value = false }
}

async function loadAddresses() {
  const res = await getAddressList()
  addressList.value = res.data
}

function getAddressRowClass({ row }) {
  return row.isDefault ? 'default-address-row' : ''
}

function openAddressDialog(row) {
  if (row) {
    Object.assign(addrForm, row)
  } else {
    Object.assign(addrForm, { id: null, receiver: '', phone: '', campus: '', detail: '', isDefault: 0 })
  }
  addrDialogVisible.value = true
}

async function handleSaveAddress() {
  await addrRef.value.validate()
  const duplicate = addressList.value.some(item =>
    item.id !== addrForm.id &&
    item.receiver === addrForm.receiver &&
    item.phone === addrForm.phone &&
    item.campus === addrForm.campus &&
    item.detail === addrForm.detail
  )
  if (duplicate) {
    ElMessage.warning('地址内容重复，请勿重复添加')
    return
  }
  await saveAddress(addrForm)
  addrDialogVisible.value = false
  ElMessage.success('保存成功')
  loadAddresses()
}

async function handleSetDefaultAddress(id) {
  await setDefaultAddress(id)
  ElMessage.success('已设为默认地址')
  loadAddresses()
}

async function handleDeleteAddress(id) {
  await ElMessageBox.confirm('确定删除该地址？', '提示', { type: 'warning' })
  await deleteAddress(id)
  ElMessage.success('删除成功')
  loadAddresses()
}

onMounted(() => { loadUserInfo(); loadAddresses() })
</script>

<style lang="scss" scoped>
.wallet-strip {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
  border: 1px solid #e4eaf2;
  border-radius: 8px;
}
.wallet-balance {
  font-size: 15px;
  color: #606266;
}
.wallet-balance strong {
  font-size: 18px;
  color: #303133;
}
:deep(.default-address-row > td) {
  background: #f0f9eb !important;
}
</style>
