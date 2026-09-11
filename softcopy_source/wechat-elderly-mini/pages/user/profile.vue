<template>
  <view class="page" v-if="isReady">
    <view class="top-bar">
      <text class="time">{{ currentTime }}</text>
      <view class="voice-btn" @click="showVoiceSheet = true">
        <text class="voice-text">语音说需求</text>
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
    <view class="row-list" v-if="userId && !loading">
      <view class="func-row" hover-class="row-pressed" @click="go('/pages/user/bills')">
        <view class="func-text">
          <text class="func-title">缴费记录</text>
          <text class="func-desc">查看水电费缴费历史</text>
        </view>
      </view>
      <view class="func-row" hover-class="row-pressed" @click="go('/pages/user/gov-tasks')">
        <view class="func-text">
          <text class="func-title">政务代办进度</text>
          <text class="func-desc">查看申请办理进度</text>
        </view>
      </view>
      <view class="func-row" hover-class="row-pressed" @click="go('/pages/order/list')">
        <view class="func-text">
          <text class="func-title">一呼馨家订单</text>
          <text class="func-desc">查看全部帮扶服务订单</text>
        </view>
      </view>
      <view class="func-row" hover-class="row-pressed" @click="go('/pages/user/emergency')">
        <view class="func-text">
          <text class="func-title">紧急联系人设置</text>
          <text class="func-desc">设置紧急求助联系人</text>
        </view>
      </view>
      <!-- 常用地址 -->
      <view class="func-row" v-if="!editingAddress" hover-class="row-pressed" @click="editingAddress = true">
        <view class="func-text">
          <text class="func-title">常用服务地址</text>
          <text class="func-desc">{{ addressInput || '未设置，点击添加' }}</text>
        </view>
        <text class="func-edit-hint">编辑</text>
      </view>
    </view>
    <view class="addr-edit-card" v-if="editingAddress">
      <text class="addr-edit-title">编辑常用地址</text>
      <input class="addr-input" v-model="addressInput" placeholder="输入常用服务地址" />
      <view class="addr-btns">
        <view class="addr-cancel" @click="editingAddress = false">取消</view>
        <view class="addr-save" @click="saveCommonAddress">保存</view>
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
import { computed, nextTick, onMounted, ref } from 'vue';
import { getUserProfile, updateUserProfile } from '../../api/user';
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
const isReady = ref(false);
const editingAddress = ref(false);
const addressInput = ref('');

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

const saveCommonAddress = async () => {
  const val = addressInput.value.trim();
  if (!val) {
    uni.showToast({ title: '请输入地址', icon: 'none' });
    return;
  }
  try {
    await updateUserProfile(userStore.userId, { address: val });
    uni.setStorageSync('commonAddress', val);
    profile.value = { ...profile.value, address: val };
    editingAddress.value = false;
    uni.showToast({ title: '已保存' });
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none' });
  }
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
    // 同步常用地址
    const cached = uni.getStorageSync('commonAddress');
    addressInput.value = cached || profile.value.address || '';
  } catch {
    profile.value = {};
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  // 等待 Pinia 从持久化存储恢复状态后再渲染，避免同时显示两套视图
  nextTick(() => {
    isReady.value = true;
    updateTime();
    setInterval(updateTime, 30000);
    loadProfile();
  });
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
  border-radius: 12px;
  margin: 0 16px 16px;
  padding: 32px 24px;
  border: 1px solid $color-border;
  box-shadow: 0 8px 16px $color-shadow;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.login-tip {
  font-size: 22px;
  color: $color-text;
  font-weight: 600;
}

.login-btn {
  background: $color-primary;
  color: #fff;
  border-radius: 8px;
  height: 56px;
  line-height: 56px;
  width: 200px;
  text-align: center;
  font-size: 22px;
  font-weight: 700;
}

.profile-card {
  background: $color-card;
  border-radius: 12px;
  margin: 0 16px 16px;
  padding: 24px;
  border: 1px solid $color-border;
  box-shadow: 0 8px 16px $color-shadow;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  width: 80px;
  height: 80px;
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

/* 功能入口:竖排文字列表 */
.row-list {
  margin: 0 16px 16px;
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 14px;
  overflow: hidden;
}

.func-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid $color-border;
}

.row-pressed {
  background: $color-bg;
}

.func-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.func-title {
  font-size: 20px;
  color: $color-text;
  font-weight: 700;
}

.func-desc {
  font-size: 16px;
  color: $color-muted;
  word-break: break-all;
}

.func-edit-hint {
  flex-shrink: 0;
  font-size: 16px;
  color: $color-primary;
  font-weight: 700;
}

.loading-box {
  padding: 32px;
  text-align: center;
}

.loading-text {
  font-size: 20px;
  color: $color-muted;
}

.logout-btn {
  margin: 0 16px;
  background: $color-card;
  color: #D32F2F;
  border: 1px solid #D32F2F;
  border-radius: 12px;
  text-align: center;
  height: 56px;
  line-height: 56px;
  font-size: 22px;
  font-weight: 700;
  box-shadow: 0 8px 16px $color-shadow;
}

.addr-edit-card {
  background: $color-card;
  border-radius: 14px;
  margin: 0 16px 16px;
  padding: 24px 20px;
  border: 1px solid $color-border;
}

.addr-edit-title {
  display: block;
  font-size: 24px;
  color: $color-text;
  font-weight: 700;
  margin-bottom: 16px;
}

.addr-input {
  display: block;
  width: 100%;
  box-sizing: border-box;
  background: $color-bg;
  border: 2px solid $color-border;
  border-radius: 8px;
  padding: 0 16px;
  height: 64px;
  line-height: 64px;
  font-size: 22px;
  margin-bottom: 16px;
}

.addr-btns {
  display: flex;
  gap: 16px;
}

.addr-cancel {
  flex: 1;
  text-align: center;
  height: 56px;
  line-height: 56px;
  border-radius: 8px;
  font-size: 20px;
  font-weight: 700;
  border: 2px solid $color-border;
  background: $color-bg;
  color: $color-text;
}

.addr-save {
  flex: 1;
  text-align: center;
  height: 56px;
  line-height: 56px;
  border-radius: 8px;
  font-size: 20px;
  font-weight: 700;
  background: $color-primary;
  color: #fff;
  border: 2px solid $color-primary;
}
</style>
