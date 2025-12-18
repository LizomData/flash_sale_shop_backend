// src/api/user.js
import request from './axios';

export function register(username, password) {
  return request.post('/users/register', {
    username,
    password,
  });
}

export function login(username, password) {
  return request.post('/users/login', {
    username,
    password,
  });
}

export function getUserInfo() {
  return request.get('/users/info');
}

export function logout() {
  return request.post('/users/logout');
}

// 获取订单列表
export function getOrders(params) {
  return request.get('/orders', { params });
}

// 获取订单详情
export function getOrderDetails(id) {
  return request.get(`/orders/${id}`);
}

// 取消订单
export function cancelOrder(id) {
  return request.put(`/orders/${id}/cancel`);
}

// 购物车相关API

// 获取购物车商品列表
export function getCartItems() {
  return request.get('/cart');
}

// 添加商品到购物车
export function addToCart(productId, quantity) {
  return request.post('/cart', {
    productId,
    quantity
  });
}

// 更新购物车商品数量
export function updateCartItem(id, quantity) {
  return request.put(`/cart/${id}`, {
    quantity
  });
}

// 删除购物车商品
export function deleteCartItem(id) {
  return request.delete(`/cart/${id}`);
}

// 批量删除购物车商品
export function deleteCartItems(ids) {
  return request.delete('/cart', {
    data: ids
  });
}

// 清空购物车
export function clearCart() {
  return request.delete('/cart/clear');
}
