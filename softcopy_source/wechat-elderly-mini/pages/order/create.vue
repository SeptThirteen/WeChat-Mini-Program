<template>
  <view class="page">
    <view class="page-header create-header">
      <text class="header-title">{{ serviceName }} · 下单</text>
    </view>

    <view class="voice-hint">
      <text class="hint-text">🎤 语音填信息 / 点选时间</text>
      <view class="voice-small-btn" @click="handleVoice">语音填写</view>
    </view>

    <!-- 预约时间 -->
    <view class="section-card">
      <text class="section-label">预约日期</text>
      <view class="chip-row">
        <view
          v-for="d in dateOptions"
          :key="d.key"
          class="chip"
          :class="{ active: selectedDate === d.key }"
          @click="selectedDate = d.key"
        >{{ d.label }}</view>
      </view>
      <text class="section-label" style="margin-top: 12px;">时间段（可滚轮精确到30分钟）</text>
      <view class="chip-row slot-row">
        <view
          v-for="s in slotOptions"
          :key="s.key"
          class="chip"
          :class="{ active: selectedSlot === s.key }"
          @click="selectSlot(s.key)"
        >{{ s.label }}</view>
      </view>
      <view class="time-picker-row">
        <text class="time-tip">当前选择：{{ slotLabelDisplay }} · {{ selectedTime }}</text>
        <picker
          mode="time"
          :value="selectedTime"
          :start="slotStart"
          :end="slotEnd"
          @change="onTimeChange"
        >
          <view class="time-btn">调整时间</view>
        </picker>
      </view>
    </view>

    <!-- 订单类型切换 -->
    <view class="section-card">
      <text class="section-label">预约类型</text>
      <view class="chip-row">
        <view class="chip" :class="{ active: orderType === 'SINGLE' }" @click="orderType = 'SINGLE'">单次预约</view>
        <view class="chip" :class="{ active: orderType === 'RECURRING' }" @click="orderType = 'RECURRING'">长期预约 🔁</view>
      </view>
      <!-- 长期订单专属字段 -->
      <view v-if="orderType === 'RECURRING'" class="recurring-fields">
        <view class="form-row">
          <text class="form-label">开始日期</text>
          <picker mode="date" :value="dateStart" :start="todayStr" @change="dateStart = $event.detail.value">
            <view class="picker-field">{{ dateStart || '请选择开始日期' }}</view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">结束日期</text>
          <picker mode="date" :value="dateEnd" :start="dateStart || todayStr" @change="dateEnd = $event.detail.value">
            <view class="picker-field">{{ dateEnd || '请选择结束日期' }}</view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">重复频率</text>
          <view class="chip-row">
            <view
              v-for="r in recurrenceOptions"
              :key="r"
              class="chip"
              :class="{ active: recurrenceRule === r }"
              @click="recurrenceRule = r"
            >{{ r }}</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 服务地址 -->
    <view class="section-card">
      <view class="label-row">
        <text class="section-label">服务地址</text>
        <view class="use-saved-btn" v-if="commonAddress" @click="address = commonAddress">
          使用常用地址
        </view>
      </view>
      <text class="saved-addr-hint" v-if="commonAddress">常用：{{ commonAddress }}</text>
      <input
        class="text-input"
        v-model="address"
        placeholder="请输入详细地址（小区/楼栋/房号）"
      />
    </view>

    <!-- 细分需求展开按钮（只对4类服务显示） -->
    <view 
      class="advanced-toggle" 
      v-if="isDayCare || isAccompany || isPurchase || isRepair"
      @click="toggleAdvanced"
    >
      <text class="toggle-text">{{ showAdvanced ? '收起详细需求 ▲' : '填写更多需求（可选） ▼' }}</text>
    </view>

    <!-- 细分需求（按服务类型） -->
    <view class="section-card" v-if="showAdvanced && isDayCare">
      <text class="section-label">日间照护需求</text>
      <view class="form-row">
        <text class="form-label">护理重点</text>
        <input class="text-input" v-model="careFocus" placeholder="如陪伴/测量血压/喂饭" />
      </view>
      <view class="form-row">
        <text class="form-label">行动能力</text>
        <input class="text-input" v-model="mobility" placeholder="可独立/需搀扶/轮椅" />
      </view>
      <view class="form-row">
        <text class="form-label">特殊提醒</text>
        <input class="text-input" v-model="specialNotes" placeholder="如慢性病/禁忌" />
      </view>
    </view>

    <view class="section-card" v-if="showAdvanced && isAccompany">
      <text class="section-label">外出陪同需求</text>
      <view class="form-row">
        <text class="form-label">出行目的</text>
        <input class="text-input" v-model="purpose" placeholder="就医/购物/办事" />
      </view>
      <view class="form-row">
        <text class="form-label">路线/地点</text>
        <input class="text-input" v-model="route" placeholder="出发地-目的地简述" />
      </view>
      <view class="form-row">
        <text class="form-label">辅助需求</text>
        <picker :range="wheelchairOptions" @change="onWheelchairChange">
          <view class="picker-field">{{ needWheelchair }}</view>
        </picker>
      </view>
    </view>

    <view class="section-card" v-if="showAdvanced && isPurchase">
      <text class="section-label">代购买清单</text>
      <view class="form-row">
        <text class="form-label">物品类型</text>
        <picker :range="purchaseTypeOptions" @change="onPurchaseTypeChange">
          <view class="picker-field">{{ purchaseType }}</view>
        </picker>
      </view>
      <view class="form-row">
        <text class="form-label">清单/数量</text>
        <input class="text-input" v-model="purchaseItems" placeholder="如 牛奶2盒+药品" />
      </view>
      <view class="form-row">
        <text class="form-label">预算/备注</text>
        <input class="text-input" v-model="purchaseBudget" placeholder="如 预算50元，需要发票请注明" />
      </view>
    </view>

    <view class="section-card" v-if="showAdvanced && isRepair">
      <text class="section-label">维修细项</text>
      <view class="chip-row">
        <view
          v-for="r in repairOptions"
          :key="r"
          class="chip"
          :class="{ active: repairCategory === r }"
          @click="repairCategory = r"
        >{{ r }}</view>
      </view>
      <view class="form-row" style="margin-top:10px;">
        <text class="form-label">故障描述</text>
        <input class="text-input" v-model="repairDetail" placeholder="如 冰箱不制冷/门锁卡顿" />
      </view>
    </view>

    <!-- 备注 -->
    <view class="section-card">
      <text class="section-label">备注</text>
      <input
        class="text-input"
        v-model="remark"
        placeholder="可补充特别说明（可选）"
      />
    </view>

    <!-- 提交 -->
    <view class="price-tip">
      <text class="price-text">服务完成后付费 · 预估 ¥{{ servicePrice }}</text>
    </view>
    <view class="submit-btn" @click="handleSubmit">
      <text class="submit-text">立即预约</text>
      <text class="submit-sub">预估¥{{ servicePrice }} · 后付费</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useUserStore } from '../../store/user';
