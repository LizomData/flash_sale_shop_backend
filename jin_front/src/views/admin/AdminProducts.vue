<!-- AdminProducts.vue -->
<template>
  <div class="admin-products">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="showAddDialog">添加商品</el-button>
    </div>
    
    <!-- 商品列表 -->
    <el-table :data="products" stripe style="width: 100%" :row-key="row => row.id">
      <el-table-column prop="id" label="商品ID" width="80"></el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="200"></el-table-column>
      <el-table-column prop="originalPrice" label="原价" width="100" :formatter="formatPrice"></el-table-column>

      <el-table-column prop="seckillPrice" label="秒杀价" width="100">
         <template v-slot:default="scope">
           <span v-if="scope.row.seckillPrice" class="seckill-price">{{ formatPrice(scope.row.seckillPrice) }}</span>
           <span v-else class="no-seckill">-</span>
         </template>
       </el-table-column>
       <el-table-column prop="stock" label="库存" width="80"></el-table-column>
       <el-table-column label="状态" width="100">
         <template v-slot:default="scope">
           <el-tag v-if="scope.row.seckillPrice" type="danger">秒杀中</el-tag>
           <el-tag v-else type="info">普通商品</el-tag>
         </template>
       </el-table-column> 

      <el-table-column label="操作" width="200" fixed="right">
         <template v-slot:default="scope">
           <el-button type="primary" size="small" @click="showEditDialog(scope.row)">编辑</el-button>
           <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
         </template>
       </el-table-column>
    </el-table>
    
    <!-- 添加/编辑商品对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入商品名称"></el-input>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input v-model.number="formData.originalPrice" placeholder="请输入原价"></el-input>
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <el-input v-model="formData.images" placeholder="请输入商品图片URL"></el-input>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input v-model.number="formData.stock" placeholder="请输入库存"></el-input>
        </el-form-item>
        <el-form-item label="商品详情" prop="detail">
          <el-input v-model="formData.detail" type="textarea" placeholder="请输入商品详情"></el-input>
        </el-form-item>
        <el-form-item label="商品规格" prop="specs">
          <el-input v-model="formData.specs" placeholder="请输入商品规格，JSON格式" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProducts, createProduct, updateProduct, deleteProduct } from '@/api/product';

export default {
  name: 'AdminProducts',
  data() {
    return {
      products: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '添加商品',
      formData: {
        id: '',
        name: '',
        originalPrice: '',
        images: '',
        stock: '',
        detail: '',
        specs: ''
      },
      formRules: {
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur', type: 'number' }],
        images: [{ required: true, message: '请输入商品图片URL', trigger: 'blur' }],
        stock: [{ required: true, message: '请输入库存', trigger: 'blur', type: 'number' }],
        specs: [{ required: true, message: '请输入商品规格', trigger: 'blur' }]
      }
    };
  },
  mounted() {
    this.fetchProducts();
  },
  methods: {
    // 获取商品列表
    async fetchProducts() {
      this.loading = true;
      try {
        // axios响应拦截器返回的是res.data，即 { total, size, page, list } 结构
        const response = await getProducts();
        // 确保取到商品列表数组
        this.products = response.list || [];
        console.log("获取商品列表：", this.products);
      } catch (error) {
        this.$message.error('获取商品列表失败：' + error.message);
      } finally {
        this.loading = false;
      }
    },
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
    showAddDialog() {
      this.dialogTitle = '添加商品';
      this.formData = {
        id: '',
        name: '',
        originalPrice: '',
        stock: ''
      };
      this.dialogVisible = true;
    },
    showEditDialog(product) {
      this.dialogTitle = '编辑商品';
      this.formData = { ...product };
      this.dialogVisible = true;
    },
    async handleSubmit() {
      this.$refs.formRef.validate(async (valid) => {
        if (valid) {
          try {
            // 准备提交数据，确保类型和字段名与后端完全匹配
            const submitData = {
              ...this.formData,
              // 确保数值类型正确
              originalPrice: Number(this.formData.originalPrice),
              stock: Number(this.formData.stock),
              // 后端会处理默认值，不需要前端设置
              // specs 字段后端是 String 类型，确保是字符串
              specs: this.formData.specs || '{}',
              // 日期字段留空，由后端处理
              seckillStartTime: null,
              seckillEndTime: null
            };
            
            if (this.formData.id) {
              // 编辑商品
              await updateProduct(this.formData.id, submitData);
              this.$message.success('商品更新成功');
            } else {
              // 添加商品
              await createProduct(submitData);
              this.$message.success('商品创建成功');
            }
            this.dialogVisible = false;
            // 重新获取商品列表
            this.fetchProducts();
          } catch (error) {
            this.$message.error('操作失败：' + error.message);
          }
        }
      });
    },
    async handleDelete(id) {
      this.$confirm('确定要删除这个商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteProduct(id);
          this.$message.success('删除成功');
          // 重新获取商品列表
          this.fetchProducts();
        } catch (error) {
          this.$message.error('删除失败：' + error.message);
        }
      }).catch(() => {
        // 取消删除操作
      });
    }
  }
};
</script>

<style scoped>
.admin-products {
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

.seckill-price {
  color: #f56c6c;
  font-weight: bold;
}

.no-seckill {
  color: #909399;
}

.dialog-footer {
  text-align: right;
}
</style>