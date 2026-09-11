<template>
  <view class="page">
    <view class="success-header">
      <text class="success-icon">🎉</text>
      <text class="success-title">下单成功</text>
    </view>

    <view class="info-card">
      <text class="service-name">{{ serviceName }} 订单已提交</text>
      <view class="order-row" v-if="orderId">
        <text class="order-label">订单号</text>
        <text class="order-id">DY{{ String(orderId).padStart(11, '0') }}</text>
      </view>
      <text class="tip-text">社区将在 5 分钟内响应，请保持手机畅通 📱</text>
    </view>

    <view class="btn-list">
      <view class="btn primary-btn" @click="goOrders">查看订单进度</view>
      <view class="btn call-btn" @click="callCommunity">语音联系社区</view>
      <view class="btn home-btn" @click="goHome">返回首页</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const orderId = ref('');
const serviceName = ref('服务');

const goOrders = () => uni.navigateTo({ url: '/pages/order/list' });
const goHome = () => uni.switchTab({ url: '/pages/index/index' });

const callCommunity = () => {
  uni.showModal({
    title: '语音联系社区',
    content: '是否拨打社区服务电话：020-12345678？',
    success: (res) => {
      if (res.confirm) {
        uni.makePhoneCall({ phoneNumber: '02012345678' });
      }
    }
  });
};

onLoad((query) => {
  orderId.value = query?.orderId || '';
  serviceName.value = decodeURIComponent(query?.serviceName || '服务');
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 32px;
}

.success-header {
  background: $gradient-brand;
  border-bottom: none;
  padding: 40px 20px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.success-icon {
  font-size: 60px;
}

.success-title {
  font-size: 24px;
  color: #fff;
  font-weight: 700;
}

.info-card {
  background: $color-card;
  border-radius: 12px;
  margin: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  border: 1px solid $color-border;
  box-shadow: 0 8px 16px $color-shadow;
}

.service-name {
  font-size: 24px;
  color: $color-text;
  font-weight: 700;
}

.order-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-top: 2px solid $color-border;
  border-bottom: 2px solid $color-border;
}

.order-label {
  font-size: 18px;
  color: $color-muted;
}

.order-id {
  font-size: 18px;
  color: $color-primary;
  font-weight: 700;
}

.tip-text {
  font-size: 18px;
  color: $color-muted;
  line-height: 1.7;
}

.btn-list {
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.btn {
  border-radius: 12px;
  text-align: center;
  height: 56px;
  line-height: 56px;
  font-size: 18px;
  font-weight: 700;
  border: 1px solid transparent;
}

.primary-btn {
  background: $color-primary;
  color: #fff;
  border-color: $color-primary;
}

.call-btn {
  background: $color-accent;
  color: #fff;
  border-color: $color-accent;
}

.home-btn {
  background: $color-card;
  color: $color-text;
  border-color: $color-border;
}
</style>
