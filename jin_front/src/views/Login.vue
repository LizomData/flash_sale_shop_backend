<!-- src/views/Login.vue -->
<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <div class="login-header">
        <h2>用户登录</h2>
        <p>欢迎来到在线秒杀系统</p>
      </div>
      <el-form 
        ref="loginForm" 
        :model="loginForm" 
        :rules="loginRules" 
        label-width="80px"
        @keyup.enter.native="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            prefix-icon="el-icon-user"
            clearable
          ></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            clearable
            show-password
          ></el-input>
        </el-form-item>
        
        <el-form-item>
          <div class="login-options">
            <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
            <a href="#" class="forget-password">忘记密码？</a>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-btn" 
            @click="handleLogin"
            :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
        
        <div class="register-link">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { login } from "@/api/user";

export default {
  data() {
    return {
      loginForm: {
        username: "",
        password: "",
        remember: false
      },
      loginRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          { min: 3, max: 20, message: "用户名长度在 3 到 20 个字符", trigger: "blur" }
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" }
        ]
      },
      loading: false
    };
  },
  mounted() {
    // 检查是否有记住的用户名
    const rememberedUsername = localStorage.getItem('rememberedUsername');
    if (rememberedUsername) {
      this.loginForm.username = rememberedUsername;
      this.loginForm.remember = true;
    }
  },
  methods: {
    async handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            // 使用真实后端API登录
            const response = await login(this.loginForm.username, this.loginForm.password);
            
            // 保存用户信息
            localStorage.setItem("token", response.token);
            localStorage.setItem("username", response.username);
            localStorage.setItem("role", response.role || 'ROLE_USER'); // 保存角色信息
            
            // 记住用户名
            if (this.loginForm.remember) {
              localStorage.setItem('rememberedUsername', this.loginForm.username);
            } else {
              localStorage.removeItem('rememberedUsername');
            }
            
            this.$message.success("登录成功！");
            // 延迟跳转，让用户看到成功提示
            setTimeout(() => {
              this.$router.push("/");
            }, 1000);
          } catch (error) {
            this.$message.error("登录失败：" + error.message);
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 140px);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: 8px;
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 24px;
}

.login-header p {
  color: #999;
  font-size: 14px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forget-password {
  color: #409eff;
  font-size: 14px;
}

.forget-password:hover {
  color: #66b1ff;
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #409eff;
  margin-left: 5px;
}

.register-link a:hover {
  color: #66b1ff;
}
</style>
