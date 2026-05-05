import { request } from './request';

export function getServiceList() {
  return request({ url: '/api/service/list' });
}

export function getServiceDetail(id) {
  return request({ url: `/api/service/${id}` });
}
