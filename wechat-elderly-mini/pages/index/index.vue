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
          <text class="voice-text">语音说需求 🔊</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-bar">
        <text class="section-title">核心场景</text>
      </view>
      <view class="section-grid">
        <view class="info-card" @click="go('/pages/home/didi')">
          <view class="card-head">
            <view class="card-dot dot-didi"></view>
            <text class="card-title">生活服务预约</text>
          </view>
          <text class="card-desc">一呼馨家上门服务</text>
          <text class="card-cta">点击进入 →</text>
        </view>

        <view class="info-card" @click="go('/pages/query/index')">
          <view class="card-head">
            <view class="card-dot dot-query"></view>
            <text class="card-title">便民缴费查询</text>
          </view>
          <text class="card-desc">水电燃气等生活缴费</text>
          <text class="card-cta">点击进入 →</text>
        </view>

        <view class="info-card" @click="go('/pages/home/public-service')">
          <view class="card-head">
            <view class="card-dot dot-public"></view>
            <text class="card-title">公共服务办理</text>
          </view>
          <text class="card-desc">社保、医保、补贴事项</text>
          <text class="card-cta">点击进入 →</text>
        </view>

        <view class="info-card" @click="go('/pages/home/ai-chat')">
          <view class="card-head">
            <view class="card-dot dot-ai"></view>
            <text class="card-title">智能咨询协助</text>
          </view>
          <text class="card-desc">语音/文字问答解疑</text>
          <text class="card-cta">点击进入 →</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-bar">
        <text class="section-title">服务体系</text>
      </view>
      <view class="section-grid">
        <view class="info-card static-card">
          <view class="card-head">
            <view class="card-dot dot-system"></view>
            <text class="card-title">线上小程序</text>
          </view>
          <text class="card-desc">一站式入口，统一管理</text>
        </view>

        <view class="info-card static-card">
          <view class="card-head">
            <view class="card-dot dot-system"></view>
            <text class="card-title">线下社区协同</text>
          </view>
          <text class="card-desc">社区服务中心协作响应</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-bar">
        <text class="section-title">运营模式</text>
      </view>
      <view class="section-grid mini-grid">
        <view class="info-card mini-card static-card">
          <text class="mini-title">大学生专业团队</text>
          <text class="mini-desc">产品研发与运营支持</text>
        </view>
        <view class="info-card mini-card static-card">
          <text class="mini-title">社区试点切入</text>
          <text class="mini-desc">聚焦真实需求验证</text>
        </view>
        <view class="info-card mini-card static-card">
          <text class="mini-title">标准化复制</text>
          <text class="mini-desc">流程固化快速扩展</text>
        </view>
        <view class="info-card mini-card static-card">
          <text class="mini-title">可量化评估</text>
          <text class="mini-desc">数据驱动持续优化</text>
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
  margin: 14px 16px 0;
  background: $gradient-brand;
  border-radius: 14px;
  padding: 8px 14px;
  box-shadow: 0 8px 18px $color-shadow;
}

.section-title {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.section-grid {
  padding: 12px 16px 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.mini-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.info-card {
  background: $color-card;
  border-radius: 14px;
  border: 1px solid $color-border;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 110px;
  box-shadow: 0 8px 16px $color-shadow;
}

.static-card {
  box-shadow: 0 6px 12px rgba(15, 61, 62, 0.08);
}

.card-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: $color-primary;
}

.dot-didi { background: $color-didi; }
.dot-query { background: $color-accent; }
.dot-public { background: $color-public; }
.dot-ai { background: $color-ai; }
.dot-system { background: $color-primary; }

.card-title {
  font-size: 20px;
  color: $color-text;
  font-weight: 700;
}

.card-desc {
  font-size: 16px;
  color: $color-muted;
  line-height: 1.4;
}

.card-cta {
  margin-top: auto;
  font-size: 16px;
  color: $color-primary;
  font-weight: 700;
}

.mini-card {
  min-height: 88px;
  align-items: center;
  text-align: center;
}

.mini-title {
  font-size: 18px;
  color: $color-text;
  font-weight: 700;
}

.mini-desc {
  font-size: 16px;
  color: $color-muted;
}
</style>
