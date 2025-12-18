<!-- src/views/UserCenter.vue -->
<template>
  <div class="user-center">
    <div class="section-header">
      <h2>用户中心</h2>
    </div>

    <el-row :gutter="20">
      <!-- 侧边栏 -->
      <el-col :xs="24" :md="6">
        <el-card class="sidebar" shadow="hover">
          <el-menu 
            :default-active="activeMenu"
            class="user-menu"
            router
            :unique-opened="true"
          >
            <el-submenu index="profile">
              <template slot="title">
                <i class="el-icon-user"></i>
                <span>个人信息</span>
              </template>
              <el-menu-item index="/user/profile">
                <i class="el-icon-info"></i>
                <span slot="title">基本信息</span>
              </el-menu-item>
            </el-submenu>
            <el-submenu index="orders">
              <template slot="title">
                <i class="el-icon-document"></i>
                <span>订单管理</span>
              </template>
              <el-menu-item index="/user/orders">
                <i class="el-icon-list"></i>
                <span slot="title">我的订单</span>
              </el-menu-item>
            </el-submenu>
            <el-submenu index="seckill">
              <template slot="title">
                <i class="el-icon-goods"></i>
                <span>秒杀记录</span>
              </template>
              <el-menu-item index="/user/seckill-records">
                <i class="el-icon-time"></i>
                <span slot="title">秒杀记录</span>
              </el-menu-item>
            </el-submenu>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 主内容区 -->
      <el-col :xs="24" :md="18">
        <el-card class="content" shadow="hover">
          <router-view />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'UserCenter',
  data() {
    return {
      activeMenu: ''
    }
  },
  watch: {
    $route: {
      handler() {
        // 根据当前路由设置激活菜单
        const path = this.$route.path
        if (path.includes('profile')) {
          this.activeMenu = 'profile'
        } else if (path.includes('orders')) {
          this.activeMenu = 'orders'
        } else if (path.includes('seckill-records')) {
          this.activeMenu = 'seckill'
        }
      },
      immediate: true
    }
  }
}
</script>

<style scoped>
.user-center {
  margin-bottom: 40px;
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e74c3c;
}

.section-header h2 {
  color: #333;
  margin: 0;
  font-size: 24px;
}

.sidebar {
  height: fit-content;
}

.user-menu {
  border-right: none;
}

.content {
  min-height: 500px;
}
</style>