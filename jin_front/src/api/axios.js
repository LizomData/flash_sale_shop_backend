// src/api/axios.js
import axios from 'axios'

// 创建axios实例
const service = axios.create({
  baseURL: 'http://localhost:28080', // 后端服务地址
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      // 将token添加到请求头
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 请求错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 处理API返回格式
    const res = response.data
    if (res.code === 200 || res.code === 201) {
      // 返回包含message和data的对象，确保前端能访问到message
      return {
        ...res.data,
        message: res.message // 将message字段合并到返回结果中
      }
    } else {
      // 处理业务错误，保留响应信息
      const message = res.message || '请求失败'
      console.error('业务错误:', message)
      // 创建一个包含response属性的错误对象，便于前端处理
      const error = new Error(message)
      error.response = {
        data: { message },
        status: res.code || 500
      }
      return Promise.reject(error)
    }
  },
  error => {
    // 响应错误处理
    let message = '请求失败'
    if (error.response) {
      // 服务器返回错误
      const { status, data } = error.response
      message = data.message || `请求错误 (${status})`
      
      // 处理401未授权
      if (status === 401) {
        // 清除token
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        // 跳转到登录页
        window.location.href = '/login'
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      message = '服务器无响应'
    }
    
    console.error('响应错误:', message)
    return Promise.reject(error)
  }
)

export default service