<template>
  <div class="report-container">
    <div class="page-header">
      <h2>现金流量表</h2>
      <div class="filter-bar">
        <el-form :model="filterForm" :inline="true" label-width="80px">
          <el-form-item label="统计期间">
            <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 300px;"
            ></el-date-picker>
          </el-form-item>
          <el-button type="primary" @click="fetchReportData">查询</el-button>
          <!-- 已删除导出Excel按钮 -->
        </el-form>
      </div>
    </div>

    <el-card shadow="hover" class="report-card">
      <div class="report-header">
        <h3 style="text-align: center; margin-bottom: 10px;">现金流量表（简化版）</h3>
        <p style="text-align: center; color: #666;">统计期间：{{ filterForm.dateRange ? filterForm.dateRange[0] + ' 至 ' + filterForm.dateRange[1] : '本月' }}</p>
      </div>

      <el-table :data="cashFlowList" border stripe style="width: 100%;">
        <el-table-column label="现金流量项目" prop="itemName" align="left" width="300"></el-table-column>
        <el-table-column label="金额（元）" prop="amount" align="right">
          <template #default="scope">{{ formatNumber(scope.row.amount) }}</template>
        </el-table-column>
      </el-table>

      <div class="total-row" style="margin-top: 20px;">
        <span class="total-label">经营活动现金净流量</span>
        <span class="total-value">{{ formatNumber(netCashFlow) }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {ref, reactive} from 'vue';
import {ElMessage} from 'element-plus';
import axios from 'axios';
// 已删除 xlsx 导入


const filterForm = reactive({
  dateRange: [
    new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0],
    new Date().toISOString().split('T')[0]
  ]
});

const cashFlowList = ref([]);
const netCashFlow = ref(0);

const formatNumber = (num) => {
  const number = typeof num === 'object' && num !== null ? Number(num) : num;
  return Number(number || 0).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
};

const calculateCashFlow = (accountsList) => {
  // 只取顶级科目（无parentId），避免子科目重复计算
  const incomeAccounts = accountsList.filter(item => item.type === '收入' && !item.parentId);
  const expenseAccounts = accountsList.filter(item => item.type === '费用' && !item.parentId);

  // 经营活动现金流入（销售商品收到的现金 = 主营业务收入 + 其他业务收入）
  const mainIncome = incomeAccounts.find(a => a.name === '主营业务收入')?.balance || 0;
  const otherIncome = incomeAccounts.find(a => a.name === '其他业务收入')?.balance || 0;
  const totalCashIn = Number(mainIncome) + Number(otherIncome);

  // 经营活动现金流出（按实际业务分类）
  const mainCost = expenseAccounts.find(a => a.name === '主营业务成本')?.balance || 0; // 购买商品支付的现金
  const saleFee = expenseAccounts.find(a => a.name === '销售费用')?.balance || 0; // 销售相关支出
  const manageFee = expenseAccounts.find(a => a.name === '管理费用')?.balance || 0; // 管理相关支出（含职工薪酬）
  const tax = expenseAccounts.find(a => a.name === '税金及附加')?.balance || 0; // 支付的税费
  const totalCashOut = Number(mainCost) + Number(saleFee) + Number(manageFee) + Number(tax);

  cashFlowList.value = [
    {itemName: '一、经营活动现金流入', amount: totalCashIn},
    {itemName: '  销售商品、提供劳务收到的现金', amount: mainIncome},
    {itemName: '  其他业务收入收到的现金', amount: otherIncome},
    {itemName: '二、经营活动现金流出', amount: totalCashOut},
    {itemName: '  购买商品、接受劳务支付的现金', amount: mainCost},
    {itemName: '  支付给职工以及为职工支付的现金', amount: manageFee},
    {itemName: '  支付的各项税费', amount: tax},
    {itemName: '  支付的其他与经营活动有关的现金', amount: saleFee},
  ];

  // 经营活动现金净流量 = 现金流入 - 现金流出
  netCashFlow.value = Number(totalCashIn) - Number(totalCashOut);
};

const fetchReportData = async () => {
  try {
    const res = await axios.get('/system/accounts/list');
    if (res.data.code === 200 && Array.isArray(res.data.data)) {
      calculateCashFlow(res.data.data);
      ElMessage.success('现金流量表数据加载成功');
    } else {
      ElMessage.error('获取科目数据失败：' + (res.data.msg || '未知错误'));
    }
  } catch (err) {
    ElMessage.error('加载报表异常：' + (err.message || '网络错误'));
  }
};

// 页面加载时自动请求数据
fetchReportData();
</script>

<style scoped>
.report-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.report-card {
  padding: 20px;
}

.report-header {
  margin-bottom: 30px;
}

.total-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-right: 20px;
  font-weight: 500;
  color: #f56c6c;
  font-size: 18px;
}

.total-label {
  width: 300px;
  text-align: left;
  padding-right: 20px;
}

.total-value {
  width: 200px;
  text-align: right;
  padding-right: 20px;
}

:deep(.el-table__header th) {
  background-color: #f8f9fa;
  font-weight: 500;
}

:deep(.el-table-column) {
  padding: 8px 0;
}
</style>