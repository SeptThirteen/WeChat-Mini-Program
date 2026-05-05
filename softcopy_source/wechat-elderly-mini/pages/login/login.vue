<template>
  <view class="page">
    <view class="header">
      <text class="title">登录</text>
    </view>

    <view class="card">
      <input
        class="input"
        v-model="phone"
        type="number"
        maxlength="11"
        border: 2px solid $color-border;
      />
      <input
        class="input"
        v-model="code"
        type="number"
        maxlength="6"
        placeholder="请输入验证码（任意6位）"
      />
      <view class="btn" :class="{ disabled: loading }" @click="handleLogin">
        {{ loading ? '登录中…' : '登录' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { login } from '../../api/auth';
import { useUserStore } from '../../store/user';

const phone = ref('');
const code = ref('');
const loading = ref(false);
const userStore = useUserStore();

const handleLogin = async () => {
  if (loading.value) return;

  const trimmedPhone = phone.value.trim();
  const trimmedCode = code.value.trim();

  if (!trimmedPhone || !trimmedCode) {
    uni.showToast({ title: '请输入手机号和验证码', icon: 'none' });
    return;
  }
  if (!/^1\d{10}$/.test(trimmedPhone)) {
    uni.showToast({ title: '请输入正确的11位手机号', icon: 'none' });
    return;
  }
  if (!/^\d{6}$/.test(trimmedCode)) {
    uni.showToast({ title: '验证码为6位数字', icon: 'none' });
    return;
  }

  loading.value = true;
  try {
    const res = await login({ phone: trimmedPhone, code: trimmedCode });
    if (res && res.data) {
      userStore.setAuth(res.data);
      uni.showToast({ title: '登录成功' });
      uni.navigateBack();
    }
  } catch (e) {
    uni.showToast({ title: e.message || '登录失败，请重试', icon: 'none' });
  } finally {
    loading.value = false;
  }
};
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding: 40px 24px 24px;
  box-sizing: border-box;
}

.header {
  margin-bottom: 32px;
}

.title {
  font-size: $fontSize-title;   // 34px 适老化大标题
  font-weight: 700;
  color: #333;
  line-height: 1.3;
}

.card {
  background: $color-card;
  border-radius: 12px;
  border: 2px solid $color-border;
  padding: 28px 20px;
}

.input {
  display: block;
  width: 100%;
  box-sizing: border-box;
  background: $color-card;
  border: 2px solid $color-border;
  border-radius: 10px;
  padding: 0 16px;
  height: 56px;
  line-height: 56px;
  font-size: $fontSize-base;
  color: $color-text;
  margin-bottom: 16px;
}

.input:focus {
  border-color: $color-primary;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 56px;
  background: $color-primary;
  border: 2px solid $color-primary;
  color: #fff;
  border-radius: 12px;
  font-size: $fontSize-md;
  font-weight: 700;
  margin-top: 8px;
  box-sizing: border-box;
}

.btn.disabled {
  opacity: 0.6;
}
</style>
