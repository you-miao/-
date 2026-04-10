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
          <el-form ref="infoRef" :model="infoForm" label-width="90px" style="max-width:500px" size="large">
            <el-form-item label="头像">
              <el-upload :action="'/api/file/upload'" :headers="uploadHeaders" :show-file-list="false" :on-success="handleAvatarSuccess" accept="image/*">
                <el-avatar :size="72" :src="imgUrl(infoForm.avatar)" icon="User" style="cursor:pointer" />
              </el-upload>
            </el-form-item>
            <el-form-item label="昵称"><el-input v-model="infoForm.nickname" /></el-form-item>
            <el-form-item label="真实姓名"><el-input v-model="infoForm.realName" /></el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="infoForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
                <el-radio :label="0">保密</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="手机号"><el-input v-model="infoForm.phone" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="infoForm.email" /></el-form-item>
            <el-form-item label="校区"><el-input v-model="infoForm.campus" /></el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveInfo">保存修改</el-button>
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
          <el-table :data="addressList" stripe>
            <el-table-column prop="receiver" label="收货人" width="100" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column prop="campus" label="校区" width="120" />
            <el-table-column prop="detail" label="详细地址" />
            <el-table-column label="默认" width="70">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button link type="primary" @click="openAddressDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteAddress(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="addrDialogVisible" :title="addrForm.id ? '编辑地址' : '添加地址'" width="480px">
      <el-form :model="addrForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="addrForm.receiver" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="addrForm.phone" /></el-form-item>
        <el-form-item label="校区"><el-input v-model="addrForm.campus" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addrForm.detail" type="textarea" /></el-form-item>
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
import { getUserInfo, updateUserInfo, updatePassword, updateAvatar, getAddressList, saveAddress, deleteAddress } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('info')
const balanceDisplay = ref('0.00')
const saving = ref(false)
const changingPwd = ref(false)
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${userStore.token}` }))

const infoForm = reactive({ nickname: '', realName: '', gender: 0, phone: '', email: '', campus: '', avatar: '' })

function imgUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPwd: '' })
const pwdRef = ref()
const addressList = ref([])
const addrDialogVisible = ref(false)
const addrForm = reactive({ id: null, receiver: '', phone: '', campus: '', detail: '', isDefault: 0 })

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
  const b = res.data?.balance
  balanceDisplay.value = b != null && b !== '' && !Number.isNaN(Number(b)) ? Number(b).toFixed(2) : '0.00'
}

async function saveInfo() {
  saving.value = true
  try {
    await updateUserInfo(infoForm)
    userStore.updateUserInfo({ nickname: infoForm.nickname, avatar: infoForm.avatar })
    ElMessage.success('保存成功')
  } finally { saving.value = false }
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

function openAddressDialog(row) {
  if (row) {
    Object.assign(addrForm, row)
  } else {
    Object.assign(addrForm, { id: null, receiver: '', phone: '', campus: '', detail: '', isDefault: 0 })
  }
  addrDialogVisible.value = true
}

async function handleSaveAddress() {
  await saveAddress(addrForm)
  addrDialogVisible.value = false
  ElMessage.success('保存成功')
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
</style>
