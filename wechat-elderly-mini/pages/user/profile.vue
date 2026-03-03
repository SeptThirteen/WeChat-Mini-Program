<template>
  <view class="page">
    <view class="top-bar">
      <text class="time">{{ currentTime }}</text>
      <view class="voice-btn" @click="showVoiceSheet = true">
        <text class="voice-text">语音说需求 🔊</text>
      </view>
    </view>

    <view class="page-title-row">
      <text class="page-title">我的</text>
    </view>

    <!-- 未登录 -->
    <view class="login-card" v-if="!userId">
      <text class="login-tip">您还未登录</text>
      <view class="login-btn" @click="goLogin">立即登录</view>
    </view>

    <!-- 已登录：头像 + 信息 -->
    <view class="profile-card" v-else>
      <view class="avatar-row">
        <view class="avatar">
          <text class="avatar-text">{{ profile.name ? profile.name.charAt(0) : '用' }}</text>
        </view>
        <view class="profile-info">
          <text class="profile-name">{{ profile.name || '未设置姓名' }}</text>
          <text class="profile-meta" v-if="profile.age">{{ profile.age }}岁{{ profile.address ? ' · ' + profile.address : '' }}</text>
          <text class="profile-meta">手机：{{ phone || '未绑定' }}</text>
          <text class="profile-meta">绑定社区：{{ profile.community || '未绑定社区' }}</text>
        </view>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="func-list" v-if="userId && !loading">
      <view class="func-card" @click="go('/pages/user/bills')">
        <text class="func-icon">💰</text>
        <view class="func-text">
          <text class="func-title">缴费记录</text>
          <text class="func-desc">查看水电费缴费历史</text>
        </view>
        <text class="func-arrow">▶</text>
      </view>

      <view class="func-card" @click="go('/pages/user/gov-tasks')">
        <text class="func-icon">📋</text>
        <view class="func-text">
          <text class="func-title">政务代办进度</text>
          <text class="func-desc">查看申请办理进度</text>
        </view>
        <text class="func-arrow">▶</text>
      </view>

      <view class="func-card" @click="go('/pages/order/list')">
        <text class="func-icon">🚗</text>
        <view class="func-text">
          <text class="func-title">滴滴摇人订单</text>
          <text class="func-desc">查看全部帮扶服务订单</text>
        </view>
        <text class="func-arrow">▶</text>
      </view>

      <view class="func-card" @click="go('/pages/user/emergency')">
        <text class="func-icon">🆘</text>
        <view class="func-text">
          <text class="func-title">紧急联系人设置</text>
          <text class="func-desc">设置紧急求助联系人</text>
        </view>
        <text class="func-arrow">▶</text>
      </view>
    </view>

    <view class="loading-box" v-if="userId && loading">
      <text class="loading-text">加载中…</text>
    </view>

    <!-- 退出登录 -->
    <view class="logout-btn" v-if="userId" @click="handleLogout">退出登录</view>

    <!-- 语音快捷操作弹窗 -->
    <VoiceActionSheet
      :show="showVoiceSheet"
      context="profile"
      @close="showVoiceSheet = false"
      @voiceResult="handleVoiceResult"
      @quickSelect="handleQuickSelect"
    />
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { getUserProfile } from '../../api/user';
import { useUserStore } from '../../store/user';
import VoiceActionSheet from '../../components/VoiceActionSheet.vue';
import { voiceQuery, textQuery } from '../../api/ai';

const userStore = useUserStore();
const userId = computed(() => userStore.userId);
const phone = computed(() => userStore.phone);
const profile = ref({});
const loading = ref(false);
const currentTime = ref('');
const showVoiceSheet = ref(false);

const updateTime = () => {
  const now = new Date();
  currentTime.value = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
};

const go = (url) => uni.navigateTo({ url });
const goLogin = () => uni.navigateTo({ url: '/pages/login/login' });

const handleVoiceResult = async (filePath) => {
  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }
  uni.showLoading({ title: 'AI思考中…' });
  try {
    const res = await voiceQuery(filePath, userStore.userId, 'BAIDU', 'free');
    uni.hideLoading();
    uni.navigateTo({
      url: `/pages/home/ai-chat?autoResult=${encodeURIComponent(JSON.stringify(res.data))}`
    });
  } catch (e) {
    uni.hideLoading();
    uni.showToast({ title: e.message || '语音识别失败', icon: 'none' });
  }
};

const handleQuickSelect = async ({ intent, text }) => {
  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }
  uni.showLoading({ title: 'AI思考中…' });
  try {
    const res = await textQuery({ userId: userStore.userId, provider: 'BAIDU', intent, text });
    uni.hideLoading();
    uni.navigateTo({
      url: `/pages/home/ai-chat?autoResult=${encodeURIComponent(JSON.stringify(res.data))}`
    });
  } catch (e) {
    uni.hideLoading();
    uni.showToast({ title: e.message || '查询失败', icon: 'none' });
  }
};

const showWip = (name) => {
  uni.showModal({
    title: '功能建设中',
    content: `「${name}」功能正在建设，敬请期待`,
    showCancel: false
  });
};

const handleLogout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout();
        profile.value = {};
      }
    }
  });
};

const loadProfile = async () => {
  if (!userStore.userId) return;
  loading.value = true;
  try {
    const res = await getUserProfile(userStore.userId);
    profile.value = res.data || {};
  } catch {
    profile.value = {};
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  updateTime();
  setInterval(updateTime, 30000);
  loadProfile();
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 32px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 10px;
}

.time {
  font-size: $fontSize-md;
  color: $color-muted;
  font-weight: 500;
}

.voice-btn {
  background: $color-primary;
  border-radius: 22px;
  padding: 10px 20px;
}

.voice-text {
  color: #fff;
  font-size: $fontSize-base;
}

.page-title-row {
  padding: 0 20px 16px;
}

.page-title {
  font-size: $fontSize-title;
  color: $color-text;
  font-weight: 700;
}

.login-card {
  background: $color-card;
  border-radius: 16px;
  margin: 0 16px 16px;
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.login-tip {
  font-size: $fontSize-md;
  color: $color-muted;
}

.login-btn {
  background: $color-primary;
  color: #fff;
  border-radius: 12px;
  padding: 14px 48px;
  font-size: $fontSize-md;
  font-weight: 600;
}

.profile-card {
  background: $color-card;
  border-radius: 16px;
  margin: 0 16px 16px;
  padding: 20px;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 68px;
  height: 68px;
  border-radius: 34px;
  background: $color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-text {
  color: #fff;
  font-size: 28px;
  font-weight: 700;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.profile-name {
  font-size: $fontSize-lg;
  color: $color-text;
  font-weight: 700;
}

.profile-meta {
  font-size: $fontSize-sm;
  color: $color-muted;
}

.func-list {
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.func-card {
  background: $color-card;
  border-radius: 14px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.func-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.func-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.func-title {
  font-size: $fontSize-md;
  color: $color-text;
  font-weight: 600;
}

.func-desc {
  font-size: $fontSize-sm;
  color: $color-muted;
}

.func-arrow {
  font-size: $fontSize-base;
  color: $color-muted;
  flex-shrink: 0;
}

.loading-box {
  padding: 32px;
  text-align: center;
}

.loading-text {
  font-size: $fontSize-base;
  color: $color-muted;
}

.logout-btn {
  margin: 0 16px;
  background: #f5f5f5;
  color: #999;
  border-radius: 14px;
  text-align: center;
  padding: 16px 0;
  font-size: $fontSize-base;
}
</style>
