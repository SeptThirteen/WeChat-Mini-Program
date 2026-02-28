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
  border-radius: 12px;
  padding: 12px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-size: 20px;
}

.price {
  color: $color-primary;
}

.desc {
  display: block;
  margin-top: 8px;
  color: $color-muted;
}

.btn {
  background: $color-primary;
  color: #fff;
  border-radius: 10px;
  text-align: center;
  padding: 10px 0;
  margin-top: 10px;
}
</style>
