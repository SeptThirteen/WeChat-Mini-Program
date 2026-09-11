import { request } from './request';
import BASE_URL from './config';

/**
 * AI 文本问答
 * @param {Object} params { userId, provider, intent, text }
 */
export function textQuery(params) {
  return request({
    url: '/api/ai/text-query',
    method: 'POST',
    data: params
  });
}

/**
 * AI 语音问答：上传音频文件
 * @param {String} filePath 录音临时文件路径
 * @param {Long} userId
 * @param {String} provider BAIDU / TENCENT
 * @param {String} intent shengbao / health / bangfu / free
 */
export function voiceQuery(filePath, userId, provider, intent) {
  const token = uni.getStorageSync('token');
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${BASE_URL}/api/ai/voice-query`,
      filePath: filePath,
      name: 'audio',
      formData: {
        userId: String(userId),
        provider: provider || 'BAIDU',
        intent: intent || 'free'
      },
      header: {
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (res) => {
        if (res.statusCode === 401 || res.statusCode === 403) {
          uni.removeStorageSync('token');
          uni.removeStorageSync('userId');
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
          setTimeout(() => uni.reLaunch({ url: '/pages/login/login' }), 1200);
          reject({ message: '登录已过期' });
          return;
        }
        try {
          const payload = JSON.parse(res.data);
          if (payload.code === 0) {
            resolve(payload);
          } else {
            reject(payload);
          }
        } catch (e) {
          reject({ message: '解析响应失败' });
        }
      },
      fail: (err) => reject(err)
    });
  });
}

/**
 * 语音下单意图解析（v1.3）
 * @param {String} filePath 录音临时文件路径
 * @param {Long} userId
 * @param {String} provider BAIDU / TENCENT
 */
export function parseOrderIntent(filePath, userId, provider) {
  const token = uni.getStorageSync('token');
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${BASE_URL}/api/ai/parse-order-intent`,
      filePath: filePath,
      name: 'audio',
      formData: {
        userId: String(userId),
        provider: provider || 'BAIDU'
      },
      header: {
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (res) => {
        if (res.statusCode === 401 || res.statusCode === 403) {
          uni.removeStorageSync('token');
          uni.removeStorageSync('userId');
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
          setTimeout(() => uni.reLaunch({ url: '/pages/login/login' }), 1200);
          reject({ message: '登录已过期' });
          return;
        }
        try {
          const payload = JSON.parse(res.data);
          if (payload.code === 0) {
            resolve(payload);
          } else {
            reject(payload);
          }
        } catch (e) {
          reject({ message: '解析响应失败' });
        }
      },
      fail: (err) => reject(err)
    });
  });
}

/**
 * 获取 FAQ 列表
 */
export function getFaqList() {
  return request({ url: '/api/ai/faq-list' });
}

/**
 * 获取 FAQ 音频 URL
 * @param {String} id FAQ编号，如 '001'
 */
export function getFaqAudioUrl(id) {
  return `${BASE_URL}/api/ai/faq-audio/${id}`;
}

/**
 * 获取AI查询历史
 * @param {Long} userId
 * @param {Number} limit
 */
export function getAiHistory(userId, limit = 20) {
  return request({ url: '/api/ai/history', data: { userId, limit } });
}
