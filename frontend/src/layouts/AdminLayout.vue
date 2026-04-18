<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-logo" @click="handleLogoClick">
        <el-icon :size="22" color="#fff"><Shop /></el-icon>
        <span>管理后台</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1d2b3a"
        text-color="#b7bec8"
        active-text-color="#ffffff"
      >
        <el-menu-item v-if="userStore.isAdmin" index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isCharity" index="/admin/charity">
          <el-icon><Present /></el-icon>
          <span>社团工作台</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin" index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin" index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin" index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin" index="/admin/donation-audit">
          <el-icon><Present /></el-icon>
          <span>爱心捐赠审核</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <div class="admin-main">
      <header class="admin-header">
        <div class="admin-header-left">
          <span class="page-name">{{ $route.meta.title }}</span>
        </div>
        <div class="admin-header-right">
          <el-button v-if="!userStore.isCharity" text @click="$router.push('/')">返回前台</el-button>
          <el-dropdown trigger="click">
            <div class="admin-user">
              <el-avatar :size="30" :src="avatarUrl" icon="User" />
              <span>{{
                userStore.userInfo?.nickname || userStore.nickname
              }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout"
                  >退出登录</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useUserStore } from "@/stores/user";
import { useRouter } from "vue-router";
// 💡 确保引入了图标
import {
  Shop,
  DataAnalysis,
  Goods,
  User,
  ChatDotRound,
  Present,
} from "@element-plus/icons-vue";

const userStore = useUserStore();
const router = useRouter();
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar;
  if (!avatar) return "";
  if (avatar.startsWith("http")) return avatar;
  return `/api${avatar.startsWith("/") ? "" : "/"}${avatar}`;
});

function handleLogout() {
  userStore.logout();
  router.push("/login");
}

function handleLogoClick() {
  if (userStore.isCharity) {
    router.push("/admin/charity");
    return;
  }
  router.push("/");
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables" as *;
.admin-layout {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: $sidebar-width;
  background: $sidebar-bg;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.sidebar-logo {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.el-menu {
  border-right: none;
  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    &.is-active {
      background-color: $primary-color !important;
    }
    &:hover {
      background-color: rgba(255, 255, 255, 0.06) !important;
    }
  }
}
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.admin-header {
  height: $header-height;
  background: $bg-white;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}
.page-name {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}
.admin-header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.admin-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: $text-regular;
}
.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: $bg-color;
}
</style>
