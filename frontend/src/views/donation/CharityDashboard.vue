<template>
  <div class="dashboard-container">
    <div class="header">
      <h2>
        <el-icon><Monitor /></el-icon> 社团公益工作台
      </h2>
      <p>按活动管理捐赠订单，支持历史活动归档和图文公告发布</p>
    </div>

    <el-tabs v-model="activeTab" class="custom-tabs" type="border-card">
      <el-tab-pane name="manage">
        <template #label>
          <span class="tab-label"><el-icon><Box /></el-icon> 活动管理</span>
        </template>
        <el-row :gutter="16" v-loading="loadingCampaigns">
          <el-col
            v-for="item in activeCampaigns"
            :key="item.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="8"
          >
            <el-card class="campaign-card" shadow="hover">
              <img
                :src="formatImageUrl(item.coverImg)"
                class="cover-image"
                alt="活动封面"
              />
              <div class="card-main">
                <div class="card-title">
                  <span>{{ item.title }}</span>
                  <el-tag :type="getCampaignStatusType(item.status)">
                    {{ getCampaignStatusText(item.status) }}
                  </el-tag>
                </div>
                <p class="desc">{{ item.description }}</p>
                <div class="stats">
                  <span>已接收：{{ item.currentCount || 0 }} 件</span>
                  <span>
                    目标：{{ !item.targetCount || item.targetCount === 0 ? "不限量" : item.targetCount + " 件" }}
                  </span>
                </div>
                <div class="actions">
                  <el-button type="primary" size="small" @click="openOrders(item)">
                    查看订单
                  </el-button>
                  <el-button
                    size="small"
                    @click="handleAdjustTarget(item, 10)"
                    :disabled="item.status !== 1"
                  >
                    +10 目标
                  </el-button>
                  <el-button
                    type="warning"
                    size="small"
                    @click="handleOffline(item)"
                    :disabled="item.status !== 1"
                  >
                    下架活动
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty
          v-if="!loadingCampaigns && activeCampaigns.length === 0"
          description="暂无可管理活动（审核中或进行中）"
        />
      </el-tab-pane>

      <el-tab-pane name="history">
        <template #label>
          <span class="tab-label"><el-icon><Box /></el-icon> 历史活动</span>
        </template>
        <el-row :gutter="16" v-loading="loadingCampaigns">
          <el-col
            v-for="item in historyCampaigns"
            :key="item.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="8"
          >
            <el-card class="campaign-card history" shadow="never">
              <img
                :src="formatImageUrl(item.coverImg)"
                class="cover-image"
                alt="历史活动封面"
              />
              <div class="card-main">
                <div class="card-title">
                  <span>{{ item.title }}</span>
                  <el-tag type="info">已下架</el-tag>
                </div>
                <p class="desc">{{ item.description }}</p>
                <div class="stats">
                  <span>最终接收：{{ item.currentCount || 0 }} 件</span>
                  <span>目标：{{ !item.targetCount || item.targetCount === 0 ? "不限量" : item.targetCount + " 件" }}</span>
                </div>
                <div class="actions">
                  <el-button type="primary" plain size="small" @click="openOrders(item)">
                    查看订单
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty
          v-if="!loadingCampaigns && historyCampaigns.length === 0"
          description="暂无历史活动"
        />
      </el-tab-pane>

      <el-tab-pane name="announce">
        <template #label>
          <span class="tab-label">
            <el-icon><Promotion /></el-icon> 发布公告
          </span>
        </template>
        <div class="form-wrapper">
          <el-form
            ref="announceFormRef"
            :model="announcementForm"
            :rules="announcementRules"
            label-width="120px"
            size="large"
          >
            <el-form-item label="关联活动" prop="campaignId">
              <el-select v-model="announcementForm.campaignId" clearable placeholder="可选：选择对应活动">
                <el-option
                  v-for="item in campaigns"
                  :key="item.id"
                  :label="item.title"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="公告标题" prop="title">
              <el-input v-model="announcementForm.title" placeholder="例如：第一批冬衣已送达山区小学" />
            </el-form-item>
            <el-form-item label="受助信息" prop="recipientInfo">
              <el-input v-model="announcementForm.recipientInfo" placeholder="例如：XX县XX小学 120名学生" />
            </el-form-item>
            <el-form-item label="公告内容" prop="content">
              <el-input
                v-model="announcementForm.content"
                type="textarea"
                :rows="5"
                placeholder="说明本次捐赠物资的去向、发放情况、感谢寄语等"
              />
            </el-form-item>
            <el-form-item label="公告图片" prop="proofImages">
              <el-upload
                action="#"
                list-type="picture-card"
                :http-request="customAnnouncementUpload"
                :file-list="announcementUploadList"
                :on-remove="removeAnnouncementImage"
                :limit="6"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="publishingAnnouncement" @click="submitAnnouncement">
                发布公告
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane name="publish">
        <template #label>
          <span class="tab-label">
            <el-icon><Promotion /></el-icon> 发布募捐活动
          </span>
        </template>

        <div class="form-wrapper">
          <el-form
            ref="formRef"
            :model="campaignForm"
            :rules="rules"
            label-width="120px"
            size="large"
          >
            <el-form-item label="活动标题" prop="title">
              <el-input
                v-model="campaignForm.title"
                placeholder="例如：2026届计算机学院毕业季旧书漂流"
              />
            </el-form-item>

            <el-form-item label="目标筹集数量" prop="targetCount">
              <el-input-number
                v-model="campaignForm.targetCount"
                :min="0"
                :step="10"
              />
              <span class="helper-text">(填 0 表示不限制数量)</span>
            </el-form-item>

            <el-form-item label="活动封面" prop="coverImg">
              <el-upload
                class="avatar-uploader"
                action="#"
                :http-request="customUpload"
                :show-file-list="false"
                accept="image/*"
              >
                <img
                  v-if="campaignForm.coverImg"
                  :src="formatImageUrl(campaignForm.coverImg)"
                  class="preview-img"
                />
                <el-icon v-else class="uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>

            <el-form-item label="活动详情描述" prop="description">
              <el-input
                v-model="campaignForm.description"
                type="textarea"
                :rows="5"
                placeholder="请详细描述本次募捐的目的、用途和急需的物品类型..."
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="success"
                @click="submitCampaign"
                :loading="publishing"
                style="width: 150px"
              >
                立即发布活动
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="ordersDialogVisible"
      width="1050px"
      :title="selectedCampaign ? `活动订单 - ${selectedCampaign.title}` : '活动订单'"
    >
      <div class="order-filter">
        <el-radio-group v-model="orderStatusFilter" @change="fetchOrdersByCampaign">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="0">待处理</el-radio-button>
          <el-radio-button :label="1">已接收</el-radio-button>
          <el-radio-button :label="4">已驳回</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="orders" v-loading="loadingOrders" stripe>
        <el-table-column prop="orderNo" label="捐赠单号" width="180" />
        <el-table-column prop="itemName" label="物品名称" min-width="140" />
        <el-table-column label="物品图片" width="95" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.itemImages"
              style="width: 52px; height: 52px; border-radius: 4px; border: 1px solid #ebeef5;"
              :src="formatImageUrl(row.itemImages.split(',')[0])"
              :preview-src-list="row.itemImages.split(',').map(url => formatImageUrl(url))"
              preview-teleported
              fit="cover"
            />
            <span v-else class="done-text">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="donorName" label="联系人" width="110" />
        <el-table-column prop="donorPhone" label="联系电话" width="125" />
        <el-table-column prop="donorAddress" label="取件地址" min-width="180" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.status)">
              {{ getOrderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleReceive(row)">
                接收
              </el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">
                驳回
              </el-button>
            </template>
            <span v-else class="done-text">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { Monitor, Box, Promotion, Plus } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "@/utils/request";
import { useUserStore } from "@/stores/user";
import {
  getCharityCampaigns,
  getCharityOrders,
  receiveDonation,
  rejectDonation,
  offlineCampaign,
  adjustCampaignTarget,
  publishAnnouncement,
} from "@/api/donation";

const userStore = useUserStore();
const activeTab = ref("manage");

const campaigns = ref([]);
const loadingCampaigns = ref(false);
const activeCampaigns = computed(() => campaigns.value.filter(item => item.status !== 3));
const historyCampaigns = computed(() => campaigns.value.filter(item => item.status === 3));

const ordersDialogVisible = ref(false);
const selectedCampaign = ref(null);
const orders = ref([]);
const loadingOrders = ref(false);
const orderStatusFilter = ref(null);

const formRef = ref(null);
const publishing = ref(false);
const campaignForm = reactive({
  title: "",
  targetCount: 0,
  coverImg: "",
  description: "",
  charityId: null,
});

const announceFormRef = ref(null);
const publishingAnnouncement = ref(false);
const announcementUploadList = ref([]);
const announcementForm = reactive({
  campaignId: null,
  title: "",
  recipientInfo: "",
  content: "",
  proofImages: "",
});

const rules = {
  title: [{ required: true, message: "请填写活动标题", trigger: "blur" }],
  coverImg: [
    { required: true, message: "请上传活动封面图片", trigger: "change" },
  ],
  description: [{ required: true, message: "请填写活动描述", trigger: "blur" }],
};

const announcementRules = {
  title: [{ required: true, message: "请填写公告标题", trigger: "blur" }],
  recipientInfo: [{ required: true, message: "请填写受助信息", trigger: "blur" }],
  content: [{ required: true, message: "请填写公告内容", trigger: "blur" }],
};

const formatImageUrl = (url) => {
  if (!url) return "";
  if (url.startsWith("http")) return url;
  return "http://localhost:8088/api" + (url.startsWith("/") ? "" : "/") + url;
};

const getCampaignStatusType = (status) => {
  const map = { 0: "warning", 1: "success", 2: "danger", 3: "info" };
  return map[status] || "info";
};

const getCampaignStatusText = (status) => {
  const map = { 0: "待审核", 1: "进行中", 2: "已驳回", 3: "已下架" };
  return map[status] || "未知状态";
};

const getOrderStatusType = (status) => {
  const map = { 0: "warning", 1: "success", 4: "danger" };
  return map[status] || "info";
};

const getOrderStatusText = (status) => {
  const map = { 0: "待处理", 1: "已接收", 4: "已驳回" };
  return map[status] || "其他";
};

const fetchCampaigns = async () => {
  loadingCampaigns.value = true;
  try {
    const res = await getCharityCampaigns();
    if (res.code === 200) {
      campaigns.value = res.data || [];
    }
  } catch (error) {
    ElMessage.error("获取活动列表失败");
  } finally {
    loadingCampaigns.value = false;
  }
};

const openOrders = async (campaign) => {
  selectedCampaign.value = campaign;
  orderStatusFilter.value = null;
  ordersDialogVisible.value = true;
  await fetchOrdersByCampaign();
};

const fetchOrdersByCampaign = async () => {
  if (!selectedCampaign.value?.id) return;
  loadingOrders.value = true;
  try {
    const params = { campaignId: selectedCampaign.value.id };
    if (orderStatusFilter.value !== null) {
      params.status = orderStatusFilter.value;
    }
    const res = await getCharityOrders(params);
    if (res.code === 200) {
      orders.value = res.data || [];
    }
  } catch (error) {
    ElMessage.error("获取订单列表失败");
  } finally {
    loadingOrders.value = false;
  }
};

const handleReceive = (row) => {
  ElMessageBox.confirm(
    `确认接收 [${row.donorName}] 的 [${row.itemName}] 吗？接收后会增加活动已接收数量。`,
    "接收确认",
    { confirmButtonText: "确认接收", cancelButtonText: "取消", type: "warning" },
  )
    .then(async () => {
      const res = await receiveDonation(row.id);
      if (res.code === 200) {
        ElMessage.success("已接收，数量已更新");
        await fetchOrdersByCampaign();
        await fetchCampaigns();
      }
    })
    .catch(() => {});
};

const handleReject = (row) => {
  ElMessageBox.prompt("请输入驳回原因（选填）", "驳回订单", {
    confirmButtonText: "确认驳回",
    cancelButtonText: "取消",
    inputType: "textarea",
  })
    .then(async ({ value }) => {
      const res = await rejectDonation(row.id, value || "");
      if (res.code === 200) {
        ElMessage.success("订单已驳回");
        await fetchOrdersByCampaign();
      }
    })
    .catch(() => {});
};

const handleOffline = (campaign) => {
  ElMessageBox.confirm(
    `确认下架活动 [${campaign.title}] 吗？下架后募捐大厅将不再展示。`,
    "下架确认",
    { confirmButtonText: "确认下架", cancelButtonText: "取消", type: "warning" },
  )
    .then(async () => {
      const res = await offlineCampaign(campaign.id);
      if (res.code === 200) {
        ElMessage.success("活动已下架");
        await fetchCampaigns();
      }
    })
    .catch(() => {});
};

const handleAdjustTarget = async (campaign, delta) => {
  const res = await adjustCampaignTarget(campaign.id, delta);
  if (res.code === 200) {
    ElMessage.success("目标数量已调整");
    await fetchCampaigns();
  }
};

const customUpload = async (options) => {
  const formData = new FormData();
  formData.append("file", options.file);
  try {
    const res = await request.post("/file/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    if (res.code === 200) {
      campaignForm.coverImg = res.data.url;
      if (formRef.value) {
        formRef.value.validateField("coverImg");
      }
      ElMessage.success("封面上传成功");
      options.onSuccess(res);
    } else {
      ElMessage.error(res.message || "图片上传失败");
      options.onError(new Error(res.message));
    }
  } catch (error) {
    ElMessage.error("上传失败，请检查网络或图片尺寸");
    options.onError(error);
  }
};

const customAnnouncementUpload = async (options) => {
  const formData = new FormData();
  formData.append("file", options.file);
  try {
    const res = await request.post("/file/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    if (res.code === 200) {
      const url = res.data.url;
      announcementUploadList.value.push({
        name: options.file.name,
        url: formatImageUrl(url),
        rawUrl: url,
      });
      announcementForm.proofImages = announcementUploadList.value
        .map(item => item.rawUrl)
        .join(",");
      options.onSuccess(res);
    } else {
      options.onError(new Error(res.message || "上传失败"));
    }
  } catch (error) {
    options.onError(error);
  }
};

const removeAnnouncementImage = (file) => {
  const rawUrl = file.rawUrl || file.response?.data?.url;
  announcementUploadList.value = announcementUploadList.value.filter(item => item.rawUrl !== rawUrl);
  announcementForm.proofImages = announcementUploadList.value
    .map(item => item.rawUrl)
    .join(",");
};

const submitCampaign = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    publishing.value = true;
    try {
      campaignForm.charityId = userStore.userId || null;
      const res = await request.post(
        "/donation/charity/campaign/publish",
        campaignForm,
      );
      if (res.code === 200) {
        ElMessage.success("发布成功！请等待系统审核。");
        formRef.value.resetFields();
        campaignForm.coverImg = "";
        activeTab.value = "manage";
        await fetchCampaigns();
      }
    } catch (error) {
      ElMessage.error("发布请求失败");
    } finally {
      publishing.value = false;
    }
  });
};

