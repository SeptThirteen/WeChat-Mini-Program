<template>
  <view class="page">
    <view class="page-header didi-header">
      <text class="header-title">🟤 一呼馨家</text>
    </view>
    <view class="back-row" @click="goBack">
      <text class="back-text">← 返回首页</text>
    </view>

    <view class="grid" v-if="!loading">
      <view
        v-for="svc in didiServices"
        :key="svc.type"
        class="grid-item"
        @click="handleTap(svc)"
      >
        <text class="grid-icon">{{ svc.icon }}</text>
        <text class="grid-name">{{ svc.name }}</text>
      </view>
    </view>
    <view class="loading-box" v-else>
      <text class="loading-text">加载中…</text>
    </view>

    <view class="order-btn" @click="goOrders">查看我的服务订单</view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getServiceList } from '../../api/service';

const loading = ref(false);
const serviceMap = ref({});

const didiServices = [
  { type: '日间照护', name: '暖心陪伴', icon: '🌞' },
  { type: '外出陪同', name: '贴心出行', icon: '🚶' },
  { type: '代购',   name: '跑腿帮买',   icon: '🛒' },
  { type: '维修',   name: '上门维修',   icon: '🔧' },
];

const loadServices = async () => {
  loading.value = true;
  try {
    const res = await getServiceList();
    const list = res.data || [];
    list.forEach(item => {
      serviceMap.value[item.category] = item;
    });
  } catch (e) {
    // 静默失败，下单时再提示
  } finally {
    loading.value = false;
  }
};

const handleTap = (svc) => {
  const matched = serviceMap.value[svc.type];
  if (matched) {
    uni.navigateTo({
      url: `/pages/order/create?serviceId=${matched.serviceId}&serviceName=${encodeURIComponent(matched.displayName || matched.category)}&servicePrice=${matched.price}`
    });
  } else {
    uni.showToast({ title: '服务暂未开放，请稍后再试', icon: 'none' });
  }
};

const goBack = () => uni.navigateBack();
const goOrders = () => uni.navigateTo({ url: '/pages/order/list' });

onMounted(loadServices);
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

.didi-header {
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

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding: 20px 16px 8px;
}

.grid-item {
  background: $color-card;
  border-radius: 14px;
  border: 1px solid $color-border;
  padding: 30px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  box-shadow: 0 8px 16px $color-shadow;
}

.grid-icon {
  font-size: 44px;
}

.grid-name {
  font-size: $fontSize-md;
  color: $color-text;
  font-weight: 600;
}

.loading-box {
  padding: 40px 16px;
  text-align: center;
}

.loading-text {
  font-size: $fontSize-base;
  color: $color-muted;
}

.order-btn {
  margin: 12px 16px 0;
  background: $color-primary;
  border: 1px solid $color-primary;
  color: #fff;
  border-radius: 12px;
  text-align: center;
  height: 56px;
  line-height: 56px;
  font-size: 18px;
  font-weight: 700;
}
</style>
