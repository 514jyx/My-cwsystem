<script setup>
import { RouterView } from 'vue-router';
</script>

<template>
  <div class="app-container">
    <RouterView />
  </div>
</template>

<style>
/* 注意：这里去掉 scoped！让样式全局生效，彻底控制 html/body */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100vw !important;
  height: 100vh !important;
  overflow: hidden !important;
  margin: 0 !important;
  padding: 0 !important;
}

.app-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
</style>