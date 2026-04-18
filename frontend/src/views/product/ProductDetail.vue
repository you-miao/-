<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="product">
      <div class="detail-main">
        <div class="detail-left">
          <el-carousel
            :autoplay="false"
            height="400px"
            indicator-position="outside"
          >
            <el-carousel-item v-for="img in product.images" :key="img.id">
              <img :src="imgUrl(img.url)" class="carousel-img" />
            </el-carousel-item>
            <el-carousel-item v-if="!product.images?.length">
              <div class="no-img">暂无图片</div>
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="detail-right">
          <h1 class="detail-title">{{ product.title }}</h1>
          <div class="detail-tags" v-if="product.tags?.length">
            <span
              v-for="tag in product.tags"
              :key="tag.id"
              class="detail-tag-chip"
              >{{ tag.name }}</span
            >
          </div>
          <div class="detail-price" v-if="product.productType === 1">
            <span class="label">价格</span>
            <span class="value">¥{{ product.price }}</span>
          </div>
          <div class="detail-price" v-else>
            <span class="label">交换条件</span>
            <span class="value exchange">{{
              product.exchangeDesc || "面议"
            }}</span>
          </div>
          <div class="detail-info">
            <div class="info-item">
              <span class="label">分类</span
              ><span>{{ product.categoryName || "-" }}</span>
            </div>
            <div class="info-item">
              <span class="label">成色</span
              ><span>{{ product.quality || "-" }} / 10</span>
            </div>
            <div class="info-item">
              <span class="label">校区</span
              ><span>{{ product.campus || "-" }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系方式</span
              ><span>{{ product.contactInfo || "-" }}</span>
            </div>
            <div class="info-item">
              <span class="label">浏览量</span
              ><span>{{ product.viewCount }}</span>
            </div>
            <div class="info-item">
              <span class="label">收藏数</span
              ><span>{{ product.favoriteCount }}</span>
            </div>
          </div>
          <div class="detail-publisher">
            <el-avatar
              :size="36"
              :src="imgUrl(product.publisherAvatar)"
              icon="User"
            />
            <span class="pub-name">{{ product.publisherName }}</span>
          </div>
          <div class="detail-actions" v-if="userStore.isLoggedIn">
            <el-button
              :type="product.isFavorited ? 'warning' : 'default'"
              @click="handleFavorite"
            >
              <el-icon><Star /></el-icon>
              {{ product.isFavorited ? "已收藏" : "收藏" }}
            </el-button>
            <el-button
              v-if="
                product.productType === 1 &&
                product.status === 1 &&
                Number(product.userId) !== Number(userStore.userId)
              "
              type="primary"
              size="large"
              @click="handleBuy"
            >
              立即购买
            </el-button>
            <el-button
              v-if="product.productType === 2 && product.status === 1"
              type="success"
              size="large"
              @click="showExchange = true"
              >申请交换</el-button
            >
          </div>
        </div>
      </div>

      <div class="page-card" style="margin-top: 20px">
        <h3 class="section-title">商品描述</h3>
        <div class="description">{{ product.description || "暂无描述" }}</div>
      </div>

      <!-- 评论区 -->
      <div class="page-card" style="margin-top: 20px">
        <h3 class="section-title">评论 ({{ commentTotal }})</h3>
        <div v-if="userStore.isLoggedIn" class="comment-form">
          <el-rate v-model="commentForm.rating" style="margin-bottom: 8px" />
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="3"
            placeholder="说说你的看法..."
          />
          <el-button
            type="primary"
            style="margin-top: 8px"
            @click="submitComment"
            >发表评论</el-button
          >
        </div>
        <div class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <el-avatar :size="36" :src="imgUrl(c.userAvatar)" icon="User" />
            <div class="comment-body">
              <div class="comment-header">
                <span class="cname">{{ c.userName }}</span>
                <el-rate
                  v-if="c.rating"
                  :model-value="c.rating"
                  disabled
                  size="small"
                />
                <span class="ctime">{{ c.createTime }}</span>
              </div>
              <p class="ccontent">{{ c.content }}</p>
              <div class="comment-actions">
                <span class="like-btn" @click="handleLike(c.id)"
                  ><el-icon><Pointer /></el-icon> {{ c.likeCount || 0 }}</span
                >
              </div>
              <div v-if="c.replies?.length" class="replies">
                <div v-for="r in c.replies" :key="r.id" class="reply-item">
                  <span class="rname">{{ r.userName }}</span>
                  <span v-if="r.replyToUserName" class="reply-to">
                    回复 {{ r.replyToUserName }}</span
                  >
                  ：{{ r.content }}
                </div>
              </div>
            </div>
          </div>
          <el-empty v-if="comments.length === 0" description="暂无评论" />
        </div>
      </div>
    </template>

    <!-- 购买对话框 -->
    <el-dialog v-model="showBuy" title="确认购买" width="560px">
      <el-form
        ref="buyFormRef"
        :model="buyForm"
        :rules="buyRules"
        label-width="100px"
      >
        <el-form-item label="联系电话" prop="contactInfo">
          <el-input
            v-model="buyForm.contactInfo"
            placeholder="请输入本次下单联系电话（可与个人信息不同）"
          />
        </el-form-item>
        <el-form-item label="交付方式" prop="deliveryType">
          <el-radio-group v-model="buyForm.deliveryType">
            <el-radio :label="2">送货上门</el-radio>
            <el-radio :label="1">线下自提</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="buyForm.deliveryType === 2"
          label="收货地址"
          prop="addressId"
        >
          <el-select
            v-model="buyForm.addressId"
            placeholder="请选择收货地址"
            style="width: 100%"
          >
            <el-option
              v-for="item in addressList"
              :key="item.id"
              :label="`${item.receiver} ${item.phone} | ${item.campus || ''} ${item.detail || ''}`"
              :value="item.id"
            />
          </el-select>
          <div
            v-if="addressList.length === 0"
            style="font-size: 12px; color: #f56c6c; margin-top: 6px"
          >
            暂无地址，请先到个人中心完善收货地址
          </div>
        </el-form-item>
        <el-form-item v-if="buyForm.deliveryType === 1" label="取货地点">
          <el-input
            v-model="buyForm.pickupLocation"
            placeholder="如不填写，默认线下协商"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="buyForm.remark"
            type="textarea"
            :rows="2"
            placeholder="补充说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBuy = false">取消</el-button>
        <el-button type="primary" @click="confirmBuy">确认下单</el-button>
      </template>
    </el-dialog>

    <!-- 交换对话框 -->
    <el-dialog
      v-model="showExchange"
      title="申请交换"
      width="600px"
      @open="loadMyProducts"
    >
      <el-form label-width="100px">
        <el-form-item label="选择交换物">
          <el-select
            v-model="exchangeForm.offerProductId"
            placeholder="请选择您已发布的商品作为交换物"
            clearable
            style="width: 100%"
            @change="onOfferProductChange"
          >
            <el-option
              v-for="p in myProducts"
              :key="p.id"
              :value="p.id"
              :label="p.title"
            >
              <div style="display: flex; align-items: center; gap: 8px">
                <el-image
                  :src="imgUrl(p.coverImg)"
                  style="
                    width: 36px;
                    height: 36px;
                    border-radius: 4px;
                    flex-shrink: 0;
                  "
                  fit="cover"
                />
                <div style="flex: 1; overflow: hidden">
                  <div
                    style="
                      font-size: 14px;
                      white-space: nowrap;
                      overflow: hidden;
                      text-overflow: ellipsis;
                    "
                  >
                    {{ p.title }}
                  </div>
                  <div style="font-size: 12px; color: #999">
                    {{ p.categoryName || "未分类" }} | 估价 ¥{{
                      p.price || "面议"
                    }}
                  </div>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <template v-if="selectedOfferProduct">
          <el-form-item label="交换物信息">
            <div class="offer-product-info">
              <el-image
                :src="imgUrl(selectedOfferProduct.coverImg)"
                style="
                  width: 80px;
                  height: 80px;
                  border-radius: 6px;
                  flex-shrink: 0;
                "
                fit="cover"
              />
              <div class="offer-product-detail">
                <div class="offer-name">{{ selectedOfferProduct.title }}</div>
                <div class="offer-meta">
                  分类：{{ selectedOfferProduct.categoryName || "未分类" }}
                </div>
                <div class="offer-meta">
                  估价：<span style="color: #e74c3c; font-weight: 600"
                    >¥{{ selectedOfferProduct.price || "面议" }}</span
                  >
                </div>
                <div class="offer-meta">
                  成色：{{ selectedOfferProduct.quality || "-" }} / 10
                </div>
              </div>
            </div>
          </el-form-item>
        </template>
        <el-form-item label="补充描述">
          <el-input
            v-model="exchangeForm.offerDesc"
            type="textarea"
            :rows="2"
            placeholder="可补充交换物品的详细说明（选填）"
          />
        </el-form-item>
        <el-form-item label="沟通说明">
          <el-input
            v-model="exchangeForm.message"
            type="textarea"
            :rows="2"
            placeholder="给物主留言（选填）"
          />
        </el-form-item>
        <el-alert
          v-if="myProducts.length === 0 && myProductsLoaded"
          type="warning"
          :closable="false"
          style="margin-bottom: 10px"
        >
          您还没有已上架的商品，请先去发布商品后再来申请交换
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showExchange = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitExchange"
          :disabled="!exchangeForm.offerProductId"
          >提交申请</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { getProductDetail, toggleFavorite } from "@/api/product";
import { getComments, addComment, toggleLike } from "@/api/comment";
import { applyExchange, getMyExchangeProducts } from "@/api/exchange";
import { createOrder } from "@/api/order";
import { getUserInfo, getAddressList } from "@/api/user";
import { ElMessage } from "element-plus";
import { resolveImageUrl } from "@/utils/image";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const loading = ref(true);
const product = ref(null);
const comments = ref([]);
const commentTotal = ref(0);
const showExchange = ref(false);

const commentForm = reactive({ content: "", rating: 5 });
const exchangeForm = reactive({
  offerProductId: null,
  offerDesc: "",
  message: "",
});
const myProducts = ref([]);
const myProductsLoaded = ref(false);
const selectedOfferProduct = ref(null);
const showBuy = ref(false);
const buyFormRef = ref();
const profileInfo = ref(null);
const addressList = ref([]);
const buyForm = reactive({
  deliveryType: 2,
  contactInfo: "",
  addressId: null,
  pickupLocation: "",
  remark: "",
});
const buyRules = {
  deliveryType: [
    { required: true, message: "请选择交付方式", trigger: "change" },
  ],
  contactInfo: [
    { required: true, message: "请填写联系电话", trigger: "blur" },
  ],
  addressId: [
    {
      validator: (rule, value, cb) => {
        if (buyForm.deliveryType === 2 && !value)
          cb(new Error("请选择收货地址"));
        else cb();
      },
      trigger: "change",
    },
  ],
};

function imgUrl(url) {
  return resolveImageUrl(url);
}

async function loadProduct() {
  loading.value = true;
  try {
    const res = await getProductDetail(route.params.id);
    product.value = res.data;
  } finally {
    loading.value = false;
  }
}

async function loadComments() {
  const res = await getComments(route.params.id, { pageNum: 1, pageSize: 50 });
  comments.value = res.data.records;
  commentTotal.value = res.data.total;
}

async function handleFavorite() {
  await toggleFavorite(product.value.id);
  product.value.isFavorited = !product.value.isFavorited;
  product.value.favoriteCount += product.value.isFavorited ? 1 : -1;
  ElMessage.success(product.value.isFavorited ? "已收藏" : "已取消收藏");
}

async function loadBuyProfileData() {
  const [userRes, addressRes] = await Promise.all([
    getUserInfo(),
    getAddressList(),
  ]);
  profileInfo.value = userRes.data || {};
  addressList.value = addressRes.data || [];
  const defaultAddr =
    addressList.value.find((a) => a.isDefault === 1) || addressList.value[0];
  buyForm.addressId = defaultAddr ? defaultAddr.id : null;
  buyForm.deliveryType = defaultAddr ? 2 : 1;
  buyForm.contactInfo = profileInfo.value?.phone || defaultAddr?.phone || "";
}

function handleBuy() {
  if (Number(product.value?.userId) === Number(userStore.userId)) {
    ElMessage.warning("不能购买自己发布的商品");
    return;
  }
  loadBuyProfileData().catch(() => {
    profileInfo.value = {};
    addressList.value = [];
    buyForm.addressId = null;
    buyForm.deliveryType = 1;
    buyForm.contactInfo = "";
  });
  buyForm.pickupLocation = "";
  buyForm.remark = "";
  showBuy.value = true;
}

async function confirmBuy() {
  await buyFormRef.value.validate();
  if (Number(product.value?.userId) === Number(userStore.userId)) {
    ElMessage.warning("不能购买自己发布的商品");
    return;
  }
  const res = await createOrder({
    productId: product.value.id,
    deliveryType: buyForm.deliveryType,
    addressId: buyForm.deliveryType === 2 ? buyForm.addressId : null,
    contactInfo: buyForm.contactInfo,
    pickupLocation: buyForm.pickupLocation,
    remark: buyForm.remark,
  });
  showBuy.value = false;
  ElMessage.success("下单成功");
  router.push("/my-orders");
}

async function loadMyProducts() {
  try {
    const res = await getMyExchangeProducts();
    myProducts.value = res.data || [];
  } catch (e) {
    myProducts.value = [];
  }
  myProductsLoaded.value = true;
  exchangeForm.offerProductId = null;
  exchangeForm.offerDesc = "";
  exchangeForm.message = "";
  selectedOfferProduct.value = null;
}

function onOfferProductChange(id) {
  selectedOfferProduct.value = id
    ? myProducts.value.find((p) => p.id === id)
    : null;
}

async function submitExchange() {
  if (!exchangeForm.offerProductId) {
    return ElMessage.warning("请选择一个交换物品");
  }
  await applyExchange({
    productId: product.value.id,
    offerProductId: exchangeForm.offerProductId,
    offerDesc: exchangeForm.offerDesc,
    message: exchangeForm.message,
  });
  showExchange.value = false;
  ElMessage.success("交换申请已提交");
}

async function submitComment() {
  if (!commentForm.content.trim()) return ElMessage.warning("请输入评论内容");
  await addComment({ productId: Number(route.params.id), ...commentForm });
  commentForm.content = "";
  commentForm.rating = 5;
  ElMessage.success("评论已提交，等待审核");
  loadComments();
}

async function handleLike(commentId) {
  await toggleLike(commentId);
  loadComments();
}

onMounted(() => {
  loadProduct();
  loadComments();
});
</script>

<style lang="scss" scoped>
@use "@/styles/variables" as *;
.detail-main {
  display: flex;
  gap: 24px;
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 24px;
  box-shadow: $shadow-sm;
}
.detail-left {
  width: 480px;
  flex-shrink: 0;
}
.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: $bg-color;
}
.no-img {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: $text-placeholder;
  background: $bg-color;
}
.detail-right {
  flex: 1;
}
.detail-title {
  font-size: 22px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 12px;
  line-height: 1.4;
}
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.detail-tag-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 16px;
  border-radius: 18px;
  font-size: 14px;
  background: #f0f2f5;
  color: $text-regular;
  border: 1px solid #e5e8ed;
  letter-spacing: 0.5px;
}
.detail-price {
  padding: 16px 0;
  border-top: 1px solid $border-light;
  border-bottom: 1px solid $border-light;
  margin-bottom: 16px;
  .label {
    color: $text-secondary;
    margin-right: 12px;
  }
  .value {
    font-size: 28px;
    font-weight: 700;
    color: #e74c3c;
    &.exchange {
      font-size: 16px;
      color: $success-color;
    }
  }
}
.detail-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
  .info-item {
    font-size: 14px;
    color: $text-regular;
    .label {
      color: $text-secondary;
      margin-right: 8px;
    }
  }
}
.detail-publisher {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-top: 1px solid $border-light;
  margin-bottom: 16px;
  .pub-name {
    font-weight: 500;
  }
}
.detail-actions {
  display: flex;
  gap: 12px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: $text-primary;
}
.description {
  color: $text-regular;
  line-height: 1.8;
  white-space: pre-wrap;
}
.comment-form {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid $border-light;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid $border-light;
  &:last-child {
    border-bottom: none;
  }
}
.comment-body {
  flex: 1;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.cname {
  font-weight: 500;
  font-size: 14px;
}
.ctime {
  font-size: 12px;
  color: $text-secondary;
  margin-left: auto;
}
.ccontent {
  color: $text-regular;
  line-height: 1.6;
  margin-bottom: 6px;
}
.comment-actions {
  .like-btn {
    cursor: pointer;
    font-size: 13px;
    color: $text-secondary;
    &:hover {
      color: $primary-color;
    }
  }
}
.replies {
  background: $bg-color;
  border-radius: $radius-sm;
  padding: 10px 12px;
  margin-top: 8px;
  font-size: 13px;
  color: $text-regular;
  .rname {
    font-weight: 500;
    color: $text-primary;
  }
  .reply-to {
    color: $text-secondary;
  }
  .reply-item {
    margin-bottom: 6px;
    &:last-child {
      margin-bottom: 0;
    }
  }
}
.offer-product-info {
  display: flex;
  gap: 14px;
  padding: 12px;
  background: #f8f9fb;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.offer-product-detail {
  flex: 1;
  .offer-name {
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 6px;
  }
  .offer-meta {
    font-size: 13px;
    color: $text-secondary;
    margin-bottom: 3px;
  }
}
</style>
