<template>
  <el-menu
      router
      active-text-color="#ffd04b"
      background-color="#545c64"
      text-color="#fff"
      style="height: 100vh; overflow-y: auto;"
      :default-active="activeMenu"
      :collapse="isCollapse"
      @select="handleMenuSelect"
  >
    <!-- 首页 -->
    <el-menu-item index="/Home">
      <i class="el-icon"><el-icon><House /></el-icon></i>
      <span>首页</span>
    </el-menu-item>

    <!-- 客户/供应商管理（基础往来） -->
    <el-menu-item index="/One">
      <i class="el-icon"><el-icon><Avatar /></el-icon></i>
      <span>供应商管理</span>
    </el-menu-item>
    <el-menu-item index="/Two">
      <i class="el-icon"><el-icon><UserFilled /></el-icon></i>
      <span>客户管理</span>
    </el-menu-item>

    <!-- 新增：产品管理（放在客户管理后面，订单管理前面，逻辑更连贯） -->
    <el-menu-item index="/ProductManage">
      <i class="el-icon"><el-icon><Box /></el-icon></i> <!-- 产品图标，适配Element Plus -->
      <span>产品管理</span>
    </el-menu-item>

    <!-- 订单/销售单管理（业务核心） -->
    <el-menu-item index="/Five">
      <i class="el-icon"><el-icon><ShoppingCart /></el-icon></i>
      <span>订单管理</span>
    </el-menu-item>
    <el-menu-item index="/Six">
      <i class="el-icon"><el-icon><FolderRemove /></el-icon></i>
      <span>销售单管理</span>
    </el-menu-item>

    <!-- 财务模块（一级子菜单） -->
    <el-sub-menu index="/finance">
      <template #title>
        <i class="el-icon"><el-icon><Document /></el-icon></i>
        <span>财务模块</span>
      </template>

      <!-- 财务模块下的一级菜单项 -->
      <el-menu-item index="/Four">
        <i class="el-icon"><el-icon><Document /></el-icon></i>
        <span>科目管理</span>
      </el-menu-item>
      <el-menu-item index="/voucher/list">
        <i class="el-icon"><el-icon><Document /></el-icon></i>
        <span>凭证管理</span>
      </el-menu-item>

      <!-- 财务报表（二级子菜单） -->
      <el-sub-menu index="/Seven">
        <template #title>
          <i class="el-icon"><el-icon><Tickets /></el-icon></i>
          <span>财务报表</span>
        </template>
        <el-menu-item index="/Seven/balance-sheet">
          <i class="el-icon"><el-icon><Tickets /></el-icon></i>
          <span>资产负债表</span>
        </el-menu-item>
        <el-menu-item index="/Seven/income-statement">
          <i class="el-icon"><el-icon><Tickets /></el-icon></i>
          <span>利润表</span>
        </el-menu-item>
        <el-menu-item index="/Seven/cash-flow">
          <i class="el-icon"><el-icon><Tickets /></el-icon></i>
          <span>现金流量表</span>
        </el-menu-item>
      </el-sub-menu>

    </el-sub-menu>
    <el-menu-item index="/Main">
      <i class="el-icon"><el-icon><User /></el-icon></i>
      <span>用户管理</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
// 新增：导入产品图标 Box
import {
  Avatar,
  Box,
  Document,
  FolderRemove,
  House,
  ShoppingCart,
  Tickets,
  User,
  UserFilled
} from "@element-plus/icons-vue";

const router = useRouter();
const isCollapse = ref(false);
const activeMenu = ref('/Home');

const handleMenuSelect = (index) => {
  router.push(index);
  activeMenu.value = index;
};
</script>

<style scoped>
:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
  color: #fff !important;
}

:deep(.el-sub-menu__title) {
  &:hover {
    background-color: #4a5059 !important;
  }
}

:deep(.el-sub-menu .el-menu) {
  animation: fadeIn 0.2s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

:deep(.el-menu--collapse) {
  width: 64px !important;

  .el-sub-menu__title .el-icon {
    margin-left: 4px;
  }
}

:deep(.el-menu) {
  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: #666;
    border-radius: 2px;
  }

  &::-webkit-scrollbar-track {
    background-color: #545c64;
  }
}
</style>