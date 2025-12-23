<template>
  <div style="padding: 20px; background: #f5f7fa;">
    <ElCard shadow="hover">
      <h3 style="margin: 0 0 20px 0; color: #1989fa; font-size: 16px;">编辑凭证</h3>

      <!-- 基础信息表单 -->
      <ElForm :model="form" :rules="formRules" ref="formRef" label-width="100px" style="margin-bottom: 20px;">
        <ElFormItem label="凭证编号" prop="transNo">
          <ElInput v-model="form.transNo" disabled placeholder="自动生成" style="width: 200px;" />
        </ElFormItem>
        <ElFormItem label="交易日期" prop="transDate">
          <ElDatePicker
              v-model="form.transDate"
              type="date"
              placeholder="选择交易日期"
              style="width: 200px;"
              value-format="YYYY-MM-DD"
              required
          />
        </ElFormItem>
        <ElFormItem label="交易类型" prop="transType">
          <ElSelect v-model="form.transType" placeholder="选择交易类型" style="width: 200px;" required>
            <ElOption v-for="item in transTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="交易说明" prop="description">
          <ElInput v-model="form.description" placeholder="输入交易说明（如：2025年12月办公费）" style="width: 500px;" />
        </ElFormItem>
      </ElForm>

      <!-- 分录列表 -->
      <div style="margin-bottom: 20px;">
        <h4 style="margin: 0 0 10px 0; font-size: 14px;">分录信息（至少2条，借贷平衡）</h4>
        <ElTable :data="entriesList" border style="width: 100%; margin-bottom: 10px;">
          <ElTableColumn label="科目" prop="accountName" width="200">
            <template #default="scope">
              <ElSelect
                  v-model="scope.row.accountId"
                  placeholder="选择科目"
                  style="width: 100%;"
                  @change="handleAccountChange(scope.$index)"
              >
                <ElOption v-for="acc in accountList" :key="acc.id" :label="acc.name" :value="acc.id" />
              </ElSelect>
            </template>
          </ElTableColumn>
          <ElTableColumn label="借贷方向" prop="entryType" width="120">
            <template #default="scope">
              <ElSelect v-model="scope.row.entryType" style="width: 100%;" @change="calcBalance">
                <ElOption label="借方" value="借方" />
                <ElOption label="贷方" value="贷方" />
              </ElSelect>
            </template>
          </ElTableColumn>
          <ElTableColumn label="金额（元）" prop="amount" width="150">
            <template #default="scope">
              <ElInput
                  v-model.number="scope.row.amount"
                  placeholder="输入金额"
                  style="width: 100%;"
                  @input="calcBalance"
                  type="number"
                  min="0.01"
                  step="0.01"
              />
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="80" align="center">
            <template #default="scope">
              <ElButton
                  type="text"
                  style="color: #f56c6c;"
                  @click="removeEntry(scope.$index)"
                  :disabled="entriesList.length <= 2"
              >
                删除
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>

        <!-- 平衡校验+新增分录按钮 -->
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div style="color: #666; font-size: 14px;">
            借方合计：<span :style="{ color: isBalance ? '#48bb78' : '#f56c6c' }">{{ debitTotal.toFixed(2) }}</span> 元 |
            贷方合计：<span :style="{ color: isBalance ? '#48bb78' : '#f56c6c' }">{{ creditTotal.toFixed(2) }}</span> 元 |
            <span :style="{ color: isBalance ? '#48bb78' : '#f56c6c' }">
              {{ isBalance ? '✅ 借贷平衡' : '❌ 借贷不平衡' }}
            </span>
          </div>
          <ElButton type="primary" @click="addEntry">
            <ElIcon><Plus /></ElIcon> 新增分录
          </ElButton>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div style="display: flex; justify-content: center; gap: 10px;">
        <ElButton @click="router.back()">取消</ElButton>
        <ElButton type="primary" @click="submitForm" :disabled="!isBalance">提交更新</ElButton>
      </div>
    </ElCard>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { ElForm, ElFormItem, ElInput, ElDatePicker, ElSelect, ElOption, ElButton, ElTable, ElTableColumn, ElIcon, ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const formRef = ref(null);

// 表单数据
const form = reactive({
  id: '', // 凭证ID
  transNo: '',
  transDate: '',
  transType: '',
  description: ''
});

// 表单校验规则
const formRules = reactive({
  transDate: [{ required: true, message: '交易日期不能为空', trigger: 'change' }],
  transType: [{ required: true, message: '交易类型不能为空', trigger: 'change' }],
  description: [{ required: true, message: '交易说明不能为空', trigger: 'blur' }]
});

// 科目列表
const accountList = ref([]);
// 分录列表
const entriesList = ref([]);

// 计算借贷合计
const debitTotal = computed(() => {
  return entriesList.value
      .filter(item => item.entryType === '借方')
      .reduce((sum, item) => sum + (item.amount || 0), 0);
});

const creditTotal = computed(() => {
  return entriesList.value
      .filter(item => item.entryType === '贷方')
      .reduce((sum, item) => sum + (item.amount || 0), 0);
});

const isBalance = computed(() => {
  return debitTotal.value > 0 && debitTotal.value === creditTotal.value;
});

// 加载凭证详情+科目列表
const loadVoucherDetail = async () => {
  try {
    const voucherId = route.params.id;
    if (!voucherId) {
      ElMessage.error('凭证ID不能为空');
      router.push('/Eight');
      return;
    }

    // 加载凭证详情（含分录）
    const res = await axios.get(`/system/transactions/${voucherId}`);
    const voucher = res.data.data;
    form.id = voucher.id;
    form.transNo = voucher.transNo;
    form.transDate = voucher.transDate;
    form.transType = voucher.transType;
    form.description = voucher.description;

    // 加载科目列表
    const accRes = await axios.get('/system/accounts/list');
    accountList.value = accRes.data.data || [];

    // 格式化分录列表
    entriesList.value = voucher.entriesList.map(entry => {
      const account = accountList.value.find(acc => acc.id === entry.accountId);
      return {
        accountId: entry.accountId,
        accountName: account ? account.name : '',
        entryType: entry.entryType,
        amount: entry.amount
      };
    });

    // 确保至少2条分录
    if (entriesList.value.length < 2) {
      entriesList.value.push({ accountId: '', accountName: '', entryType: '借方', amount: 0 });
    }
  } catch (err) {
    ElMessage.error('加载详情失败：' + (err.response?.data?.msg || err.message));
    router.push('/Eight');
  }
};

// 新增分录
const addEntry = () => {
  entriesList.value.push({ accountId: '', accountName: '', entryType: '借方', amount: 0 });
  calcBalance();
};

// 删除分录
const removeEntry = (index) => {
  entriesList.value.splice(index, 1);
  calcBalance();
};

// 选择科目更新名称
const handleAccountChange = (index) => {
  const entry = entriesList.value[index];
  const account = accountList.value.find(acc => acc.id === entry.accountId);
  entry.accountName = account ? account.name : '';
  calcBalance();
};

// 重新计算平衡
const calcBalance = () => {
  debitTotal.value;
  creditTotal.value;
};

// 提交更新
const submitForm = async () => {
  try {
    await formRef.value.validate();

    // 校验分录科目
    const emptyAccount = entriesList.value.some(item => !item.accountId);
    if (emptyAccount) {
      ElMessage.error('请为所有分录选择科目');
      return;
    }

    // 构造参数
    const params = {
      id: form.id,
      transNo: form.transNo, // 锁定编号，不允许修改
      transDate: form.transDate,
      transType: form.transType,
      description: form.description,
      entriesList: entriesList.value.map(item => ({
        accountId: item.accountId,
        entryType: item.entryType,
        amount: item.amount
      }))
    };

    // 调用编辑接口
    await axios.put(`/system/transactions/update/${form.id}`, params);
    ElMessage.success('更新凭证成功');
    router.push('/voucher/list');
  } catch (err) {
    ElMessage.error('更新失败：' + (err.response?.data?.msg || err.message));
  }
};

// 初始化
onMounted(() => {
  loadVoucherDetail();
});
</script>