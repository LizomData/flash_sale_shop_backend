<!-- src/views/Register.vue -->
<template>
  <div class="register-container">
    <el-card class="register-card" shadow="hover">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>欢迎来到在线秒杀系统</p>
      </div>
      <el-form 
        ref="registerForm" 
        :model="registerForm" 
        :rules="registerRules" 
        label-width="80px"
        @keyup.enter.native="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="请输入用户名" 
            prefix-icon="el-icon-user"
            clearable
          ></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            clearable
            show-password
          ></el-input>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请确认密码"
            prefix-icon="el-icon-lock"
            clearable
            show-password
          ></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            class="register-btn" 
            @click="handleRegister"
            :loading="loading"
          >
            注册
          </el-button>
        </el-form-item>
        
        <div class="login-link">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { register } from "@/api/user";

export default {
  data() {
    return {
      registerForm: {
        username: "",
        password: "",
        confirmPassword: ""
      },
      registerRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          { min: 3, max: 20, message: "用户名长度在 3 到 20 个字符", trigger: "blur" }
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: "请确认密码", trigger: "blur" },
          { validator: this.validateConfirmPassword, trigger: "blur" }
        ]
      },
      loading: false
    };
  },
  methods: {
    validateConfirmPassword(rule, value, callback) {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    },
    async handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            await register(this.registerForm.username, this.registerForm.password);
            this.$message.success("注册成功！");
            // 延迟跳转，让用户看到成功提示
            setTimeout(() => {
              this.$router.push("/login");
            }, 1000);
          } catch (error) {
            this.$message.error("注册失败：" + error.message);
          } finally {
            this.loading = false;
          }
        }
      });
    },
  },
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 140px);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 400px;
  border-radius: 8px;
  padding: 20px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 24px;
}

.register-header p {
  color: #999;
  font-size: 14px;
}

.register-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #409eff;
  margin-left: 5px;
}

.login-link a:hover {
  color: #66b1ff;
}
</style>
