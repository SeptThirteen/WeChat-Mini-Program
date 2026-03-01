<template>
  <view class="page">
    <view class="page-header ai-header">
      <text class="header-title">🟠 AI问答</text>
    </view>
    <view class="back-row" @click="goBack">
      <text class="back-text">← 返回一级菜单</text>
    </view>

    <view class="btn-list">
      <view
        v-for="item in items"
        :key="item.key"
        class="ai-btn"
        @click="wip(item.name)"
      >
        <text class="ai-icon">{{ item.icon }}</text>
        <view class="ai-text">
          <text class="ai-name">{{ item.name }}</text>
          <text class="ai-desc">{{ item.desc }}</text>
        </view>
        <text class="ai-arrow">▶</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const goBack = () => uni.navigateBack();

const wip = (name) => {
  uni.showModal({
    title: '功能开发中',
    content: `「${name}」功能正在建设，敬请期待`,
    showCancel: false
  });
};

const items = [
  { key: 'shengbao', name: '语音问社保/政务',        desc: '社保、医保、政务咨询',  icon: '🎤' },
  { key: 'health',   name: '语音问健康',             desc: '慢病管理、用药咨询',    icon: '🎤' },
  { key: 'bangfu',   name: '语音问帮扶服务',          desc: '代购、维修、陪护问询',  icon: '🎤' },
  { key: 'faq',      name: '常见问题语音播报',        desc: '点击收听常见解答',      icon: '📢' },
  { key: 'free',     name: '直接语音输入提问',        desc: '全屏语音自由提问',      icon: '🎤' },
];
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 24px;
}

.page-header {
  padding: 24px 20px 20px;
}

.ai-header {
  background: linear-gradient(135deg, #C85A3A 0%, #F4A261 100%);
}

.header-title {
  color: #fff;
  font-size: $fontSize-title;
  font-weight: 700;
}

.back-row {
  padding: 14px 20px;
  background: $color-card;
  border-bottom: 1px solid #f0e8e4;
}

.back-text {
  font-size: $fontSize-base;
  color: $color-primary;
}

.btn-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ai-btn {
  background: $color-card;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.ai-icon {
  font-size: 30px;
  flex-shrink: 0;
}

.ai-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ai-name {
  font-size: $fontSize-md;
  color: $color-text;
  font-weight: 600;
}

.ai-desc {
  font-size: $fontSize-sm;
  color: $color-muted;
}

.ai-arrow {
  font-size: $fontSize-base;
  color: $color-muted;
  flex-shrink: 0;
}
</style>
