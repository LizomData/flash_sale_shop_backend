<!-- src/views/UserOrders.vue -->
<template>
  <div class="user-orders">
    <div class="section-header">
      <h3>我的订单</h3>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="订单状态">
          <el-select v-model="filterForm.status" placeholder="全部">
            <el-option label="全部" value=""></el-option>
            <el-option label="待支付" value="pending"></el-option>
            <el-option label="已支付" value="paid"></el-option>
            <el-option label="已发货" value="shipped"></el-option>
            <el-option label="已送达" value="delivered"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card class="orders-card" shadow="hover">
      <el-table :data="orders" border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180"></el-table-column>
        <el-table-column prop="productName" label="商品名称"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template slot-scope="scope">
            <span class="order-amount">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button 
              v-if="scope.row.status === 'pending'"
              type="primary" 
              size="small"
              @click="handlePay(scope.row)"
            >
              立即支付
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending'"
              type="danger" 
              size="small"
              @click="handleCancel(scope.row)"
            >
              取消订单
            </el-button>
            <el-button 
              type="info" 
              size="small"
              @click="handleDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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
    </el-card>
  </div>
</template>

<script>
import { getOrders, cancelOrder } from '@/api/user';

export default {
  name: 'UserOrders',
  data() {
    return {
      filterForm: {
        status: ''
      },
      orders: [],
      total: 0,
      currentPage: 1,
      pageSize: 10
    };
  },
  mounted() {
    this.fetchOrders();
  },
  methods: {
    async fetchOrders() {
      try {
        const params = {
          status: this.filterForm.status,
          page: this.currentPage,
          size: this.pageSize
        };
        const response = await getOrders(params);
        
        console.log('获取订单列表响应:', response);
        
        // 直接使用response.list和response.total，因为axios拦截器已经处理了数据结构
        this.orders = response.list || [];
        this.total = response.total || 0;
        
        console.log('订单列表:', this.orders);
        console.log('订单总数:', this.total);
      } catch (error) {
        console.error('获取订单列表错误:', error);
        this.$message.error('获取订单列表失败：' + error.message);
      }
    },
    handleSearch() {
      this.currentPage = 1;
      this.fetchOrders();
    },
    handleReset() {
      this.filterForm = {
        status: ''
      };
      this.currentPage = 1;
      this.fetchOrders();
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchOrders();
    },
    getStatusType(status) {
      const statusMap = {
        pending: 'warning',
        paid: 'success',
        shipped: 'info',
        delivered: 'success',
        cancelled: 'danger'
      };
      return statusMap[status] || 'info';
    },
    getStatusText(status) {
      const statusMap = {
        pending: '待支付',
        paid: '已支付',
        shipped: '已发货',
        delivered: '已送达',
        cancelled: '已取消'
      };
      return statusMap[status] || status;
    },
    handlePay(order) {
      // 跳转到支付页面
      this.$message.success('跳转到支付页面');
    },
    handleCancel(order) {
      this.$confirm('确定要取消该订单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await cancelOrder(order.id);
          this.$message.success('订单已取消');
          this.fetchOrders();
        } catch (error) {
          this.$message.error('取消订单失败：' + error.message);
        }
      }).catch(() => {
        this.$message.info('已取消操作');
      });
    },
    handleDetail(order) {
      // 跳转到订单详情页
      this.$message.success('查看订单详情');
    }
  }
};
</script>

<style scoped>
.user-orders {
  padding: 20px 0;
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.section-header h3 {
  color: #333;
  margin: 0;
  font-size: 18px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 0;
}

.orders-card {
  margin-bottom: 20px;
}

.order-amount {
  color: #e74c3c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>