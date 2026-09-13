<template>
  <view class="page">
    <view class="header">
      <text class="title">便民查询</text>
    </view>

    <!-- 官方渠道引导(真实账单) -->
    <view class="card official-card">
      <view class="official-head">
        <text class="official-title">查真实账单请用官方渠道</text>
        <text class="official-tag">推荐</text>
      </view>
      <view class="official-row" @click="showWechatPayGuide">
        <view class="official-info">
          <text class="official-name">微信 · 生活缴费</text>
          <text class="official-desc">电费 / 水费 / 燃气,全国城市通用</text>
        </view>
        <text class="official-action">查看步骤</text>
      </view>
      <view class="official-row official-row-last" @click="openSgcc">
        <view class="official-info">
          <text class="official-name">网上国网(国家电网官方)</text>
          <text class="official-desc">电费查询与缴纳,覆盖国家电网区域</text>
        </view>
        <text class="official-action">{{ sgccAppId ? '立即打开' : '去微信搜索' }}</text>
      </view>
    </view>

    <!-- Bill type selector (演示) -->
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

      <template v-if="result && typeof result === 'object' && result.amount !== undefined">
        <view class="amount-row">
          <text class="amount-label">本期{{ result.typeName }}账单</text>
          <text class="amount-value">¥{{ result.amount.toFixed(2) }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">账单月份</text>
          <text class="meta-value">{{ result.month }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">缴费账户</text>
          <text class="meta-value">{{ result.accountNo || '—' }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">缴费状态</text>
          <text class="meta-value status-pending">{{ result.status }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">缴费截止</text>
          <text class="meta-value">{{ result.dueDate }}</text>
        </view>

        <view class="divider"></view>
        <text class="section-label">账单明细</text>
        <view class="item-row" v-for="(it, i) in result.items" :key="i">
          <text class="item-name">{{ it.name }}</text>
          <text class="item-amount">¥{{ it.amount.toFixed(2) }}</text>
        </view>
        <text class="sim-note">* 演示项目：账单为模拟明细数据，非真实账单</text>
      </template>

      <text v-else class="result-content">{{ result }}</text>
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

// ============ 官方渠道引导 ============
// 拿到"网上国网"官方 appId 后填入即可启用一键跳转:
// 微信打开"网上国网"小程序 → 右上角"…" → 关于 → 更多资料 里可查 appId;
// 同时需在小程序管理后台配置跳转白名单(navigateToMiniProgramAppIdList)。
const SGCC_APPID = '';

const showWechatPayGuide = () => {
  const steps = [
    '1. 打开微信,点右上角"+"号',
    '2. 点"收付款"下方的"服务"',
    '3. 找到"生活缴费"点进去',
    '4. 选择城市和缴费类型(电费/水费/燃气)',
    '5. 输入户号即可查询和缴费'
  ];
  uni.showModal({
    title: '微信生活缴费 · 使用步骤',
    content: steps.join('\n'),
    confirmText: '知道了',
    showCancel: false
  });
};

const openSgcc = () => {
  if (SGCC_APPID) {
    uni.navigateToMiniProgram({
      appId: SGCC_APPID,
      success: () => {},
      fail: () => uni.showToast({ title: '打开失败,可微信搜索"网上国网"', icon: 'none', duration: 3000 })
    });
    return;
  }
  uni.setClipboardData({
    data: '网上国网',
    success: () => {
      const steps = [
        '1. 退出本小程序回到微信首页',
        '2. 从上往下滑出搜索框',
        '3. 粘贴"网上国网"并搜索',
        '4. 认准官方主体:国家电网有限公司客户服务中心'
      ];
      uni.showModal({
        title: '已为你复制小程序名',
        content: steps.join('\n'),
        confirmText: '知道了',
        showCancel: false
      });
    }
  });
};

const types = [
  { label: '电费', value: 'ELECTRICITY' },
  { label: '水费', value: 'WATER' },
  { label: '有线电视', value: 'TV' }
];

const queryType = ref('ELECTRICITY');
const queryParams = ref('');
const submitting = ref(false);
const result = ref(null);
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
    const data = res.data;
    let displayText;
    if (data && typeof data === 'object' && data.amount !== undefined) {
      result.value = data;
      displayText = `${data.month} ${data.typeName} ¥${Number(data.amount).toFixed(2)}（${data.status}）`;
    } else {
      const snapshot = data?.resultSnapshot || data || '查询成功，暂无账单数据';
      result.value = typeof snapshot === 'string' ? snapshot : JSON.stringify(snapshot);
      displayText = result.value;
    }

    const typeLabel = types.find((t) => t.value === queryType.value)?.label || queryType.value;
    history.value.unshift({ label: typeLabel, params: queryParams.value.trim(), result: displayText });
    if (history.value.length > 5) history.value.pop();
  } catch (e) {
    errorMsg.value = e.message || '查询失败，请检查户号后重试';
  } finally {
    submitting.value = false;
  }
};
</script>

<style lang="scss" scoped>
/* 官方渠道引导卡 */
.official-card {
  border: 2px solid $color-primary;
}

.official-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.official-title {
  flex: 1;
  font-size: 20px;
  font-weight: 700;
  color: $color-text;
}

.official-tag {
  flex-shrink: 0;
  background: $color-primary;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 8px;
}

.official-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid $color-border;
}

.official-row-last {
  border-bottom: none;
  padding-bottom: 4px;
}

.official-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.official-name {
  font-size: 18px;
  font-weight: 700;
  color: $color-text;
}

.official-desc {
  font-size: 15px;
  color: $color-muted;
}

.official-action {
  flex-shrink: 0;
  background: $color-primary;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  padding: 10px 16px;
  border-radius: 999px;
}

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

.amount-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 14px;
}

.amount-label {
  font-size: 20px;
  color: $color-muted;
}

.amount-value {
  font-size: 40px;
  font-weight: 700;
  color: $color-primary;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.meta-label {
  font-size: 18px;
  color: $color-muted;
}

.meta-value {
  font-size: 20px;
  color: $color-text;
  font-weight: 600;
}

.status-pending {
  color: #e65100;
  font-weight: 700;
}

.divider {
  height: 1px;
  background: $color-border;
  margin: 12px 0;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.item-name {
  font-size: 18px;
  color: $color-text;
  flex: 1;
  line-height: 1.5;
}

.item-amount {
  font-size: 20px;
  color: $color-text;
  font-weight: 700;
  margin-left: 12px;
}

.sim-note {
  display: block;
  font-size: 14px;
  color: $color-muted;
  margin-top: 10px;
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
