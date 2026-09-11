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
  background: $gradient-soft;
  color: $color-text;
  font-family: "Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
}
</style>
