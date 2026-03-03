<template>
  <view class="page">
    <view class="page-header ai-header">
      <text class="header-title">🟠 AI问答</text>
      <!-- AI引擎选择 -->
      <view class="provider-switch">
        <view
          class="provider-chip"
          :class="{ active: provider === 'BAIDU' }"
          @click="provider = 'BAIDU'"
        >百度文心</view>
        <view
          class="provider-chip"
          :class="{ active: provider === 'TENCENT' }"
          @click="provider = 'TENCENT'"
        >腾讯混元</view>
      </view>
    </view>
    <view class="back-row" @click="goBack">
      <text class="back-text">← 返回一级菜单</text>
    </view>

    <!-- 功能按钮列表 -->
    <view class="btn-list">
      <view
        v-for="item in items"
        :key="item.key"
        class="ai-btn"
        @click="handleItemClick(item)"
      >
        <text class="ai-icon">{{ item.icon }}</text>
        <view class="ai-text">
          <text class="ai-name">{{ item.name }}</text>
          <text class="ai-desc">{{ item.desc }}</text>
        </view>
        <text class="ai-arrow">▶</text>
      </view>
    </view>

    <!-- 录音浮层 -->
    <view class="record-overlay" v-if="showRecordPanel" @click.self="cancelRecord">
      <view class="record-panel">
        <text class="record-title">{{ recordTitle }}</text>
        <view class="record-wave" :class="{ active: recording }">
          <text class="wave-icon">{{ recording ? '🔴' : '🎤' }}</text>
        </view>
        <text class="record-hint">{{ recording ? '正在录音…松开停止' : '点击开始录音' }}</text>
        <view class="record-timer" v-if="recording">
          <text class="timer-text">{{ recordSeconds }}s / 60s</text>
        </view>
        <view class="record-actions">
          <view class="record-btn start-btn" v-if="!recording" @click="doStartRecord">
            <text class="record-btn-text">开始录音</text>
          </view>
          <view class="record-btn stop-btn" v-if="recording" @click="doStopRecord">
            <text class="record-btn-text">停止录音</text>
          </view>
          <view class="record-btn cancel-btn" @click="cancelRecord">
            <text class="record-btn-text">取消</text>
          </view>
        </view>
      </view>
    </view>

    <!-- FAQ 选择列表 -->
    <view class="faq-overlay" v-if="showFaqPanel" @click.self="showFaqPanel = false">
      <view class="faq-panel">
        <text class="faq-title">📢 常见问题语音播报</text>
        <text class="faq-subtitle">点击收听预录语音解答</text>
        <view class="faq-list">
          <view
            v-for="faq in faqList"
            :key="faq.id"
            class="faq-item"
            @click="playFaq(faq)"
          >
            <text class="faq-icon">{{ playingFaqId === faq.id ? '⏹️' : '▶️' }}</text>
            <view class="faq-info">
              <text class="faq-name">{{ faq.title }}</text>
              <text class="faq-desc">{{ faq.desc }}</text>
            </view>
          </view>
        </view>
        <view class="faq-close" @click="showFaqPanel = false">
          <text class="faq-close-text">关闭</text>
        </view>
      </view>
    </view>

    <!-- 对话结果区域 -->
    <view class="chat-section" v-if="chatMessages.length > 0">
      <view class="chat-header-row">
        <text class="chat-header">💬 对话记录</text>
        <text class="chat-clear" @click="chatMessages = []">清空</text>
      </view>
      <scroll-view class="chat-scroll" scroll-y :scroll-into-view="scrollToId">
        <view
          v-for="(msg, idx) in chatMessages"
          :key="idx"
          :id="'msg-' + idx"
          class="chat-bubble"
          :class="msg.role"
        >
          <text class="bubble-label">{{ msg.role === 'user' ? '🙋 我的问题' : '🤖 AI回答' }}</text>
          <text class="bubble-provider" v-if="msg.provider">{{ msg.provider === 'BAIDU' ? '百度文心' : '腾讯混元' }}</text>
          <text class="bubble-text">{{ msg.text }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 文本输入区（备用） -->
    <view class="text-input-section">
      <view class="input-row">
        <input
          class="text-input"
          v-model="textInput"
          placeholder="也可以打字提问…"
          confirm-type="send"
          @confirm="handleTextSend"
        />
        <view class="send-btn" :class="{ disabled: sending }" @click="handleTextSend">
          <text class="send-text">{{ sending ? '…' : '发送' }}</text>
        </view>
      </view>
    </view>

    <!-- Loading 遮罩 -->
    <view class="loading-overlay" v-if="sending">
      <view class="loading-box">
        <text class="loading-emoji">🤔</text>
        <text class="loading-text">AI正在思考中…</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useUserStore } from '../../store/user';
