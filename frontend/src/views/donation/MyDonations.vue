<template>
  <div class="my-donations">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">我的捐赠记录</span>
        </div>
      </template>

      <el-table :data="donationList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="campaignTitle" label="所属活动" min-width="180">
          <template #default="scope">
            {{ scope.row.campaignTitle || '自愿捐赠（未绑定活动）' }}
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="捐赠物品" />
        <el-table-column prop="createTime" label="捐赠时间" width="180" />
        <el-table-column prop="status" label="当前状态">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="捐赠详情" width="620px" destroy-on-close>
      <el-descriptions v-if="currentDetail" :column="1" border>
        <el-descriptions-item label="订单编号">{{ currentDetail.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属活动">{{ currentDetail.campaignTitle || '自愿捐赠（未绑定活动）' }}</el-descriptions-item>
        <el-descriptions-item label="活动ID">{{ currentDetail.campaignId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="捐赠物品">{{ currentDetail.itemName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentDetail.donorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentDetail.donorPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="捐赠地址">{{ currentDetail.donorAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusTag(currentDetail.status)">
            {{ getStatusText(currentDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="捐赠时间">{{ currentDetail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentDetail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyDonations } from '@/api/donation'

const loading = ref(false)
const donationList = ref([])
const detailVisible = ref(false)
const currentDetail = ref(null)

// 获取状态标签样式
const getStatusTag = (status) => {
  const tags = {
    0: 'info',    // 待取件
    1: 'success', // 社团已接收
    2: 'warning', // 已发往受助区
    3: 'success', // 已公示
    4: 'danger'   // 已取消
  }
  return tags[status] || 'info'
}

// 获取状态文字
const getStatusText = (status) => {
  const texts = {
    0: '待取件',
    1: '社团已接收',
    2: '已发往受助区',
    3: '已公示',
    4: '已取消'
  }
  return texts[status] || '未知'
}

// 获取捐赠数据
const fetchMyDonations = async () => {
  loading.value = true
  try {
    const res = await getMyDonations()
    if (res.code === 200) {
      donationList.value = res.data
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchMyDonations()
})
</script>

<style scoped>
.my-donations {
  max-width: 1000px;
  margin: 20px auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
</style>
