<!-- src/views/ProductDetail.vue -->
<template>
  <div class="product-detail">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item><router-link to="/">首页</router-link></el-breadcrumb-item>
      <el-breadcrumb-item><router-link to="/products">秒杀商品</router-link></el-breadcrumb-item>
      <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-row :gutter="20">
      <!-- 商品图片区 -->
      <el-col :xs="24" :md="12">
        <el-carousel height="400px" class="product-images">
          <el-carousel-item v-for="(image, index) in product.images" :key="index">
            <img :src="image" :alt="product.name" class="product-image">
          </el-carousel-item>
        </el-carousel>
      </el-col>

      <!-- 商品信息区 -->
      <el-col :xs="24" :md="12">
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <div class="product-prices">
            <div class="price-item">
              <span class="price-label">秒杀价：</span>
              <span class="seckill-price">¥{{ product.seckillPrice }}</span>
            </div>
            <div class="price-item">
              <span class="price-label">原价：</span>
              <span class="original-price">¥{{ product.originalPrice }}</span>
            </div>
          </div>

          <!-- 秒杀倒计时 -->
          <div class="seckill-countdown" v-if="isSeckillActive">
            <span class="countdown-label">距结束还有：</span>
            <span class="countdown-time">{{ countdownTime }}</span>
          </div>

          <!-- 商品库存 -->
          <div class="product-stock">
            <span class="stock-label">库存：</span>
            <span :class="{ 'stock-low': product.stock < 10 }">
              {{ product.stock }}/{{ product.totalStock }}件
            </span>
            <el-progress 
              :percentage="(product.stock / product.totalStock) * 100" 
              :stroke-width="10" 
              status="success"
              style="margin-top: 10px;"
            ></el-progress>
          </div>

          <!-- 商品销量 -->
          <div class="product-sales">
            <span class="sales-label">已售：</span>
            <span class="sales-value">{{ product.sold }}</span>
          </div>

          <!-- 购买数量 -->
          <div class="buy-quantity">
            <span class="quantity-label">数量：</span>
            <el-input-number 
              v-model="buyQuantity" 
              :min="1" 
              :max="product.stock" 
              :step="1"
              size="large"
            ></el-input-number>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <!-- 秒杀商品 -->
            <template v-if="product.seckillPrice !== null">
              <el-button 
                type="danger" 
                size="large" 
                class="seckill-btn"
                :disabled="!isSeckillActive || product.stock <= 0"
                @click="handleSeckill"
                :loading="seckillLoading"
              >
                {{ seckillLoading ? '秒杀中...' : (isSeckillActive ? (product.stock > 0 ? '立即秒杀' : '已抢光') : '秒杀尚未开始') }}
              </el-button>
            </template>
            <!-- 普通商品 -->
            <template v-else>
              <el-button 
                type="danger" 
                size="large" 
                class="buy-btn"
                @click="handleBuy"
                :loading="buyLoading"
              >
                {{ buyLoading ? '购买中...' : '立即购买' }}
              </el-button>
              <el-button 
                type="primary" 
                size="large" 
                class="add-cart-btn"
                @click="handleAddCart"
                :loading="cartLoading"
              >
                {{ cartLoading ? '添加中...' : '加入购物车' }}
              </el-button>
            </template>
          </div>

          <!-- 商品标签 -->
          <div class="product-tags">
            <el-tag type="danger" v-if="isSeckillActive">秒杀商品</el-tag>
            <el-tag type="success" v-if="product.stock > 50">库存充足</el-tag>
            <el-tag type="warning" v-else-if="product.stock > 0">库存紧张</el-tag>
            <el-tag type="info">正品保障</el-tag>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 商品详情 -->
    <el-tabs v-model="activeTab" class="product-tabs">
      <el-tab-pane label="商品详情" name="detail">
        <div class="detail-content" v-html="product.detail"></div>
      </el-tab-pane>
      <el-tab-pane label="规格参数" name="specs">
        <el-table :data="product.specs" border>
          <el-table-column prop="name" label="参数名称" width="150"></el-table-column>
          <el-table-column prop="value" label="参数值"></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="用户评价" name="comments">
        <div class="comments-content">
          <el-empty description="暂无评价"></el-empty>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 进度弹窗 -->
    <el-dialog
      title="处理中"
      :visible.sync="progressDialogVisible"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      show-close="false"
    >
      <div class="progress-content">
        <el-progress
          :percentage="progressPercentage"
          :stroke-width="20"
          :text-inside="true"
          status="success"
        ></el-progress>
        <div class="progress-text">{{ progressText }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProductDetail, seckillOrder, nomarlOrder } from '@/api/product';

