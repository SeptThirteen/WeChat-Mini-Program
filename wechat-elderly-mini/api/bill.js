import { request } from './request';

export function queryBill(data) {
  // data: { queryType, queryParams, userId }
  return request({ url: '/api/bill/query', method: 'POST', data });
}

export function getBillHistory(userId) {
  return request({ url: '/api/bill/history', data: { userId } });
}
