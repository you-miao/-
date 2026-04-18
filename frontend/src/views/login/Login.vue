<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <el-icon :size="36" color="#2b5ce6"><Shop /></el-icon>
        <h2>校园闲置物交易平台</h2>
        <p>登录你的账号</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" autocomplete="off" data-form-type="other">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="学号 / 手机号 / 用户名" prefix-icon="User" autocomplete="off" name="campus_user" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" :type="pwdVisible ? 'text' : 'password'" placeholder="密码" prefix-icon="Lock" autocomplete="off" name="campus_pwd" @keyup.enter="handleLogin">
            <template #suffix>
              <el-icon class="pwd-toggle" @click="pwdVisible = !pwdVisible">
                <View v-if="pwdVisible" /><Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const pwdVisible = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLoginInfo(res.data)
    ElMessage.success('登录成功')
    let redirect = route.query.redirect || '/'
    if (userStore.isCharity) {
      redirect = '/admin/charity'
    } else if (userStore.isAdmin) {
      redirect = '/admin'
    }
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0fe 0%, #f7f8fa 100%);
}
.login-card {
  width: 420px;
  background: $bg-white;
  border-radius: $radius-lg;
  box-shadow: $shadow-lg;
  padding: 48px 40px 36px;
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
  h2 {
    font-size: 22px;
    color: $text-primary;
    margin: 12px 0 6px;
  }
  p {
    color: $text-secondary;
    font-size: 14px;
  }
}
.pwd-toggle {
  cursor: pointer;
  color: #86909c;
  &:hover { color: #4e5969; }
}
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}
.login-footer {
  text-align: center;
  color: $text-secondary;
  font-size: 14px;
  margin-top: 16px;
  a {
    color: $primary-color;
    font-weight: 500;
  }
}
</style>
