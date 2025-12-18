<!-- AdminLayout.vue -->
<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="admin-sidebar">
        <el-menu
          :default-active="activeIndex"
          class="admin-menu"
          router
          unique-opened
        >
          <el-menu-item index="/admin/products">
            <i class="el-icon-goods"></i>
            <span slot="title">商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/seckill">
            <i class="el-icon-time"></i>
            <span slot="title">秒杀管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'AdminLayout',
  data() {
    return {
      activeIndex: '/admin/products'
    };
  },
  watch: {
    $route() {
      this.activeIndex = this.$route.path;
    }
  }
};
</script>

<style scoped>
.admin-layout {
  height: calc(100vh - 140px);
}

.admin-sidebar {
  background-color: #304156;
  color: white;
}

.admin-menu {
  height: 100%;
  border-right: none;
}

.admin-main {
  padding: 20px;
  background-color: #f5f7fa;
}
</style>