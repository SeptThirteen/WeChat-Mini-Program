import { request } from './request';

export function submitGovTask(data) {
  // data: { userId, taskType, taskDesc }
  return request({ url: '/api/gov/submit', method: 'POST', data });
}

export function getGovTaskList(userId) {
  return request({ url: '/api/gov/list', data: { userId } });
}