const submitAnnouncement = () => {
  announceFormRef.value.validate(async (valid) => {
    if (!valid) return;
    publishingAnnouncement.value = true;
    try {
      const payload = {
        campaignId: announcementForm.campaignId,
        title: announcementForm.title,
        recipientInfo: announcementForm.recipientInfo,
        content: announcementForm.content,
        proofImages: announcementForm.proofImages,
      };
      const res = await publishAnnouncement(payload);
      if (res.code === 200) {
        ElMessage.success("公告发布成功");
        announceFormRef.value.resetFields();
        announcementUploadList.value = [];
        announcementForm.proofImages = "";
      }
    } catch (error) {
      ElMessage.error("公告发布失败");
    } finally {
      publishingAnnouncement.value = false;
    }
  });
};

onMounted(() => {
  fetchCampaigns();
});
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 20px;
  border-left: 5px solid #67c23a;
  padding-left: 15px;
}

.header h2 {
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #303133;
}

.header p {
  margin: 0;
  color: #909399;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: bold;
}

.campaign-card {
  margin-bottom: 16px;
  overflow: hidden;
}

.campaign-card.history {
  opacity: 0.92;
}

.cover-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.card-main {
  padding-top: 12px;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 600;
}

.desc {
  color: #606266;
  font-size: 13px;
  min-height: 38px;
  margin: 0 0 10px 0;
}

.stats {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.form-wrapper {
  max-width: 800px;
  margin: 30px auto;
}

.helper-text {
  margin-left: 15px;
  color: #909399;
  font-size: 13px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.preview-img {
  width: 178px;
  height: 178px;
  object-fit: cover;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.order-filter {
  margin-bottom: 12px;
}

.done-text {
  color: #909399;
  font-size: 13px;
}
</style>
