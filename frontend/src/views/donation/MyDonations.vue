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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request' // 使用项目统一的 axios 实例

const loading = ref(false)
const donationList = ref([])

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
    // 这里的接口路径需对应后端 DonationController 中的用户查询接口
    const res = await request.get('/donation/user/my-list')
    if (res.code === 200) {
      donationList.value = res.data
    }
  } catch (error) {
    console.error(error)
    // ElMessage.error('获取记录失败') // 初始调试时可以先注释掉，等待后端接口完全配好
  } finally {
    loading.value = false
  }
}

const handleDetail = (row) => {
  ElMessage.info('查看订单: ' + row.orderNo)
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