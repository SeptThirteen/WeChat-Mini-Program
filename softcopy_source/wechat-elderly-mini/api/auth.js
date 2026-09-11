import { request } from './request';

export function login(data) {
  return request({ url: '/api/auth/login', method: 'POST', data });
}

export function sendSmsCode(phone) {
  return request({ url: '/api/auth/sms-code', method: 'POST', data: { phone } });
}
