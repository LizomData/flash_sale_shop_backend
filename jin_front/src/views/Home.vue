<template>
  <div class="home">
    <!-- 轮播图 -->
    <el-carousel :interval="3000" arrow="always" height="400px" class="home-carousel">
      <el-carousel-item v-for="item in carouselItems" :key="item.id">
        <img :src="item.image" alt="轮播图" class="carousel-image">
        <div class="carousel-content">
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 秒杀活动区 -->
    <div class="seckill-section">
      <div class="section-header">
        <h2>限时秒杀</h2>
<!-- 秒杀倒计时 -->
      <div class="countdown">
        <span class="countdown-label">距结束还有：</span>
        <span class="countdown-time">{{ countdownTime }}</span>
      </div>
      </div>

      <!-- 秒杀商品列表 -->
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in seckillProducts" :key="product.id">
          <el-card class="product-card" shadow="hover">
            <router-link :to="`/product/${product.id}`" class="product-link">
              <div class="product-image">
                <img :src="product.image" :alt="product.name">
                <div class="seckill-tag">秒杀</div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <div class="product-price">
                  <span class="seckill-price">¥{{ product.seckillPrice }}</span>
                  <span class="original-price">¥{{ product.originalPrice }}</span>
                </div>
                <!-- 商品级别的秒杀倒计时 -->
                <div class="seckill-product-countdown" v-if="product.countdownTime">
                  <span class="countdown-label">距结束：</span>
                  <span class="countdown-time">{{ product.countdownTime }}</span>
                </div>
                <div class="product-stock">
                  <el-progress 
                    :percentage="(product.stock / product.totalStock) * 100" 
                    :stroke-width="8" 
                    :show-text="false"
                    status="success"
                  ></el-progress>
                  <span>{{ product.stock }}/{{ product.totalStock }}件</span>
                </div>
                <el-button 
                  type="danger" 
                  size="small" 
                  class="seckill-btn"
                  :disabled="product.stock <= 0"
                  @click.stop="handleSeckill(product.id)"
                >
                  {{ product.stock > 0 ? '立即秒杀' : '已抢光' }}
                </el-button>
              </div>
            </router-link>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 热门商品区 -->
    <div class="hot-section">
      <div class="section-header">
        <h2>热门商品</h2>
        <router-link to="/products" class="more-link">查看更多 ></router-link>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in hotProducts" :key="product.id">
          <el-card class="product-card" shadow="hover">
            <router-link :to="`/product/${product.id}`" class="product-link">
              <div class="product-image">
                <img :src="product.image" :alt="product.name">
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <div class="product-price">
                  <span class="normal-price">¥{{ product.originalPrice }}</span>
                </div>
              </div>
            </router-link>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { getSeckillProducts, getProducts } from '@/api/product';

