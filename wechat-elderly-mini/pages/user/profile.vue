<template>
  <view class="page">
    <view class="header">
      <text class="title">我的</text>
    </view>

    <!-- Not logged in -->
    <view class="card" v-if="!userId">
      <text class="desc">您还未登录</text>
      <view class="btn" @click="goLogin">登录</view>
    </view>

    <!-- Loading -->
    <view class="card" v-else-if="loading">
      <text class="desc">加载中…</text>
    </view>

    <!-- Profile -->
    <view class="card" v-else>
      <!-- View mode -->
      <view v-if="!editing">
        <text class="name">用户ID：{{ userId }}</text>
        <text class="desc">姓名：{{ profile.name || '未填写' }}</text>
        <text class="desc">年龄：{{ profile.age ?? '未填写' }}</text>
        <text class="desc">手机号：{{ phone || '未绑定' }}</text>
        <view class="btn-row">
          <view class="btn edit-btn" @click="startEdit">编辑资料</view>
          <view class="btn logout-btn" @click="handleLogout">退出登录</view>
        </view>
      </view>

      <!-- Edit mode -->
      <view v-else>
        <text class="edit-label">姓名</text>
        <input class="edit-input" v-model="editForm.name" placeholder="请输入姓名" />
        <text class="edit-label">年龄</text>
        <input class="edit-input" v-model="editForm.age" type="number" placeholder="请输入年龄" />
        <view class="btn-row">
          <view class="btn save-btn" :class="{ disabled: saving }" @click="handleSave">
            {{ saving ? '保存中…' : '保存' }}
          </view>
          <view class="btn cancel-edit-btn" @click="editing = false">取消</view>
        </view>
      </view>
    </view>

    <view class="card stats" v-if="userId && !loading">
      <text class="stats-title">我的统计</text>
      <view class="stats-row">
        <view class="stats-item">
          <text class="stats-value">{{ stats.total }}</text>
          <text class="stats-label">总订单</text>
        </view>
        <view class="stats-item">
          <text class="stats-value">{{ stats.created }}</text>
          <text class="stats-label">待处理</text>
        </view>
        <view class="stats-item">
          <text class="stats-value">{{ stats.completed }}</text>
          <text class="stats-label">已完成</text>
        </view>
        <view class="stats-item">
          <text class="stats-value">{{ stats.rated }}</text>
          <text class="stats-label">已评价</text>
        </view>
        <view class="stats-item">
          <text class="stats-value">{{ stats.cancelled }}</text>
          <text class="stats-label">已取消</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { getUserProfile, updateUserProfile } from '../../api/user';
import { getOrderList } from '../../api/order';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const userId = computed(() => userStore.userId);
const phone = computed(() => userStore.phone);
const profile = ref({});
const loading = ref(false);
const editing = ref(false);
const saving = ref(false);
const editForm = ref({ name: '', age: '' });
const stats = ref({ total: 0, created: 0, completed: 0, rated: 0, cancelled: 0 });

const loadProfile = async () => {
  if (!userStore.userId) {
    profile.value = {};
    stats.value = { total: 0, created: 0, completed: 0, rated: 0, cancelled: 0 };
    return;
  }
  loading.value = true;
  try {
    const res = await getUserProfile(userStore.userId);
    profile.value = res.data || {};
    const ordersRes = await getOrderList(userStore.userId);
    const list = ordersRes.data || [];
    const next = { total: list.length, created: 0, completed: 0, rated: 0, cancelled: 0 };
    list.forEach((item) => {
      if (item.status === 'CREATED') next.created += 1;
      if (item.status === 'COMPLETED') next.completed += 1;
      if (item.status === 'RATED') next.rated += 1;
      if (item.status === 'CANCELLED') next.cancelled += 1;
    });
    stats.value = next;
  } catch (e) {
    profile.value = {};
    stats.value = { total: 0, created: 0, completed: 0, rated: 0, cancelled: 0 };
  } finally {
    loading.value = false;
  }
};

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' });
};

const handleLogout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout();
        profile.value = {};
        stats.value = { total: 0, created: 0, completed: 0, rated: 0, cancelled: 0 };
      }
    }
  });
};

const startEdit = () => {
  editForm.value = { name: profile.value.name || '', age: profile.value.age ?? '' };
  editing.value = true;
};

const handleSave = async () => {
  if (saving.value) return;
  const name = String(editForm.value.name).trim();
  const age = editForm.value.age !== '' ? Number(editForm.value.age) : null;
  if (!name) {
    uni.showToast({ title: '姓名不能为空', icon: 'none' });
    return;
  }
  if (age !== null && (isNaN(age) || age < 0 || age > 150)) {
    uni.showToast({ title: '请输入有效年龄', icon: 'none' });
    return;
  }
  saving.value = true;
  try {
    await updateUserProfile(userStore.userId, { name, age });
    profile.value = { ...profile.value, name, age };
    editing.value = false;
    uni.showToast({ title: '保存成功' });
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' });
  } finally {
    saving.value = false;
  }
};

onMounted(loadProfile);
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

.name {
  font-size: 20px;
  margin-bottom: 8px;
  display: block;
}

.desc {
  color: $color-muted;
  margin-bottom: 12px;
  display: block;
}

.btn {
  background: $color-primary;
  color: #fff;
  border-radius: 10px;
  text-align: center;
  padding: 10px 0;
}

.logout-btn {
  background: #f5f5f5;
  color: #666;
  margin-top: 4px;
}

.btn-row {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

.btn-row .btn {
  flex: 1;
}

.edit-btn {
  background: $color-accent;
}

.save-btn {
  background: $color-primary;
}

.save-btn.disabled {
  opacity: 0.6;
}

.cancel-edit-btn {
  background: #f5f5f5;
  color: #666;
}

.edit-label {
  display: block;
  font-size: 15px;
  color: $color-muted;
  margin-bottom: 6px;
}

.edit-input {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
  width: 100%;
  box-sizing: border-box;
  font-size: 16px;
}

.stats {
  background: #fff8f4;
  border: 1px solid #f3ddd2;
}

.stats-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 10px;
  display: block;
}

.stats-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.stats-item {
  flex: 1 1 45%;
  background: #ffffff;
  border-radius: 10px;
  padding: 10px;
  text-align: center;
}

.stats-value {
  font-size: 22px;
  font-weight: 700;
  color: $color-primary;
  display: block;
}

.stats-label {
  color: $color-muted;
  font-size: 14px;
}
</style>
