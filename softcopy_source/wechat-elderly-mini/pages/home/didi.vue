<template>
  <view class="page">
    <view class="page-header didi-header">
      <text class="header-title">一呼馨家</text>
      <view class="voice-order-btn" @click="goVoiceOrder">
        <text class="voice-order-text">语音下单</text>
      </view>
    </view>
    <view class="back-row" @click="goBack">
      <text class="back-text">← 返回首页</text>
    </view>

    <view class="row-list" v-if="!loading">
      <view
        v-for="(svc, idx) in didiServices"
        :key="svc.type"
        class="func-row"
        :class="{ 'func-row-last': idx === didiServices.length - 1 }"
        hover-class="row-pressed"
        @click="handleTap(svc)"
      >
        <view class="row-main">
          <text class="row-title">{{ svc.name }}</text>
          <text class="row-desc">上门服务 · {{ svc.type }}</text>
        </view>
        <text class="row-price" v-if="serviceMap[svc.type]">¥{{ serviceMap[svc.type].price }}</text>
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
  { type: '日间照护', name: '暖心陪伴' },
  { type: '外出陪同', name: '贴心出行' },
  { type: '代购',   name: '跑腿帮买' },
  { type: '维修',   name: '上门维修' },
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
const goVoiceOrder = () => uni.navigateTo({ url: '/pages/order/voice-create' });

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
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

.voice-order-btn {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 999px;
  padding: 10px 20px;
  box-shadow: 0 4px 10px rgba(22, 78, 99, 0.18);
}

.voice-order-text {
  font-size: 18px;
  font-weight: 700;
  color: $color-primary;
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

/* 竖排服务列表:名称+价格,纯文字 */
.row-list {
  margin: 16px;
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 14px;
  overflow: hidden;
}

.func-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid $color-border;
}

.func-row-last {
  border-bottom: none;
}

.row-pressed {
  background: $color-bg;
}

.row-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.row-title {
  font-size: 22px;
  color: $color-text;
  font-weight: 700;
}

.row-desc {
  font-size: 16px;
  color: $color-muted;
}

.row-price {
  flex-shrink: 0;
  font-size: $fontSize-md;
  font-weight: 700;
  color: $color-primary;
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
