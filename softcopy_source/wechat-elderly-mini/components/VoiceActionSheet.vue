<template>
  <!-- 底部弹窗：语音快捷操作 -->
  <view class="voice-sheet-mask" v-if="show" @click.self="$emit('close')">
    <view class="voice-sheet">
      <view class="sheet-handle"></view>
      <text class="sheet-title">语音快捷操作</text>

      <!-- 上下文感知的快捷问题 -->
      <view class="quick-list">
        <view
          v-for="(item, idx) in quickItems"
          :key="idx"
          class="quick-item"
          @click="handleQuickItem(item)"
        >
          <text class="quick-text">{{ item.label }}</text>
        </view>
      </view>

      <!-- 直接语音提问 -->
      <view class="voice-record-row">
        <view class="voice-big-btn" :class="{ recording: isRecording }" @click="toggleRecord">
          <text class="voice-big-text">{{ isRecording ? '录音中 · 点击结束' : '点击说话提问' }}</text>
        </view>
        <text class="voice-hint" v-if="isRecording">{{ recordSec }}s / 60s</text>
        <text class="voice-hint" v-else>点击语音直接提问</text>
      </view>

      <!-- 去AI问答页 -->
      <view class="goto-ai" @click="goAiChat">
        <text class="goto-text">进入 AI 问答完整页面</text>
      </view>

      <view class="sheet-cancel" @click="$emit('close')">
        <text class="cancel-text">取消</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue';
import { startRecord, stopRecord, onRecordEnd, onRecordError } from '../utils/voiceRecorder';

const props = defineProps({
  show: { type: Boolean, default: false },
  context: { type: String, default: '' } // 'index' / 'order' / 'bill' / 'profile' / 'service'
});

const emit = defineEmits(['close', 'voiceResult', 'quickSelect']);

const isRecording = ref(false);
const recordSec = ref(0);
let timer = null;

// 根据上下文生成快捷问题
const quickItems = computed(() => {
  const base = [
    { label: '语音问社保/政务', intent: 'shengbao' },
    { label: '语音问健康', intent: 'health' },
    { label: '语音问帮扶服务', intent: 'bangfu' },
  ];

  const contextItems = {
    index: [
      { label: '如何预约帮扶服务？', intent: 'bangfu', preset: '如何预约帮扶服务' },
      { label: '查询水电费', intent: 'free', preset: '如何查询水电费' },
    ],
    order: [
      { label: '订单如何取消？', intent: 'free', preset: '如何取消订单' },
      { label: '如何评价订单？', intent: 'free', preset: '如何评价服务订单' },
    ],
    bill: [
      { label: '电费怎么查？', intent: 'free', preset: '如何查询电费' },
      { label: '水费怎么查？', intent: 'free', preset: '如何查询水费' },
    ],
    profile: [
      { label: '如何设置紧急联系人？', intent: 'free', preset: '如何设置紧急联系人' },
      { label: '如何查看政务代办？', intent: 'free', preset: '如何查看政务代办进度' },
    ],
    service: [
      { label: '这个服务怎么预约？', intent: 'bangfu', preset: '如何预约上门服务' },
      { label: '服务收费标准？', intent: 'bangfu', preset: '帮扶服务的收费标准是什么' },
    ]
  };

  const extra = contextItems[props.context] || [];
  return [...extra, ...base];
});

const handleQuickItem = (item) => {
  if (item.preset) {
    // 直接发文本问题
    emit('quickSelect', { intent: item.intent, text: item.preset });
    emit('close');
  } else {
    // 跳转到AI问答页对应 intent
    uni.navigateTo({
      url: `/pages/home/ai-chat?intent=${item.intent}`
    });
    emit('close');
  }
};

const toggleRecord = () => {
  if (isRecording.value) {
    stopRecording();
  } else {
    startRecording();
  }
};

const startRecording = () => {
  uni.authorize({
    scope: 'scope.record',
    success: () => {
      onRecordEnd((filePath, duration) => {
        isRecording.value = false;
        clearInterval(timer);
        emit('voiceResult', filePath);
        emit('close');
      });

      onRecordError(() => {
        isRecording.value = false;
        clearInterval(timer);
        uni.showToast({ title: '录音失败', icon: 'none' });
      });

      const ok = startRecord({ format: 'wav', sampleRate: 16000 });
      if (ok) {
        isRecording.value = true;
        recordSec.value = 0;
        timer = setInterval(() => {
          recordSec.value++;
          if (recordSec.value >= 60) stopRecording();
        }, 1000);
      }
    },
    fail: () => {
      uni.showModal({
        title: '需要录音权限',
        content: '请在设置中允许使用麦克风',
        showCancel: false
      });
    }
  });
};

const stopRecording = () => {
  stopRecord();
  clearInterval(timer);
};

const goAiChat = () => {
  emit('close');
  uni.navigateTo({ url: '/pages/home/ai-chat' });
};

// 关闭弹窗时清理录音
watch(() => props.show, (val) => {
  if (!val && isRecording.value) {
    stopRecording();
  }
});

onUnmounted(() => {
  clearInterval(timer);
});
</script>

<style lang="scss" scoped>
.voice-sheet-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 998;
  display: flex;
  align-items: flex-end;
}

.voice-sheet {
  background: $color-card;
  border-top: 3px solid $color-accent;
  border-radius: 12px 12px 0 0;
  width: 100%;
  padding: 12px 20px 32px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 -12px 24px $color-shadow;
}

.sheet-handle {
  width: 40px;
  height: 4px;
  background: $color-accent;
  border-radius: 2px;
  margin: 0 auto 14px;
}

.sheet-title {
  font-size: 24px;
  font-weight: 700;
  color: $color-text;
  display: block;
  margin-bottom: 16px;
}

.quick-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.quick-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: $color-card;
  border-radius: 12px;
  border: 1px solid $color-border;
}

.quick-text { flex: 1; font-size: 18px; color: $color-text; font-weight: 700; }

.voice-record-row {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px 0;
  border-top: 2px solid $color-border;
  border-bottom: 2px solid $color-border;
  margin-bottom: 12px;
}

.voice-big-btn {
  width: 100%;
  height: 64px;
  border-radius: 999px;
  background: $gradient-brand;
  border: 1px solid $color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.voice-big-btn.recording {
  background: #dc2626;
  border: 2px solid #dc2626;
}

.voice-big-text { font-size: 20px; color: #fff; font-weight: 700; }

.voice-hint {
  font-size: 18px;
  color: $color-muted;
}

.goto-ai {
  height: 56px;
  line-height: 56px;
  padding: 0 14px;
  text-align: center;
  margin-bottom: 8px;
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 12px;
  box-shadow: 0 6px 14px rgba(15, 61, 62, 0.08);
}

.goto-text {
  font-size: 18px;
  color: $color-primary;
  font-weight: 700;
}

.sheet-cancel {
  text-align: center;
  height: 56px;
  line-height: 56px;
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 12px;
  box-shadow: 0 6px 14px rgba(15, 61, 62, 0.08);
}

.cancel-text {
  font-size: 18px;
  color: $color-text;
  font-weight: 700;
}
</style>
