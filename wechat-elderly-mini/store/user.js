import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userId: uni.getStorageSync('userId') || '',
    phone: ''
  }),
  actions: {
    setAuth(payload) {
      this.token = payload.token || '';
      this.userId = payload.userId || '';
      this.phone = payload.phone || '';
      uni.setStorageSync('token', this.token);
      uni.setStorageSync('userId', this.userId);
    },
    logout() {
      this.token = '';
      this.userId = '';
      this.phone = '';
      uni.removeStorageSync('token');
      uni.removeStorageSync('userId');
    }
  }
});
