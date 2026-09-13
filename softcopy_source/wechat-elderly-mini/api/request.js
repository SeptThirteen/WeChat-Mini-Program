import BASE_URL from './config';

function getToken() {
  return uni.getStorageSync('token');
}

function handleUnauthorized() {
  uni.removeStorageSync('token');
  uni.removeStorageSync('userId');
  uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
  // Delay navigation so the toast is visible first
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/login/login' });
  }, 1200);
}

export function request({ url, method = 'GET', data = {}, header = {} }) {
  const token = getToken();
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      timeout: 15000,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...header
      },
      success: (res) => {
        if (res.statusCode === 401 || res.statusCode === 403) {
          handleUnauthorized();
          reject({ message: '登录已过期' });
          return;
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const payload = res.data || {};
          if (typeof payload.code !== 'undefined') {
            if (payload.code === 0) {
              resolve(payload);
            } else {
              reject(payload);
            }
          } else {
            resolve(payload);
          }
        } else if (res.statusCode >= 500) {
          reject({ message: '服务器开小差了，请稍后再试' });
        } else {
          reject(res.data || res);
        }
      },
      fail: (err) => {
        const msg = (err && err.errMsg) || '';
        if (msg.includes('timeout')) {
          reject({ message: '请求超时，请检查网络后重试' });
        } else {
          reject({ message: '网络连接失败，请检查网络后重试' });
        }
      }
    });
  });
}
