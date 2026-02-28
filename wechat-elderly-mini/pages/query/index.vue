<template>
  <view class="page">
    <view class="header">
      <text class="title">便民查询</text>
    </view>

    <!-- Bill type selector -->
    <view class="card">
      <text class="section-label">查询类型</text>
      <view class="type-row">
        <view
          v-for="t in types"
          :key="t.value"
          class="type-chip"
          :class="{ active: queryType === t.value }"
          @click="queryType = t.value"
        >{{ t.label }}</view>
      </view>

      <text class="section-label">户号 / 账号</text>
      <input
        class="input"
        v-model="queryParams"
        placeholder="请输入户号或账号"
      />

      <view class="btn" :class="{ disabled: submitting }" @click="handleQuery">
        {{ submitting ? '查询中…' : '立即查询' }}
      </view>
    </view>

    <!-- Result -->
    <view class="card result-card" v-if="result">
      <text class="result-title">查询结果</text>
      <text class="result-content">{{ result }}</text>
    </view>

    <!-- Error -->
    <view class="card result-card" v-if="errorMsg">
      <text class="result-error">{{ errorMsg }}</text>
    </view>

    <!-- History -->
    <view class="card" v-if="history.length > 0">
      <text class="section-label">最近查询</text>
      <view class="history-item" v-for="(h, i) in history" :key="i">
        <text class="history-type">{{ h.label }}</text>
        <text class="history-params">{{ h.params }}</text>
        <text class="history-result">{{ h.result }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { queryBill } from '../../api/bill';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();

const types = [
  { label: '电费', value: 'ELECTRICITY' },
  { label: '水费', value: 'WATER' },
  { label: '有线电视', value: 'TV' }
];

const queryType = ref('ELECTRICITY');
const queryParams = ref('');
const submitting = ref(false);
const result = ref('');
const errorMsg = ref('');
const history = ref([]);

const handleQuery = async () => {
  if (submitting.value) return;
  if (!queryParams.value.trim()) {
    uni.showToast({ title: '请输入户号或账号', icon: 'none' });
    return;
  }

  submitting.value = true;
  result.value = '';
  errorMsg.value = '';

  try {
    const res = await queryBill({
      queryType: queryType.value,
      queryParams: queryParams.value.trim(),
      userId: userStore.userId || undefined
    });
    const snapshot = res.data?.resultSnapshot || res.data || '查询成功，暂无账单数据';
    result.value = typeof snapshot === 'string' ? snapshot : JSON.stringify(snapshot);

    const typeLabel = types.find((t) => t.value === queryType.value)?.label || queryType.value;
    history.value.unshift({ label: typeLabel, params: queryParams.value.trim(), result: result.value });
    if (history.value.length > 5) history.value.pop();
  } catch (e) {
    errorMsg.value = e.message || '查询失败，请检查户号后重试';
  } finally {
    submitting.value = false;
  }
};
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
  padding: 16px;
  margin-bottom: 12px;
}

.section-label {
  display: block;
  font-size: 16px;
  color: $color-muted;
  margin-bottom: 8px;
}

.type-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.type-chip {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 20px;
  border: 1px solid #ddd;
  font-size: 16px;
  color: $color-muted;
}

.type-chip.active {
  background: $color-primary;
  border-color: $color-primary;
  color: #fff;
}

.input {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 14px;
  width: 100%;
  box-sizing: border-box;
}

.btn {
  background: $color-primary;
  color: #fff;
  border-radius: 10px;
  text-align: center;
  padding: 10px 0;
}

.btn.disabled {
  opacity: 0.6;
}

.result-card {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.result-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.result-content {
  display: block;
  font-size: 16px;
  color: $color-text;
  line-height: 1.6;
}

.result-error {
  display: block;
  color: #dc2626;
  font-size: 15px;
}

.history-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0e8e4;
}

.history-item:last-child {
  border-bottom: none;
}

.history-type {
  display: inline-block;
  background: $color-accent;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 6px;
}

.history-params {
  font-size: 15px;
  color: $color-text;
}

.history-result {
  display: block;
  font-size: 14px;
  color: $color-muted;
  margin-top: 4px;
}
</style>
