<template>
  <view class="page">
    <view class="header">
      <text class="title">编辑个人资料</text>
      <text class="subtitle">带 * 为必填,手机号不可修改</text>
    </view>

    <view class="card">
      <view class="form-row">
        <text class="form-label">* 姓名</text>
        <input class="form-input" v-model="name" placeholder="请输入姓名" maxlength="20" />
      </view>

      <view class="form-row">
        <text class="form-label">年龄</text>
        <picker mode="selector" :range="ageOptions" :value="ageIndex" @change="onAgeChange">
          <view class="picker-field">{{ ageText }}</view>
        </picker>
      </view>

      <view class="form-row">
        <text class="form-label">常用服务地址</text>
        <input class="form-input" v-model="address" placeholder="请输入常用地址(小区/楼栋/房号)" />
      </view>

      <view class="form-row form-row-last">
        <text class="form-label">绑定社区</text>
        <input class="form-input" v-model="community" placeholder="如:阳光社区居委会" maxlength="30" />
      </view>

      <view class="form-row form-row-last">
        <text class="form-label">手机号</text>
        <text class="readonly-value">{{ phone }}</text>
      </view>
    </view>

    <view class="btn save-btn" :class="{ disabled: saving }" @click="handleSave">
      {{ saving ? '保存中…' : '保存' }}
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { getUserProfile, updateUserProfile } from '../../api/user';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const name = ref('');
const age = ref(null);
const address = ref('');
const community = ref('');
const saving = ref(false);

// 年龄滚轮:1 ~ 120 岁
const ageOptions = Array.from({ length: 120 }, (_, i) => `${i + 1} 岁`);
const ageIndex = computed(() => (age.value ? Math.min(Math.max(age.value - 1, 0), 119) : 0));
const ageText = computed(() => (age.value ? `${age.value} 岁` : '请选择'));

let loadRequested = false;

onLoad(() => {
  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    setTimeout(() => uni.navigateBack(), 800);
    return;
  }
  load();
});

/** onShow 兜底刷新(仅当 onLoad 因未登录被跳过时) */
onShow(() => {
  if (userStore.userId && !loadRequested) load();
});

const load = async () => {
  loadRequested = true;
  try {
    const res = await getUserProfile(userStore.userId);
    const p = res.data || {};
    name.value = p.name || '';
    age.value = p.age || null;
    address.value = p.address || '';
    community.value = p.community || '';
  } catch (e) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' });
  }
};

const onAgeChange = (e) => {
  age.value = Number(e.detail.value) + 1;
};

const handleSave = async () => {
  if (saving.value) return;
  if (!name.value.trim()) {
    uni.showToast({ title: '请填写姓名', icon: 'none' });
    return;
  }

  saving.value = true;
  try {
    await updateUserProfile(userStore.userId, {
      name: name.value.trim(),
      age: age.value || undefined,
      address: address.value.trim() || undefined,
      community: community.value.trim() || undefined
    });
    uni.setStorageSync('commonAddress', address.value.trim());
    uni.showToast({ title: '已保存' });
    setTimeout(() => uni.navigateBack(), 800);
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败,请重试', icon: 'none' });
  } finally {
    saving.value = false;
  }
};
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding: 16px;
  box-sizing: border-box;
}

.header {
  margin-bottom: 14px;
}

.title {
  display: block;
  font-size: 26px;
  font-weight: 700;
  color: $color-text;
}

.subtitle {
  display: block;
  margin-top: 6px;
  font-size: 16px;
  color: $color-muted;
}

.card {
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 14px;
  padding: 6px 16px;
  box-shadow: 0 8px 16px $color-shadow;
}

.form-row {
  padding: 16px 0;
  border-bottom: 1px solid $color-border;
}

.form-row-last {
  border-bottom: none;
}

.form-label {
  display: block;
  font-size: 16px;
  color: $color-muted;
  margin-bottom: 8px;
}

.form-input {
  display: block;
  width: 100%;
  box-sizing: border-box;
  background: $color-bg;
  border: 2px solid $color-border;
  border-radius: 10px;
  height: 56px;
  padding: 0 14px;
  font-size: 20px;
  color: $color-text;
}

.picker-field {
  background: $color-bg;
  border: 2px solid $color-border;
  border-radius: 10px;
  height: 56px;
  line-height: 56px;
  padding: 0 14px;
  font-size: 20px;
  color: $color-text;
}

.readonly-value {
  display: block;
  height: 56px;
  line-height: 56px;
  font-size: 20px;
  color: $color-muted;
  font-weight: 600;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: $btn-height;
  border-radius: 12px;
  font-size: 22px;
  font-weight: 700;
  margin-top: 20px;
  box-sizing: border-box;
}

.save-btn {
  background: $color-primary;
  border: 1px solid $color-primary;
  color: #fff;
}

.save-btn.disabled {
  opacity: 0.6;
}
</style>
