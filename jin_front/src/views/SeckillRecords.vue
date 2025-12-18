<!-- src/views/SeckillRecords.vue -->
<template>
  <div class="seckill-records">
    <div class="section-header">
      <h3>秒杀记录</h3>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="时间范围">
          <el-date-picker 
            v-model="filterForm.dateRange" 
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部">
            <el-option label="全部" value=""></el-option>
            <el-option label="成功" value="success"></el-option>
            <el-option label="失败" value="failed"></el-option>
            <el-option label="进行中" value="processing"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 秒杀记录列表 -->
    <el-card class="records-card" shadow="hover">
      <el-table :data="records" border style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="100"></el-table-column>
        <el-table-column prop="productName" label="商品名称"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
        <el-table-column prop="seckillPrice" label="秒杀价" width="100">
          <template slot-scope="scope">
            <span class="seckill-price">¥{{ scope.row.seckillPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="秒杀时间" width="180"></el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button 
              v-if="scope.row.status === 'success'"
              type="info" 
              size="small"
              @click="handleOrderDetail(scope.row)"
            >
              查看订单
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
export default {
  name: 'SeckillRecords',
  data() {
    return {
      filterForm: {
        dateRange: [],
        status: ''
      },
      records: [
        {
          id: 1,
          productId: 1,
          productName: 'iPhone 15 Pro',
          quantity: 1,
          seckillPrice: 7999,
          status: 'success',
          createdAt: '2024-01-01 10:00:00',
          orderId: 1
        },
        {
          id: 2,
          productId: 2,
          productName: '小米14 Ultra',
          quantity: 1,
          seckillPrice: 5999,
          status: 'failed',
          createdAt: '2024-01-02 15:30:00'
        }
      ],
      total: 2,
      currentPage: 1,
      pageSize: 10,
      loading: false
    };
  },
  mounted() {
    this.fetchRecords();
  },
  methods: {
    async fetchRecords() {
      this.loading = true;
      try {
        // const response = await getSeckillRecords({
        //   startDate: this.filterForm.dateRange[0],
        //   endDate: this.filterForm.dateRange[1],
        //   status: this.filterForm.status,
        //   page: this.currentPage,
        //   size: this.pageSize
        // });
        // this.records = response.data.list;
        // this.total = response.data.total;
        // 暂时使用模拟数据
      } catch (error) {
        this.$message.error('获取秒杀记录失败：' + error.message);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.currentPage = 1;
      this.fetchRecords();
    },
    handleReset() {
      this.filterForm = {
        dateRange: [],
        status: ''
      };
      this.currentPage = 1;
      this.fetchRecords();
    },
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchRecords();
    },
    getStatusType(status) {
      const statusMap = {
        success: 'success',
        failed: 'danger',
        processing: 'warning'
      };
      return statusMap[status] || 'info';
    },
    getStatusText(status) {
      const statusMap = {
        success: '成功',
        failed: '失败',
        processing: '进行中'
      };
      return statusMap[status] || status;
    },
    handleOrderDetail(record) {
      // 跳转到订单详情页
      this.$router.push(`/user/orders/${record.orderId}`);
    }
  }
};
</script>

<style scoped>
.seckill-records {
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

.records-card {
  margin-bottom: 20px;
}

.seckill-price {
  color: #e74c3c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>