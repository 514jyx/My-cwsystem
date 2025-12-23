<template>
  <div style="padding: 20px; background: #f5f7fa;">
    <!-- 头部操作区 -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="margin: 0; color: #1989fa; font-size: 18px;">凭证管理</h2>
      <ElButton type="primary" @click="goToCreate">
        <ElIcon><Plus /></ElIcon> 新增凭证
      </ElButton>
    </div>

    <!-- 筛选表单 -->
    <ElCard shadow="hover" style="margin-bottom: 20px;">
      <div style="display: flex; align-items: center; gap: 20px; flex-wrap: wrap; padding: 10px 0;">
        <!-- 交易日期 -->
        <div style="display: flex; align-items: center;">
          <label style="width: 80px; text-align: right; font-size: 14px;">交易日期：</label>
          <ElDatePicker
              v-model="searchForm.startDate"
              type="date"
              placeholder="开始日期"
              style="width: 160px;"
              value-format="YYYY-MM-DD"
          />
          <span style="margin: 0 8px;">至</span>
          <ElDatePicker
              v-model="searchForm.endDate"
              type="date"
              placeholder="结束日期"
              style="width: 160px;"
              value-format="YYYY-MM-DD"
          />
        </div>

        <!-- 交易类型 -->
        <div style="display: flex; align-items: center;">
          <label style="width: 80px; text-align: right; font-size: 14px;">交易类型：</label>
          <ElSelect v-model="searchForm.transType" placeholder="全部" style="width: 140px;">
            <ElOption label="全部" value="" />
            <ElOption v-for="item in transTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </ElSelect>
        </div>

        <!-- 凭证状态（审核状态） -->
        <div style="display: flex; align-items: center;">
          <label style="width: 80px; text-align: right; font-size: 14px;">凭证状态：</label>
          <ElSelect v-model="searchForm.status" placeholder="全部" style="width: 140px;">
            <ElOption v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </ElSelect>
        </div>

        <!-- 搜索/重置按钮 -->
        <div style="margin-left: auto;">
          <ElButton type="primary" @click="searchVoucher" style="margin-right: 8px;">
            <ElIcon><Search /></ElIcon> 搜索
          </ElButton>
          <ElButton @click="resetSearch">
            <ElIcon><Refresh /></ElIcon> 重置
          </ElButton>
        </div>
      </div>
    </ElCard>

    <!-- 凭证表格 -->
    <ElCard shadow="hover">
      <ElTable
          :data="voucherList"
          border
          style="width: 100%;"
          :loading="isLoading"
          :empty-text="isLoading ? '加载中...' : '暂无凭证数据'"
      >
        <ElTableColumn label="序号" type="index" width="60" align="center" />
        <ElTableColumn label="凭证编号" prop="transNo" width="130" align="center" />
        <ElTableColumn label="交易日期" align="center" width="110">
          <template #default="scope">{{ formatDate(scope.row.transDate) }}</template>
        </ElTableColumn>
        <ElTableColumn label="交易类型" prop="transType" width="100" align="center" />
        <ElTableColumn label="交易说明" prop="description" width="300" />
        <ElTableColumn label="审核状态" width="100" align="center">
          <template #default="scope">
            <ElTag :type="scope.row.status === '已过账' ? 'success' : 'info'">
              {{ scope.row.status === '已过账' ? '已审核' : '待审核' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="220" align="center">
          <template #default="scope">
            <!-- 审核过账：仅待审核凭证可操作 -->
            <ElButton
                type="text"
                style="color: #48bb78; margin-right: 8px;"
                @click="postVoucher(scope.row.id)"
                :disabled="scope.row.status === '已过账'"
            >
              <ElIcon><Check /></ElIcon> 审核过账
            </ElButton>

            <!-- 编辑：仅待审核凭证可操作 -->
            <ElButton
                type="text"
                style="color: #4299e1; margin-right: 8px;"
                @click="goToEdit(scope.row.id)"
                :disabled="scope.row.status === '已过账'"
            >
              <ElIcon><Edit /></ElIcon> 编辑
            </ElButton>

            <!-- 删除：仅待审核凭证可操作 -->
            <ElPopconfirm
                title="确定删除该凭证？"
                @confirm="deleteVoucher(scope.row.id)"
                confirm-button-text="确认"
                cancel-button-text="取消"
            >
              <ElButton
                  type="text"
                  style="color: #f56c6c;"
                  :disabled="scope.row.status === '已过账'"
              >
                <ElIcon><Delete /></ElIcon> 删除
              </ElButton>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
      </ElTable>

      <!-- 分页 -->
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <ElPagination
            @size-change="(val) => (pageSize = val)"
            @current-change="(val) => (currentPage = val)"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
        />
      </div>
    </ElCard>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted} from 'vue';
import {useRouter} from 'vue-router';
import axios from 'axios';
import {
  ElTable, ElTableColumn, ElButton, ElDatePicker,
  ElSelect, ElOption, ElIcon, ElMessage, ElPopconfirm, ElPagination,
  ElCard, ElTag
} from 'element-plus';
import {Edit, Delete, Search, Refresh, Plus, Check} from '@element-plus/icons-vue';

const router = useRouter();

// 筛选条件
const searchForm = reactive({
  startDate: '',
  endDate: '',
  transType: '',
  status: ''
});

// 表格数据
const voucherList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const isLoading = ref(false);

// 下拉选项
const transTypeOptions = ref([
  {label: '采购', value: '采购'},
  {label: '销售', value: '销售'},
  {label: '收款', value: '收款'},
  {label: '付款', value: '付款'},
  {label: '费用', value: '费用'},
  {label: '收入', value: '收入'}
]);

const statusOptions = ref([
  {label: '全部', value: ''},
  {label: '待审核', value: '草稿'},
  {label: '已审核', value: '已过账'}
]);

// 加载凭证列表
const loadVoucherList = async () => {
  try {
    isLoading.value = true;
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      startDate: searchForm.startDate || undefined,
      endDate: searchForm.endDate || undefined,
      transType: searchForm.transType || undefined,
      status: searchForm.status || undefined
    };
    const res = await axios.get('/system/transactions/page', {params});
    voucherList.value = res.data.data?.list || [];
    total.value = res.data.data?.total || 0;
  } catch (err) {
    ElMessage.error('加载凭证失败：' + (err.response?.data?.msg || err.message));
    voucherList.value = [];
    total.value = 0;
  } finally {
    isLoading.value = false;
  }
};

