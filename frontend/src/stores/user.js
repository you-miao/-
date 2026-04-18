import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUserInfo() || {})

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value.role === 'ROLE_ADMIN')
  const isCharity = computed(() => userInfo.value.role === 'ROLE_CHARITY')
  const userId = computed(() => userInfo.value.userId)
  const nickname = computed(() => userInfo.value.nickname || userInfo.value.username)

  function setLoginInfo(data) {
    token.value = data.token
    setToken(data.token)
    const info = {
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar,
      role: data.role
    }
    userInfo.value = info
    setUserInfo(info)
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    removeToken()
    removeUserInfo()
  }

  function updateUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    setUserInfo(userInfo.value)
  }

  return { token, userInfo, isLoggedIn, isAdmin, isCharity, userId, nickname, setLoginInfo, logout, updateUserInfo }
})
