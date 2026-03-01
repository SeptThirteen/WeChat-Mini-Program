import { request } from './request';

export function getUserProfile(userId) {
  return request({ url: '/api/user/profile', data: { userId } });
}

export function updateUserProfile(userId, data) {
  return request({ url: '/api/user/update', method: 'PUT', data: { userId, ...data } });
}
