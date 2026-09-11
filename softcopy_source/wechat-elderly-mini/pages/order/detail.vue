<template>
  <view class="page">
    <view class="header">
      <text class="title">订单详情</text>
    </view>

    <!-- Loading -->
    <view class="state-box" v-if="loading">
      <text class="state-text">加载中…</text>
    </view>

    <!-- Error -->
    <view class="state-box" v-else-if="error">
      <text class="state-text">{{ error }}</text>
    </view>

    <!-- Detail -->
    <view v-else-if="order" class="card">
      <view class="row">
        <text class="label">订单编号</text>
        <text class="value">#{{ order.orderId }}</text>
      </view>
      <view class="row">
        <text class="label">服务类型</text>
        <text class="value">{{ order.serviceName || ('服务ID：' + order.serviceId) }}</text>
      </view>
      <view class="row">
        <text class="label">状态</text>
        <text class="value status" :class="statusClass">{{ statusLabel }}</text>
      </view>
      <view class="row" v-if="order.scheduledDate">
        <text class="label">预约时间</text>
        <text class="value">{{ order.scheduledDate }} {{ order.scheduledSlot || '' }}</text>
      </view>
      <view class="row recurring-row" v-if="order.orderType === 'RECURRING'">
        <text class="label">预约类型</text>
        <text class="value recurring-tag">长期预约</text>
      </view>
      <view class="row" v-if="order.orderType === 'RECURRING' && order.dateStart">
        <text class="label">服务周期</text>
        <text class="value">{{ order.dateStart }} 至 {{ order.dateEnd }}</text>
      </view>
      <view class="row" v-if="order.orderType === 'RECURRING' && order.recurrenceRule">
        <text class="label">重复频率</text>
        <text class="value">{{ order.recurrenceRule }}</text>
      </view>
      <view class="row" v-if="order.address">
        <text class="label">服务地址</text>
        <text class="value">{{ order.address }}</text>
      </view>
      <view class="row" v-if="order.remark">
        <text class="label">备注</text>
        <text class="value">{{ order.remark }}</text>
      </view>
      <!-- 结单/评价后显示联系电话 -->
      <view class="row contact-row" v-if="order.contactPhone && (order.status === 'COMPLETED' || order.status === 'RATED')">
        <text class="label">联系电话</text>
        <view class="contact-phone-box">
          <text class="value contact-phone">{{ order.contactPhone }}</text>
          <view class="call-btn" @click="makeCall(order.contactPhone)">拨打</view>
        </view>
      </view>
      <!-- 接单服务人员信息 -->
      <view class="worker-card" v-if="order.workerName && order.status !== 'CREATED' && order.status !== 'CANCELLED'">
        <text class="worker-title">服务人员{{ order.workerCategory ? ' · ' + order.workerCategory : '' }}</text>
        <view class="worker-info">
          <text class="worker-name">{{ order.workerName }}</text>
          <view class="worker-phone-row" v-if="order.workerPhone">
            <text class="worker-phone">{{ order.workerPhone }}</text>
            <view class="call-btn" @click="makeCall(order.workerPhone)">拨打</view>
          </view>
        </view>
      </view>
      <view class="row" v-if="order.rating">
        <text class="label">评分</text>
        <text class="value rating-stars">{{ '★'.repeat(order.rating) }}{{ '☆'.repeat(5 - order.rating) }}</text>
      </view>
      <view class="row" v-if="order.createdTime">
        <text class="label">下单时间</text>
        <text class="value">{{ order.createdTime }}</text>
      </view>

      <!-- Cancel -->
      <view
        class="btn cancel-btn"
        v-if="order.status === 'CREATED'"
        @click="handleCancel"
      >取消订单</view>

      <!-- Rate -->
      <view
        class="btn rate-btn"
        v-if="order.status === 'COMPLETED'"
        @click="handleRate"
      >立即评价</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getOrderDetail, cancelOrder, rateOrder } from '../../api/order';

const order = ref(null);
const loading = ref(false);
const error = ref('');
const orderId = ref('');

const statusLabel = computed(() => {
  const map = { CREATED: '待处理', COMPLETED: '已完成', RATED: '已评价', CANCELLED: '已取消' };
  return map[order.value?.status] || order.value?.status || '';
});

