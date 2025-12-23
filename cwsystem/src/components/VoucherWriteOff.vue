<template>
  <div style="padding: 20px; background: #f5f7fa;">
    <ElCard shadow="hover">
      <h3 style="margin: 0 0 20px 0; color: #f56c6c; font-size: 16px;">红字冲销凭证</h3>

      <!-- 原凭证信息（只读） -->
      <ElForm :model="originalForm" label-width="100px" style="margin-bottom: 20px;" disabled>
        <ElFormItem label="原凭证编号" prop="transNo">
          <ElInput v-model="originalForm.transNo" style="width: 200px;" />
        </ElFormItem>
        <ElFormItem label="原交易日期" prop="transDate">
          <ElInput v-model="originalForm.transDate" style="width: 200px;" />
        </ElFormItem>
        <ElFormItem label="原交易类型" prop="transType">
          <ElInput v-model="originalForm.transType" style="width: 200px;" />
        </ElFormItem>
      </ElForm>

      <!-- 冲销凭证信息 -->
      <ElForm :model="form" :rules="formRules" ref="formRef" label-width="100px" style="margin-bottom: 20px;">
        <ElFormItem label="冲销编号" prop="transNo">
          <ElInput v-model="form.transNo" disabled style="width: 200px;" />
        </ElFormItem>
        <ElFormItem label="冲销日期" prop="transDate">
          <ElDatePicker
              v-model="form.transDate"
              type="date"
              placeholder="选择冲销日期"
              style="width: 200px;"
              value-format="YYYY-MM-DD"
              required
          />
        </ElFormItem>
        <ElFormItem label="冲销说明" prop="description">
          <ElInput v-model="form.description" placeholder="输入冲销原因（如：原凭证金额录入错误）" style="width: 500px;" />
        </ElFormItem>
      </ElForm>

      <!-- 反向分录列表（不可修改） -->
      <div style="margin-bottom: 20px;">
        <h4 style="margin: 0 0 10px 0; font-size: 14px;">冲销分录（自动生成反向分录）</h4>
        <ElTable :data="entriesList" border style="width: 100%;">
          <ElTableColumn label="科目" prop="accountName" width="200" />
          <ElTableColumn label="原方向" prop="originalEntryType" width="120" align="center" />
          <ElTableColumn label="冲销方向" prop="entryType" width="120" align="center">
            <template #default="scope">
              <ElTag type="danger">{{ scope.row.entryType }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="金额（元）" prop="amount" width="150" align="right">
            <template #default="scope">{{ scope.row.amount.toFixed(2) }}</template>
          </ElTableColumn>
        </ElTable>

        <!-- 平衡提示 -->
        <div style="color: #48bb78; font-size: 14px; margin-top: 10px;">
          ✅ 冲销分录已自动生成，借贷平衡
        </div>
      </div>

      <!-- 底部按钮 -->
      <div style="display: flex; justify-content: center; gap: 10px;">
        <ElButton @click="router.back()">取消</ElButton>
        <ElButton type="danger" @click="submitForm">生成冲销凭证</ElButton>
      </div>
    </ElCard>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { ElForm, ElFormItem, ElInput, ElDatePicker, ElButton, ElTable, ElTableColumn, ElTag, ElMessage } from 'element-plus';

const router = useRouter();
const route = useRoute();
const formRef = ref(null);

// 原凭证信息（只读）
const originalForm = reactive({
  transNo: '',
  transDate: '',
  transType: '',
  id: '' // 原凭证ID
});

// 冲销凭证信息
const form = reactive({
  transNo: '',
  transDate: new Date().toISOString().split('T')[0], // 默认今天
  transType: '红字冲销',
  description: '',
  writeOffTargetId: '' // 关联原凭证ID
});

// 表单校验规则
const formRules = reactive({
  transDate: [{ required: true, message: '冲销日期不能为空', trigger: 'change' }],
  description: [{ required: true, message: '冲销说明不能为空', trigger: 'blur' }]
});

// 冲销分录列表
const entriesList = ref([]);
// 科目列表
const accountList = ref([]);

// 加载原凭证详情+生成冲销数据
const loadWriteOffData = async () => {
  try {
    const originalId = route.params.id;
    if (!originalId) {
      ElMessage.error('原凭证ID不能为空');
      router.push('/Eight');
      return;
    }

    // 加载原凭证详情
    const voucherRes = await axios.get(`/system/transactions/${originalId}`);
    const originalVoucher = voucherRes.data.data;
    originalForm.transNo = originalVoucher.transNo;
    originalForm.transDate = originalVoucher.transDate;
    originalForm.transType = originalVoucher.transType;
    originalForm.id = originalVoucher.id;
    form.writeOffTargetId = originalVoucher.id;

    // 生成冲销凭证编号
    const noRes = await axios.get('/system/transactions/generate-no');
    form.transNo = noRes.data.data;

    // 加载科目列表
    const accRes = await axios.get('/system/accounts/list');
    accountList.value = accRes.data.data || [];

    // 生成反向分录（核心逻辑）
    entriesList.value = originalVoucher.entriesList.map(entry => {
      const account = accountList.value.find(acc => acc.id === entry.accountId);
      return {
        accountId: entry.accountId,
        accountName: account ? account.name : '未知科目',
        originalEntryType: entry.entryType,
        entryType: entry.entryType === '借方' ? '贷方' : '借方', // 反向
        amount: entry.amount // 金额不变
      };
    });
  } catch (err) {
    ElMessage.error('加载冲销数据失败：' + (err.response?.data?.msg || err.message));
    router.push('/Eight');
  }
};

// 提交冲销凭证
const submitForm = async () => {
  try {
    await formRef.value.validate();

    // 构造参数
    const params = {
      transNo: form.transNo,
      transDate: form.transDate,
      transType: form.transType,
      description: form.description,
      writeOffTargetId: form.writeOffTargetId,
      entriesList: entriesList.value.map(item => ({
        accountId: item.accountId,
        entryType: item.entryType,
        amount: item.amount
      }))
    };

    // 调用新增接口（冲销凭证本质是新增一条特殊凭证）
    await axios.post('/system/transactions/add', params);
    ElMessage.success('冲销凭证生成成功');
    router.push('/voucher/list');
  } catch (err) {
    ElMessage.error('生成失败：' + (err.response?.data?.msg || err.message));
  }
};

// 初始化
onMounted(() => {
  loadWriteOffData();
});
</script>