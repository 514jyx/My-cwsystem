<template>
  <div style="padding: 20px; background: #f5f7fa;">
    <ElCard shadow="hover">
      <h2 style="margin: 0 0 20px 0; color: #1989fa; font-size: 18px;">新增凭证</h2>

      <!-- 凭证基本信息 -->
      <div style="margin-bottom: 20px;">
        <ElForm :model="voucherForm" label-width="100px" :rules="formRules" ref="formRef">
          <ElFormItem label="交易日期" prop="transDate">
            <ElDatePicker
                v-model="voucherForm.transDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 200px;"
                required
            />
          </ElFormItem>

          <ElFormItem label="交易类型" prop="transType">
            <ElSelect v-model="voucherForm.transType" style="width: 200px;" required>
              <ElOption label="采购" value="采购" />
              <ElOption label="销售" value="销售" />
              <ElOption label="收款" value="收款" />
              <ElOption label="付款" value="付款" />
              <ElOption label="费用" value="费用" />
              <ElOption label="收入" value="收入" />
            </ElSelect>
          </ElFormItem>

          <ElFormItem label="交易说明" prop="description">
            <ElInput v-model="voucherForm.description" placeholder="请输入交易说明" style="width: 400px;" />
          </ElFormItem>
        </ElForm>
      </div>

      <!-- 分录列表（下拉选择科目，显示代码+名称） -->
      <div style="margin-bottom: 20px;">
        <h3 style="font-size: 16px; margin-bottom: 10px;">分录信息（至少一借一贷）</h3>
        <div v-for="(entry, index) in entryList" :key="index" style="display: flex; align-items: center; gap: 10px; margin: 10px 0;">
          <!-- 科目选择：下拉框（显示代码+名称） -->
          <ElFormItem :prop="`entryList[${index}].accountId`" :rules="[{ required: true, message: '请选择科目', trigger: 'change' }]">
            <ElSelect
                v-model="entry.accountId"
                placeholder="选择科目（代码-名称）"
                style="width: 250px;"
            >
              <ElOption
                  v-for="account in accountList"
                  :key="account.id"
                  :label="`${account.code} - ${account.name}`"
                  :value="account.id"
              />
            </ElSelect>
          </ElFormItem>

          <!-- 借贷方向 -->
          <ElFormItem :prop="`entryList[${index}].entryType`" :rules="[{ required: true, message: '请选择方向', trigger: 'change' }]">
            <ElSelect v-model="entry.entryType" style="width: 100px;">
              <ElOption label="借方" value="借方" />
              <ElOption label="贷方" value="贷方" />
            </ElSelect>
          </ElFormItem>

          <!-- 金额 -->
          <ElFormItem :prop="`entryList[${index}].amount`" :rules="[{ required: true, message: '请输入金额', trigger: 'blur' }, { type: 'number', min: 0.01, message: '金额必须大于0' }]">
            <ElInput
                v-model.number="entry.amount"
                type="number"
                step="0.01"
                placeholder="金额"
                style="width: 150px;"
            />
          </ElFormItem>

          <!-- 删除分录（至少保留2条） -->
          <ElButton
              type="text"
              style="color: #f56c6c;"
              @click="removeEntry(index)"
              :disabled="entryList.length <= 2"
          >
            <ElIcon><Delete /></ElIcon> 删除
          </ElButton>
        </div>

        <!-- 添加分录按钮 -->
        <ElButton type="text" @click="addEntry" style="color: #1989fa;">
          <ElIcon><Plus /></ElIcon> 添加分录
        </ElButton>
      </div>

      <!-- 提交/取消 -->
      <div style="display: flex; gap: 10px;">
        <ElButton type="primary" @click="submitForm">提交凭证</ElButton>
        <ElButton @click="goBack">取消</ElButton>
      </div>
    </ElCard>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage, ElForm, ElFormItem, ElSelect, ElOption, ElInput, ElButton, ElCard, ElDatePicker, ElIcon } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';

const router = useRouter();
const formRef = ref(null);

// 凭证基本信息
const voucherForm = reactive({
  transDate: new Date().toISOString().split('T')[0], // 默认当前日期
  transType: '',
  description: ''
});

// 分录列表（存储科目ID，用于后端关联）
const entryList = ref([
  { accountId: '', entryType: '借方', amount: '' },
  { accountId: '', entryType: '贷方', amount: '' }
]);

// 科目列表（从后端加载，含id、code、name）
const accountList = ref([]);

