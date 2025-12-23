<template>
  <div class="user-container">
    <!-- 渲染 Header.vue（头部） -->
    <Header />
    <!-- 渲染 Main.vue（用户管理主体内容） -->
    <div class="user-main">
      <Main />
    </div>
  </div>
</template>

<script setup>
// 导入 Header 和 Main 组件
import Header from './Header.vue';
import Main from './Main.vue';
</script>

<style scoped>
.user-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.user-main {
  flex: 1; /* 占满剩余高度 */
  padding: 20px;
  overflow-y: auto; /* 主体内容超出时滚动 */
  background-color: #f5f7fa; /* 和其他页面保持一致的背景色 */
}
</style>