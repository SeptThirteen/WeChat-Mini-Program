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
          <view class="picker-value">{{ form.taskType || '请选择' }} ▼</view>
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
}

.page-header {
  padding: 24px 20px 20px;
}

.gov-header {
  background: linear-gradient(135deg, #1565c0 0%, #42a5f5 100%);
}

.header-title {
  color: #fff;
  font-size: $fontSize-title;
  font-weight: 700;
}

.submit-card {
  background: $color-card;
  border-radius: 16px;
  margin: 12px 16px 0;
  padding: 20px;
}

.submit-title {
  font-size: $fontSize-md;
  font-weight: 700;
  color: $color-text;
  margin-bottom: 12px;
  display: block;
}

.form-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5ede8;
  gap: 12px;
}

.form-label {
  font-size: $fontSize-base;
  color: $color-muted;
  min-width: 64px;
  flex-shrink: 0;
}

.picker-value {
  font-size: $fontSize-base;
  color: $color-text;
}

.form-input {
  flex: 1;
  font-size: $fontSize-base;
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 8px 12px;
}

.submit-btn {
  margin-top: 16px;
  background: $color-primary;
  color: #fff;
  border-radius: 12px;
  text-align: center;
  padding: 14px 0;
  font-size: $fontSize-md;
  font-weight: 600;
}

.submit-btn.disabled {
  opacity: 0.6;
}

.section-title {
  font-size: $fontSize-md;
  font-weight: 700;
  color: $color-text;
  padding: 20px 16px 8px;
}

.task-card {
  background: $color-card;
  border-radius: 14px;
  margin: 8px 16px 0;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-type {
  font-size: $fontSize-base;
  color: $color-text;
  font-weight: 600;
}

.task-status {
  font-size: $fontSize-sm;
  padding: 2px 10px;
  border-radius: 8px;
}

.st-submitted  { background: #e3f2fd; color: #1565c0; }
.st-processing { background: #fff3e0; color: #e65100; }
.st-completed  { background: #e8f5e9; color: #2e7d32; }
.st-rejected   { background: #fce4ec; color: #c62828; }

.task-desc {
  display: block;
  font-size: $fontSize-sm;
  color: $color-muted;
  margin-bottom: 8px;
}

.task-meta {
  display: flex;
  justify-content: space-between;
}

.task-time {
  font-size: $fontSize-sm;
  color: $color-muted;
}

.empty-box {
  background: $color-card;
  border-radius: 14px;
  margin: 16px;
  padding: 40px 16px;
  text-align: center;
}

.empty-text {
  font-size: $fontSize-base;
  color: $color-muted;
}
</style>
