<template>
  <view class="page">
    <view class="top-bar">
      <text class="time">{{ currentTime }}</text>
      <view class="voice-btn" @click="handleVoice">
        <text class="voice-text">语音说需求 🔊</text>
      </view>
    </view>

    <view class="cards">
      <view class="main-card didi-card" @click="go('/pages/home/didi')">
        <text class="main-title">🟤 滴滴摇人</text>
        <text class="main-desc">点击查看/呼叫帮扶服务 ▶</text>
      </view>

      <view class="main-card public-card" @click="go('/pages/home/public-service')">
        <text class="main-title">🟡 公共服务</text>
        <text class="main-desc">点击查询/办理政务/缴费 ▶</text>
      </view>

      <view class="main-card ai-card" @click="go('/pages/home/ai-chat')">
        <text class="main-title">🟠 AI问答</text>
        <text class="main-desc">点击语音问社保/健康/服务 ▶</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const currentTime = ref('');

const updateTime = () => {
  const now = new Date();
  currentTime.value = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
};

const go = (url) => uni.navigateTo({ url });

const handleVoice = () => {
  uni.showToast({ title: '语音功能开发中', icon: 'none' });
};

onMounted(() => {
  updateTime();
  setInterval(updateTime, 30000);
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 12px;
}

.time {
  font-size: $fontSize-md;
  color: $color-muted;
  font-weight: 500;
}

.voice-btn {
  background: $color-primary;
  border-radius: 22px;
  padding: 10px 20px;
}

.voice-text {
  color: #fff;
  font-size: $fontSize-base;
  font-weight: 500;
}

.cards {
  padding: 8px 16px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.main-card {
  border-radius: 18px;
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
  min-height: 120px;
  justify-content: center;
}

.didi-card {
  background: linear-gradient(135deg, #7B3F1F 0%, #A0522D 100%);
}

.public-card {
  background: linear-gradient(135deg, #8B6914 0%, #C8961A 100%);
}

.ai-card {
  background: linear-gradient(135deg, #C85A3A 0%, #F4A261 100%);
}

.main-title {
  font-size: $fontSize-title;
  color: #fff;
  font-weight: 700;
}

.main-desc {
  font-size: $fontSize-md;
  color: rgba(255, 255, 255, 0.92);
}
</style>