export default {
  name: "Home",
  data() {
    return {
      carouselItems: [
        {
          id: 1,
          image: require('@/assets/logo.png'),
          title: '限时秒杀活动',
          description: '全场商品低至1折起，限时抢购'
        },
        {
          id: 2,
          image: require('@/assets/logo.png'),
          title: '新品上市',
          description: '最新款商品，限时特价'
        },
        {
          id: 3,
          image: require('@/assets/logo.png'),
          title: '会员专享',
          description: '会员享受额外优惠，不容错过'
        }
      ],
      endTime: new Date().getTime() + 3600000 * 24, // 24小时后结束
      countdownTime: '',
      seckillProducts: [],
      hotProducts: []
    };
  },
  mounted() {
    this.fetchSeckillProducts();
    this.fetchHotProducts();
    this.startCountdown();
  },
  beforeDestroy() {
    // 清理计时器
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
    }
  },
  methods: {
    async fetchSeckillProducts() {
      try {
        const response = await getSeckillProducts();
        
        console.log('获取秒杀商品响应:', response);
        
        // 为每个秒杀商品添加倒计时相关属性
        let seckillList = [];
        
        // 处理不同的数据结构
        if (response.list) {
          // 情况1：响应有list属性，直接使用
          seckillList = response.list;
        } else if (Array.isArray(response)) {
          // 情况2：响应本身是数组
          seckillList = response;
        } else {
          // 情况3：响应是包含数字索引的对象（如控制台输出的格式）
          // 使用Object.values获取所有值，然后过滤掉非商品对象
          seckillList = Object.values(response).filter(item => 
            typeof item === 'object' && item !== null && typeof item.id === 'number'
          );
        }
        
        console.log('提取的秒杀商品列表:', seckillList);
        
        // 确保是数组
        if (!Array.isArray(seckillList)) {
          seckillList = [];
        }
        
        // 为每个商品添加倒计时属性
        this.seckillProducts = seckillList.map(product => {
          return {
            ...product,
            countdownTime: '',
            seckillEndTime: new Date(product.seckillEndTime).getTime()
          };
        });

        console.log('处理后的秒杀商品列表:', this.seckillProducts);
        
        // 启动所有商品的倒计时
        this.startAllCountdowns();
      } catch (error) {
        console.error('获取秒杀商品错误:', error);
        this.$message.error('获取秒杀商品失败：' + error.message);
      }
    },
    async fetchHotProducts() {
      try {
        const response = await getProducts();
        // 过滤掉秒杀商品（seckillPrice不为null的是秒杀商品）
        this.hotProducts = response.list.filter(product => product.seckillPrice === null);
      } catch (error) {
        this.$message.error('获取热门商品失败：' + error.message);
      }
    },
    handleSeckill(productId) {
      // 检查是否登录
      const token = localStorage.getItem('token');
      if (!token) {
        this.$message.warning('请先登录');
        this.$router.push('/login');
        return;
      }
      // 执行秒杀
      // 这里可以添加秒杀逻辑
      this.$router.push(`/product/${productId}`);
    },
    // 开始所有商品的倒计时
    startAllCountdowns() {
      // 清理之前的计时器
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer);
      }
      
      // 更新所有商品的倒计时
      this.seckillProducts.forEach(product => {
        this.updateProductCountdown(product);
      });
      
      // 设置全局计时器，每秒更新一次
      this.countdownTimer = setInterval(() => {
        this.seckillProducts.forEach(product => {
          this.updateProductCountdown(product);
        });
      }, 1000);
    },
    // 更新单个商品的倒计时
    updateProductCountdown(product) {
      const now = new Date().getTime();
      const distance = product.seckillEndTime - now;
      
      if (distance < 0) {
        product.countdownTime = '秒杀已结束';
        return;
      }
      
      // 计算天、时、分、秒
      const days = Math.floor(distance / (1000 * 60 * 60 * 24));
      const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);
      
      // 格式化时间
      product.countdownTime = `${days}天${hours}时${minutes}分${seconds}秒`;
    }
  }
};
</script>

<style scoped>
.home {
  margin-bottom: 40px;
}

/* 轮播图样式 */
.home-carousel {
  margin-bottom: 30px;
}

.carousel-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.carousel-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.carousel-content h3 {
  font-size: 36px;
  margin-bottom: 10px;
}

.carousel-content p {
  font-size: 18px;
}

/* 秒杀活动区样式 */
.seckill-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e74c3c;
}

.section-header h2 {
  color: #333;
  margin: 0;
  font-size: 24px;
}

.countdown {
  display: flex;
  align-items: center;
}

.countdown-label {
  margin-right: 10px;
  color: #666;
}

/* 商品卡片样式 */
.product-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-link {
  color: #333;
  text-decoration: none;
  display: block;
}

.product-link:hover {
  color: #333;
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.seckill-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background-color: #e74c3c;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.product-info {
  padding: 15px;
}

.product-name {
  font-size: 16px;
  margin-bottom: 10px;
  height: 48px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin-bottom: 10px;
  display: flex;
  align-items: baseline;
}

.seckill-price {
  color: #e74c3c;
  font-size: 20px;
  font-weight: bold;
  margin-right: 10px;
}

.normal-price {
  color: #e74c3c;
  font-size: 20px;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
}

/* 商品级别的秒杀倒计时样式 */
.seckill-product-countdown {
  margin-bottom: 10px;
  padding: 5px 0;
  color: #e74c3c;
  font-size: 14px;
  display: flex;
  align-items: center;
  background-color: #fef0f0;
  padding: 5px 10px;
  border-radius: 4px;
}

.seckill-product-countdown .countdown-label {
  margin-right: 5px;
  font-weight: bold;
}

.product-stock {
  margin-bottom: 15px;
}

.product-stock span {
  display: block;
  text-align: right;
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

.seckill-btn {
  width: 100%;
}

/* 热门商品区样式 */
.hot-section {
  margin-bottom: 40px;
}

.more-link {
  color: #409eff;
  font-size: 14px;
}

.more-link:hover {
  color: #66b1ff;
}
</style>
