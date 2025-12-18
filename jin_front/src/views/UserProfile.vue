<!-- src/views/UserProfile.vue -->
<template>
  <div class="user-profile">
    <div class="profile-header">
      <h3>基本信息</h3>
    </div>

    <el-form 
      ref="profileForm" 
      :model="userInfo" 
      :rules="profileRules" 
      label-width="120px"
      class="profile-form"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="userInfo.username" disabled placeholder="请输入用户名"></el-input>
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="userInfo.email" placeholder="请输入邮箱"></el-input>
      </el-form-item>

      <el-form-item label="手机号码" prop="phone">
        <el-input v-model="userInfo.phone" placeholder="请输入手机号码"></el-input>
      </el-form-item>

      <el-form-item label="性别">
        <el-radio-group v-model="userInfo.gender">
          <el-radio label="male">男</el-radio>
          <el-radio label="female">女</el-radio>
          <el-radio label="other">其他</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="生日">
        <el-date-picker 
          v-model="userInfo.birthday" 
          type="date" 
          placeholder="选择生日"
          format="yyyy-MM-dd"
          value-format="yyyy-MM-dd"
        ></el-date-picker>
      </el-form-item>

      <el-form-item label="地址">
        <el-input 
          v-model="userInfo.address" 
          type="textarea" 
          :rows="3" 
          placeholder="请输入地址"
        ></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">保存修改</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      userInfo: {
        username: 'test',
        email: 'test@example.com',
        phone: '13800138000',
        gender: 'male',
        birthday: '1990-01-01',
        address: '北京市朝阳区'
      },
      profileRules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码格式', trigger: 'blur' }
        ]
      },
      loading: false
    };
  },
  mounted() {
    this.fetchUserInfo();
  },
  methods: {
    async fetchUserInfo() {
      try {
        // const response = await getUserInfo();
        // this.userInfo = response.data;
        // 暂时使用模拟数据
      } catch (error) {
        this.$message.error('获取用户信息失败：' + error.message);
      }
    },
    handleSubmit() {
      this.$refs.profileForm.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            // await updateUserInfo(this.userInfo);
            this.$message.success('保存成功！');
          } catch (error) {
            this.$message.error('保存失败：' + error.message);
          } finally {
            this.loading = false;
          }
        }
      });
    },
    handleReset() {
      this.$refs.profileForm.resetFields();
    }
  }
};
</script>

<style scoped>
.user-profile {
  padding: 20px 0;
}

.profile-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.profile-header h3 {
  color: #333;
  margin: 0;
  font-size: 18px;
}

.profile-form {
  max-width: 600px;
}
</style>