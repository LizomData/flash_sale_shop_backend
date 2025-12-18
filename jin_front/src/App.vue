<template>
  <div id="app">
    <!-- 导航栏 -->
    <Navigation />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <div class="container">
        <router-view />
      </div>
    </div>
    
    <!-- 页脚 -->
    <Footer />
  </div>
</template>

<script>
import Navigation from './components/Navigation.vue'
import Footer from './components/Footer.vue'

export default {
  name: 'App',
  components: {
    Navigation,
    Footer
  }
}
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f5f7fa;
  color: #333;
  line-height: 1.6;
}

/* 链接样式 */
a {
  color: #409eff;
  text-decoration: none;
}

a:hover {
  color: #66b1ff;
}

/* 容器样式 */
.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* App容器样式 */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  padding: 20px 0;
  background-color: #f5f7fa;
}

/* 防止内容区域过窄 */
@media (max-width: 1200px) {
  .container {
    width: 100%;
  }
}
</style>
