<template>
  <view class="page">
    <view class="header">
      <text class="title">服务详情</text>
    </view>

    <view class="card" v-if="detail">
      <view class="row">
        <text class="name">{{ detail.category }}</text>
        <text class="price">¥{{ detail.price }}</text>
      </view>
      <text class="desc">{{ detail.description }}</text>
      <view class="btn" @click="handleCreateOrder">立即下单</view>
    </view>
    <view class="empty" v-else>
      <text class="desc">服务信息加载失败，请返回重试</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getServiceDetail, getServiceList } from '../../api/service';
import { createOrder } from '../../api/order';
import { useUserStore } from '../../store/user';

const detail = ref(null);
const userStore = useUserStore();
const serviceId = ref('');

const load = async () => {
  if (!serviceId.value) {
    detail.value = null;
    return;
  }
  try {
    const res = await getServiceDetail(serviceId.value);
    detail.value = res.data;
  } catch (e) {
    try {
      const listRes = await getServiceList();
      const list = listRes.data || [];
      detail.value = list.find((item) => String(item.serviceId) === String(serviceId.value)) || null;
    } catch (innerErr) {
      detail.value = null;
    }
  }
};

const handleCreateOrder = async () => {
  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    uni.navigateTo({ url: '/pages/login/login' });
    return;
  }
  try {
    await createOrder({ userId: userStore.userId, serviceId: detail.value.serviceId });
    uni.showToast({ title: '下单成功' });
    uni.navigateTo({ url: '/pages/order/list' });
  } catch (e) {
    uni.showToast({ title: e.message || '下单失败', icon: 'none' });
  }
};

onLoad((query) => {
  serviceId.value = query?.id || '';
  load();
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding: 16px;
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

.card {
  background: $color-card;
  border-radius: 12px;
  padding: 16px;
  border: 2px solid $color-border;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.name {
  font-size: 24px;
  color: $color-text;
  font-weight: 700;
}

.price {
  color: $color-primary;
  font-size: 24px;
  font-weight: 700;
}

.desc {
  display: block;
  margin-top: 12px;
  color: $color-muted;
  font-size: 18px;
  line-height: 1.6;
}

.empty {
  background: $color-card;
  border-radius: 12px;
  padding: 16px;
  border: 2px solid $color-border;
}

.btn {
  background: $color-primary;
  color: #fff;
  border-radius: 12px;
  text-align: center;
  height: 56px;
  line-height: 56px;
  margin-top: 12px;
  border: 2px solid $color-primary;
  font-size: 18px;
  font-weight: 700;
}
</style>
