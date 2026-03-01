<template>
  <view class="page">
    <view class="header">
      <text class="title">我的订单</text>
    </view>

    <!-- Not logged in -->
    <view class="state-box" v-if="!userId">
      <text class="state-text">请先登录查看订单</text>
      <view class="action-btn" @click="goLogin">去登录</view>
    </view>

    <template v-else>
      <!-- Status Tabs -->
      <view class="filter-tabs">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="tab"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >{{ tab.label }}</view>
      </view>

      <!-- Loading -->
      <view class="state-box" v-if="loading">
        <text class="state-text">加载中…</text>
      </view>

      <!-- Error -->
      <view class="state-box" v-else-if="error">
        <text class="state-text">{{ error }}</text>
        <view class="action-btn" @click="load">重新加载</view>
      </view>

      <!-- Empty -->
      <view class="state-box" v-else-if="orders.length === 0">
        <text class="state-text">暂无订单</text>
      </view>

      <!-- List -->
      <view v-else>
        <view class="list">
          <OrderCard
            v-for="item in pagedOrders"
            :key="item.orderId"
            :orderId="item.orderId"
            :serviceId="item.serviceId"
            :serviceName="item.serviceName"
            :status="item.status"
            :createdTime="item.createdTime"
            class="list-item"
            @click-detail="goDetail(item.orderId)"
            @cancel="handleCancel(item)"
            @rate="handleRate(item)"
          />
        </view>

        <!-- Pagination -->
        <view class="pagination" v-if="totalPages > 1">
          <view class="page-btn" :class="{ disabled: currentPage === 1 }" @click="prevPage">上一页</view>
          <text class="page-info">{{ currentPage }} / {{ totalPages }}</text>
          <view class="page-btn" :class="{ disabled: currentPage === totalPages }" @click="nextPage">下一页</view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { getOrderList, cancelOrder, rateOrder } from '../../api/order';
import { useUserStore } from '../../store/user';
import OrderCard from '../../components/OrderCard.vue';

const orders = ref([]);
const loading = ref(false);
const error = ref('');
const userStore = useUserStore();
const userId = computed(() => userStore.userId);
const currentPage = ref(1);
const pageSize = 5;
const activeTab = ref('ALL');

const tabs = [
  { key: 'ALL',       label: '全部' },
  { key: 'CREATED',   label: '进行中' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'CANCELLED', label: '已取消' },
];

const totalPages = computed(() => Math.max(1, Math.ceil(orders.value.length / pageSize)));
const pagedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return orders.value.slice(start, start + pageSize);
});

const prevPage = () => { if (currentPage.value > 1) currentPage.value -= 1; };
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value += 1; };

const switchTab = (key) => {
  activeTab.value = key;
  load();
};

const load = async () => {
  if (!userStore.userId) return;
  loading.value = true;
  error.value = '';
  currentPage.value = 1;
  try {
    const status = activeTab.value === 'ALL' ? undefined : activeTab.value;
    const res = await getOrderList(userStore.userId, status);
    orders.value = res.data || [];
  } catch (e) {
    error.value = '订单加载失败，请重试';
  } finally {
    loading.value = false;
  }
};

const goDetail = (orderId) => {
  uni.navigateTo({ url: `/pages/order/detail?id=${orderId}` });
};

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' });
};

const handleCancel = (item) => {
  uni.showModal({
    title: '确认取消',
    content: `确定要取消订单 #${item.orderId} 吗？`,
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await cancelOrder(item.orderId);
        uni.showToast({ title: '已取消' });
        load();
      } catch (e) {
        uni.showToast({ title: e.message || '取消失败', icon: 'none' });
      }
    }
  });
};

const handleRate = (item) => {
  uni.showActionSheet({
    itemList: ['★★★★★ 非常满意', '★★★★ 满意', '★★★ 一般', '★★ 较差', '★ 非常差'],
    success: async (res) => {
      const rating = 5 - res.tapIndex;
      try {
        await rateOrder(item.orderId, rating);
        uni.showToast({ title: '评价成功' });
        load();
      } catch (e) {
        uni.showToast({ title: e.message || '评价失败', icon: 'none' });
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

.filter-tabs {
  display: flex;
  background: $color-card;
  border-radius: 12px;
  padding: 4px;
  gap: 4px;
  margin-bottom: 12px;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 10px 4px;
  border-radius: 10px;
  font-size: 15px;
  color: $color-muted;
}

.tab.active {
  background: $color-primary;
  color: #fff;
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

.action-btn {
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