import { textQuery, voiceQuery, getFaqList, getFaqAudioUrl } from '../../api/ai';
import { startRecord, stopRecord, onRecordEnd, onRecordError } from '../../utils/voiceRecorder';
import { playAudio, stopAudio, destroyAudio } from '../../utils/audioPlayer';

const userStore = useUserStore();

// AI引擎选择
const provider = ref('BAIDU');

// 录音相关
const showRecordPanel = ref(false);
const recording = ref(false);
const recordSeconds = ref(0);
const recordTitle = ref('');
const currentIntent = ref('free');
let recordTimer = null;

// FAQ相关
const showFaqPanel = ref(false);
const faqList = ref([]);
const playingFaqId = ref('');

// 对话消息
const chatMessages = ref([]);
const scrollToId = ref('');

// 文本输入
const textInput = ref('');
const sending = ref(false);

// 功能按钮配置
const items = [
  { key: 'shengbao', name: '语音问社保/政务',   desc: '社保、医保、政务咨询',  icon: '🎤', type: 'voice', intent: 'shengbao' },
  { key: 'health',   name: '语音问健康',        desc: '慢病管理、用药咨询',    icon: '🎤', type: 'voice', intent: 'health' },
  { key: 'bangfu',   name: '语音问帮扶服务',    desc: '代购、维修、陪护问询',  icon: '🎤', type: 'voice', intent: 'bangfu' },
  { key: 'faq',      name: '常见问题语音播报',  desc: '点击收听常见解答',      icon: '📢', type: 'faq' },
  { key: 'free',     name: '直接语音输入提问',  desc: '全屏语音自由提问',      icon: '🎤', type: 'voice', intent: 'free' },
];

const goBack = () => uni.navigateBack();

// ========== 按钮点击路由 ==========
const handleItemClick = (item) => {
  if (!userStore.userId) {
    uni.showModal({
      title: '请先登录',
      content: '使用AI问答功能需要先登录',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/login/login' });
        }
      }
    });
    return;
  }

  if (item.type === 'faq') {
    openFaqPanel();
  } else if (item.type === 'voice') {
    openRecordPanel(item);
  }
};

// ========== 录音逻辑 ==========
const openRecordPanel = (item) => {
  currentIntent.value = item.intent || 'free';
  recordTitle.value = item.name;
  showRecordPanel.value = true;
  recording.value = false;
  recordSeconds.value = 0;
};