// 表单校验规则
const formRules = reactive({
  transDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }],
  transType: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入交易说明', trigger: 'blur' }]
});

// 加载科目列表（从后端获取所有科目，含1001这种代码）
const loadAccounts = async () => {
  try {
    const res = await axios.get('/system/accounts/list');
    accountList.value = res.data.data || [];
    console.log("加载科目列表成功：", accountList.value);
  } catch (err) {
    console.error("加载科目失败：", err);
    ElMessage.error('加载科目失败：' + err.message);
  }
};

// （可选）如果后端返回的是科目树，需转平级列表（复制粘贴即可）
const flattenAccountTree = (treeList) => {
  let flatList = [];
  const traverse = (list) => {
    list.forEach(item => {
      flatList.push(item); // 取当前节点
      if (item.children && item.children.length > 0) {
        traverse(item.children); // 递归子节点
      }
    });
  };
  traverse(treeList);
  return flatList;
};

// 添加分录
const addEntry = () => {
  entryList.value.push({ accountId: '', entryType: '借方', amount: '' });
};

// 删除分录
const removeEntry = (index) => {
  entryList.value.splice(index, 1);
};

// 提交凭证（完整修复版：加日志+错误捕获+编号校验）
const submitForm = async () => {
  try {
    // 1. 表单校验
    await formRef.value.validate();
    console.log("表单校验通过");

    // 2. 校验借贷平衡
    const debitTotal = entryList.value
        .filter(item => item.entryType === '借方')
        .reduce((sum, item) => sum + (item.amount || 0), 0);
    const creditTotal = entryList.value
        .filter(item => item.entryType === '贷方')
        .reduce((sum, item) => sum + (item.amount || 0), 0);

    console.log(`借贷平衡校验：借方=${debitTotal}，贷方=${creditTotal}`);
    if (Math.abs(debitTotal - creditTotal) > 0.01) {
      ElMessage.error(`借贷不平衡：借方合计${debitTotal}元，贷方合计${creditTotal}元`);
      return;
    }

    // 3. 生成凭证编号（核心修复：详细日志+异常捕获）
    console.log("=== 开始调用生成编号接口 ===");
    let transNo = '';
    try {
      const noRes = await axios.get('/system/transactions/generate-no');
      console.log("生成编号接口返回完整响应：", noRes);
      console.log("响应体数据：", noRes.data);

      // 校验接口返回格式和编号
      if (noRes.status !== 200) {
        throw new Error(`接口调用失败，状态码：${noRes.status}`);
      }
      if (noRes.data.code !== 200) {
        throw new Error(`后端返回失败：${noRes.data.msg || '未知错误'}`);
      }
      transNo = noRes.data.data;
      if (!transNo) {
        throw new Error(`后端返回无效编号：${transNo}`);
      }
      console.log("成功获取交易编号：", transNo);
    } catch (noErr) {
      console.error("生成编号失败详情：", noErr);
      ElMessage.error(`生成编号失败：${noErr.message}`);
      return; // 编号失败，终止提交
    }

    // 4. 构造提交参数（确保和后端 DTO 完全匹配）
    const submitParams = {
      transNo: transNo,
      transDate: voucherForm.transDate,
      transType: voucherForm.transType,
      description: voucherForm.description,
      status: '草稿',
      entriesList: entryList.value.map(item => ({
        accountId: item.accountId, // 仅传递后端需要的字段
        entryType: item.entryType,
        amount: item.amount,
        description: voucherForm.description
      }))
    };
    console.log("提交给后端的参数：", submitParams);

    // 5. 提交凭证
    const submitRes = await axios.post('/system/transactions/add', submitParams);
    console.log("提交接口返回：", submitRes.data);

    if (submitRes.data.code === 200) {
      ElMessage.success('凭证新增成功！');
      router.push('/voucher/list');
    } else {
      ElMessage.error(`提交失败：${submitRes.data.msg}`);
    }
  } catch (err) {
    console.error("提交凭证总异常：", err);
    // 处理网络错误、接口404/500等
    const errMsg = err.response
        ? `[${err.response.status}] ${err.response.data.msg || '接口错误'}`
        : err.message || '未知错误';
    ElMessage.error(`提交失败：${errMsg}`);
  }
};

// 返回
const goBack = () => router.push('/voucher/list');

// 初始化加载科目列表
onMounted(() => {
  loadAccounts();
});
</script>