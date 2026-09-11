<template>
  <view class="page">
    <view class="header">
      <text class="title">🎤 语音下单</text>
      <text class="subtitle">按下方按钮，说出您的需求</text>
    </view>

    <!-- 录音区 -->
    <view class="card record-card">
      <view
        class="mic-btn"
        :class="{ recording: state === 'recording', loading: state === 'uploading' }"
        @click="toggleRecord"
      >
        <text class="mic-icon">{{ state === 'recording' ? '🔴' : (state === 'uploading' ? '⏳' : '🎤') }}</text>
        <text class="mic-text">
          {{ state === 'recording' ? recordSec + 's · 点击结束' : (state === 'uploading' ? '正在理解您的话…' : '点击说话') }}
        </text>
      </view>
      <text class="record-hint" v-if="state === 'idle'">例如：“明天上午帮我修一下水龙头，地址幸福小区3号楼”</text>
      <text class="record-hint" v-if="state === 'recording'">说完请点击红色按钮结束</text>
    </view>

    <!-- 识别结果 -->
    <view class="card result-card" v-if="state === 'result' && intent">
      <text class="heard-label">我听到您说：</text>
      <text class="heard-text">“{{ queryText }}”</text>

      <view class="divider"></view>
      <view class="field-row">
        <text class="field-label">服务</text>
        <text class="field-value strong">{{ intent.serviceName }} · ¥{{ intent.servicePrice }}</text>
      </view>
      <view class="field-row">
        <text class="field-label">日期</text>
        <text class="field-value" :class="{ missing: !intent.date }">{{ intent.date || '到下一步再选' }}</text>
      </view>
      <view class="field-row">
        <text class="field-label">时间段</text>
        <text class="field-value" :class="{ missing: !intent.timeSlot }">{{ slotText || '到下一步再选' }}</text>
      </view>
      <view class="field-row">
        <text class="field-label">地址</text>
        <text class="field-value" :class="{ missing: !intent.address }">{{ intent.address || '到下一步再填' }}</text>
      </view>
      <view class="field-row" v-if="intent.remark">
        <text class="field-label">备注</text>
        <text class="field-value">{{ intent.remark }}</text>
      </view>

      <view class="btn confirm-btn" @click="confirmGoCreate">确认，去填写订单</view>
      <view class="btn retry-btn" @click="resetToIdle">再说一遍</view>
    </view>

    <!-- 失败提示 -->
    <view class="card error-card" v-if="state === 'error'">
      <text class="error-icon">😮</text>
      <text class="error-text">{{ errorMsg }}</text>
      <text class="heard-text" v-if="queryText">（我听到您说：“{{ queryText }}”）</text>
      <view class="btn confirm-btn" @click="resetToIdle">再试一次</view>
      <view class="btn retry-btn" @click="goManual">手动选服务</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { parseOrderIntent } from '../../api/ai';
import { useUserStore } from '../../store/user';
import { startRecord, stopRecord, onRecordEnd, onRecordError } from '../../utils/voiceRecorder';

const userStore = useUserStore();

const state = ref('idle'); // idle | recording | uploading | result | error
const recordSec = ref(0);
const queryText = ref('');
const intent = ref(null);
const errorMsg = ref('');

let timer = null;
const MAX_SEC = 60;

const slotTextMap = { morning: '上午', noon: '下午', afternoon: '傍晚' };
const slotText = computed(() => (intent.value?.timeSlot ? slotTextMap[intent.value.timeSlot] : ''));

onLoad(() => {
  onRecordEnd(handleRecordEnd);
  onRecordError(handleRecordError);
});

const toggleRecord = () => {
  if (state.value === 'uploading') return;
  if (state.value === 'recording') {
    stopRecord();
    return;
  }
  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }
  queryText.value = '';
  intent.value = null;
  errorMsg.value = '';
  recordSec.value = 0;
  if (startRecord()) {
    state.value = 'recording';
    timer = setInterval(() => {
      recordSec.value++;
      if (recordSec.value >= MAX_SEC) {
        stopRecord();
      }
    }, 1000);
  }
};

const handleRecordEnd = async (filePath) => {
  clearInterval(timer);
  if (!filePath) {
    state.value = 'error';
    errorMsg.value = '录音失败，请重试';
    return;
  }
  state.value = 'uploading';
  try {
    const res = await parseOrderIntent(filePath, userStore.userId, 'BAIDU');
    const data = res.data || {};
    queryText.value = data.queryText || '';
    if (data.error || !data.intent) {
      state.value = 'error';
      errorMsg.value = data.error || '没听懂您的需求，请再试一次';
    } else {
      intent.value = data.intent;
      state.value = 'result';
      uni.vibrateShort && uni.vibrateShort();
    }
  } catch (e) {
    state.value = 'error';
    errorMsg.value = (e && e.message) || '网络异常，请重试';
  }
};

