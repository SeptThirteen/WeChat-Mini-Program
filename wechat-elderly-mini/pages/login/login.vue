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
      <view class="code-row">
        <input
          class="input code-input"
          v-model="code"
          type="number"
          maxlength="6"
          placeholder="验证码"
        />
        <view
          class="send-btn"
          :class="{ disabled: countdown > 0 || sending }"
          @click="handleSendCode"
        >
          {{ countdown > 0 ? countdown + 's' : (sending ? '发送中…' : '获取验证码') }}
        </view>
      </view>
      <view class="btn" :class="{ disabled: loading }" @click="handleLogin">
        {{ loading ? '登录中…' : '登录' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { login, sendSmsCode } from '../../api/auth';
import { useUserStore } from '../../store/user';

const phone = ref('');
const code = ref('');
const loading = ref(false);
const sending = ref(false);
const countdown = ref(0);
const userStore = useUserStore();

let countdownTimer = null;

const startCountdown = () => {
  countdown.value = 60;
  countdownTimer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) clearInterval(countdownTimer);
  }, 1000);
};

const handleSendCode = async () => {
  if (sending.value || countdown.value > 0) return;
  const trimmedPhone = phone.value.trim();
  if (!/^1\d{10}$/.test(trimmedPhone)) {
    uni.showToast({ title: '请先输入正确的11位手机号', icon: 'none' });
    return;
  }

  sending.value = true;
  try {
    const res = await sendSmsCode(trimmedPhone);
    startCountdown();
    const devCode = res?.data?.devCode;
    if (devCode) {
      uni.showModal({
        title: '验证码已发送（演示模式）',
        content: `验证码：${devCode}\n（未接短信网关，验证码在此展示）`,
        showCancel: false,
        confirmText: '知道了'
      });
    } else {
      uni.showToast({ title: '验证码已发送，请查收短信', icon: 'none', duration: 2500 });
    }
  } catch (e) {
    uni.showToast({ title: e.message || '验证码发送失败，请重试', icon: 'none' });
  } finally {
    sending.value = false;
  }
};

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
  margin-bottom: 24px;
  background: $gradient-brand;
  border-radius: 16px;
  padding: 18px 20px;
  box-shadow: 0 10px 20px $color-shadow;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  line-height: 1.3;
}

.card {
  background: $color-card;
  border-radius: 14px;
  border: 1px solid $color-border;
  padding: 28px 20px;
  box-shadow: 0 10px 20px $color-shadow;
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

.code-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.code-input {
  flex: 1;
  margin-bottom: 16px;
}

.send-btn {
  flex-shrink: 0;
  width: 150px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-card;
  border: 2px solid $color-primary;
  color: $color-primary;
  border-radius: 10px;
  font-size: 18px;
  font-weight: 700;
  box-sizing: border-box;
  margin-bottom: 16px;
}

.send-btn.disabled {
  opacity: 0.55;
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
