<!-- src/views/Cart.vue -->
<template>
  <div class="cart">
    <div class="section-header">
      <h2>购物车</h2>
    </div>

    <!-- 购物车为空时的提示 -->
    <el-empty description="购物车是空的，快去添加商品吧！" v-if="cartItems.length === 0">
      <el-button type="primary" @click="goToProducts">去秒杀商品</el-button>
    </el-empty>

    <!-- 购物车列表 -->
    <el-card class="cart-card" shadow="hover" v-else>
      <el-table :data="cartItems" border style="width: 100%">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="productName" label="商品名称">
          <template slot-scope="scope">
            <div class="product-info">
              <img :src="scope.row.productImage" :alt="scope.row.productName" class="product-image">
              <span class="product-name">{{ scope.row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="120">
          <template slot-scope="scope">
            <span class="product-price">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="180">
          <template slot-scope="scope">
            <div class="quantity-control">
              <el-button-group>
                <el-button 
                  size="mini" 
                  type="primary" 
                  @click="handleDecrease(scope.row)"
                  :disabled="scope.row.quantity <= 1"
                >
                  -</el-button>
                <el-input-number 
                  v-model="scope.row.quantity" 
                  :min="1" 
                  :max="scope.row.stock" 
                  size="mini"
                  @change="handleQuantityChange(scope.row)"
                ></el-input-number>
                <el-button 
                  size="mini" 
                  type="primary" 
                  @click="handleIncrease(scope.row)"
                  :disabled="scope.row.quantity >= scope.row.stock"
                >
                  +</el-button>
              </el-button-group>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="120">
          <template slot-scope="scope">
            <span class="subtotal-price">¥{{ scope.row.subtotal }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 购物车底部 -->
      <div class="cart-footer">
        <div class="select-all">
          <el-checkbox v-model="isSelectAll" @change="handleSelectAll">全选</el-checkbox>
        </div>
        <div class="cart-actions">
          <el-button type="danger" @click="handleDeleteSelected">删除选中</el-button>
          <div class="total-amount">
            <span class="total-label">合计：</span>
            <span class="total-price">¥{{ totalAmount }}</span>
          </div>
          <el-button type="primary" @click="handleCheckout">结算</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getCartItems, updateCartItem, deleteCartItem, deleteCartItems } from '@/api/user';

export default {
  name: 'Cart',
  data() {
    return {
      cartItems: [],
      isSelectAll: false,
      selectedItems: []
    };
  },
  computed: {
    // 计算总金额
    totalAmount() {
      return this.cartItems.reduce((total, item) => {
        return total + item.subtotal;
      }, 0);
    }
  },
  mounted() {
    this.fetchCartItems();
  },
  methods: {
    async fetchCartItems() {
      try {
        console.log('获取购物车商品');
        const response = await getCartItems();
        
        console.log('购物车商品响应:', response);
        
        // 处理响应数据，确保只包含商品对象
        let items = [];
        
        // 处理不同的数据结构
        if (response.list) {
          // 情况1：响应有list属性，直接使用
          items = response.list;
        } else if (Array.isArray(response)) {
          // 情况2：响应本身是数组
          items = response;
        } else {
          // 情况3：响应是包含数字索引的对象
          items = Object.values(response).filter(item => 
            typeof item === 'object' && item !== null && typeof item.id === 'number'
          );
        }
        
        console.log('过滤后的购物车商品:', items);
        
        // 确保是数组
        if (!Array.isArray(items)) {
          items = [];
        }
        
        // 为每个商品计算小计，适配后端字段
        this.cartItems = items.map(item => {
          return {
            ...item,
            // 确保图片URL有效
            productImage: item.productImage || require('@/assets/logo.png'),
            // 将productPrice映射为price，适配前端组件
            price: item.productPrice,
            // 确保stock字段存在，使用一个较大的值作为默认值
            stock: item.stock || 999,
            // 确保小计正确，如果后端已计算则直接使用，否则重新计算
            subtotal: item.subtotal || (item.productPrice * item.quantity)
          };
        });
        
        console.log('处理后的购物车商品:', this.cartItems);
      } catch (error) {
        console.error('获取购物车商品错误:', error);
        this.$message.error('获取购物车商品失败：' + error.message);
      }
    },
    goToProducts() {
      this.$router.push('/products');
    },
    handleDecrease(item) {
      if (item.quantity > 1) {
        item.quantity--;
        this.updateSubtotal(item);
        this.updateCartItem(item);
      }
    },
    handleIncrease(item) {
      if (item.quantity < item.stock) {
        item.quantity++;
        this.updateSubtotal(item);
        this.updateCartItem(item);
      }
    },
    handleQuantityChange(item) {
      this.updateSubtotal(item);
      this.updateCartItem(item);
    },
    updateSubtotal(item) {
      item.subtotal = item.price * item.quantity;
    },
    async updateCartItem(item) {
      try {
        console.log('更新购物车商品:', item.id, item.quantity);
        await updateCartItem(item.id, item.quantity);
        this.$message.success('购物车已更新');
      } catch (error) {
        console.error('更新购物车失败:', error);
        this.$message.error('更新购物车失败：' + error.message);
        // 回滚数量变化
        this.fetchCartItems();
      }
    },
    handleDelete(item) {
      this.$confirm('确定要删除该商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          console.log('删除购物车商品:', item.id);
          await deleteCartItem(item.id);
          const index = this.cartItems.indexOf(item);
          if (index > -1) {
            this.cartItems.splice(index, 1);
          }
          this.$message.success('商品已删除');
        } catch (error) {
          console.error('删除购物车商品失败:', error);
          this.$message.error('删除商品失败：' + error.message);
          // 重新获取购物车数据
          this.fetchCartItems();
        }
      }).catch(() => {
        this.$message.info('已取消操作');
      });
    },
    handleSelectAll(checked) {
      // 全选/取消全选
      this.selectedItems = checked ? this.cartItems.map(item => item.id) : [];
    },
    handleDeleteSelected() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请选择要删除的商品');
        return;
      }

      this.$confirm('确定要删除选中的商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          console.log('批量删除购物车商品:', this.selectedItems);
          await deleteCartItems(this.selectedItems);
          this.cartItems = this.cartItems.filter(item => !this.selectedItems.includes(item.id));
          this.selectedItems = [];
          this.isSelectAll = false;
          this.$message.success('商品已删除');
        } catch (error) {
          console.error('批量删除购物车商品失败:', error);
          this.$message.error('删除商品失败：' + error.message);
          // 重新获取购物车数据
          this.fetchCartItems();
        }
      }).catch(() => {
        this.$message.info('已取消操作');
      });
    },
    handleCheckout() {
      if (this.cartItems.length === 0) {
        this.$message.warning('购物车是空的，无法结算');
        return;
      }

      // 跳转到结算页面
      this.$message.success('跳转到结算页面');
      // 这里可以添加真实的结算逻辑，比如跳转到订单确认页
      // this.$router.push('/checkout');
    }
  }
};
</script>

<style scoped>
.cart {
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

.cart-card {
  margin-bottom: 20px;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 15px;
  border-radius: 4px;
}

.product-name {
  font-size: 14px;
  max-width: 300px;
}

.product-price {
  color: #e74c3c;
  font-weight: bold;
}

.quantity-control {
  display: flex;
  align-items: center;
}

.subtotal-price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 16px;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.select-all {
  display: flex;
  align-items: center;
}

.cart-actions {
  display: flex;
  align-items: center;
}

.total-amount {
  margin: 0 20px;
}

.total-label {
  font-size: 16px;
  font-weight: bold;
  margin-right: 10px;
}

.total-price {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}
</style>