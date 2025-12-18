<!-- AdminSeckill.vue -->
<template>
  <div class="admin-seckill">
    <div class="page-header">
      <h2>秒杀管理</h2>
      <el-button type="primary" @click="showSetSeckillDialog">设置秒杀</el-button>
    </div>
    
    <!-- 秒杀商品列表 -->
    <el-table :data="seckillProducts" stripe style="width: 100%" :row-key="row => row.id">
      <el-table-column prop="id" label="商品ID" width="80"></el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="200"></el-table-column>
      <el-table-column prop="originalPrice" label="原价" width="100" :formatter="formatPrice"></el-table-column> 
       <el-table-column prop="seckillPrice" label="秒杀价" width="100" :formatter="formatPrice"></el-table-column>
      <el-table-column prop="stock" label="库存" width="80"></el-table-column>
      <el-table-column prop="seckillStartTime" label="开始时间" width="180"></el-table-column>
      <el-table-column prop="seckillEndTime" label="结束时间" width="180"></el-table-column>
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">
          <el-tag type="success" v-if="isSeckillActive(scope.row)">进行中</el-tag>
          <el-tag type="warning" v-else-if="isSeckillUpcoming(scope.row)">即将开始</el-tag>
          <el-tag type="info" v-else>已结束</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template slot-scope="scope">
          <el-button type="danger" size="small" @click="handleCancelSeckill(scope.row.id)">取消秒杀</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 设置秒杀对话框 -->
    <el-dialog title="设置秒杀" :visible.sync="dialogVisible" width="600px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="选择商品" prop="productId">
          <el-select v-model="formData.productId" placeholder="请选择商品" @change="handleProductChange">
            <el-option
              v-for="product in normalProducts"
              :key="product.id"
              :label="`${product.name} (原价: ¥${product.originalPrice})`"
              :value="product.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="商品原价" prop="originalPrice">
          <el-input v-model="selectedProductOriginalPrice" placeholder="商品原价" disabled></el-input>
        </el-form-item>
        <el-form-item label="秒杀价格" prop="seckillPrice">
          <el-input v-model.number="formData.seckillPrice" placeholder="请输入秒杀价格"></el-input>
        </el-form-item>
        <el-form-item label="秒杀库存" prop="stock">
          <el-input v-model.number="formData.stock" placeholder="请输入秒杀库存"></el-input>
        </el-form-item>
        <el-form-item label="开始时间" prop="seckillStartTime">
          <el-date-picker
            v-model="formData.seckillStartTime"
            type="datetime"
            placeholder="选择开始时间"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="{ disabledDate: disabledDateBeforeToday }"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="seckillEndTime">
          <el-date-picker
            v-model="formData.seckillEndTime"
            type="datetime"
            placeholder="选择结束时间"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="{ 
              disabledDate: disabledDateBeforeToday
            }"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSetSeckill">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProducts, addSeckillProduct, cancelSeckillProduct, getAdminSeckillProducts } from '@/api/product';