import { getUserProfile } from '../../api/user';

const userStore = useUserStore();

const serviceId = ref('');
const serviceName = ref('服务');
const servicePrice = ref('0.00');
const commonAddress = ref('');
const selectedDate = ref('today');
const selectedSlot = ref('morning');
const selectedTime = ref('09:00');
const address = ref('');
const remark = ref('');

// 长期订单
const orderType = ref('SINGLE');
const dateStart = ref('');
const dateEnd = ref('');
const recurrenceRule = ref('每天');
const recurrenceOptions = ['每天', '每周', '每周一三五', '每周二四六', '工作日'];
const todayStr = (() => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
})();

const today = new Date();
const tomorrow = new Date(today); tomorrow.setDate(today.getDate() + 1);
const afterTomorrow = new Date(today); afterTomorrow.setDate(today.getDate() + 2);

const fmt = (d) => `${d.getMonth() + 1}月${d.getDate()}日`;

const dateOptions = [
  { key: 'today',        label: `今天 ${fmt(today)}` },
  { key: 'tomorrow',     label: `明天 ${fmt(tomorrow)}` },
  { key: 'aftertomorrow', label: `后天 ${fmt(afterTomorrow)}` },
];

const slotConfigs = {
  morning: { label: '上午', display: '上午 09:00-11:00', start: '08:00', end: '12:00', def: '09:00' },
  noon: { label: '下午', display: '下午 14:00-16:00', start: '12:00', end: '17:00', def: '14:00' },
  afternoon: { label: '傍晚', display: '傍晚 17:00-19:00', start: '16:00', end: '20:00', def: '17:00' },
};
const slotOptions = [
  { key: 'morning',   label: slotConfigs.morning.display },
  { key: 'noon',      label: slotConfigs.noon.display },
  { key: 'afternoon', label: slotConfigs.afternoon.display },
];

