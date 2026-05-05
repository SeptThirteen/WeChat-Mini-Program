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
  background: $color-card;
  border-bottom: 2px solid $color-border;
}

.header-title {
  color: $color-text;
  font-size: 24px;
  font-weight: 700;
}

.contact-card {
  background: $color-card;
  border-radius: 12px;
  margin: 12px 16px 0;
  overflow: hidden;
  border: 2px solid $color-border;
}

.contact-badge {
  padding: 12px 20px;
}

.badge-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.contact-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 2px solid $color-border;
}

.contact-row:last-child {
  border-bottom: none;
}

.card-actions {
  display: flex;
  gap: 12px;
  padding: 12px 20px 16px;
}

.card-action-btn {
  flex: 1;
  text-align: center;
  height: 56px;
  line-height: 56px;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 700;
  background: $color-bg;
  color: $color-text;
  border: 2px solid $color-border;
}

.card-action-btn.delete {
  background: #fce4ec;
  color: #c62828;
  border-color: #f8bbd0;
}

.contact-label {
  font-size: 18px;
  color: $color-muted;
}

.contact-value {
  font-size: 18px;
  color: $color-text;
  font-weight: 700;
}

.phone-value {
  color: $color-primary;
}

.edit-btn {
  background: $color-card;
  border-radius: 12px;
  margin: 12px 16px 0;
  padding: 0 20px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: none;
  border: 2px solid $color-border;
}

.edit-icon {
  font-size: 24px;
}

.edit-text {
  font-size: 18px;
  color: $color-text;
  font-weight: 700;
}

.tip-card {
  background: $color-card;
  border-radius: 12px;
  margin: 12px 16px 0;
  padding: 16px;
  border: 2px solid $color-border;
}

.tip-text {
  font-size: 18px;
  color: $color-muted;
  line-height: 1.7;
}

.empty-box {
  background: $color-card;
  border-radius: 12px;
  margin: 12px 16px 0;
  padding: 40px 16px;
  text-align: center;
  border: 2px solid $color-border;
}

.empty-text {
  font-size: 18px;
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
  background: $color-card;
  border-radius: 12px;
  width: 85%;
  max-width: 360px;
  padding: 24px 20px;
  border: 2px solid $color-border;
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: $color-text;
  display: block;
  margin-bottom: 16px;
  text-align: center;
}

.modal-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  gap: 10px;
  border-bottom: 2px solid $color-border;
}

.modal-label {
  font-size: 18px;
  color: $color-muted;
  min-width: 56px;
  flex-shrink: 0;
}

.modal-input {
  flex: 1;
  font-size: 18px;
  background: $color-card;
  border: 2px solid $color-border;
  border-radius: 12px;
  height: 56px;
  padding: 0 12px;
  box-sizing: border-box;
}

.modal-btns {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.modal-cancel {
  flex: 1;
  background: $color-bg;
  color: $color-text;
  border-radius: 12px;
  text-align: center;
  height: 56px;
  line-height: 56px;
  font-size: 18px;
  font-weight: 700;
  border: 2px solid $color-border;
}

.modal-confirm {
  flex: 1;
  background: $color-primary;
  color: #fff;
  border-radius: 12px;
  text-align: center;
  height: 56px;
  line-height: 56px;
  font-size: 18px;
  font-weight: 700;
  border: 2px solid $color-primary;
}

.modal-confirm.disabled {
  opacity: 0.6;
}
</style>
