<template>
  <view class="page" v-if="isReady">
    <view class="hero">
      <view class="hero-top">
        <view class="hero-title-wrap">
          <text class="hero-title">银龄馨家</text>
          <text class="hero-sub">社区老年群体数字化适配服务</text>
        </view>
        <view class="hero-time">{{ currentTime }}</view>
      </view>
      <view class="hero-actions">
        <view class="voice-btn" @click="showVoiceSheet = true">
          <text class="voice-text">语音说需求</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-bar">
        <text class="section-title">常用功能</text>
      </view>
      <view class="row-list">
        <view class="func-row" hover-class="row-pressed" @click="go('/pages/home/didi')">
          <text class="row-title">生活服务预约</text>
          <text class="row-desc">一呼馨家上门服务</text>
        </view>
        <view class="func-row" hover-class="row-pressed" @click="go('/pages/query/index')">
          <text class="row-title">便民缴费查询</text>
          <text class="row-desc">水电燃气等生活缴费</text>
        </view>
        <view class="func-row" hover-class="row-pressed" @click="go('/pages/home/public-service')">
          <text class="row-title">公共服务办理</text>
          <text class="row-desc">社保、医保、补贴事项</text>
        </view>
        <view class="func-row func-row-last" hover-class="row-pressed" @click="go('/pages/home/ai-chat')">
          <text class="row-title">智能咨询协助</text>
          <text class="row-desc">语音/文字问答解疑</text>
        </view>
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
import { nextTick, ref, onMounted } from 'vue';
import VoiceActionSheet from '../../components/VoiceActionSheet.vue';
import { voiceQuery, textQuery } from '../../api/ai';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const currentTime = ref('');
const showVoiceSheet = ref(false);
const isReady = ref(false);

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
  nextTick(() => {
    isReady.value = true;
    updateTime();
    setInterval(updateTime, 30000);
  });
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 24px;
}

.hero {
  margin: 16px;
  padding: 18px 18px 16px;
  background: $gradient-brand;
  border-radius: 18px;
  box-shadow: 0 12px 24px $color-shadow;
  color: #fff;
}

.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.hero-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
}

.hero-sub {
  font-size: 18px;
  font-weight: 600;
  opacity: 0.95;
}

.hero-time {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 14px;
  padding: 4px 10px;
  font-size: 16px;
  font-weight: 700;
  white-space: nowrap;
}

.hero-actions {
  margin-top: 14px;
  display: flex;
}

.voice-btn {
  background: #fff;
  border-radius: 18px;
  padding: 10px 16px;
  border: 2px solid #fff;
}

.voice-text {
  color: $color-primary;
  font-size: $fontSize-base;
  font-weight: 700;
}

.section {
  margin-top: 10px;
}

.section-bar {
  margin: 18px 16px 10px;
}

.section-title {
  color: $color-text;
  font-size: 20px;
  font-weight: 700;
}

/* 竖排功能列表:纯文字行,适老化最熟悉的形态 */
.row-list {
  margin: 0 16px;
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 14px;
  overflow: hidden;
}

.func-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px;
  border-bottom: 1px solid $color-border;
}

.func-row-last {
  border-bottom: none;
}

.row-pressed {
  background: $color-bg;
}

.row-title {
  font-size: 22px;
  color: $color-text;
  font-weight: 700;
}

.row-desc {
  font-size: 16px;
  color: $color-muted;
}
</style>
