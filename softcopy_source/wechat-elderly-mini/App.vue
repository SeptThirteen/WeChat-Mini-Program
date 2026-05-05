<template>
  <view class="app">
    <slot />
  </view>
</template>

<script setup>
</script>

<style lang="scss">
@import "@/styles/theme.scss";
@import "@/uni.scss";

.app {
  min-height: 100vh;
  background: $color-bg;
  color: $color-text;
}
</style>