const statusClass = computed(() => {
  const map = { CREATED: 'status-created', COMPLETED: 'status-completed', RATED: 'status-rated', CANCELLED: 'status-cancelled' };
  return map[order.value?.status] || '';
});

const load = async () => {
  if (!orderId.value) return;
  loading.value = true;
  error.value = '';
  try {
    const res = await getOrderDetail(orderId.value);
    order.value = res.data || null;
  } catch (e) {
    error.value = '订单信息加载失败，请返回重试';
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  uni.showModal({
    title: '确认取消',
    content: `确定要取消订单 #${order.value.orderId} 吗？`,
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await cancelOrder(order.value.orderId);
        uni.showToast({ title: '已取消' });
        load();
      } catch (e) {
        uni.showToast({ title: e.message || '取消失败', icon: 'none' });
      }
    }
  });
};

const makeCall = (phoneNum) => {
  uni.makePhoneCall({ phoneNumber: phoneNum });
};

const handleRate = () => {
  uni.showActionSheet({
    itemList: ['★★★★★ 非常满意', '★★★★ 满意', '★★★ 一般', '★★ 较差', '★ 非常差'],
    success: async (res) => {
      const rating = 5 - res.tapIndex;
      try {
        await rateOrder(order.value.orderId, rating);
        uni.showToast({ title: '评价成功' });
        load();
      } catch (e) {
        uni.showToast({ title: e.message || '评价失败', icon: 'none' });
      }
    }
  });
};

onLoad((query) => {
  orderId.value = query?.id || '';
  load();
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding: 16px 16px 32px;
  box-sizing: border-box;
}

.header {
  margin-bottom: 12px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: $color-text;
}

.state-box {
  background: $color-card;
  border-radius: 12px;
  border: 1px solid $color-border;
  padding: 28px 16px;
  text-align: center;
  box-shadow: 0 8px 16px $color-shadow;
}

.state-text {
  color: $color-muted;
  font-size: 18px;
}

.card {
  background: $color-card;
  border-radius: 12px;
  border: 1px solid $color-border;
  padding: 16px;
  box-shadow: 0 8px 16px $color-shadow;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 2px solid $color-border;
  gap: 12px;
}

.row:last-of-type {
  border-bottom: none;
}

.label {
  color: $color-muted;
  font-size: 18px;
  flex-shrink: 0;
}

.value {
  font-size: 18px;
  color: $color-text;
  text-align: right;
}

.status {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 700;
}

.contact-row {
  background: $color-bg;
  border: 1px solid $color-border;
  border-radius: 12px;
  padding: 12px 10px !important;
  margin-top: 4px;
}

.contact-phone-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.contact-phone {
  font-size: 18px;
  font-weight: 700;
  color: $color-primary;
}

.call-btn {
  background: $color-primary;
  color: #fff;
  font-size: 18px;
  height: 56px;
  line-height: 56px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1px solid $color-primary;
  flex-shrink: 0;
}

.worker-card {
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 12px;
  padding: 14px 12px;
  margin-top: 12px;
  box-shadow: 0 8px 16px $color-shadow;
}

.worker-title {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: $color-text;
  margin-bottom: 8px;
}

.worker-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.worker-name {
  font-size: 18px;
  color: $color-text;
  font-weight: 600;
}

.worker-phone-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.worker-phone {
  font-size: 18px;
  color: $color-primary;
  font-weight: 700;
}

.status-created  { background: #fff3e0; color: #e65100; }
.status-completed { background: #e8f5e9; color: #2e7d32; }
.status-rated    { background: #e3f2fd; color: #1565c0; }
.status-cancelled { background: #f5f5f5; color: #5f6368; }

.recurring-row {
  background: $color-bg;
  border: 1px solid $color-border;
  border-radius: 12px;
  padding: 10px !important;
}

.recurring-tag {
  color: #e65100;
  font-weight: 700;
}

.btn {
  border-radius: 12px;
  text-align: center;
  height: 56px;
  line-height: 56px;
  margin-top: 16px;
  font-size: 18px;
  font-weight: 700;
}

.cancel-btn {
  border: 1px solid $color-border;
  color: $color-text;
  background: $color-card;
}

.rate-btn {
  background: $color-primary;
  color: #fff;
  border: 1px solid $color-primary;
}
</style>
