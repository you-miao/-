<template>
  <div class="page-card">
    <div class="search-bar">
      <el-select v-model="query.status" placeholder="审核状态" clearable style="width:140px" @change="loadData">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
        <el-option label="被举报" :value="3" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="评论人" width="120">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:6px">
            <el-avatar :size="28" :src="imgUrl(row.userAvatar)" icon="User" />
            <span>{{ row.userName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评论内容" show-overflow-tooltip />
      <el-table-column label="评分" width="80">
        <template #default="{ row }">
          <span v-if="row.rating">{{ row.rating }}星</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="commentStatusMap[row.status]?.type" size="small">{{ commentStatusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="评论时间" width="170" />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <button v-if="row.status === 0 || row.status === 3" class="act-btn act-approve" @click="handleAudit(row.id, 1)">通过</button>
            <button v-if="row.status === 0 || row.status === 3" class="act-btn act-reject" @click="handleAudit(row.id, 2)">驳回</button>
            <button class="act-btn act-delete" @click="handleDelete(row.id)">删除</button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div style="display:flex;justify-content:center;margin-top:16px">
      <el-pagination v-model:current-page="query.pageNum" :page-size="20" :total="total" layout="total, prev, pager, next" @change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminComments, audit } from '@/api/admin'
import { deleteComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, status: 0 })

function imgUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/api' + url
}

const commentStatusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已驳回', type: 'info' },
  3: { label: '被举报', type: 'danger' }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminComments(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleAudit(id, action) {
  const label = action === 1 ? '通过' : '驳回'
  await ElMessageBox.confirm(`确认${label}该评论？`, '确认')
  await audit({ targetId: id, targetType: 2, auditAction: action })
  ElMessage.success('操作成功')
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除该评论？', '确认', { type: 'warning' })
  await deleteComment(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(() => loadData())
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
.act-approve {
  color: #1d6e3f;
  border-color: #c6e6d5;
  background: #f0f9f4;
  &:hover { background: #e0f2e9; border-color: #8dcfab; }
}
.act-reject {
  color: #9b1c1c;
  border-color: #f0c6c6;
  background: #fdf2f2;
  &:hover { background: #fce4e4; border-color: #e8a0a0; }
}
.act-delete {
  color: #6b7280;
  border-color: #e5e7eb;
  background: #f9fafb;
  &:hover { background: #f3f4f6; border-color: #d1d5db; }
}
</style>