const doStartRecord = () => {
  uni.authorize({
    scope: 'scope.record',
    success: () => {
      onRecordEnd((filePath, duration) => {
        recording.value = false;
        clearInterval(recordTimer);
        showRecordPanel.value = false;
        handleVoiceResult(filePath);
      });

      onRecordError((err) => {
        recording.value = false;
        clearInterval(recordTimer);
        uni.showToast({ title: '录音失败，请重试', icon: 'none' });
      });

      const started = startRecord({ format: 'wav', sampleRate: 16000 });
      if (started) {
        recording.value = true;
        recordSeconds.value = 0;
        recordTimer = setInterval(() => {
          recordSeconds.value++;
          if (recordSeconds.value >= 60) {
            doStopRecord();
          }
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

const doStopRecord = () => {
  stopRecord();
  clearInterval(recordTimer);
};

const cancelRecord = () => {
  if (recording.value) {
    doStopRecord();
  }
  showRecordPanel.value = false;
};

// 语音录音结果 → 上传后端
const handleVoiceResult = async (filePath) => {
  sending.value = true;
  chatMessages.value.push({
    role: 'user',
    text: '🎤 [语音输入中…]',
    provider: provider.value
  });
  scrollToBottom();

  try {
    const res = await voiceQuery(filePath, userStore.userId, provider.value, currentIntent.value);
    const data = res.data;

    // 更新用户消息为识别后的文字
    chatMessages.value[chatMessages.value.length - 1].text = data.queryText || '[语音]';

    // 添加AI回复
    chatMessages.value.push({
      role: 'ai',
      text: data.responseText,
      provider: data.provider
    });
    scrollToBottom();
  } catch (e) {
    chatMessages.value.push({
      role: 'ai',
      text: e.message || '请求失败，请稍后再试',
      provider: provider.value
    });
    scrollToBottom();
  } finally {
    sending.value = false;
  }
};

// ========== 文本发送 ==========
const handleTextSend = async () => {
  const text = textInput.value.trim();
  if (!text || sending.value) return;

  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }

  chatMessages.value.push({ role: 'user', text, provider: provider.value });
  textInput.value = '';
  sending.value = true;
  scrollToBottom();

  try {
    const res = await textQuery({
      userId: userStore.userId,
      provider: provider.value,
      intent: 'free',
      text
    });
    chatMessages.value.push({
      role: 'ai',
      text: res.data.responseText,
      provider: res.data.provider
    });
    scrollToBottom();
  } catch (e) {
    chatMessages.value.push({
      role: 'ai',
      text: e.message || '请求失败，请稍后再试',
      provider: provider.value
    });
    scrollToBottom();
  } finally {
    sending.value = false;
  }
};

// ========== FAQ 逻辑 ==========
const openFaqPanel = async () => {
  showFaqPanel.value = true;
  if (faqList.value.length === 0) {
    try {
      const res = await getFaqList();
      faqList.value = res.data || [];
    } catch (e) {
      // 使用默认列表
      faqList.value = [
        { id: '001', title: '如何查询社保？', desc: '介绍社保查询的方式和步骤' },
        { id: '002', title: '如何预约帮扶服务？', desc: '说明预约上门服务的流程' },
        { id: '003', title: '水电费怎么查询？', desc: '水电费缴费查询方法' },
        { id: '004', title: '紧急联系人怎么设置？', desc: '设置紧急联系人的操作指南' },
        { id: '005', title: '订单取消/评价怎么操作？', desc: '订单取消和评价操作说明' },
        { id: '006', title: '政务代办如何申请？', desc: '政务代办的申办流程' },
      ];
    }
  }
};

const playFaq = (faq) => {
  if (playingFaqId.value === faq.id) {
    stopAudio();
    playingFaqId.value = '';
    return;
  }
  const url = getFaqAudioUrl(faq.id);
  playingFaqId.value = faq.id;
  playAudio(url, () => {
    playingFaqId.value = '';
  });
};

// ========== 辅助 ==========
const scrollToBottom = () => {
  nextTick(() => {
    scrollToId.value = '';
    nextTick(() => {
      scrollToId.value = 'msg-' + (chatMessages.value.length - 1);
    });
  });
};

// 接收从首页/个人中心语音按钮直接带过来的AI结果
onLoad((query) => {
  if (query && query.autoResult) {
    try {
      const data = JSON.parse(decodeURIComponent(query.autoResult));
      if (data.queryText) {
        chatMessages.value.push({ role: 'user', text: data.queryText, provider: data.provider || provider.value });
      }
      if (data.responseText) {
        chatMessages.value.push({ role: 'ai', text: data.responseText, provider: data.provider || provider.value });
        if (data.provider) provider.value = data.provider;
        nextTick(() => scrollToBottom());
      }
    } catch (e) {
      console.warn('autoResult parse error', e);
    }
  }
});

onMounted(() => {});

onUnmounted(() => {
  clearInterval(recordTimer);
  destroyAudio();
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 80px;
}

.page-header {
  padding: 24px 20px 16px;
}

.ai-header {
  background: linear-gradient(135deg, #C85A3A 0%, #F4A261 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.header-title {
  color: #fff;
  font-size: $fontSize-title;
  font-weight: 700;
}

.provider-switch {
  display: flex;
  gap: 8px;
}

.provider-chip {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: $fontSize-sm;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.provider-chip.active {
  color: $color-ai;
  background: #fff;
  border-color: #fff;
  font-weight: 600;
}

.back-row {
  padding: 14px 20px;
  background: $color-card;
  border-bottom: 1px solid #f0e8e4;
}

.back-text {
  font-size: $fontSize-base;
  color: $color-primary;
}

.btn-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ai-btn {
  background: $color-card;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.ai-icon { font-size: 30px; flex-shrink: 0; }
.ai-text { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.ai-name { font-size: $fontSize-md; color: $color-text; font-weight: 600; }
.ai-desc { font-size: $fontSize-sm; color: $color-muted; }
.ai-arrow { font-size: $fontSize-base; color: $color-muted; flex-shrink: 0; }

/* ========== 录音浮层 ========== */
.record-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-panel {
  background: #fff;
  border-radius: 24px;
  padding: 32px 28px;
  width: 80%;
  max-width: 340px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.record-title {
  font-size: $fontSize-md;
  font-weight: 700;
  color: $color-text;
}

.record-wave {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #fef2ee;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.record-wave.active {
  background: #fdd;
  box-shadow: 0 0 0 12px rgba(231, 111, 81, 0.15);
}

.wave-icon {
  font-size: 42px;
}

.record-hint {
  font-size: $fontSize-sm;
  color: $color-muted;
}

.record-timer {
  padding: 4px 14px;
  background: #fef2ee;
  border-radius: 12px;
}

.timer-text {
  font-size: $fontSize-sm;
  color: $color-primary;
  font-weight: 600;
}

.record-actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

.record-btn {
  flex: 1;
  text-align: center;
  padding: 14px 0;
  border-radius: 14px;
  font-size: $fontSize-base;
}

.start-btn { background: $color-primary; }
.stop-btn { background: #dc2626; }
.cancel-btn { background: #f5f5f5; }

.start-btn .record-btn-text,
.stop-btn .record-btn-text { color: #fff; font-weight: 600; }
.cancel-btn .record-btn-text { color: $color-muted; }

/* ========== FAQ浮层 ========== */
.faq-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.faq-panel {
  background: #fff;
  border-radius: 24px 24px 0 0;
  padding: 24px 20px 32px;
  width: 100%;
  max-height: 70vh;
}

.faq-title {
  font-size: $fontSize-md;
  font-weight: 700;
  color: $color-text;
  display: block;
  margin-bottom: 4px;
}

.faq-subtitle {
  font-size: $fontSize-sm;
  color: $color-muted;
  display: block;
  margin-bottom: 16px;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.faq-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: $color-bg;
  padding: 16px;
  border-radius: 14px;
}

.faq-icon { font-size: 24px; flex-shrink: 0; }
.faq-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.faq-name { font-size: $fontSize-base; color: $color-text; font-weight: 600; }
.faq-desc { font-size: $fontSize-sm; color: $color-muted; }

.faq-close {
  margin-top: 16px;
  text-align: center;
  padding: 14px;
  background: #f5f5f5;
  border-radius: 14px;
}

.faq-close-text {
  font-size: $fontSize-base;
  color: $color-muted;
}

/* ========== 对话区域 ========== */
.chat-section {
  padding: 0 16px 16px;
}

.chat-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.chat-header {
  font-size: $fontSize-md;
  font-weight: 700;
  color: $color-text;
}

.chat-clear {
  font-size: $fontSize-sm;
  color: $color-muted;
  padding: 4px 10px;
}

.chat-scroll {
  max-height: 400px;
  border-radius: 16px;
  background: $color-card;
  padding: 12px;
}

.chat-bubble {
  padding: 12px 14px;
  border-radius: 14px;
  margin-bottom: 10px;
}

.chat-bubble.user {
  background: #fef2ee;
  margin-left: 32px;
}

.chat-bubble.ai {
  background: #f0fdf4;
  margin-right: 32px;
}

.bubble-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  color: $color-muted;
}

.bubble-provider {
  display: inline-block;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 8px;
  background: $color-accent;
  color: #fff;
  margin-bottom: 6px;
}

.bubble-text {
  display: block;
  font-size: $fontSize-base;
  color: $color-text;
  line-height: 1.6;
  word-break: break-all;
}

/* ========== 文本输入区 ========== */
.text-input-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.text-input {
  flex: 1;
  background: $color-bg;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: $fontSize-base;
}

.send-btn {
  background: $color-primary;
  border-radius: 12px;
  padding: 12px 20px;
  flex-shrink: 0;
}

.send-btn.disabled {
  opacity: 0.6;
}

.send-text {
  color: #fff;
  font-size: $fontSize-base;
  font-weight: 600;
}

/* ========== Loading ========== */
.loading-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-box {
  background: #fff;
  border-radius: 20px;
  padding: 28px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading-emoji { font-size: 40px; }
.loading-text { font-size: $fontSize-base; color: $color-text; }
</style>
