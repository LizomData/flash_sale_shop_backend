// src/api/product.js
import request from './axios';

// 获取秒杀商品列表
export function getSeckillProducts() {
  return request.get('/products/seckill');
}

// 获取商品详情
export function getProductDetail(id) {
  return request.get(`/products/${id}`);
}

// 获取商品分类
export function getProductCategories() {
  return request.get('/products/categories');
}

// 获取商品列表
export function getProducts(params) {
  return request.get('/products', { params });
}

// 秒杀商品下单
export function seckillOrder(productId, quantity) {
  return request.post('/orders/seckill', {
    productId,
    quantity
  });
}

export function nomarlOrder(productId, quantity) {
  return request.post('/orders/normal', {
    productId,
    quantity
  });
}




// 管理员：获取所有商品（用于秒杀设置）
export function getAllProducts() {
  return request.get('/products');
}

// 管理员：添加秒杀商品
export function addSeckillProduct(data) {
  return request.post('/admin/seckill/set', data);
}

// 管理员：取消秒杀商品
export function cancelSeckillProduct(productId) {
  return request.post(`/admin/seckill/cancel/${productId}`);
}

// 管理员：获取秒杀商品列表
export function getAdminSeckillProducts() {
  return request.get('/products/seckill');
}

// 管理员：创建商品
export function createProduct(data) {
  return request.post('/products', data);
}

// 管理员：更新商品
export function updateProduct(id, data) {
  return request.put(`/products/${id}`, data);
}

// 管理员：删除商品
export function deleteProduct(id) {
  return request.delete(`/products/${id}`);
}