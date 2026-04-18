<template>
  <div class="default-layout">
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="24"><Shop /></el-icon>
          <span>校园闲置物交易平台</span>
        </div>

        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首页</router-link>

          <router-link
            to="/donation/hall"
            class="nav-item"
            style="color: #67c23a; font-weight: bold"
          >
            💚 爱心公益
          </router-link>
          <router-link
            v-if="userStore.isLoggedIn"
            to="/publish"
            class="nav-item"
            >发布商品</router-link
          >
          <router-link
            v-if="userStore.isLoggedIn"
            to="/my-orders"
            class="nav-item"
            >我的订单</router-link
          >
          <router-link v-if="userStore.isLoggedIn" to="/wallet" class="nav-item"
            >我的钱包</router-link
          >
          <router-link
            v-if="userStore.isLoggedIn"
            to="/my-exchanges"
            class="nav-item"
            >我的交换</router-link
          >
        </nav>

        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <router-link v-if="userStore.isAdmin" to="/admin" class="admin-btn">
              <el-button class="admin-entry-btn" size="small"
                >管理后台</el-button
              >
            </router-link>

            <el-dropdown trigger="click">
              <div class="user-info">
                <el-avatar
                  :size="32"
                  :src="imgUrl(userStore.userInfo.avatar)"
                  icon="User"
                />
                <span class="username">{{ userStore.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/user')"
                    >个人中心</el-dropdown-item
                  >
                  <el-dropdown-item @click="$router.push('/my-products')"
                    >我的发布</el-dropdown-item
                  >
                  <el-dropdown-item @click="$router.push('/my-favorites')"
                    >我的收藏</el-dropdown-item
                  >
                  <el-dropdown-item @click="$router.push('/donation/my')"
                    >我的捐赠</el-dropdown-item
                  >
                  <el-dropdown-item @click="$router.push('/wallet')"
                    >我的钱包</el-dropdown-item
                  >
                  <el-dropdown-item divided @click="handleLogout"
                    >退出登录</el-dropdown-item
                  >
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <template v-else>
            <el-button type="primary" @click="$router.push('/login')"
              >登录</el-button
            >
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="main-content">
      <router-view />
    </main>
    <footer class="footer">
      <p>校园闲置物交易平台 &copy; 2025 All Rights Reserved</p>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from "@/stores/user";
import { useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";

const userStore = useUserStore();
const router = useRouter();

function imgUrl(url) {
  if (!url) return "";
  if (url.startsWith("http")) return url;
  return "/api" + url;
}

function handleLogout() {
  ElMessageBox.confirm("确定退出登录吗？", "提示", { type: "warning" })
    .then(() => {
      userStore.logout();
      router.push("/login");
    })
    .catch(() => {});
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables" as *;
.default-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  height: $header-height;
  background: $bg-white;
  box-shadow: $shadow-sm;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 18px;
  font-weight: 700;
  color: $primary-color;
  margin-right: 40px;
  white-space: nowrap;
}
.nav-menu {
  display: flex;
  gap: 8px;
  flex: 1;
}
.nav-item {
  padding: 8px 16px;
  border-radius: $radius-md;
  color: $text-regular;
  font-size: 14px;
  transition: all 0.2s;
  &:hover,
  &.router-link-exact-active {
    color: $primary-color;
    background: $primary-bg;
  }
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-btn {
  margin-right: 8px;
  text-decoration: none;
}
.admin-btn :deep(.admin-entry-btn) {
  border: none;
  color: #fff;
  font-weight: 600;
  border-radius: 999px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #409eff 0%, #6f7dff 100%);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.28);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    filter 0.2s ease;
}
.admin-btn :deep(.admin-entry-btn:hover) {
  transform: translateY(-1px);
  box-shadow: 0 10px 18px rgba(64, 158, 255, 0.34);
  filter: brightness(1.03);
}
.admin-btn :deep(.admin-entry-btn:active) {
  transform: translateY(0);
  box-shadow: 0 5px 12px rgba(64, 158, 255, 0.25);
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: $radius-md;
  &:hover {
    background: $bg-color;
  }
}
.username {
  font-size: 14px;
  color: $text-primary;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}
.footer {
  text-align: center;
  padding: 20px;
  color: $text-secondary;
  font-size: 13px;
  border-top: 1px solid $border-light;
  background: $bg-white;
}
</style>
