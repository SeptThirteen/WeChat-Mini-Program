import { request } from './request';

export function getContactList(userId) {
  return request({ url: '/api/contact/list', data: { userId } });
}

export function saveContact(data) {
  // data: { contactId?, userId, name, phone, relation }
  return request({ url: '/api/contact/save', method: 'POST', data });
}

export function deleteContact(contactId) {
  return request({ url: `/api/contact/${contactId}`, method: 'DELETE' });
}
