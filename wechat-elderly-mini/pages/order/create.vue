<template>
  <view class="page">
    <view class="page-header create-header">
      <text class="header-title">{{ serviceName }} · 下单</text>
    </view>

    <view class="voice-hint">
      <text class="hint-text">🎤 语音填信息 / 点选时间</text>
      <view class="voice-small-btn" @click="handleVoice">语音填写</view>
    </view>

    <!-- 预约时间 -->
    <view class="section-card">
      <text class="section-label">预约日期</text>
      <view class="chip-row">
        <view
          v-for="d in dateOptions"
          :key="d.key"
          class="chip"
          :class="{ active: selectedDate === d.key }"
          @click="selectedDate = d.key"
        >{{ d.label }}</view>
      </view>
      <text class="section-label" style="margin-top: 12px;">时间段</text>
      <view class="chip-row">
        <view
          v-for="s in slotOptions"
          :key="s.key"
          class="chip"
          :class="{ active: selectedSlot === s.key }"
          @click="selectedSlot = s.key"
        >{{ s.label }}</view>
      </view>
    </view>

    <!-- 服务地址 -->
    <view class="section-card">
      <text class="section-label">服务地址</text>
      <input
        class="text-input"
        v-model="address"
        placeholder="请输入详细地址（小区/楼栋/房号）"
      />
    </view>

    <!-- 备注 -->
    <view class="section-card">
      <text class="section-label">备注</text>
      <input
        class="text-input"
        v-model="remark"
        placeholder="可补充特别说明（可选）"
      />
    </view>

    <!-- 提交 -->
    <view class="price-tip">
      <text class="price-text">服务完成后付费 · 预估 ¥{{ servicePrice }}</text>
    </view>
    <view class="submit-btn" @click="handleSubmit">
      确认下单（预估¥{{ servicePrice }}·后付费）
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const serviceId = ref('');
const serviceName = ref('服务');
const servicePrice = ref('0.00');
const selectedDate = ref('today');
const selectedSlot = ref('morning');
const address = ref('');
const remark = ref('');

const today = new Date();
const tomorrow = new Date(today); tomorrow.setDate(today.getDate() + 1);
const afterTomorrow = new Date(today); afterTomorrow.setDate(today.getDate() + 2);

const fmt = (d) => `${d.getMonth() + 1}月${d.getDate()}日`;

const dateOptions = [
  { key: 'today',        label: `今天 ${fmt(today)}` },
  { key: 'tomorrow',     label: `明天 ${fmt(tomorrow)}` },
  { key: 'aftertomorrow', label: `后天 ${fmt(afterTomorrow)}` },
];

const slotOptions = [
  { key: 'morning',   label: '上午 9-11点' },
  { key: 'noon',      label: '下午 14-16点' },
  { key: 'afternoon', label: '傍晚 17-19点' },
];

const getDateStr = (key) => {
  const map = { today, tomorrow, aftertomorrow: afterTomorrow };
  const d = map[key];
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const getSlotStr = (key) => {
  const map = { morning: '9-11点', noon: '14-16点', afternoon: '17-19点' };
  return map[key];
};

const handleVoice = () => uni.showToast({ title: '语音功能开发中', icon: 'none' });

const handleSubmit = () => {
  if (!address.value.trim()) {
    uni.showToast({ title: '请填写服务地址', icon: 'none' });
    return;
  }
  const params = {
    serviceId: serviceId.value,
    serviceName: serviceName.value,
    servicePrice: servicePrice.value,
    dateStr: getDateStr(selectedDate.value),
    slotStr: getSlotStr(selectedSlot.value),
    address: address.value.trim(),
    remark: remark.value.trim(),
  };
  uni.navigateTo({
    url: `/pages/order/confirm?params=${encodeURIComponent(JSON.stringify(params))}`
  });
};

onLoad((query) => {
  serviceId.value = query?.serviceId || '';
  serviceName.value = decodeURIComponent(query?.serviceName || '服务');
  servicePrice.value = query?.servicePrice || '0.00';
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 32px;
}

.page-header {
  padding: 24px 20px 20px;
}

.create-header {
  background: linear-gradient(135deg, #7B3F1F 0%, #A0522D 100%);
}

.header-title {
  color: #fff;
  font-size: $fontSize-lg;
  font-weight: 700;
}

.voice-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff9f0;
  margin: 12px 16px 0;
  border-radius: 10px;
  border: 1px dashed $color-accent;
}

.hint-text {
  font-size: $fontSize-base;
  color: $color-text;
}

.voice-small-btn {
  background: $color-accent;
  color: #fff;
  border-radius: 20px;
  padding: 6px 16px;
  font-size: $fontSize-sm;
}

.section-card {
  background: $color-card;
  border-radius: 14px;
  padding: 16px;
  margin: 12px 16px 0;
}

.section-label {
  display: block;
  font-size: $fontSize-base;
  color: $color-muted;
  margin-bottom: 10px;
  font-weight: 500;
}

.chip-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  flex: 1;
  min-width: 80px;
  text-align: center;
  padding: 10px 6px;
  border-radius: 10px;
  border: 1px solid #ddd;
  font-size: $fontSize-sm;
  color: $color-muted;
  white-space: nowrap;
}

.chip.active {
  background: $color-primary;
  border-color: $color-primary;
  color: #fff;
}

.text-input {
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px;
  width: 100%;
  box-sizing: border-box;
  font-size: $fontSize-base;
}

.price-tip {
  text-align: center;
  padding: 12px 16px 4px;
}

.price-text {
  font-size: $fontSize-base;
  color: $color-muted;
}

.submit-btn {
  margin: 10px 16px 0;
  background: $color-primary;
  color: #fff;
  border-radius: 14px;
  text-align: center;
  padding: 18px 0;
  font-size: $fontSize-md;
  font-weight: 600;
}
</style>
