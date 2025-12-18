<template>
  <el-footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-info">
          <h3>在线秒杀系统</h3>
          <p>专业的在线秒杀平台，为您提供优质的商品和服务</p>
        </div>
        <div class="footer-links">
          <h4>快速链接</h4>
          <ul>
            <li><a href="/">首页</a></li>
            <li><a href="/products">秒杀商品</a></li>
            <li><a href="/about">关于我们</a></li>
            <li><a href="/contact">联系我们</a></li>
          </ul>
        </div>
        <div class="footer-contact">
          <h4>联系我们</h4>
          <p>邮箱：contact@seckill.com</p>
          <p>电话：400-123-4567</p>
          <p>地址：北京市朝阳区科技园</p>
        </div>
      </div>
      <div class="footer-copyright">
        <p>© 2024 在线秒杀系统 版权所有</p>
      </div>
    </div>
  </el-footer>
</template>

<script>
export default {
  name: 'Footer'
}
</script>

<style scoped>
.footer {
  background-color: #333;
  color: #fff;
  padding: 30px 0;
  margin-top: auto;
}

.container {
  width: 1200px;
  margin: 0 auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.footer-info,
.footer-links,
.footer-contact {
  flex: 1;
  padding: 0 20px;
}

.footer-info h3 {
  margin-bottom: 10px;
  color: #e74c3c;
}

.footer-links h4,
.footer-contact h4 {
  margin-bottom: 15px;
  color: #e74c3c;
}

.footer-links ul {
  list-style: none;
  padding: 0;
}

.footer-links li {
  margin-bottom: 10px;
}

.footer-links a {
  color: #ccc;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #fff;
}

.footer-contact p {
  margin-bottom: 10px;
  color: #ccc;
}

.footer-copyright {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #444;
  color: #666;
  font-size: 14px;
}
</style>