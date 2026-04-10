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
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/product/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'publish',
        name: 'ProductPublish',
        component: () => import('@/views/product/ProductPublish.vue'),
        meta: { title: '发布商品', requireAuth: true }
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('@/views/user/UserCenter.vue'),
        meta: { title: '个人中心', requireAuth: true }
      },
      {
        path: 'my-products',
        name: 'MyProducts',
        component: () => import('@/views/user/MyProducts.vue'),
        meta: { title: '我的发布', requireAuth: true }
      },
      {
        path: 'my-orders',
        name: 'MyOrders',
        component: () => import('@/views/user/MyOrders.vue'),
        meta: { title: '我的订单', requireAuth: true }
      },
      {
        path: 'my-exchanges',
        name: 'MyExchanges',
        component: () => import('@/views/user/MyExchanges.vue'),
        meta: { title: '我的交换', requireAuth: true }
      },
      {
        path: 'my-favorites',
        name: 'MyFavorites',
        component: () => import('@/views/user/MyFavorites.vue'),
        meta: { title: '我的收藏', requireAuth: true }
      },
      {
        path: 'wallet',
        name: 'Wallet',
        component: () => import('@/views/user/Wallet.vue'),
        meta: { title: '我的钱包', requireAuth: true }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理首页' }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/ProductAudit.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('@/views/admin/CommentManage.vue'),
        meta: { title: '评论管理' }
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
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.requireAdmin && !userStore.isAdmin) {
    next('/')
  } else {
    next()
  }
})

export default router
