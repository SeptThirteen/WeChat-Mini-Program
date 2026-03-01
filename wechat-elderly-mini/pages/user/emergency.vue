<template>
  <view class="page">
    <view class="page-header emergency-header">
      <text class="header-title">紧急联系人设置</text>
    </view>

    <view
      class="contact-card"
      v-for="(c, i) in contacts"
      :key="i"
    >
      <view class="contact-badge" :style="{ background: c.color }">
        <text class="badge-text">{{ i === 0 ? '🔴 紧急联系人1' : '🟡 紧急联系人2' }}</text>
      </view>
      <view class="contact-row">
        <text class="contact-label">姓名</text>
        <text class="contact-value">{{ c.name }}</text>
      </view>
      <view class="contact-row">
        <text class="contact-label">关系</text>
        <text class="contact-value">{{ c.relation }}</text>
      </view>
      <view class="contact-row" @click="call(c.phone)">
        <text class="contact-label">电话</text>
        <text class="contact-value phone-value">{{ c.phone }} 📞</text>
      </view>
    </view>

    <view class="edit-btn" @click="showEditModal">
      <text class="edit-icon">🎤</text>
      <text class="edit-text">修改联系人（语音录入号码）</text>
    </view>

    <view class="tip-card">
      <text class="tip-text">💡 紧急联系人将在您使用一键呼叫时优先通知。建议设置子女或社区居委会为联系人。</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';

const contacts = ref([
  { name: '子女/家人', relation: '家属',   phone: '138XXXX1234', color: '#ef5350' },
  { name: '社区居委会', relation: '社区服务', phone: '020-XXXX8888', color: '#FFA726' },
]);

const call = (phone) => {
  uni.showModal({
    title: '拨打电话',
    content: `确认拨打 ${phone}？`,
    success: (res) => {
      if (res.confirm) {
        uni.makePhoneCall({ phoneNumber: phone.replace(/[^0-9]/g, '') });
      }
    }
  });
};

const showEditModal = () => {
  uni.showModal({
    title: '修改联系人',
    content: '语音录入联系人功能开发中，敬请期待',
    showCancel: false
  });
};
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

.emergency-header {
  background: linear-gradient(135deg, #c62828 0%, #ef5350 100%);
}

.header-title {
  color: #fff;
  font-size: $fontSize-title;
  font-weight: 700;
}

.contact-card {
  background: $color-card;
  border-radius: 16px;
  margin: 12px 16px 0;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.contact-badge {
  padding: 12px 20px;
}

.badge-text {
  color: #fff;
  font-size: $fontSize-base;
  font-weight: 600;
}

.contact-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #f5ede8;
}

.contact-row:last-child {
  border-bottom: none;
}

.contact-label {
  font-size: $fontSize-base;
  color: $color-muted;
}

.contact-value {
  font-size: $fontSize-base;
  color: $color-text;
  font-weight: 500;
}

.phone-value {
  color: $color-primary;
}

.edit-btn {
  background: $color-card;
  border-radius: 14px;
  margin: 12px 16px 0;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.edit-icon {
  font-size: 26px;
}

.edit-text {
  font-size: $fontSize-md;
  color: $color-text;
  font-weight: 600;
}

.tip-card {
  background: #fff8e1;
  border-radius: 12px;
  margin: 12px 16px 0;
  padding: 16px;
  border: 1px solid #FFE082;
}

.tip-text {
  font-size: $fontSize-sm;
  color: #795548;
  line-height: 1.7;
}
</style>