export default {
  name: 'ProductDetail',
  data() {
    return {
      productId: this.$route.params.id,
      product: {
        id: '',
        name: '',
        images: [],
        seckillPrice: 0,
        originalPrice: 0,
        stock: 0,
        totalStock: 0,
        sold: 0,
        detail: '',
        specs: [],
        seckillStartTime: 0,
        seckillEndTime: 0
      },
      buyQuantity: 1,
      seckillEndTime: 0,
      isSeckillActive: true,
      activeTab: 'detail',
      seckillLoading: false,
      buyLoading: false,
      cartLoading: false,
      countdownTime: '',
      countdownTimer: null,
      // 进度弹窗相关
      progressDialogVisible: false,
      progressText: '',
      progressPercentage: 0
    };
  },
  mounted() {
    this.fetchProductDetail();
  },
  beforeDestroy() {
    // 清理计时器
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
    }
  },
  watch: {
    $route: {
      handler() {
        this.productId = this.$route.params.id;
        this.fetchProductDetail();
      },
      immediate: false
    },
    // 监听秒杀结束时间变化，重新开始倒计时
    seckillEndTime() {
      this.startCountdown();
    }
  },
  methods: {
    async fetchProductDetail() {
      try {
        const response = await getProductDetail(this.productId);
        this.product = response;
        this.seckillEndTime = new Date(this.product.seckillEndTime).getTime();
        this.isSeckillActive = this.checkSeckillStatus();
        this.startCountdown();
      } catch (error) {
        this.$message.error('获取商品详情失败：' + error.message);
      }
    },
    checkSeckillStatus() {
      const now = new Date().getTime();
      const startTime = new Date(this.product.seckillStartTime).getTime();
      const endTime = new Date(this.product.seckillEndTime).getTime();
      return now >= startTime && now <= endTime;
    },
    // 开始倒计时
    startCountdown() {
      // 清理之前的计时器
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer);
      }
      this.updateCountdown();
      this.countdownTimer = setInterval(() => {
        this.updateCountdown();
      }, 1000);
    },
    // 更新倒计时时间
    updateCountdown() {
      const now = new Date().getTime();
      const distance = this.seckillEndTime - now;
      
      if (distance < 0) {
        this.countdownTime = '秒杀已结束';
        this.isSeckillActive = false;
        clearInterval(this.countdownTimer);
        return;
      }
      
      // 计算天、时、分、秒
      const days = Math.floor(distance / (1000 * 60 * 60 * 24));
      const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);
      
      // 格式化时间
      this.countdownTime = `${days}天${hours}时${minutes}分${seconds}秒`;
    },
    // 更新进度弹窗
    updateProgress(text, percentage) {
      this.progressText = text;
      this.progressPercentage = percentage;
    },

    // 立即秒杀
    async handleSeckill() {
      // 检查是否登录
      const token = localStorage.getItem('token');
      if (!token) {
        this.$message.warning('请先登录');
        this.$router.push('/login');
        return;
      }

      if (!this.isSeckillActive) {
        this.$message.warning('秒杀活动尚未开始或已结束');
        return;
      }

      if (this.product.stock <= 0) {
        this.$message.warning('商品已售罄');
        return;
      }

      // 显示进度弹窗
      this.progressDialogVisible = true;
      this.updateProgress('正在提交秒杀请求...', 10);

      this.seckillLoading = true;
      try {
        // 模拟处理进度
        this.updateProgress('正在检查库存...', 30);
        await new Promise(resolve => setTimeout(resolve, 500));
        
        this.updateProgress('正在处理秒杀请求...', 50);
        // 发起真实的秒杀请求，添加日志
        console.log('发起秒杀请求，商品ID:', this.product.id, '数量:', this.buyQuantity);
        const response = await seckillOrder(this.product.id, this.buyQuantity);
        
        this.updateProgress('秒杀请求处理完成...', 80);
        await new Promise(resolve => setTimeout(resolve, 500));
        
        this.updateProgress('处理完成...', 100);
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 关闭进度弹窗
        this.progressDialogVisible = false;
        
        console.log('秒杀请求响应:', response);
        this.$message.success(response.message || '秒杀成功！');
        // 跳转到订单详情页
        this.$router.push('/user/orders');
      } catch (error) {
        // 关闭进度弹窗
        this.progressDialogVisible = false;
        console.error('秒杀请求错误:', error);
        // 显示更详细的错误信息
        let errorMsg = '系统错误';
        // 检查错误类型
        if (error.response) {
          // 服务器返回错误
          errorMsg = error.response.data.message || errorMsg;
        } else if (error.message) {
          // 前端创建的错误，直接使用message
          errorMsg = error.message;
        }
        this.$message.error('秒杀失败：' + errorMsg);
      } finally {
        this.seckillLoading = false;
      }
    },

    // 立即购买
    async handleBuy() {
      // 检查是否登录
      const token = localStorage.getItem('token');
      if (!token) {
        this.$message.warning('请先登录');
        this.$router.push('/login');
        return;
      }

      // 显示进度弹窗
      this.progressDialogVisible = true;
      this.updateProgress('正在处理购买请求...', 10);

      this.buyLoading = true;
      try {
        // 模拟处理进度
        this.updateProgress('正在检查库存...', 30);
        await new Promise(resolve => setTimeout(resolve, 500));
        
        this.updateProgress('正在生成订单...', 60);
        // 发起真实的购买请求
        const response = await nomarlOrder(this.product.id, this.buyQuantity);
        
        this.updateProgress('订单生成成功...', 100);
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 关闭进度弹窗
        this.progressDialogVisible = false;
        
        this.$message.success(response.message || '购买成功！');
        // 跳转到订单详情页
        this.$router.push('/user/orders');
      } catch (error) {
        // 关闭进度弹窗
        this.progressDialogVisible = false;
        this.$message.error('购买失败：' + (error.message || '系统错误'));
      } finally {
        this.buyLoading = false;
      }
    },
    // 加入购物车
    async handleAddCart() {
      // 检查是否登录
      const token = localStorage.getItem('token');
      if (!token) {
        this.$message.warning('请先登录');
        this.$router.push('/login');
        return;
      }

      // 显示进度弹窗
      this.progressDialogVisible = true;
      this.updateProgress('正在添加到购物车...', 10);

      this.cartLoading = true;
      try {
        // 模拟处理进度
        this.updateProgress('正在验证商品信息...', 50);
        await new Promise(resolve => setTimeout(resolve, 500));
        
        this.updateProgress('正在添加到购物车...', 100);
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 关闭进度弹窗
        this.progressDialogVisible = false;
        
        this.$message.success('已添加到购物车');
      } catch (error) {
        // 关闭进度弹窗
        this.progressDialogVisible = false;
        this.$message.error('添加到购物车失败：' + error.message);
      } finally {
        this.cartLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.product-detail {
  margin-bottom: 40px;
}

.breadcrumb {
  margin-bottom: 20px;
  background-color: transparent;
}

.product-images {
  margin-bottom: 20px;
}

.product-image {
  width: 100%;
  height: 400px;
  object-fit: contain;
}

.product-info {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.product-name {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.product-prices {
  margin-bottom: 20px;
}

.price-item {
  margin-bottom: 10px;
}

.price-label {
  font-size: 16px;
  color: #666;
  margin-right: 10px;
}

.seckill-price {
  font-size: 32px;
  font-weight: bold;
  color: #e74c3c;
}

.original-price {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
}

.seckill-countdown {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fef0f0;
  border-radius: 8px;
  display: flex;
  align-items: center;
}

.countdown-label {
  font-size: 16px;
  color: #e74c3c;
  margin-right: 10px;
}

.product-stock {
  margin-bottom: 20px;
}

.stock-label {
  font-size: 16px;
  color: #666;
  margin-right: 10px;
}

.stock-low {
  color: #e74c3c;
  font-weight: bold;
}

.product-sales {
  margin-bottom: 20px;
}

.sales-label {
  font-size: 16px;
  color: #666;
  margin-right: 10px;
}

.sales-value {
  font-size: 16px;
  color: #e74c3c;
  font-weight: bold;
}

.buy-quantity {
  margin-bottom: 30px;
  display: flex;
  align-items: center;
}

.quantity-label {
  font-size: 16px;
  color: #666;
  margin-right: 20px;
}

.action-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
}

.seckill-btn, .add-cart-btn {
  flex: 1;
}

.product-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.product-tabs {
  margin-top: 30px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.detail-content {
  padding: 20px;
  font-size: 16px;
  line-height: 1.8;
}

.detail-content img {
  max-width: 100%;
  margin: 10px 0;
}

.comments-content {
  padding: 20px;
}

/* 进度弹窗样式 */
.progress-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.progress-text {
  margin-top: 20px;
  font-size: 16px;
  color: #666;
  text-align: center;
}
</style>