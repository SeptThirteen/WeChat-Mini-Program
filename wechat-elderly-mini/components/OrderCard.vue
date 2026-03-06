<template>
  <view class="card" @click="$emit('click-detail')">
    <view class="row">
      <text class="name">订单 #{{ orderId }}</text>
      <text class="status" :class="statusClass">{{ statusLabel }}</text>
    </view>
    <text class="desc">服务：{{ serviceName || ('服务ID：' + serviceId) }}</text>
    <text class="desc recurring-badge" v-if="orderType === 'RECURRING'">🔁 长期预约 · {{ recurrenceRule }}</text>
    <text class="desc" v-if="createdTime">下单时间：{{ createdTime }}</text>

    <view class="actions" v-if="status === 'CREATED' || status === 'COMPLETED'">
      <view
        v-if="status === 'CREATED'"
        class="action-btn cancel"
        @click.stop="$emit('cancel')"
      >取消订单</view>
      <view
        v-if="status === 'COMPLETED'"
        class="action-btn rate"
        @click.stop="$emit('rate')"
      >评价</view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  orderId: Number,
  serviceId: Number,
  serviceName: String,
  status: String,
  createdTime: String,
  orderType: String,
  recurrenceRule: String
});

defineEmits(['click-detail', 'cancel', 'rate']);

const statusLabel = computed(() => {
  const map = { CREATED: '待处理', COMPLETED: '已完成', RATED: '已评价', CANCELLED: '已取消' };
  return map[props.status] || props.status;
});

const statusClass = computed(() => {
  const map = { CREATED: 'status-created', COMPLETED: 'status-completed', RATED: 'status-rated', CANCELLED: 'status-cancelled' };
  return map[props.status] || '';
});
</script>

<style lang="scss" scoped>
.card {
  background: $color-card;
  border-radius: 12px;
  padding: 12px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-size: 18px;
  font-weight: 600;
}

.status {
  font-size: 15px;
  padding: 2px 8px;
  border-radius: 8px;
}

.status-created  { background: #fff3e0; color: #e65100; }
.status-completed { background: #e8f5e9; color: #2e7d32; }
.status-rated    { background: #e3f2fd; color: #1565c0; }
.status-cancelled { background: #f5f5f5; color: #9e9e9e; }

.desc {
  display: block;
  margin-top: 6px;
  color: $color-muted;
  font-size: 15px;
}

.recurring-badge {
  color: #e65100;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.action-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 8px;
  font-size: 15px;
}

.cancel {
  border: 1px solid #ccc;
  color: #666;
}

.rate {
  background: $color-primary;
  color: #fff;
}
</style>
