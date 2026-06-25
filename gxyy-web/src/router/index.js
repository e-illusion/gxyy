import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
  },
  {
    path: '/item/create',
    name: 'ItemCreate',
    component: () => import('../views/item/Create.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/item/:id',
    name: 'ItemDetail',
    component: () => import('../views/item/Detail.vue'),
  },
  {
    path: '/item/:id/edit',
    name: 'ItemEdit',
    component: () => import('../views/item/Edit.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/my-items',
    name: 'MyItems',
    component: () => import('../views/item/MyItems.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/item/Favorites.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/exchange-requests',
    name: 'ExchangeRequests',
    component: () => import('../views/exchange/Requests.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/chat/:id',
    name: 'Chat',
    component: () => import('../views/exchange/Chat.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/user/:id',
    name: 'UserProfile',
    component: () => import('../views/user/UserProfile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('../views/user/Notifications.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/user/Profile.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫 - 未登录跳转
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
