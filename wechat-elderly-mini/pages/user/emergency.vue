<template>
  <view class="page">
    <view class="page-header emergency-header">
      <text class="header-title">紧急联系人设置</text>
    </view>

    <!-- 联系人列表 -->
    <view
      class="contact-card"
      v-for="(c, i) in contacts"
      :key="c.contactId || i"
    >
      <view class="contact-badge" :style="{ background: i === 0 ? '#ef5350' : '#FFA726' }">
        <text class="badge-text">{{ i === 0 ? '🔴 紧急联系人1' : '🟡 紧急联系人2' }}</text>
      </view>
      <view class="contact-row">
        <text class="contact-label">姓名</text>
        <text class="contact-value">{{ c.name }}</text>
      </view>
      <view class="contact-row">
        <text class="contact-label">关系</text>
        <text class="contact-value">{{ c.relation || '未设置' }}</text>
      </view>
      <view class="contact-row" @click="call(c.phone)">
        <text class="contact-label">电话</text>
        <text class="contact-value phone-value">{{ c.phone }} 📞</text>
      </view>
      <view class="card-actions">
        <view class="card-action-btn" @click="openEdit(c)">✏️ 修改</view>
        <view class="card-action-btn delete" @click="handleDelete(c)">🗑️ 删除</view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-box" v-if="loaded && contacts.length === 0">
      <text class="empty-text">暂未设置紧急联系人</text>
    </view>

    <!-- 新增按钮 -->
    <view class="edit-btn" @click="openEdit(null)" v-if="contacts.length < 2">
      <text class="edit-icon">➕</text>
      <text class="edit-text">添加紧急联系人</text>
    </view>

    <view class="tip-card">
      <text class="tip-text">💡 紧急联系人将在您使用一键呼叫时优先通知。建议设置子女或社区居委会为联系人。</text>
    </view>

    <!-- 编辑弹窗 -->
    <view class="modal-mask" v-if="showModal" @click.self="showModal = false">
      <view class="modal-box">
        <text class="modal-title">{{ editTarget ? '修改联系人' : '添加联系人' }}</text>
        <view class="modal-row">
          <text class="modal-label">姓名</text>
          <input class="modal-input" v-model="editForm.name" placeholder="请输入姓名" />
        </view>
        <view class="modal-row">
          <text class="modal-label">电话</text>
          <input class="modal-input" v-model="editForm.phone" placeholder="请输入电话号码" type="number" />
        </view>
        <view class="modal-row">
          <text class="modal-label">关系</text>
          <input class="modal-input" v-model="editForm.relation" placeholder="如：家属、社区服务" />
        </view>
        <view class="modal-btns">
          <view class="modal-cancel" @click="showModal = false">取消</view>
          <view class="modal-confirm" :class="{ disabled: saving }" @click="handleSave">
            {{ saving ? '保存中…' : '保存' }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { getContactList, saveContact, deleteContact } from '../../api/contact';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const contacts = ref([]);
const loaded = ref(false);
const showModal = ref(false);
const saving = ref(false);
const editTarget = ref(null);
const editForm = ref({ name: '', phone: '', relation: '' });

const loadContacts = async () => {
  if (!userStore.userId) return;
  try {
    const res = await getContactList(userStore.userId);
    contacts.value = res.data || [];
  } catch { /* ignore */ }
  loaded.value = true;
};

const openEdit = (contact) => {
  if (contact) {
    editTarget.value = contact;
    editForm.value = { name: contact.name, phone: contact.phone, relation: contact.relation || '' };
  } else {
    editTarget.value = null;
    editForm.value = { name: '', phone: '', relation: '' };
  }
  showModal.value = true;
};

const handleSave = async () => {
  if (saving.value) return;
  if (!editForm.value.name.trim() || !editForm.value.phone.trim()) {
    uni.showToast({ title: '姓名和电话不能为空', icon: 'none' });
    return;
  }
  saving.value = true;
  try {
    await saveContact({
      contactId: editTarget.value?.contactId || undefined,
      userId: userStore.userId,
      name: editForm.value.name.trim(),
      phone: editForm.value.phone.trim(),
      relation: editForm.value.relation.trim() || undefined,
    });
    uni.showToast({ title: '保存成功' });
    showModal.value = false;
    loadContacts();
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' });
  } finally {
    saving.value = false;
  }
};

const handleDelete = (contact) => {
  uni.showModal({
    title: '确认删除',
    content: `确定删除联系人「${contact.name}」吗？`,
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await deleteContact(contact.contactId);
        uni.showToast({ title: '已删除' });
        loadContacts();
      } catch (e) {
        uni.showToast({ title: e.message || '删除失败', icon: 'none' });
      }
    }
  });
};

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

onMounted(loadContacts);
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

.card-actions {
  display: flex;
  gap: 10px;
  padding: 10px 20px 14px;
}

.card-action-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 8px;
  font-size: $fontSize-sm;
  background: #f5f5f5;
  color: $color-text;
}

.card-action-btn.delete {
  background: #fce4ec;
  color: #c62828;
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

.empty-box {
  background: $color-card;
  border-radius: 14px;
  margin: 12px 16px 0;
  padding: 40px 16px;
  text-align: center;
}

.empty-text {
  font-size: $fontSize-base;
  color: $color-muted;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-box {
  background: #fff;
  border-radius: 16px;
  width: 85%;
  max-width: 360px;
  padding: 24px 20px;
}

.modal-title {
  font-size: $fontSize-md;
  font-weight: 700;
  color: $color-text;
  display: block;
  margin-bottom: 16px;
  text-align: center;
}

.modal-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  gap: 10px;
  border-bottom: 1px solid #f5ede8;
}

.modal-label {
  font-size: $fontSize-base;
  color: $color-muted;
  min-width: 48px;
  flex-shrink: 0;
}

.modal-input {
  flex: 1;
  font-size: $fontSize-base;
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 8px 12px;
}

.modal-btns {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.modal-cancel {
  flex: 1;
  background: #f5f5f5;
  color: #666;
  border-radius: 10px;
  text-align: center;
  padding: 12px 0;
  font-size: $fontSize-base;
}

.modal-confirm {
  flex: 1;
  background: $color-primary;
  color: #fff;
  border-radius: 10px;
  text-align: center;
  padding: 12px 0;
  font-size: $fontSize-base;
  font-weight: 600;
}

.modal-confirm.disabled {
  opacity: 0.6;
}
</style>
