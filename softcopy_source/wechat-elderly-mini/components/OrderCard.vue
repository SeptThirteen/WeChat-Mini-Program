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
  border-radius: 14px;
  padding: 16px;
  border: 1px solid $color-border;
  margin-bottom: 12px;
  box-shadow: 0 8px 16px $color-shadow;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-size: 22px;
  font-weight: 700;
  color: $color-text;
}

.status {
  font-size: 18px;
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 700;
}

.status-created  { background: #fff3e0; color: #e65100; border: 1px solid #ffb74d; }
.status-completed { background: #e8f5e9; color: #2e7d32; border: 1px solid #a5d6a7; }
.status-rated    { background: #e3f2fd; color: #1565c0; border: 1px solid #90caf9; }
.status-cancelled { background: #f5f5f5; color: #5f6368; border: 1px solid #e0e0e0; }

.desc {
  display: block;
  margin-top: 8px;
  color: $color-muted;
  font-size: 18px;
}

.recurring-badge {
  color: #e65100;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 16px;
  margin-top: 20px;
}

.action-btn {
  flex: 1;
  text-align: center;
  height: 56px;
  line-height: 56px;
  border-radius: 12px;
  font-size: 20px;
  font-weight: 700;
}

.cancel {
  border: 1px solid $color-border;
  color: $color-text;
  background: $color-card;
}
.rate {
  background: $color-primary;
  color: #ffffff;
  border: 1px solid $color-primary;
}
</style>
