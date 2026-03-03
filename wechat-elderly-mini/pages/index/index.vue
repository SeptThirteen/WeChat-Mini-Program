<template>
  <view class="page">
    <view class="top-bar">
      <text class="time">{{ currentTime }}</text>
      <view class="voice-btn" @click="showVoiceSheet = true">
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

    <!-- 语音快捷操作弹窗 -->
    <VoiceActionSheet
      :show="showVoiceSheet"
      context="index"
      @close="showVoiceSheet = false"
      @voiceResult="handleVoiceResult"
      @quickSelect="handleQuickSelect"
    />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import VoiceActionSheet from '../../components/VoiceActionSheet.vue';
import { voiceQuery, textQuery } from '../../api/ai';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const currentTime = ref('');
const showVoiceSheet = ref(false);

const updateTime = () => {
  const now = new Date();
  currentTime.value = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
};

const go = (url) => uni.navigateTo({ url });

// 语音录音结果：上传并跳转AI问答页
 const handleVoiceResult = async (filePath) => {
  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }
  uni.showLoading({ title: 'AI思考中…' });
  try {
    const res = await voiceQuery(filePath, userStore.userId, 'BAIDU', 'free');
    uni.hideLoading();
    const data = res.data;
    uni.navigateTo({
      url: `/pages/home/ai-chat?autoResult=${encodeURIComponent(JSON.stringify(data))}`
    });
  } catch (e) {
    uni.hideLoading();
    uni.showToast({ title: e.message || '语音识别失败', icon: 'none' });
  }
};

// 快捷问题点击：发送文本并跳转
const handleQuickSelect = async ({ intent, text }) => {
  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }
  uni.showLoading({ title: 'AI思考中…' });
  try {
    const res = await textQuery({ userId: userStore.userId, provider: 'BAIDU', intent, text });
    uni.hideLoading();
    const data = res.data;
    uni.navigateTo({
      url: `/pages/home/ai-chat?autoResult=${encodeURIComponent(JSON.stringify(data))}`
    });
  } catch (e) {
    uni.hideLoading();
    uni.showToast({ title: e.message || '查询失败', icon: 'none' });
  }
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
