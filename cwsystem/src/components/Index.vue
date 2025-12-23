<script setup>
import Aside from './Aside.vue'; // 引入侧边栏组件
</script>

<template>
  <el-container style="width: 100vw; height: 100vh; overflow: hidden;">
    <!-- 侧边栏 -->
    <el-aside width="200px" style="height: 100vh; background-color: #545c64; padding-top: 20px;">
      <Aside />
    </el-aside>

    <!-- 主内容区 -->
    <el-container style="height: 100vh; overflow: hidden;">
      <el-main style="height: calc(100vh - 20px); overflow-y: auto; padding: 20px; margin: 0; background-color: #f8f9fa;">
        <!-- 路由视图：渲染首页/各功能页 -->
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
/* 清除默认边距 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 侧边栏样式优化 */
:deep(.el-aside) {
  border-right: none;
}
</style>