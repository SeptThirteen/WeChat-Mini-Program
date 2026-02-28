import { request } from './request';

export function queryBill(data) {
  // data: { queryType, queryParams, userId }
  return request({ url: '/api/bill/query', method: 'POST', data });
}
