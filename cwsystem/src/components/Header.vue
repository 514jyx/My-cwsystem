<script setup>

import { ArrowDownBold, Fold, User } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { ElMessage, ElDropdown, ElDropdownMenu, ElDropdownItem, ElIcon, ElMessageBox } from "element-plus";

const router = useRouter();

const toUser = () => {
  ElMessage.info("个人中心功能开发中～");
  console.log("个人中心功能开发中");
};

const logout = async () => {
  try {
    await ElMessageBox.confirm(
        "确定要退出登录吗？",
        "退出确认",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
          center: true
        }
    );

    // 清除登录状态
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    localStorage.removeItem("rememberedUser");
    ElMessage.success("退出登录成功");
    router.push("/login");
  } catch (error) {
    ElMessage.info("已取消退出");
  }
};
</script>

<template>
  <div class="toolbar" style="display: flex; line-height: 60px; width: 100%;">


    <div style="flex: 1; text-align: center; font-size: 22px; font-weight: 500; color: #303133;">
      欢迎来到企业财务管理系统
    </div>

    <div style="display: flex; align-items: center; margin-right: 20px;">

      <div
          style="display: flex; align-items: center; cursor: pointer; margin-right: 20px;"
      >
        <el-icon style="margin-right: 5px; font-size: 16px;"></el-icon>
        <span style="font-size: 14px;">账号设置</span>
      </div>

      <el-dropdown>
        <div style="cursor: pointer; display: flex; align-items: center;">
          <el-icon style="font-size: 18px;"><ArrowDownBold /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="toUser">
              <el-icon style="margin-right: 5px;"><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item @click="logout" type="danger">
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style scoped>

.toolbar div:hover:not(.no-hover) {
  color: var(--el-color-primary);
}


:deep(.el-dropdown-menu) {
  z-index: var(--el-index-popper) !important;
}
</style>