<template>
  <div class="report-container">
    <div class="page-header">
      <h2>利润表</h2>
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
        <h3 style="text-align: center; margin-bottom: 10px;">利润表</h3>
        <p style="text-align: center; color: #666;">统计期间：{{ filterForm.dateRange ? filterForm.dateRange[0] + ' 至 ' + filterForm.dateRange[1] : '本月' }}</p>
      </div>

      <el-table :data="incomeList" border stripe style="width: 100%;">
        <el-table-column label="报表项目" prop="itemName" align="left" width="300"></el-table-column>
        <el-table-column label="本期发生额（元）" prop="amount" align="right">
          <template #default="scope">{{ formatNumber(scope.row.amount) }}</template>
        </el-table-column>
      </el-table>

      <div class="total-row" style="margin-top: 20px;">
        <span class="total-label">净利润</span>
        <span class="total-value">{{ formatNumber(netProfit) }}</span>
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

const incomeList = ref([]);
const netProfit = ref(0);

const formatNumber = (num) => {
  const number = typeof num === 'object' && num !== null ? Number(num) : num;
  return Number(number || 0).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
};

const calculateIncomeStatement = (accountsList) => {
  // 只取顶级科目（无parentId），避免子科目重复计算
  const incomeAccounts = accountsList.filter(item => item.type === '收入' && !item.parentId);
  const expenseAccounts = accountsList.filter(item => item.type === '费用' && !item.parentId);

  const mainIncome = incomeAccounts.find(a => a.name === '主营业务收入')?.balance || 0;
  const otherIncome = incomeAccounts.find(a => a.name === '其他业务收入')?.balance || 0;
  const totalIncome = Number(mainIncome) + Number(otherIncome);

  const mainCost = expenseAccounts.find(a => a.name === '主营业务成本')?.balance || 0;
  const tax = expenseAccounts.find(a => a.name === '税金及附加')?.balance || 0;
  const saleFee = expenseAccounts.find(a => a.name === '销售费用')?.balance || 0;
  const manageFee = expenseAccounts.find(a => a.name === '管理费用')?.balance || 0;
  const financeFee = expenseAccounts.find(a => a.name === '财务费用')?.balance || 0;
  const totalExpense = Number(mainCost) + Number(tax) + Number(saleFee) + Number(manageFee) + Number(financeFee);

  incomeList.value = [
    {itemName: '一、营业收入', amount: totalIncome},
    {itemName: '  其中：主营业务收入', amount: mainIncome},
    {itemName: '  其他业务收入', amount: otherIncome},
    {itemName: '减：营业成本', amount: mainCost},
    {itemName: '  其中：主营业务成本', amount: mainCost},
    {itemName: '税金及附加', amount: tax},
    {itemName: '销售费用', amount: saleFee},
    {itemName: '管理费用', amount: manageFee},
    {itemName: '财务费用', amount: financeFee},
    {itemName: '二、营业利润', amount: totalIncome - totalExpense},
  ];

  // 净利润 = 营业利润（无其他收支时）
  netProfit.value = totalIncome - totalExpense;
};

const fetchReportData = async () => {
  try {
    const res = await axios.get('/system/accounts/list');
    if (res.data.code === 200 && Array.isArray(res.data.data)) {
      calculateIncomeStatement(res.data.data);
      ElMessage.success('利润表数据加载成功');
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