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
    uni.switchTab({ url: '/pages/order/list' });
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
  padding: 16px;
}

.header {
  margin-bottom: 12px;
}

.title {
  font-size: 22px;
  font-weight: 600;
}

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
  font-size: 20px;
}

.price {
  color: $color-primary;
}

.desc {
  display: block;
  margin-top: 8px;
  color: $color-muted;
}

.empty {
  background: $color-card;
  border-radius: 12px;
  padding: 16px;
}

.btn {
  background: $color-primary;
  color: #fff;
  border-radius: 10px;
  text-align: center;
  padding: 10px 0;
  margin-top: 12px;
}
</style>
