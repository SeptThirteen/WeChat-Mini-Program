<template>
  <view class="page">
    <view class="header">
      <text class="title">社区服务</text>
    </view>

    <view class="emergency" @click="handleEmergency">
      <text class="emergency-title">一键呼叫</text>
      <text class="emergency-sub">紧急情况请及时求助</text>
    </view>

    <!-- Loading -->
    <view class="state-box" v-if="loading">
      <text class="state-text">加载中…</text>
    </view>

    <!-- Error -->
    <view class="state-box" v-else-if="error">
      <text class="state-text">{{ error }}</text>
      <view class="retry-btn" @click="load">重新加载</view>
    </view>

    <!-- Empty -->
    <view class="state-box" v-else-if="services.length === 0">
      <text class="state-text">暂无服务，请稍后再试</text>
    </view>

    <!-- List -->
    <view v-else>
      <view class="list">
        <ServiceCard
          v-for="item in pagedServices"
          :key="item.serviceId"
          :title="item.category"
          :desc="item.description"
          :price="item.price"
          class="list-item"
          @click="goDetail(item.serviceId)"
        />
      </view>

      <!-- Pagination -->
      <view class="pagination" v-if="totalPages > 1">
        <view class="page-btn" :class="{ disabled: currentPage === 1 }" @click="prevPage">上一页</view>
        <text class="page-info">{{ currentPage }} / {{ totalPages }}</text>
        <view class="page-btn" :class="{ disabled: currentPage === totalPages }" @click="nextPage">下一页</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { getServiceList } from '../../api/service';
import ServiceCard from '../../components/ServiceCard.vue';

const services = ref([]);
const loading = ref(false);
const error = ref('');
const currentPage = ref(1);
const pageSize = 5;

const totalPages = computed(() => Math.max(1, Math.ceil(services.value.length / pageSize)));
const pagedServices = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return services.value.slice(start, start + pageSize);
});

const prevPage = () => { if (currentPage.value > 1) currentPage.value -= 1; };
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value += 1; };

const load = async () => {
  loading.value = true;
  error.value = '';
  currentPage.value = 1;
  try {
    const res = await getServiceList();
    services.value = res.data || [];
  } catch (e) {
    error.value = '服务列表加载失败，请检查网络后重试';
  } finally {
    loading.value = false;
  }
};

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/service/detail?id=${id}` });
};

const handleEmergency = () => {
  uni.showModal({
    title: '确认呼叫',
    content: '是否拨打紧急电话 120？',
    success: (res) => {
      if (res.confirm) {
        uni.makePhoneCall({ phoneNumber: '120' });
      }
    }
  });
};

onMounted(load);
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

.emergency {
  background: $color-accent;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 14px;
  color: #fff;
}

.emergency-title {
  font-size: 22px;
  font-weight: 700;
}

.emergency-sub {
  display: block;
  margin-top: 6px;
  font-size: 16px;
  opacity: 0.95;
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

.retry-btn {
  margin-top: 12px;
  background: $color-primary;
  color: #fff;
  border-radius: 8px;
  padding: 8px 24px;
  display: inline-block;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  display: block;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 12px;
  padding-bottom: 8px;
}

.page-btn {
  background: $color-primary;
  color: #fff;
  border-radius: 8px;
  padding: 6px 18px;
  font-size: 15px;
}

.page-btn.disabled {
  background: #ddd;
  color: #999;
}

.page-info {
  font-size: 15px;
  color: $color-muted;
}
</style>
