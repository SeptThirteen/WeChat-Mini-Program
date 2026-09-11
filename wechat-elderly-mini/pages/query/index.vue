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
import { onLoad } from '@dcloudio/uni-app';
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

onLoad((query) => {
  if (query?.type && types.some(t => t.value === query.type)) {
    queryType.value = query.type;
  }
});

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
  background: $color-bg;
  min-height: 100vh;
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
  border: 1px solid $color-border;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 8px 16px $color-shadow;
}

.section-label {
  display: block;
  font-size: 18px;
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
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  border: 1px solid $color-border;
  font-size: 18px;
  color: $color-muted;
  background: $color-card;
  box-sizing: border-box;
}

.type-chip.active {
  background: $color-primary;
  border: 1px solid $color-primary;
  color: #fff;
  font-weight: 700;
}

.input {
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 12px;
  height: 56px;
  padding: 0 14px;
  margin-bottom: 14px;
  width: 100%;
  box-sizing: border-box;
  font-size: 18px;
  color: $color-text;
}

.btn {
  background: $color-primary;
  color: #fff;
  border: 1px solid $color-primary;
  border-radius: 12px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  box-sizing: border-box;
}

.btn.disabled {
  opacity: 0.6;
}

.result-card {
  background: $color-card;
  border: 1px solid $color-border;
  box-shadow: 0 8px 16px $color-shadow;
}

.result-title {
  display: block;
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  color: $color-text;
}

.result-content {
  display: block;
  font-size: 24px;
  color: $color-text;
  line-height: 1.6;
}

.result-error {
  display: block;
  color: #dc2626;
  font-size: 18px;
}

.history-item {
  padding: 12px 0;
  border-bottom: 2px solid $color-border;
}

.history-item:last-child {
  border-bottom: none;
}

.history-type {
  display: inline-block;
  background: $color-primary;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 8px;
  margin-right: 8px;
  margin-bottom: 6px;
}

.history-params {
  font-size: 24px;
  color: $color-text;
  font-weight: 700;
  display: block;
}

.history-result {
  display: block;
  font-size: 18px;
  color: $color-muted;
  margin-top: 6px;
}
</style>