const getDateStr = (key) => {
  const map = { today, tomorrow, aftertomorrow: afterTomorrow };
  const d = map[key];
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const slotLabelDisplay = computed(() => slotConfigs[selectedSlot.value].display);
const slotStart = computed(() => slotConfigs[selectedSlot.value].start);
const slotEnd = computed(() => slotConfigs[selectedSlot.value].end);

const selectSlot = (key) => {
  selectedSlot.value = key;
  selectedTime.value = slotConfigs[key].def;
};

const onTimeChange = (e) => {
  selectedTime.value = e.detail.value;
};

const handleVoice = () => uni.showToast({ title: '语音功能开发中', icon: 'none' });

const detectServiceKey = computed(() => {
  const name = (serviceName.value || '').toLowerCase();
  if (name.includes('照护')) return 'daycare';
  if (name.includes('陪同')) return 'accompany';
  if (name.includes('代购')) return 'purchase';
  if (name.includes('维修')) return 'repair';
  return '';
});

const isDayCare = computed(() => detectServiceKey.value === 'daycare');
const isAccompany = computed(() => detectServiceKey.value === 'accompany');
const isPurchase = computed(() => detectServiceKey.value === 'purchase');
const isRepair = computed(() => detectServiceKey.value === 'repair');

// 折叠控制
const showAdvanced = ref(false);
const toggleAdvanced = () => { showAdvanced.value = !showAdvanced.value; };

// 细分字段
const careFocus = ref('');
const mobility = ref('');
const specialNotes = ref('');

const purpose = ref('');
const route = ref('');
const wheelchairOptions = ['无需轮椅/车辆', '需要轮椅', '需要车辆'];
const needWheelchair = ref('无需轮椅/车辆');

const purchaseTypeOptions = ['日用品', '食品', '药品', '其他'];
const purchaseType = ref('日用品');
const purchaseItems = ref('');
const purchaseBudget = ref('');

const repairOptions = ['家电-冰箱', '家电-洗衣机', '家电-电视', '管道/水电', '门锁', '其他'];
const repairCategory = ref('');
const repairDetail = ref('');

const onWheelchairChange = (e) => {
  needWheelchair.value = wheelchairOptions[e.detail.value];
};

const onPurchaseTypeChange = (e) => {
  purchaseType.value = purchaseTypeOptions[e.detail.value];
};

const buildRemark = () => {
  const extra = [];
  if (isDayCare.value) {
    if (careFocus.value) extra.push(`照护重点:${careFocus.value}`);
    if (mobility.value) extra.push(`行动:${mobility.value}`);
    if (specialNotes.value) extra.push(`提醒:${specialNotes.value}`);
  }
  if (isAccompany.value) {
    if (purpose.value) extra.push(`目的:${purpose.value}`);
    if (route.value) extra.push(`路线:${route.value}`);
    if (needWheelchair.value) extra.push(needWheelchair.value);
  }
  if (isPurchase.value) {
    if (purchaseType.value) extra.push(`类型:${purchaseType.value}`);
    if (purchaseItems.value) extra.push(`清单:${purchaseItems.value}`);
    if (purchaseBudget.value) extra.push(`预算:${purchaseBudget.value}`);
  }
  if (isRepair.value) {
    if (repairCategory.value) extra.push(`维修:${repairCategory.value}`);
    if (repairDetail.value) extra.push(`故障:${repairDetail.value}`);
  }
  const base = remark.value.trim();
  if (extra.length === 0) return base;
  return base ? `${base}；${extra.join('；')}` : extra.join('；');
};

watch(selectedSlot, (val) => {
  selectedTime.value = slotConfigs[val].def;
});

const handleSubmit = () => {
  // 触觉反馈
  uni.vibrateShort({ type: 'light' });
  
  if (!address.value.trim()) {
    uni.showToast({ title: '请填写服务地址', icon: 'none' });
    return;
  }
  if (isRepair.value && !repairCategory.value && !repairDetail.value) {
    uni.showToast({ title: '请补充维修项目或故障', icon: 'none' });
    return;
  }
  // 长期订单校验
  if (orderType.value === 'RECURRING') {
    if (!dateStart.value || !dateEnd.value) {
      uni.showToast({ title: '请选择长期预约的起止日期', icon: 'none' });
      return;
    }
    if (dateEnd.value <= dateStart.value) {
      uni.showToast({ title: '结束日期须晚于开始日期', icon: 'none' });
      return;
    }
  }
  const slotStr = `${slotConfigs[selectedSlot.value].label} ${selectedTime.value}`;
  const params = {
    serviceId: serviceId.value,
    serviceName: serviceName.value,
    servicePrice: servicePrice.value,
    dateStr: getDateStr(selectedDate.value),
    slotStr,
    address: address.value.trim(),
    remark: buildRemark(),
    orderType: orderType.value,
    dateStart: orderType.value === 'RECURRING' ? dateStart.value : '',
    dateEnd: orderType.value === 'RECURRING' ? dateEnd.value : '',
    recurrenceRule: orderType.value === 'RECURRING' ? recurrenceRule.value : '',
  };
  uni.navigateTo({
    url: `/pages/order/confirm?params=${encodeURIComponent(JSON.stringify(params))}`
  });
};

onLoad((query) => {
  serviceId.value = query?.serviceId || '';
  serviceName.value = decodeURIComponent(query?.serviceName || '服务');
  servicePrice.value = query?.servicePrice || '0.00';
  
  // 智能默认时段：根据当前时间选择最近可用时段
  const now = new Date();
  const hour = now.getHours();
  if (hour < 9) {
    selectedSlot.value = 'morning'; // 09:00 前 → 上午
  } else if (hour < 14) {
    selectedSlot.value = 'noon'; // 14:00 前 → 中午
  } else {
    selectedSlot.value = 'afternoon'; // 否则 → 下午
  }
  // 根据默认段初始化时间
  selectedTime.value = slotConfigs[selectedSlot.value].def;
  
  // 加载用户地址预填
  loadUserAddress();
});

const loadUserAddress = async () => {
  try {
    const userId = userStore.userId;
    if (!userId) return;
    const res = await getUserProfile(userId);
    if (res.data && res.data.address) {
      commonAddress.value = res.data.address;
      address.value = res.data.address; // 预填地址
    }
  } catch (e) {
    // 静默失败，用户可手动输入
  }
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

.create-header {
  background: $gradient-brand;
  border-bottom: none;
}

.header-title {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

.voice-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: $color-card;
  margin: 12px 16px 0;
  border-radius: 12px;
  border: 1px solid $color-border;
  box-shadow: 0 8px 16px $color-shadow;
}

.hint-text {
  font-size: 18px;
  color: $color-text;
  font-weight: 700;
}

.voice-small-btn {
  background: $color-primary;
  color: #fff;
  border-radius: 12px;
  height: 56px;
  line-height: 56px;
  padding: 0 16px;
  font-size: 18px;
  font-weight: 700;
  border: 1px solid $color-primary;
  flex-shrink: 0;
}

.section-card {
  background: $color-card;
  border-radius: 12px;
  padding: 16px;
  margin: 12px 16px 0;
  border: 1px solid $color-border;
  box-shadow: 0 8px 16px $color-shadow;
}

.section-label {
  display: block;
  font-size: 18px;
  color: $color-muted;
  margin-bottom: 10px;
  font-weight: 700;
}

.chip-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  flex: 1;
  min-width: 96px;
  text-align: center;
  min-height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 10px;
  border-radius: 12px;
  border: 2px solid $color-border;
  font-size: 18px;
  color: $color-muted;
  white-space: nowrap;
  box-sizing: border-box;
  background: $color-card;
}

.chip.active {
  background: $color-primary;
  border-color: $color-primary;
  color: #fff;
  font-weight: 700;
}

.slot-row .chip {
  white-space: normal;
  line-height: 1.4;
  min-width: 44%;
  flex: 1 1 44%;
}

.time-picker-row {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.time-tip {
  font-size: 18px;
  color: $color-muted;
}

.time-btn {
  background: $color-card;
  color: $color-primary;
  padding: 0 14px;
  height: 56px;
  line-height: 56px;
  border-radius: 12px;
  font-size: 18px;
  border: 2px solid $color-border;
}

.form-row {
  margin-top: 10px;
}

.form-label {
  display: block;
  font-size: 18px;
  color: $color-muted;
  margin-bottom: 6px;
  font-weight: 700;
}

.picker-field {
  background: $color-card;
  border: 2px solid $color-border;
  border-radius: 12px;
  padding: 0 14px;
  height: 56px;
  display: flex;
  align-items: center;
  font-size: 18px;
  color: $color-text;
  box-sizing: border-box;
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.label-row .section-label {
  margin-bottom: 0;
}

.use-saved-btn {
  background: $color-primary;
  color: #fff;
  font-size: 18px;
  height: 56px;
  line-height: 56px;
  padding: 0 16px;
  border-radius: 12px;
  border: 2px solid $color-primary;
  flex-shrink: 0;
}

.saved-addr-hint {
  display: block;
  font-size: 18px;
  color: $color-muted;
  margin-bottom: 8px;
}

.text-input {
  display: block;
  width: 100%;
  box-sizing: border-box;
  background: $color-card;
  border: 2px solid $color-border;
  border-radius: 12px;
  padding: 0 14px;
  height: 56px;
  line-height: 56px;
  font-size: 18px;
  color: $color-text;
}

.price-tip {
  text-align: center;
  padding: 12px 16px 4px;
}

.price-text {
  font-size: 18px;
  color: $color-muted;
}

.submit-btn {
  margin: 10px 16px 0;
  background: $color-primary;
  border-radius: 12px;
  border: 1px solid $color-primary;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 72px;
  gap: 4px;
}

.submit-text {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.submit-sub {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.92);
  line-height: 1.2;
}

.advanced-toggle {
  margin: 16px;
  padding: 0 12px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-card;
  border: 1px solid $color-border;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 8px 16px $color-shadow;
}

.toggle-text {
  font-size: 18px;
  color: $color-primary;
  font-weight: 700;
}

.recurring-fields {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 2px solid $color-border;
}
</style>
