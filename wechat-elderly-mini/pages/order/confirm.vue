<template>
  <view class="page">
    <view class="page-header confirm-header">
      <text class="header-title">确认订单</text>
    </view>

    <view class="info-card" v-if="params">
      <view class="info-row">
        <text class="info-label">服务类型</text>
        <text class="info-value">{{ params.serviceName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">预约时间</text>
        <text class="info-value">{{ params.dateStr }} {{ params.slotStr }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">服务地址</text>
        <text class="info-value">{{ params.address }}</text>
      </view>
      <view class="info-row" v-if="params.remark">
        <text class="info-label">备注</text>
        <text class="info-value">{{ params.remark }}</text>
      </view>
      <view class="info-row" v-if="params.orderType === 'RECURRING'">
        <text class="info-label">预约类型</text>
        <text class="info-value recurring-tag">🔁 长期预约</text>
      </view>
      <view class="info-row" v-if="params.orderType === 'RECURRING'">
        <text class="info-label">服务周期</text>
        <text class="info-value">{{ params.dateStart }} 至 {{ params.dateEnd }}</text>
      </view>
      <view class="info-row" v-if="params.orderType === 'RECURRING'">
        <text class="info-label">重复频率</text>
        <text class="info-value">{{ params.recurrenceRule }}</text>
      </view>
      <view class="info-row price-row">
        <text class="info-label">费用</text>
        <text class="info-value price-value">¥{{ params.servicePrice }}（完成后支付）</text>
      </view>
    </view>

    <view class="btn-row">
      <view class="cancel-btn" @click="goBack">取消</view>
      <view class="confirm-btn" :class="{ disabled: loading }" @click="handleConfirm">
        {{ loading ? '提交中…' : '确认提交' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { createOrder } from '../../api/order';
import { useUserStore } from '../../store/user';

const params = ref(null);
const loading = ref(false);
const userStore = useUserStore();

const goBack = () => uni.navigateBack();

const handleConfirm = async () => {
  if (loading.value) return;
  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    uni.navigateTo({ url: '/pages/login/login' });
    return;
  }
  loading.value = true;
  try {
    const res = await createOrder({
      userId: userStore.userId,
      serviceId: params.value.serviceId,
      scheduledDate: params.value.dateStr || '',
      scheduledSlot: params.value.slotStr || '',
      address: params.value.address || '',
      remark: params.value.remark || '',
      orderType: params.value.orderType || 'SINGLE',
      dateStart: params.value.dateStart || '',
      dateEnd: params.value.dateEnd || '',
      recurrenceRule: params.value.recurrenceRule || '',
    });
    const orderId = res.data?.orderId || '';
    uni.redirectTo({
      url: `/pages/order/success?orderId=${orderId}&serviceName=${encodeURIComponent(params.value.serviceName)}`
    });
  } catch (e) {
    uni.showToast({ title: e.message || '下单失败，请重试', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

onLoad((query) => {
  try {
    params.value = JSON.parse(decodeURIComponent(query?.params || '{}'));
  } catch {
    params.value = null;
  }
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 32px;
}

.page-header {
  padding: 24px 20px 20px;
}

.confirm-header {
  background: linear-gradient(135deg, #7B3F1F 0%, #A0522D 100%);
}

.header-title {
  color: #fff;
  font-size: $fontSize-title;
  font-weight: 700;
}

.info-card {
  background: $color-card;
  border-radius: 16px;
  margin: 16px;
  padding: 4px 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f5ede8;
  gap: 12px;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: $fontSize-base;
  color: $color-muted;
  flex-shrink: 0;
  min-width: 72px;
}

.info-value {
  font-size: $fontSize-base;
  color: $color-text;
  text-align: right;
  flex: 1;
}

.price-row {
  background: #fff8f4;
  margin: 0 -16px;
  padding: 16px 16px;
  border-radius: 0 0 16px 16px;
}

.price-value {
  color: $color-primary;
  font-weight: 600;
}

.btn-row {
  display: flex;
  gap: 12px;
  padding: 8px 16px 0;
}

.cancel-btn {
  flex: 1;
  background: #f5f5f5;
  color: #666;
  border-radius: 14px;
  text-align: center;
  padding: 18px 0;
  font-size: $fontSize-md;
}

.confirm-btn {
  flex: 2;
  background: $color-primary;
  color: #fff;
  border-radius: 14px;
  text-align: center;
  padding: 18px 0;
  font-size: $fontSize-md;
  font-weight: 600;
}

.confirm-btn.disabled {
  opacity: 0.6;
}

.recurring-tag {
  color: #e65100;
  font-weight: 600;
}
</style>
