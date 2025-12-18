<template>
  <el-header class="header">
    <div class="container">
      <div class="logo">
        <router-link to="/">在线秒杀系统</router-link>
      </div>
      
      <el-menu 
        :default-active="activeIndex" 
        class="nav-menu"
        mode="horizontal"
        @select="handleSelect"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/products">秒杀商品</el-menu-item>
        <el-menu-item index="/user/orders">我的订单</el-menu-item>
        <el-menu-item index="/cart">购物车</el-menu-item>
      </el-menu>
      
      <div class="user-info">
        <template v-if="isLoggedIn">
          <el-dropdown>
            <span class="el-dropdown-link">
              {{ username }} <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <!-- 普通用户菜单 -->
              <el-dropdown-item v-if="!isAdmin" ><router-link to="/user/profile">个人中心</router-link></el-dropdown-item>
              <el-dropdown-item  v-if="!isAdmin" ><router-link to="/user/orders">我的订单</router-link></el-dropdown-item>
              <el-dropdown-item  v-if="!isAdmin"  ><router-link to="/user/seckill-records">秒杀记录</router-link></el-dropdown-item>
              <!-- 管理员菜单 -->
              <el-dropdown-item v-if="isAdmin" divided><router-link to="/admin/products">商品管理</router-link></el-dropdown-item>
              <el-dropdown-item v-if="isAdmin"><router-link to="/admin/seckill">秒杀管理</router-link></el-dropdown-item>
              <!-- 公共菜单 -->
              <el-dropdown-item divided @click.native="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="text" @click="goToLogin">登录</el-button>
          <el-button type="primary" @click="goToRegister">注册</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script>
export default {
  name: 'Navigation',
  data() {
    return {
      activeIndex: '/',
      isLoggedIn: false,
      username: ''
    }
  },
  mounted() {
    this.checkLoginStatus()
  },
  watch: {
    $route: {
      handler() {
        this.activeIndex = this.$route.path
        this.checkLoginStatus()
      },
      immediate: true
    }
  },
  computed: {
    isAdmin() {
      return localStorage.getItem('role') === 'ROLE_ADMIN'
    }
  },
  methods: {
    handleSelect(key) {
      if (this.$route.path !== key) {
        this.$router.push(key)
      }
    },
    checkLoginStatus() {
      // 检查localStorage中是否有token
      const token = localStorage.getItem('token')
      this.isLoggedIn = !!token
      // 从localStorage获取用户名
      this.username = localStorage.getItem('username') || '用户'
      
    },
    goToLogin() {
      if (this.$route.path !== '/login') {
        this.$router.push('/login')
      }
    },
    goToRegister() {
      if (this.$route.path !== '/register') {
        this.$router.push('/register')
      }
    },
    async handleLogout() { //退出登录
      try {
        // 清除本地存储
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role') // 清除角色信息
        // 更新登录状态
        this.isLoggedIn = false


        if (this.$route.path !== '/') {
          this.$router.push('/')
        } else {

        }

        this.$message.success('退出登录成功')
      } catch (error) {
        this.$message.error('退出登录失败')
      }
    }
  }
}
</script>

<style scoped>
.header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 60px;
  line-height: 60px;
}

.container {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #e74c3c;
}

.logo a {
  color: #e74c3c;
  text-decoration: none;
}

.nav-menu {
  flex: 1;
  justify-content: center;
  border-bottom: none;
  background-color: transparent;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-info .el-button {
  margin-left: 10px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #333;
  display: inline-block;
  padding: 0 10px;
}

.el-dropdown-link:hover {
  color: #e74c3c;
}
</style>