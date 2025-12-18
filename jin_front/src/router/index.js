import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Register from "@/views/Register.vue";
import Login from "@/views/Login.vue";
import ProductDetail from "@/views/ProductDetail.vue";

Vue.use(VueRouter)

const routes = [
  {
    path: "/",
    name: "Home",
    component: Home,
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/product/:id",
    name: "ProductDetail",
    component: ProductDetail,
  },
  {
    path: "/products",
    name: "Products",
    component: () => import("@/views/Products.vue"),
  },
  {
    path: "/user",
    name: "UserCenter",
    component: () => import("@/views/UserCenter.vue"),
    children: [
      {
        path: "profile",
        name: "UserProfile",
        component: () => import("@/views/UserProfile.vue"),
      },
      {
        path: "orders",
        name: "UserOrders",
        component: () => import("@/views/UserOrders.vue"),
      },
      {
        path: "seckill-records",
        name: "SeckillRecords",
        component: () => import("@/views/SeckillRecords.vue"),
      },
    ],
  },
  {
    path: "/cart",
    name: "Cart",
    component: () => import("@/views/Cart.vue"),
  },
  // 管理员相关路由
  {
    path: "/admin",
    name: "Admin",
    component: () => import("@/views/admin/AdminLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      {
        path: "products",
        name: "AdminProducts",
        component: () => import("@/views/admin/AdminProducts.vue"),
        meta: { requiresAdmin: true }
      },
      {
        path: "seckill",
        name: "AdminSeckill",
        component: () => import("@/views/admin/AdminSeckill.vue"),
        meta: { requiresAdmin: true }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  
  // 需要登录的路由
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      next({ path: '/login' })
    } else {
      // 需要管理员权限的路由
      if (to.matched.some(record => record.meta.requiresAdmin)) {
        if (role !== 'ROLE_ADMIN') {
          next({ path: '/' })
        } else {
          next()
        }
      } else {
        next()
      }
    }
  } else {
    next()
  }
})

export default router
