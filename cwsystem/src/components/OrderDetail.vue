<template>
  <div class="order-detail-container">
    <el-page-header content="订单详情" @back="goBack" />

    <el-card shadow="hover" class="order-main-info" style="margin-bottom: 20px;">
      <el-descriptions title="订单主信息" :column="3" border>
        <el-descriptions-item label="订单编号">{{ orderInfo.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商名称">{{ orderInfo.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusTagType(orderInfo.status)">{{ orderInfo.status || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单日期">{{ orderInfo.orderDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">{{ orderInfo.totalAmount ? orderInfo.totalAmount.toFixed(2) : '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="已付金额">{{ orderInfo.paidAmount ? orderInfo.paidAmount.toFixed(2) : '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ orderInfo.createdAt ? formatDate(orderInfo.createdAt) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ orderInfo.updatedAt ? formatDate(orderInfo.updatedAt) : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="hover" class="order-items-info">
      <el-descriptions title="订单项信息" border style="margin-bottom: 15px;">
        <el-descriptions-item label="订单项总数">{{ orderItems.length }} 条</el-descriptions-item>
      </el-descriptions>
      <el-table :data="orderItems" border stripe style="width: 100%;">
        <el-table-column label="序号" type="index" align="center" width="80px" />
        <el-table-column label="商品ID" prop="productId" align="center" />
        <el-table-column label="商品名称" prop="productName" align="center" />
        <el-table-column label="数量" prop="quantity" align="center" />
        <el-table-column label="单价" prop="unitPrice" align="center">
          <template #default="scope">{{ scope.row.unitPrice ? scope.row.unitPrice.toFixed(2) : '0.00' }}</template>
        </el-table-column>
        <el-table-column label="小计金额" prop="amount" align="center">
          <template #default="scope">{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

// 订单ID（从路由参数获取）
const orderId = ref(route.params.id);
// 订单主信息
const orderInfo = ref({});
// 订单项列表
const orderItems = ref([]);

// 页面加载时查询详情
onMounted(() => {
  fetchOrderDetail();
});

// 1. 查询订单详情（主表+订单项）
const fetchOrderDetail = async () => {
  try {
    const res = await proxy.$axios({
      url: `/system/orders/purchase/detail/${orderId.value}`,
      method: 'get'
    });
    if (res.code === 200) {
      orderInfo.value = res.data.order;
      orderItems.value = res.data.items;
    } else {
      ElMessage.error(res.msg || '查询详情失败');
      goBack(); // 失败后返回列表页
    }
  } catch (err) {
    ElMessage.error('查询异常：' + err.message);
    goBack();
  }
};

// 2. 返回上一页（订单列表）
const goBack = () => {
  router.push('/Five');
};

// 3. 日期格式化（毫秒数转字符串）
const formatDate = (time) => {
  if (!time) return '-';
  const date = new Date(time);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 4. 订单状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '草稿': return 'info';
    case '已确认': return 'primary';
    case '已完成': return 'success';
    case '已取消': return 'danger';
    default: return 'default';
  }
};
</script>

<style scoped>
.order-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.order-main-info {
  padding: 20px;
}

.order-items-info {
  padding: 20px;
}

/* 描述列表样式优化 */
:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #666;
}

:deep(.el-descriptions__content) {
  color: #333;
}
</style>