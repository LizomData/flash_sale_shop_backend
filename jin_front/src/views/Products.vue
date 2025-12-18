<!-- src/views/Products.vue -->
<template>
  <div class="products">
    <div class="section-header">
      <h2>秒杀商品列表</h2>
    </div>

    <!-- 商品列表 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in products" :key="product.id">
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
              >
                {{ product.stock > 0 ? '立即秒杀' : '已抢光' }}
              </el-button>
            </div>
          </router-link>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
import { getSeckillProducts } from '@/api/product';

export default {
  name: 'Products',
  data() {
    return {
      products: [],
      total: 0,
      currentPage: 1,
      pageSize: 12
    };
  },
  mounted() {
    this.fetchProducts();
  },
  methods: {
    async fetchProducts() {
      try {
        const response = await getSeckillProducts();
        
        console.log('获取商品列表响应:', response);
        
        // 处理响应数据，确保只包含商品对象
        let productList = [];
        
        // 处理不同的数据结构
        if (response.list) {
          // 情况1：响应有list属性，直接使用
          productList = response.list;
        } else if (Array.isArray(response)) {
          // 情况2：响应本身是数组
          productList = response;
        } else {
          // 情况3：响应是包含数字索引的对象（如控制台输出的格式）
          // 使用Object.values获取所有值，然后过滤掉非商品对象
          productList = Object.values(response).filter(item => 
            typeof item === 'object' && item !== null && typeof item.id === 'number'
          );
        }
        
        console.log('过滤后的商品列表:', productList);
        
        // 确保是数组
        if (!Array.isArray(productList)) {
          productList = [];
        }
        
        this.products = productList;
        this.total = productList.length;
      } catch (error) {
        console.error('获取商品列表错误:', error);
        this.$message.error('获取商品列表失败：' + error.message);
      }
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchProducts();
    }
  }
};
</script>

<style scoped>
.products {
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

.original-price {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
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

.pagination {
  margin-top: 30px;
  text-align: center;
}
</style>