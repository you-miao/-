<template>
  <div class="login-page">
    <div class="login-card" style="width:480px">
      <div class="login-header">
        <el-icon :size="36" color="#2b5ce6"><Shop /></el-icon>
        <h2>注册账号</h2>
        <p>加入校园闲置物交易平台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" size="large" autocomplete="off" data-form-type="other">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="off" name="reg_user" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" :type="regPwdVisible ? 'text' : 'password'" placeholder="请输入密码" autocomplete="off" name="reg_pwd">
            <template #suffix>
              <el-icon class="pwd-toggle" @click="regPwdVisible = !regPwdVisible">
                <View v-if="regPwdVisible" /><Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPwd">
          <el-input v-model="form.confirmPwd" :type="regPwd2Visible ? 'text' : 'password'" placeholder="请再次输入密码" autocomplete="off" name="reg_pwd2">
            <template #suffix>
              <el-icon class="pwd-toggle" @click="regPwd2Visible = !regPwd2Visible">
                <View v-if="regPwd2Visible" /><Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="校区">
          <el-input v-model="form.campus" placeholder="请输入所在校区" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const regPwdVisible = ref(false)
const regPwd2Visible = ref(false)

const form = reactive({
  username: '', password: '', confirmPwd: '',
  nickname: '', phone: '', studentNo: '', campus: ''
})

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次密码不一致'))
  else callback()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPwd: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirmPwd, trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0fe 0%, #f7f8fa 100%);
  padding: 40px 0;
}
.login-card {
  background: $bg-white;
  border-radius: $radius-lg;
  box-shadow: $shadow-lg;
  padding: 48px 40px 36px;
}
.login-header {
  text-align: center;
  margin-bottom: 28px;
  h2 { font-size: 22px; color: $text-primary; margin: 12px 0 6px; }
  p { color: $text-secondary; font-size: 14px; }
}
.pwd-toggle {
  cursor: pointer;
  color: #86909c;
  &:hover { color: #4e5969; }
}
.login-btn { width: 100%; height: 44px; font-size: 16px; }
.login-footer {
  text-align: center;
  color: $text-secondary;
  font-size: 14px;
  margin-top: 16px;
  a { color: $primary-color; font-weight: 500; }
}
</style>
