<template>
  <view class="page">
    <view class="page-header gov-header">
      <text class="header-title">政务代办进度</text>
    </view>

    <!-- 提交新申办 -->
    <view class="submit-card">
      <text class="submit-title">提交新申办</text>
      <view class="form-row">
        <text class="form-label">申办类型</text>
        <picker :range="taskTypes" @change="onTypePick">
          <view class="picker-value">{{ form.taskType || '请选择' }}</view>
        </picker>
      </view>
      <view class="form-row">
        <text class="form-label">描述</text>
        <input class="form-input" v-model="form.taskDesc" placeholder="简要说明（选填）" />
      </view>
      <view class="submit-btn" :class="{ disabled: submitting }" @click="handleSubmit">
        {{ submitting ? '提交中…' : '提交申办' }}
      </view>
    </view>

    <!-- 进度列表 -->
    <view class="section-title" v-if="tasks.length > 0">我的申办记录</view>

    <view class="task-card" v-for="item in tasks" :key="item.taskId">
      <view class="task-header">
        <text class="task-type">{{ item.taskType }}</text>
        <text class="task-status" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</text>
      </view>
      <text class="task-desc" v-if="item.taskDesc">{{ item.taskDesc }}</text>
      <view class="task-meta">
        <text class="task-time">提交时间：{{ item.submittedTime || '' }}</text>
      </view>
    </view>

    <view class="empty-box" v-if="loaded && tasks.length === 0">
      <text class="empty-text">暂无申办记录，请提交新申办</text>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { submitGovTask, getGovTaskList } from '../../api/gov';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const tasks = ref([]);
const loaded = ref(false);
const submitting = ref(false);

const taskTypes = ['养老补贴申请', '医保备案', '高龄津贴', '护理补贴', '其他'];

const form = ref({
  taskType: '',
  taskDesc: '',
});

const onTypePick = (e) => {
  form.value.taskType = taskTypes[e.detail.value];
};

const statusLabel = (s) => {
  const map = { SUBMITTED: '已提交', PROCESSING: '办理中', COMPLETED: '已完成', REJECTED: '已驳回' };
  return map[s] || s;
};

const statusClass = (s) => {
  const map = { SUBMITTED: 'st-submitted', PROCESSING: 'st-processing', COMPLETED: 'st-completed', REJECTED: 'st-rejected' };
  return map[s] || '';
};

const loadTasks = async () => {
  if (!userStore.userId) return;
  try {
    const res = await getGovTaskList(userStore.userId);
    tasks.value = res.data || [];
  } catch { /* ignore */ }
  loaded.value = true;
};

const handleSubmit = async () => {
  if (submitting.value) return;
  if (!form.value.taskType) {
    uni.showToast({ title: '请选择申办类型', icon: 'none' });
    return;
  }
  if (!userStore.userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    await submitGovTask({
      userId: userStore.userId,
      taskType: form.value.taskType,
      taskDesc: form.value.taskDesc,
    });
    uni.showToast({ title: '提交成功' });
    form.value = { taskType: '', taskDesc: '' };
    loadTasks();
  } catch (e) {
    uni.showToast({ title: e.message || '提交失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
};

onMounted(loadTasks);
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 32px;
  box-sizing: border-box;
}

.page-header {
  padding: 24px 20px 20px;
}

.gov-header {
  background: $gradient-brand;
  border-bottom: none;
}

.header-title {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

.submit-card {
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 12px;
  margin: 12px 16px 0;
  padding: 20px;
  box-shadow: 0 8px 16px $color-shadow;
}

.submit-title {
  font-size: 24px;
  font-weight: 700;
  color: $color-text;
  margin-bottom: 12px;
  display: block;
}

.form-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  gap: 12px;
}

.form-label {
  font-size: 18px;
  color: $color-muted;
  min-width: 80px;
  flex-shrink: 0;
}

.picker-value {
  font-size: 24px;
  color: $color-text;
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  border: 1px solid $color-border;
  border-radius: 12px;
  background: $color-card;
  min-width: 150px;
  box-sizing: border-box;
}

.form-input {
  flex: 1;
  font-size: 18px;
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 12px;
  height: 56px;
  padding: 0 14px;
  box-sizing: border-box;
}

.submit-btn {
  margin-top: 16px;
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

.submit-btn.disabled {
  opacity: 0.6;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: $color-text;
  padding: 20px 16px 8px;
}

.task-card {
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 12px;
  margin: 8px 16px 0;
  padding: 16px 20px;
  box-shadow: 0 8px 16px $color-shadow;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-type {
  font-size: 24px;
  color: $color-text;
  font-weight: 700;
}

.task-status {
  font-size: 18px;
  padding: 4px 12px;
  border-radius: 8px;
  font-weight: 700;
}

.st-submitted  { background: #e3f2fd; color: #1565c0; }
.st-processing { background: #fff3e0; color: #e65100; }
.st-completed  { background: #e8f5e9; color: #2e7d32; }
.st-rejected   { background: #fce4ec; color: #c62828; }

.task-desc {
  display: block;
  font-size: 18px;
  color: $color-muted;
  margin-bottom: 8px;
}

.task-meta {
  display: flex;
  justify-content: space-between;
}

.task-time {
  font-size: 18px;
  color: $color-muted;
}

.empty-box {
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 12px;
  margin: 16px;
  padding: 40px 16px;
  text-align: center;
  box-shadow: 0 8px 16px $color-shadow;
}

.empty-text {
  font-size: 18px;
  color: $color-muted;
}
</style>
