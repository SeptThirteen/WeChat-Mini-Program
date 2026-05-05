<template>
  <view class="page">
    <view class="page-header public-header">
      <text class="header-title">🟡 公共服务</text>
    </view>
    <view class="back-row" @click="goBack">
      <text class="back-text">← 返回一级菜单</text>
    </view>

    <view class="grid">
      <view
        v-for="svc in services"
        :key="svc.key"
        class="grid-item"
        @click="svc.action()"
      >
        <text class="grid-icon">{{ svc.icon }}</text>
        <text class="grid-name">{{ svc.name }}</text>
        <text class="grid-tag" :class="svc.tag === 'bill' ? 'tag-bill' : 'tag-gov'">{{ svc.tag === 'bill' ? '缴费查询' : '政务代办' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const goBack = () => uni.navigateBack();

const openMini = (shortLink) => {
  uni.navigateToMiniProgram({
    shortLink,
    success: () => {
      uni.showToast({ title: '即将跳转', icon: 'none' });
    },
    fail: () => {
      uni.showToast({ title: '跳转失败，请重试', icon: 'none' });
    }
  });
};

const toQuery = (type) => uni.navigateTo({ url: `/pages/query/index?type=${type}` });

const services = [
  { key: 'shebao',  name: '社保查询',     icon: '🏛',  tag: 'gov',  action: () => openMini('#小程序://我的社保卡/A8fzVgm5OGPNYRp') },
  { key: 'yanglao', name: '养老补贴申请',  icon: '👴',  tag: 'gov',  action: () => openMini('#小程序://民政通/KcvVG5Ky8iqicDv') },
  { key: 'yibao',   name: '医保备案',     icon: '🏥',  tag: 'gov',  action: () => openMini('#小程序://国家医保/tdL3MZpbCQz2SZl') },
  { key: 'dianfei', name: '电费缴纳',     icon: '💡',  tag: 'bill', action: () => toQuery('ELECTRICITY') },
  { key: 'shuifei', name: '水费缴纳',     icon: '💧',  tag: 'bill', action: () => toQuery('WATER') },
  { key: 'ranqi',   name: '燃气/有线电视', icon: '📺',  tag: 'bill', action: () => toQuery('TV') },
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

.public-header {
  background: $color-card;
  border-bottom: 2px solid $color-border;
}

.header-title {
  color: $color-text;
  font-size: 24px;
  font-weight: 700;
}

.back-row {
  padding: 14px 20px;
  background: $color-card;
  border-bottom: 2px solid $color-border;
}

.back-text {
  font-size: $fontSize-base;
  color: $color-primary;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  padding: 20px 16px;
}

.grid-item {
  background: $color-card;
  border-radius: 12px;
  border: 2px solid $color-border;
  padding: 24px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.grid-icon {
  font-size: 40px;
}

.grid-name {
  font-size: $fontSize-base;
  color: $color-text;
  font-weight: 600;
  text-align: center;
}

.grid-tag {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 10px;
}

.tag-bill {
  background: #e8f5e9;
  color: #2e7d32;
}

.tag-gov {
  background: #e3f2fd;
  color: #1565c0;
}
</style>
