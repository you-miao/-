<template>
  <div class="admin-page">
    <h2>爱心捐赠活动审核</h2>
    
    <el-form :inline="true" class="filter-form">
      <el-form-item label="活动状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" style="width: 160px" @change="fetchData">
          <el-option label="全部" :value="-1"></el-option>
          <el-option label="待审核" :value="0"></el-option>
          <el-option label="已通过" :value="1"></el-option>
          <el-option label="已驳回" :value="2"></el-option>
          <el-option label="已下架" :value="3"></el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="活动ID" width="80" align="center" />
      <el-table-column prop="title" label="活动标题" min-width="150" />
      <el-table-column label="目标数量" width="120" align="center">
        <template #default="{ row }">
          {{ formatTargetCount(row.targetCount) }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleAudit(row, 1)">通过</el-button>
            <el-button size="small" type="danger" @click="handleAudit(row, 2)">驳回</el-button>
          </template>
          <el-button size="small" type="primary" plain @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pagination"
      background
      layout="prev, pager, next, total"
      :total="total"
      :page-size="queryParams.pageSize"
      :current-page="queryParams.pageNum"
      @current-change="handlePageChange"
    />

    <el-dialog v-model="detailVisible" title="活动详情" width="680px">
      <template v-if="currentDetail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="活动标题">
            {{ currentDetail.title || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="活动状态">
            <el-tag :type="getStatusType(currentDetail.status)">
              {{ getStatusText(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目标数量">
            {{ formatTargetCount(currentDetail.targetCount) }}
          </el-descriptions-item>
          <el-descriptions-item label="已筹数量">
            {{ currentDetail.currentCount || 0 }} 件
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">
            {{ currentDetail.createTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="封面图">
            <el-image
              v-if="currentDetail.coverImg"
              :src="formatImageUrl(currentDetail.coverImg)"
              :preview-src-list="[formatImageUrl(currentDetail.coverImg)]"
              preview-teleported
              style="width: 160px; height: 100px; border-radius: 6px;"
              fit="cover"
            />
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="活动介绍">
            <div class="detail-desc">
              {{ currentDetail.description || '-' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminDonationList, auditDonation } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentDetail = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  status: 0 // 默认进来先看待审核的数据
})

// 获取表格数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryParams.value }
    if (params.status === -1) {
      delete params.status
    }
    const res = await getAdminDonationList(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

// 分页切换
const handlePageChange = (page) => {
  queryParams.value.pageNum = page
  fetchData()
}

// 状态样式枚举
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

// 状态文字枚举
const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已下架' }
  return map[status] || '未知'
}

const formatTargetCount = (count) => {
  if (!count || count === 0) return '不限量'
  return `${count} 件`
}

const formatImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8088/api${url.startsWith('/') ? '' : '/'}${url}`
}

// 审核操作 (与后端的 AuditDTO 字段完全对应)
const handleAudit = (row, status) => {
  const actionText = status === 1 ? '通过' : '驳回'
  ElMessageBox.prompt(`请输入${actionText}原因 (选填)`, '审核确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(async ({ value }) => {
    // 💡 这里的属性名必须和后端的 AuditDTO 完全一致
    const res = await auditDonation({
      targetId: row.id,
      targetType: 2,       // 这里假定2代表捐赠活动，如果有特定枚举请按需修改
      auditAction: status, // 1是通过，2是驳回
      reason: value
    })
    
    if (res.code === 200) {
      ElMessage.success('审核处理完成')
      fetchData() // 刷新列表
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  }).catch(() => {})
}

// 预留的查看详情方法
const viewDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.admin-page {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  .filter-form {
    margin-bottom: 20px;
  }
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
