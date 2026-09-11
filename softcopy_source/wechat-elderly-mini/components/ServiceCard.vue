<template>
  <view class="card" @click="$emit('click')">
    <view class="row">
      <text class="name">{{ title }}</text>
      <text class="price">¥{{ price }}</text>
    </view>
    <text class="desc">{{ desc }}</text>
    <view class="btn">查看详情</view>
  </view>
</template>

<script setup>
defineProps({
  title: String,
  desc: String,
  price: [String, Number]
});

defineEmits(['click']);
</script>

<style lang="scss" scoped>
.card {
  background: $color-card;
  border-radius: 14px;
  padding: 16px;
  border: 1px solid $color-border;
  margin-bottom: 12px;
  box-shadow: 0 8px 16px $color-shadow;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-size: 22px;
  font-weight: 700;
  color: $color-text;
}

.price {
  font-size: 22px;
  font-weight: 700;
  color: $color-primary;
}

.desc {
  display: block;
  margin-top: 12px;
  font-size: 18px;
  color: $color-muted;
}

.btn {
  background: $color-primary;
  color: #fff;
  border-radius: 12px;
  text-align: center;
  height: 56px;
  line-height: 56px;
  margin-top: 20px;
  font-size: 20px;
  font-weight: 700;
  border: 1px solid $color-primary;
}
</style>
