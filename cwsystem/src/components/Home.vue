<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';

const router = useRouter();
// 模拟用户信息（实际从接口获取）
const userInfo = JSON.parse(localStorage.getItem('user') || '{"name":"超市管理员"}');


// 退出登录功能
const logout = async () => {
  try {
    await ElMessageBox.confirm(
        "确定退出登录？",
        "提示",
        { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" }
    );
    // 清除本地存储
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    // 跳转到登录页
    router.push("/login");
    ElMessage.success("退出成功");
  } catch (err) {
    ElMessage.info("已取消退出");
  }
};
</script>

<template>
  <div class="home-container">
    <!-- 头部：标题+退出按钮 -->
    <div class="home-header">
      <h2>超市财务系统概览</h2>
      <el-button type="text" color="#f56c6c" @click="logout">退出登录</el-button>
    </div>

    <!-- 欢迎信息 -->
    <div class="welcome-info">
      <p>欢迎回来，{{ userInfo.name }}！实时掌握超市财务状况</p>
    </div>



    <div class="quick-func">
      <h3>快捷操作</h3>
      <div class="func-buttons">
        <button @click="router.push('/One')" class="func-btn">供应商管理</button>
        <button @click="router.push('/Two')" class="func-btn">客户管理</button>
        <button @click="router.push('/Four')" class="func-btn">科目管理</button>
        <button @click="router.push('/Five')" class="func-btn">订单管理</button>
        <button @click="router.push('/Six')" class="func-btn">销售单管理</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  background-color: #f8f9fa;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.home-header h2 {
  font-size: 22px;
  color: #333;
  margin: 0;
}

.welcome-info {
  margin-bottom: 30px;
}

.welcome-info p {
  font-size: 16px;
  color: #666;
}

.stats-container {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 200px;
  background: #fff;
  padding: 25px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.quick-func {
  background: #fff;
  padding: 25px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.quick-func h3 {
  font-size: 18px;
  color: #333;
  margin: 0 0 15px;
}

.func-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.func-btn {
  padding: 10px 24px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.func-btn:hover {
  background: #3086d6;
}
</style>