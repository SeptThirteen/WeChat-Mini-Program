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
        placeholder="请输入11位手机号"
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
  padding: 16px;
}

.header {
  margin-bottom: 12px;
}

.title {
  font-size: 22px;
  font-weight: 600;
}

.card {
  background: $color-card;
  border-radius: 12px;
  padding: 16px;
}

.input {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  width: 100%;
  box-sizing: border-box;
}

.btn {
  background: $color-primary;
  color: #fff;
  border-radius: 10px;
  text-align: center;
  padding: 10px 0;
}

.btn.disabled {
  opacity: 0.6;
}
</style>