// 搜索凭证
const searchVoucher = () => {
  currentPage.value = 1;
  loadVoucherList();
};

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    startDate: '',
    endDate: '',
    transType: '',
    status: ''
  });
  currentPage.value = 1;
  loadVoucherList();
};

// 删除凭证
const deleteVoucher = async (id) => {
  try {
    await axios.delete(`/system/transactions/delete/${id}`);
    ElMessage.success('删除成功');
    loadVoucherList();
  } catch (err) {
    ElMessage.error('删除失败：' + (err.response?.data?.msg || err.message));
  }
};

// 审核过账（仅变更状态，不更新余额）
const postVoucher = async (id) => {
  try {
    if (!id) {
      ElMessage.warning('凭证ID不存在');
      return;
    }

    // 调用后端过账接口（仅变更状态）
    await axios.put(`/system/transactions/update-status/${id}/已过账`);

    ElMessage.success('审核过账成功！');
    loadVoucherList(); // 刷新列表
  } catch (err) {
    ElMessage.error('审核过账失败：' + (err.response?.data?.msg || err.message));
  }
};

// 路由跳转
const goToCreate = () => router.push('/voucher/create');
const goToEdit = (id) => router.push(`/voucher/edit/${id}`);

// 辅助函数：格式化日期
const formatDate = (dateStr) => {
  if (!dateStr || typeof dateStr !== 'string') return '';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '';
  return date.toISOString().split('T')[0];
};

// 初始化加载
onMounted(() => {
  loadVoucherList();
});
</script>