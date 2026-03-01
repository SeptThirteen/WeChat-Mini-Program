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
  background: linear-gradient(135deg, #2e7d32 0%, #43a047 100%);
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
  font-size: $fontSize-title;
  color: #fff;
  font-weight: 700;
}

.info-card {
  background: $color-card;
  border-radius: 16px;
  margin: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.service-name {
  font-size: $fontSize-md;
  color: $color-text;
  font-weight: 600;
}

.order-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid #f5ede8;
  border-bottom: 1px solid #f5ede8;
}

.order-label {
  font-size: $fontSize-base;
  color: $color-muted;
}

.order-id {
  font-size: $fontSize-base;
  color: $color-primary;
  font-weight: 600;
}

.tip-text {
  font-size: $fontSize-sm;
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
  border-radius: 14px;
  text-align: center;
  padding: 18px 0;
  font-size: $fontSize-md;
  font-weight: 600;
}

.primary-btn {
  background: $color-primary;
  color: #fff;
}

.call-btn {
  background: $color-accent;
  color: #fff;
}

.home-btn {
  background: #f5f5f5;
  color: #666;
}
</style>
