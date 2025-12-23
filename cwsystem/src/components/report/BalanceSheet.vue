<template>
  <div class="report-container">
    <div class="page-header">
      <h2>资产负债表</h2>
      <div class="filter-bar">
        <el-form :model="filterForm" :inline="true" label-width="80px">
          <el-form-item label="报表日期">
            <el-date-picker
                v-model="filterForm.reportDate"
                type="date"
                placeholder="选择报表日期"
                value-format="yyyy-MM-dd"
                style="width: 200px;"
            ></el-date-picker>
          </el-form-item>
          <el-button type="primary" @click="fetchReportData">查询</el-button>
        </el-form>
      </div>
    </div>

    <el-card shadow="hover" class="report-card">
      <div class="report-header">
        <h3 style="text-align: center; margin-bottom: 10px;">资产负债表</h3>
        <p style="text-align: center; color: #666;">编制日期：{{ filterForm.reportDate || new Date().toLocaleDateString() }}</p>
      </div>

      <div class="report-section">
        <h4 class="section-title">资产</h4>
        <el-table :data="assetList" border stripe style="width: 100%; margin-bottom: 20px;">
          <el-table-column label="资产项目" prop="itemName" align="left" width="300"></el-table-column>
          <el-table-column label="期末余额（元）" prop="balance" align="right">
            <template #default="scope">{{ formatNumber(scope.row.balance) }}</template>
          </el-table-column>
        </el-table>
        <div class="total-row">
          <span class="total-label">资产总计</span>
          <span class="total-value">{{ formatNumber(assetTotal) }}</span>
        </div>
      </div>

      <div class="report-section">
        <h4 class="section-title">负债及所有者权益</h4>
        <el-table :data="liabilityList" border stripe style="width: 100%; margin-bottom: 20px;">
          <el-table-column label="负债项目" prop="itemName" align="left" width="300"></el-table-column>
          <el-table-column label="期末余额（元）" prop="balance" align="right">
            <template #default="scope">{{ formatNumber(scope.row.balance) }}</template>
          </el-table-column>
        </el-table>
        <div class="total-row">
          <span class="total-label">负债总计</span>
          <span class="total-value">{{ formatNumber(liabilityTotal) }}</span>
        </div>

        <el-table :data="equityList" border stripe style="width: 100%; margin-top: 20px; margin-bottom: 20px;">
          <el-table-column label="所有者权益项目" prop="itemName" align="left" width="300"></el-table-column>
          <el-table-column label="期末余额（元）" prop="balance" align="right">
            <template #default="scope">{{ formatNumber(scope.row.balance) }}</template>
          </el-table-column>
        </el-table>
        <div class="total-row">
          <span class="total-label">所有者权益总计</span>
          <span class="total-value">{{ formatNumber(equityTotal) }}</span>
        </div>

        <div class="total-row total-grand">
          <span class="total-label">负债及所有者权益总计</span>
          <span class="total-value">{{ formatNumber(liabilityEquityTotal) }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {ref, reactive} from 'vue';
import {ElMessage} from 'element-plus';
import axios from 'axios';


// 筛选条件
const filterForm = reactive({
  reportDate: new Date().toISOString().split('T')[0]
});

// 报表数据存储
const assetList = ref([]); // 资产项目
const liabilityList = ref([]); // 负债项目
const equityList = ref([]); // 所有者权益项目
const assetTotal = ref(0); // 资产总计
const liabilityTotal = ref(0); // 负债总计
const equityTotal = ref(0); // 所有者权益总计
const liabilityEquityTotal = ref(0); // 负债及所有者权益总计

// 数字格式化（千分位+保留2位小数）
const formatNumber = (num) => {
  const number = typeof num === 'object' && num !== null ? Number(num) : num;
  return Number(number || 0).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
};

// 计算资产负债表数据（核心修复：只取顶级科目，避免子科目重复计算）
const calculateBalanceSheet = (accountsList) => {
  // 按科目类型分类 + 只取无parentId的顶级科目（子科目已包含在父科目余额中）
  const assetAccounts = accountsList.filter(item => item.type === '资产' && !item.parentId);
  const liabilityAccounts = accountsList.filter(item => item.type === '负债' && !item.parentId);
  const equityAccounts = accountsList.filter(item => item.type === '权益' && !item.parentId);

  // 构建资产项目（匹配数据库科目名称，无重复计算）
  assetList.value = [
    {
      itemName: '货币资金',
      balance: (assetAccounts.find(a => a.name === '库存现金')?.balance || 0) +
          (assetAccounts.find(a => a.name === '银行存款')?.balance || 0)
    },
    {itemName: '应收账款', balance: assetAccounts.find(a => a.name === '应收账款')?.balance || 0},
    {itemName: '存货', balance: assetAccounts.find(a => a.name === '库存商品')?.balance || 0},
    {itemName: '固定资产', balance: assetAccounts.find(a => a.name === '固定资产')?.balance || 0},
  ];

  // 构建负债项目
  liabilityList.value = [
    {itemName: '短期借款', balance: liabilityAccounts.find(l => l.name === '短期借款')?.balance || 0},
    {itemName: '长期借款', balance: liabilityAccounts.find(l => l.name === '长期借款')?.balance || 0},
    {itemName: '应付账款', balance: liabilityAccounts.find(l => l.name === '应付账款')?.balance || 0},
    {itemName: '应付职工薪酬', balance: liabilityAccounts.find(l => l.name === '应付职工薪酬')?.balance || 0},
    {itemName: '应交税费', balance: liabilityAccounts.find(l => l.name === '应交税费')?.balance || 0},
  ];

  // 构建所有者权益项目
  equityList.value = [
    {itemName: '实收资本', balance: equityAccounts.find(e => e.name === '实收资本')?.balance || 0},
    {itemName: '资本公积', balance: equityAccounts.find(e => e.name === '资本公积')?.balance || 0},
    {itemName: '未分配利润', balance: equityAccounts.find(e => e.name === '本年利润')?.balance || 0},
  ];

  // 计算总计（确保资产=负债+所有者权益，符合会计恒等式）
  assetTotal.value = assetList.value.reduce((sum, item) => sum + Number(item.balance || 0), 0);
  liabilityTotal.value = liabilityList.value.reduce((sum, item) => sum + Number(item.balance || 0), 0);
  equityTotal.value = equityList.value.reduce((sum, item) => sum + Number(item.balance || 0), 0);
  liabilityEquityTotal.value = liabilityTotal.value + equityTotal.value;
};

// 获取科目数据并渲染报表（接口路径已匹配后端/list接口）
const fetchReportData = async () => {
  try {
    const res = await axios.get('/system/accounts/list'); // 正确路径：http://localhost:8090/system/accounts/list
    console.log('接口返回科目数据：', res.data.data); // 调试日志，可删除

    if (res.data.code === 200 && Array.isArray(res.data.data)) {
      calculateBalanceSheet(res.data.data);
      ElMessage.success(`已加载 ${filterForm.reportDate} 资产负债表数据`);
    } else {
      ElMessage.error('获取科目数据失败：' + (res.data.msg || '未知错误'));
    }
  } catch (err) {
    ElMessage.error('加载报表异常：' + (err.message || '网络错误'));
    console.error('接口请求失败详情：', err); // 调试日志，可删除
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

.report-section {
  margin-bottom: 30px;
}

.section-title {
  border-bottom: 1px solid #e6e6e6;
  padding-bottom: 8px;
  margin-bottom: 15px;
  color: #409eff;
  font-size: 16px;
}

.total-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-right: 20px;
  font-weight: 500;
  color: #333;
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

.total-grand {
  color: #f56c6c;
  font-size: 15px;
  margin-top: 10px;
}

/* 表格样式优化 */
:deep(.el-table__header th) {
  background-color: #f8f9fa;
  font-weight: 500;
}

:deep(.el-table-column) {
  padding: 8px 0;
}
</style>