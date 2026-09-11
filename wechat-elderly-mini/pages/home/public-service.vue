<template>
  <view class="page">
    <view class="page-header public-header">
      <text class="header-title">公共服务</text>
    </view>
    <view class="back-row" @click="goBack">
      <text class="back-text">← 返回一级菜单</text>
    </view>

    <view class="row-list">
      <view
        v-for="(svc, idx) in services"
        :key="svc.key"
        class="func-row"
        :class="{ 'func-row-last': idx === services.length - 1 }"
        hover-class="row-pressed"
        @click="svc.action()"
      >
        <view class="row-main">
          <text class="row-title">{{ svc.name }}</text>
          <text class="row-desc">{{ svc.tag === 'bill' ? '便民缴费查询' : '政务事项办理' }}</text>
        </view>
        <text class="row-tag" :class="svc.tag === 'bill' ? 'tag-bill' : 'tag-gov'">{{ svc.tag === 'bill' ? '缴费查询' : '政务代办' }}</text>
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
      uni.showToast({ title: '跳转失败，请重试', icon: 'none' })
    }
  });
};

const toQuery = (type) => uni.navigateTo({ url: `/pages/query/index?type=${type}` });

const services = [
  { key: 'shebao',  name: '社保查询',      tag: 'gov',  action: () => openMini('#小程序://我的社保卡/A8fzVgm5OGPNYRp') },
  { key: 'yanglao', name: '养老补贴申请',   tag: 'gov',  action: () => openMini('#小程序://民政通/KcvVG5Ky8iqicDv') },
  { key: 'yibao',   name: '医保备案',      tag: 'gov',  action: () => openMini('#小程序://国家医保/tdL3MZpbCQz2SZl') },
  { key: 'dianfei', name: '电费缴纳',      tag: 'bill', action: () => toQuery('ELECTRICITY') },
  { key: 'shuifei', name: '水费缴纳',      tag: 'bill', action: () => toQuery('WATER') },
  { key: 'ranqi',   name: '燃气/有线电视',  tag: 'bill', action: () => toQuery('TV') },
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
  background: $gradient-brand;
}

.header-title {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

.back-row {
  padding: 14px 20px;
  background: $color-card;
  border-bottom: 1px solid $color-border;
}

.back-text {
  font-size: $fontSize-base;
  color: $color-primary;
}

/* 竖排服务列表:名称+分类标签,纯文字 */
.row-list {
  margin: 16px;
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 14px;
  overflow: hidden;
}

.func-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid $color-border;
}

.func-row-last {
  border-bottom: none;
}

.row-pressed {
  background: $color-bg;
}

.row-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
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

.row-tag {
  flex-shrink: 0;
  font-size: 14px;
  padding: 4px 10px;
  border-radius: 10px;
  border: 1px solid transparent;
}

.tag-bill {
  background: #E2F5F2;
  color: $color-primary;
  border-color: #BEE8E3;
}

.tag-gov {
  background: #E6F7F6;
  color: $color-text;
  border-color: #CFEDE9;
}
</style>