export default {
  name: 'AdminSeckill',
  data() {
    return {
      products: [],
      dialogVisible: false,
      formData: {
        productId: '',
        seckillPrice: '',
        stock: '',
        seckillStartTime: '',
        seckillEndTime: ''
      },
      selectedProductOriginalPrice: '',
      selectedProductStock: 0,
      formRules: {
        productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
        seckillPrice: [{ required: true, message: '请输入秒杀价格', trigger: 'blur', type: 'number' }],
        stock: [
          { required: true, message: '请输入秒杀库存', trigger: 'blur', type: 'number' },
          { 
            validator: (rule, value, callback) => {
              if (value > this.selectedProductStock) {
                callback(new Error(`秒杀库存不能超过原有库存 ${this.selectedProductStock}`));
              } else {
                callback();
              }
            }, 
            trigger: 'blur' 
          }
        ],
        seckillStartTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        seckillEndTime: [
          { required: true, message: '请选择结束时间', trigger: 'change' },
          { 
            validator: (rule, value, callback) => {
              if (this.formData.seckillStartTime && value) {
                if (new Date(value) <= new Date(this.formData.seckillStartTime)) {
                  callback(new Error('结束时间必须大于开始时间'));
                } else {
                  callback();
                }
              } else {
                callback();
              }
            }, 
            trigger: 'change' 
          }
        ]
      },
      loading: false
    };
  },
  computed: {
    // 秒杀商品列表
    seckillProducts() {
      return this.products.filter(p => p.seckillPrice !== null);
    },
    // 普通商品列表（未设置秒杀的商品）
    normalProducts() {
      return this.products.filter(p => p.seckillPrice === null);
    }
  },
  created() {
    // 页面加载时获取商品列表
    this.fetchProducts();
  },
  methods: {
    // 获取所有商品
    async fetchProducts() {
      this.loading = true;
      try {
        // 先获取所有商品 - axios返回 { total, size, page, list } 结构
        const allProductsResponse = await getProducts();
        const allProducts = allProductsResponse.list || [];
        
        // 再获取秒杀商品列表 - axios返回 { total, size, page, list } 结构
        const seckillProductsResponse = await getAdminSeckillProducts();
        const seckillProducts = seckillProductsResponse.list || [];
        
        // 将秒杀信息合并到商品列表中
        const mergedProducts = allProducts.map(product => {
          const seckillInfo = seckillProducts.find(sp => sp.id === product.id);
          if (seckillInfo) {
            return {
              ...product,
              seckillPrice: seckillInfo.seckillPrice,
              seckillStartTime: seckillInfo.seckillStartTime,
              seckillEndTime: seckillInfo.seckillEndTime
            };
          }
          return product;
        });
        
        this.products = mergedProducts;
      } catch (error) {
        this.$message.error('获取商品列表失败：' + error.message);
      } finally {
        this.loading = false;
      }
    },
    // 格式化价格
    formatPrice(row, column, cellValue) {
      // 处理两种调用方式：
      // 1. 作为Element UI表格格式化函数时：formatPrice(row, column, cellValue)
      // 2. 直接在template中调用时：formatPrice(price)
      let price;
      if (column === undefined) {
        // 直接调用方式，第一个参数就是价格
        price = row;
      } else {
        // Element UI格式化函数方式，第三个参数是价格
        price = cellValue;
      }
      if (price === null || price === undefined) {
        return '-';
      }
      return `¥${price.toFixed(2)}`;
    },
    // 检查秒杀是否正在进行
    isSeckillActive(product) {
      const now = new Date();
      const startTime = new Date(product.seckillStartTime);
      const endTime = new Date(product.seckillEndTime);
      return now >= startTime && now <= endTime;
    },
    // 检查秒杀是否即将开始
    isSeckillUpcoming(product) {
      const now = new Date();
      const startTime = new Date(product.seckillStartTime);
      return now < startTime;
    },
    // 禁用今天之前的日期
    disabledDateBeforeToday(time) {
      return time.getTime() < Date.now() - 8.64e7;
    },

    // 显示设置秒杀对话框
    showSetSeckillDialog() {
      this.formData = {
        productId: '',
        seckillPrice: '',
        stock: '',
        seckillStartTime: '',
        seckillEndTime: ''
      };
      this.selectedProductOriginalPrice = '';
      this.selectedProductStock = 0;
      this.dialogVisible = true;
    },
    // 选择商品变化时更新原价和库存
    handleProductChange(productId) {
      const selectedProduct = this.normalProducts.find(p => p.id === productId);
      if (selectedProduct) {
        this.selectedProductOriginalPrice = selectedProduct.originalPrice;
        this.selectedProductStock = selectedProduct.stock;
        // 重置秒杀库存，确保不超过原有库存
        if (this.formData.stock > selectedProduct.stock) {
          this.formData.stock = '';
        }
      } else {
        this.selectedProductOriginalPrice = '';
        this.selectedProductStock = 0;
        this.formData.stock = '';
      }
    },
    // 设置秒杀
    async handleSetSeckill() {
      this.$refs.formRef.validate(async (valid) => {
        if (valid) {
          try {
            // 转换时间格式为ISO格式
            const seckillStartTime = new Date(this.formData.seckillStartTime).toISOString();
            const seckillEndTime = new Date(this.formData.seckillEndTime).toISOString();
            
            await addSeckillProduct({
              productId: this.formData.productId,
              seckillPrice: this.formData.seckillPrice,
              stock: this.formData.stock,
              seckillStartTime: seckillStartTime,
              seckillEndTime: seckillEndTime
            });
            this.dialogVisible = false;
            this.$message.success('设置秒杀成功');
            // 重新获取商品列表
            this.fetchProducts();
          } catch (error) {
            this.$message.error('设置秒杀失败：' + error.message);
            console.error('设置秒杀失败详情：', error);
          }
        }
      });
    },
    // 取消秒杀
    async handleCancelSeckill(id) {
      this.$confirm('确定要取消这个商品的秒杀吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await cancelSeckillProduct(id);
          this.$message.success('取消秒杀成功');
          // 重新获取商品列表
          this.fetchProducts();
        } catch (error) {
          this.$message.error('取消秒杀失败：' + error.message);
        }
      }).catch(() => {
        // 取消操作
      });
    }
  }
};
</script>

<style scoped>
.admin-seckill {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e6e6e6;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>