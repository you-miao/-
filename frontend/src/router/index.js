import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { title: '注册' }
  },
  // ================= 前台布局 (C端：普通用户) =================
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/home/Home.vue'), meta: { title: '首页' } },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('@/views/product/ProductDetail.vue'), meta: { title: '商品详情' } },
      { path: 'publish', name: 'ProductPublish', component: () => import('@/views/product/ProductPublish.vue'), meta: { title: '发布商品', requireAuth: true } },
      { path: 'user', name: 'UserCenter', component: () => import('@/views/user/UserCenter.vue'), meta: { title: '个人中心', requireAuth: true } },
      { path: 'my-products', name: 'MyProducts', component: () => import('@/views/user/MyProducts.vue'), meta: { title: '我的发布', requireAuth: true } },
      { path: 'my-orders', name: 'MyOrders', component: () => import('@/views/user/MyOrders.vue'), meta: { title: '我的订单', requireAuth: true } },
      { path: 'my-exchanges', name: 'MyExchanges', component: () => import('@/views/user/MyExchanges.vue'), meta: { title: '我的交换', requireAuth: true } },
      { path: 'my-favorites', name: 'MyFavorites', component: () => import('@/views/user/MyFavorites.vue'), meta: { title: '我的收藏', requireAuth: true } },
      { path: 'wallet', name: 'Wallet', component: () => import('@/views/user/Wallet.vue'), meta: { title: '我的钱包', requireAuth: true } },
      
      // --- 爱心公益模块 (大厅与个人操作) ---
      {
        path: 'donation/hall',
        name: 'DonationHall',
        component: () => import('@/views/donation/DonationHall.vue'),
        meta: { title: '募捐大厅' } 
      },
      {
        path: 'donation/submit',
        name: 'DonationSubmit',
        component: () => import('@/views/donation/DonationSubmit.vue'),
        meta: { title: '填写捐赠单', requireAuth: true }
      },
      {
        path: 'donation/my',
        name: 'MyDonations',
        component: () => import('@/views/donation/MyDonations.vue'),
        meta: { title: '我的捐赠', requireAuth: true }
      }
    ]
  },
  // ================= 后台布局 (B端：管理员/社团) =================
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requireAuth: true }, // 💡 移除了全局 requireAdmin，在下面子路由里分别控制
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '管理首页', requireAdmin: true } },
      { path: 'products', name: 'AdminProducts', component: () => import('@/views/admin/ProductAudit.vue'), meta: { title: '商品管理', requireAdmin: true } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理', requireAdmin: true } },
      { path: 'comments', name: 'AdminComments', component: () => import('@/views/admin/CommentManage.vue'), meta: { title: '评论管理', requireAdmin: true } },
      
      // --- 💡 社团工作台 (专属后台页面) ---
      {
        path: 'charity',
        name: 'CharityDashboard',
        component: () => import('@/views/donation/CharityDashboard.vue'),
        meta: { title: '社团工作台', requireCharity: true } 
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 校园闲置物交易平台` : '校园闲置物交易平台'
  const userStore = useUserStore()
  
  if (to.meta.requireAuth && !userStore.isLoggedIn) {
    // 拦截未登录用户
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.requireAdmin && !userStore.isAdmin) {
    // 拦截非管理员访问超级后台
    next('/')
  } else if (to.meta.requireCharity && userStore.userInfo?.role !== 'ROLE_CHARITY' && userStore.userInfo?.role !== 'ROLE_ADMIN' && !userStore.isAdmin) {
    // 拦截非社团（和非管理员）访问社团工作台
    // 💡 考虑到你现有的逻辑，如果是 Admin 也可以进社团后台
    next('/')
  } else {
    // 放行
    next()
  }
})

export default router