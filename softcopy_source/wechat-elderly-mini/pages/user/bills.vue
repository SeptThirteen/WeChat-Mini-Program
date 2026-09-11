<template>
  <view class="page">
    <view class="page-header bills-header">
      <text class="header-title">缴费记录</text>
    </view>

    <!-- 类型筛选 -->
    <view class="filter-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="tab"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >{{ tab.label }}</view>
    </view>

    <!-- 查询表单 -->
    <view class="query-card">
      <input
        class="query-input"
        v-model="queryParams"
        :placeholder="queryPlaceholder"
      />
      <view class="query-btn" :class="{ disabled: querying }" @click="doQuery">
        {{ querying ? '查询中…' : '查询' }}
      </view>
    </view>

    <!-- 结果列表 -->
    <view class="record-list" v-if="records.length > 0">
      <view class="record-card" v-for="(r, i) in records" :key="i">
        <view class="record-header">
          <text class="record-type">{{ r.typeLabel }}</text>
          <text class="record-date">{{ r.date }}</text>
        </view>
        <view class="record-body">
          <text class="record-amount">¥{{ r.amount }}</text>
          <view class="record-status"><text>已缴费 ✅</text></view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-box" v-else-if="queried && !querying">
      <text class="empty-text">暂无缴费记录</text>
    </view>
    <view class="empty-box" v-else-if="!queried">
      <text class="empty-text">输入户号或账号后点击查询</text>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { queryBill, getBillHistory } from '../../api/bill';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const activeTab = ref('ALL');
const queryParams = ref('');
const querying = ref(false);
const queried = ref(false);
const allRecords = ref([]);

const tabs = [
  { key: 'ALL',         label: '全部' },
  { key: 'ELECTRICITY', label: '电费' },
  { key: 'WATER',       label: '水费' },
  { key: 'TV',          label: '有线/燃气' },
];

const queryPlaceholder = computed(() => {
  const map = {
    ALL: '请输入户号或账号',
    ELECTRICITY: '请输入电表户号',
    WATER: '请输入水表户号',
    TV: '请输入有线/燃气账号',
  };
  return map[activeTab.value] || '请输入户号或账号';
});

const records = computed(() => {
  if (activeTab.value === 'ALL') return allRecords.value;
  return allRecords.value.filter(r => r.type === activeTab.value);
});

const typeLabelMap = { ELECTRICITY: '电费', WATER: '水费', TV: '有线/燃气' };

const loadHistory = async () => {
  if (!userStore.userId) return;
  try {
    const res = await getBillHistory(userStore.userId);
    const list = res.data || [];
    allRecords.value = list.map(item => ({
      type: item.queryType,
      typeLabel: typeLabelMap[item.queryType] || item.queryType,
      date: item.createdTime ? item.createdTime.slice(0, 10) : '',
      amount: parseFloat(item.resultSnapshot?.match(/amount=([\\d.]+)/)?.[1] || '0'),
    }));
    if (list.length > 0) queried.value = true;
  } catch { /* ignore */ }
};

const doQuery = async () => {
  if (!queryParams.value.trim()) {
    uni.showToast({ title: queryPlaceholder.value, icon: 'none' });
    return;
  }
  querying.value = true;
  try {
    const types = activeTab.value === 'ALL'
      ? ['ELECTRICITY', 'WATER', 'TV']
      : [activeTab.value];
    const results = [];
    for (const t of types) {
      const res = await queryBill({
        queryType: t,
        queryParams: queryParams.value.trim(),
        userId: userStore.userId || undefined,
      });
      const amount = res.data?.amount || 0;
      const month = res.data?.month || new Date().toISOString().slice(0, 7);
      results.push({ type: t, typeLabel: typeLabelMap[t], date: month, amount });
    }
    allRecords.value = results;
    queried.value = true;
  } catch (e) {
    uni.showToast({ title: e.message || '查询失败，请重试', icon: 'none' });
  } finally {
    querying.value = false;
  }
};

onMounted(loadHistory);
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 32px;
  box-sizing: border-box;
}

.page-header {
  padding: 24px 20px 20px;
}

.bills-header {
  background: $gradient-brand;
  border-bottom: none;
}

.header-title {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

.filter-tabs {
  display: flex;
  background: $color-card;
  margin: 12px 16px 0;
  border-radius: 12px;
  border: 1px solid $color-border;
  padding: 4px;
  gap: 4px;
  box-shadow: 0 8px 16px $color-shadow;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 10px 4px;
  border-radius: 10px;
  font-size: 18px;
  color: $color-muted;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.tab.active {
  background: $color-primary;
  color: #fff;
  font-weight: 700;
}

.query-card {
  display: flex;
  gap: 10px;
  background: $color-card;
  border-radius: 12px;
  border: 1px solid $color-border;
  padding: 14px;
  margin: 12px 16px 0;
  align-items: center;
  box-shadow: 0 8px 16px $color-shadow;
}

.query-input {
  flex: 1;
  background: $color-card;
  border: 2px solid $color-border;
  border-radius: 12px;
  height: 56px;
  padding: 0 14px;
  font-size: 18px;
  color: $color-text;
  box-sizing: border-box;
}

.query-btn {
  background: $color-primary;
  color: #fff;
  border: 1px solid $color-primary;
  border-radius: 12px;
  height: 56px;
  padding: 0 22px;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.query-btn.disabled {
  opacity: 0.6;
}

.record-list {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-card {
  background: $color-card;
  border-radius: 12px;
  border: 1px solid $color-border;
  padding: 16px 20px;
  box-shadow: 0 8px 16px $color-shadow;
}

.record-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.record-type {
  font-size: 24px;
  color: $color-text;
  font-weight: 700;
}

.record-date {
  font-size: 18px;
  color: $color-muted;
}

.record-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-amount {
  font-size: 30px;
  color: $color-primary;
  font-weight: 700;
}

.record-status {
  background: #e8f5e9;
  color: #2e7d32;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 8px;
  font-weight: 700;
}

.empty-box {
  background: $color-card;
  border-radius: 12px;
  border: 1px solid $color-border;
  margin: 16px;
  padding: 40px 16px;
  text-align: center;
  box-shadow: 0 8px 16px $color-shadow;
}

.empty-text {
  font-size: 18px;
  color: $color-muted;
}
</style>
