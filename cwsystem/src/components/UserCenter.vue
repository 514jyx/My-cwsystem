<!-- src/components/UserCenter.vue -->
<script setup>
import { useRouter } from 'vue-router';
import { ElButton } from 'element-plus';

const router = useRouter();
// 返回主页
const goBack = () => {
  router.push('/');
};
</script>

<template>
  <div style="width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; padding-top: 50px;">
    <h2 style="margin-bottom: 30px;">个人中心</h2>
    <div style="width: 80%; max-width: 600px; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
      <p>当前功能正在开发中...</p>
      <ElButton type="primary" @click="goBack" style="margin-top: 20px;">返回主页</ElButton>
    </div>
  </div>
</template>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>