const handleRecordError = () => {
  clearInterval(timer);
  if (state.value === 'recording' || state.value === 'uploading') {
    state.value = 'error';
    errorMsg.value = '录音出错，请检查麦克风权限后重试';
  }
};

const resetToIdle = () => {
  clearInterval(timer);
  state.value = 'idle';
  recordSec.value = 0;
};

/** ISO日期 → create.vue 的日期chip键（today/tomorrow/aftertomorrow），超出3天返回空走默认 */
const dateKeyOf = (iso) => {
  if (!iso || !/^\d{4}-\d{2}-\d{2}$/.test(iso)) return '';
  const d = new Date(iso.replace(/-/g, '/'));
  const now = new Date();
  const base = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const diff = Math.round((d - base) / 86400000);
  if (diff === 0) return 'today';
  if (diff === 1) return 'tomorrow';
  if (diff === 2) return 'aftertomorrow';
  return '';
};

const confirmGoCreate = () => {
  const it = intent.value;
  if (!it || !it.serviceId) {
    uni.showToast({ title: '服务信息缺失，请重试', icon: 'none' });
    return;
  }
  const params = [
    `serviceId=${it.serviceId}`,
    `serviceName=${encodeURIComponent(it.serviceName || '服务')}`,
    `servicePrice=${it.servicePrice ?? '0.00'}`,
    `date=${dateKeyOf(it.date)}`,
    `slot=${it.timeSlot || ''}`,
    `address=${encodeURIComponent(it.address || '')}`,
    `remark=${encodeURIComponent(it.remark || '')}`
  ].join('&');
  uni.redirectTo({ url: `/pages/order/create?${params}` });
};

const goManual = () => {
  uni.redirectTo({ url: '/pages/home/didi' });
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
  margin-bottom: 14px;
}

.title {
  display: block;
  font-size: $fontSize-title;
  font-weight: 700;
  color: $color-text;
}

.subtitle {
  display: block;
  font-size: $fontSize-base;
  color: $color-muted;
  margin-top: 6px;
}

.card {
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 14px;
  padding: 22px 18px;
  margin-bottom: 14px;
  box-shadow: 0 8px 16px $color-shadow;
}

.record-card {
  text-align: center;
}

.mic-btn {
  width: 100%;
  height: 150px;
  border-radius: 20px;
  background: $gradient-brand;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 20px $color-shadow;
}

.mic-btn.recording {
  background: linear-gradient(180deg, #ef5350 0%, #d32f2f 100%);
}

.mic-btn.loading {
  opacity: 0.75;
}

.mic-icon {
  font-size: 52px;
}

.mic-text {
  margin-top: 10px;
  font-size: $fontSize-md;
  font-weight: 700;
  color: #ffffff;
}

.record-hint {
  display: block;
  margin-top: 14px;
  font-size: $fontSize-sm;
  color: $color-muted;
  line-height: 1.5;
}

.heard-label {
  display: block;
  font-size: $fontSize-base;
  color: $color-muted;
}

.heard-text {
  display: block;
  margin-top: 8px;
  font-size: $fontSize-lg;
  font-weight: 700;
  color: $color-text;
  line-height: 1.5;
}

.divider {
  height: 1px;
  background: $color-border;
  margin: 16px 0;
}

.field-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 0;
}

.field-label {
  flex-shrink: 0;
  width: 90px;
  font-size: $fontSize-sm;
  color: $color-muted;
  padding-top: 2px;
}

.field-value {
  flex: 1;
  text-align: right;
  font-size: $fontSize-base;
  color: $color-text;
  font-weight: 600;
  line-height: 1.5;
  word-break: break-all;
}

.field-value.strong {
  color: $color-primary;
  font-weight: 700;
}

.field-value.missing {
  color: $color-muted;
  font-weight: 400;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: $btn-height;
  border-radius: 12px;
  font-size: $fontSize-md;
  font-weight: 700;
  margin-top: 14px;
  box-sizing: border-box;
}

.confirm-btn {
  background: $color-primary;
  border: 1px solid $color-primary;
  color: #ffffff;
}

.retry-btn {
  background: $color-card;
  border: 1px solid $color-border;
  color: $color-text;
}

.error-card {
  text-align: center;
}

.error-icon {
  display: block;
  font-size: 48px;
}

.error-text {
  display: block;
  margin-top: 12px;
  font-size: $fontSize-lg;
  font-weight: 700;
  color: $color-text;
  line-height: 1.5;
}

.error-card .heard-text {
  font-size: $fontSize-sm;
  font-weight: 400;
  color: $color-muted;
}
</style>
