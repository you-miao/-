<template>
  <div class="page-card">
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索学号 / 手机号 / 昵称"
        clearable
        style="width: 280px"
        @keyup.enter="loadData"
      >
        <template #prefix
          ><el-icon><Search /></el-icon
        ></template>
      </el-input>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar :size="36" :src="imgUrl(row.avatar)" icon="User" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="100" />
      <el-table-column prop="studentNo" label="学号" width="130" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="账户余额" width="110">
        <template #default="{ row }"
          >¥{{
            row.balance != null ? Number(row.balance).toFixed(2) : "0.00"
          }}</template
        >
      </el-table-column>
      <el-table-column label="性别" width="70">
        <template #default="{ row }">{{
          { 1: "男", 2: "女" }[row.gender] || "-"
        }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 1 ? 'success' : 'danger'"
            size="small"
            >{{ row.status === 1 ? "正常" : "禁用" }}</el-tag
          >
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <button
              v-if="row.status === 1"
              class="act-btn act-disable"
              @click="handleToggle(row.id, 0)"
            >
              禁用
            </button>
            <button
              v-else
              class="act-btn act-enable"
              @click="handleToggle(row.id, 1)"
            >
              启用
            </button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div style="display: flex; justify-content: center; margin-top: 16px">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="20"
        :total="total"
        layout="total, prev, pager, next"
        @change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getAdminUsers, updateUserStatus } from "@/api/admin";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const list = ref([]);
const total = ref(0);
const keyword = ref("");
const pageNum = ref(1);

function imgUrl(url) {
  if (!url) return "";
  if (url.startsWith("http")) return url;
  return "/api" + url;
}

async function loadData() {
  loading.value = true;
  try {
    const res = await getAdminUsers({
      keyword: keyword.value,
      pageNum: pageNum.value,
      pageSize: 20,
    });
    list.value = res.data.records;
    total.value = res.data.total;
  } finally {
    loading.value = false;
  }
}

async function handleToggle(userId, status) {
  const label = status === 0 ? "禁用" : "启用";
  await ElMessageBox.confirm(`确认${label}该用户？`, "确认", {
    type: "warning",
  });
  await updateUserStatus(userId, status);
  ElMessage.success(`已${label}`);
  loadData();
}

onMounted(() => loadData());
</script>

<style lang="scss" scoped>
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;
}
.action-btns {
  display: flex;
  gap: 8px;
}
.act-btn {
  padding: 4px 14px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid;
  transition: all 0.2s;
  background: #fff;
  line-height: 1.6;
}
.act-disable {
  color: #9b1c1c;
  border-color: #f0c6c6;
  background: #fdf2f2;
  &:hover {
    background: #fce4e4;
    border-color: #e8a0a0;
  }
}
.act-enable {
  color: #1d6e3f;
  border-color: #c6e6d5;
  background: #f0f9f4;
  &:hover {
    background: #e0f2e9;
    border-color: #8dcfab;
  }
}
</style>
