import { request } from './request';

export function getOrderList(userId, status) {
  const data = { userId };
  if (status) data.status = status;
  return request({ url: '/api/order/list', data });
}

export function getOrderDetail(orderId) {
  return request({ url: `/api/order/${orderId}` });
}

export function createOrder(data) {
  return request({ url: '/api/order/create', method: 'POST', data });
}

export function cancelOrder(orderId) {
  return request({ url: `/api/order/${orderId}/cancel`, method: 'POST' });
}

export function rateOrder(orderId, rating) {
  return request({ url: `/api/order/${orderId}/rate`, method: 'POST', data: { rating } });
}
