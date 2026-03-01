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
      <view class="row" v-if="order.address">
        <text class="label">服务地址</text>
        <text class="value">{{ order.address }}</text>
      </view>
      <view class="row" v-if="order.remark">
        <text class="label">备注</text>
        <text class="value">{{ order.remark }}</text>
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
  padding: 16px;
}

.header {
  margin-bottom: 12px;
}

.title {
  font-size: 22px;
  font-weight: 600;
}

.state-box {
  background: $color-card;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
}

.state-text {
  color: $color-muted;
  font-size: 16px;
}

.card {
  background: $color-card;
  border-radius: 12px;
  padding: 16px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5ede8;
}

.row:last-of-type {
  border-bottom: none;
}

.label {
  color: $color-muted;
  font-size: 16px;
}

.value {
  font-size: 16px;
  color: $color-text;
}

.status {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 14px;
}

.status-created  { background: #fff3e0; color: #e65100; }
.status-completed { background: #e8f5e9; color: #2e7d32; }
.status-rated    { background: #e3f2fd; color: #1565c0; }
.status-cancelled { background: #f5f5f5; color: #9e9e9e; }

.btn {
  border-radius: 10px;
  text-align: center;
  padding: 10px 0;
  margin-top: 16px;
}

.cancel-btn {
  border: 1px solid #ccc;
  color: #666;
}

.rate-btn {
  background: $color-primary;
  color: #fff;
}
</style>
