<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox, ElTable, ElTableColumn, ElInput, ElButton, ElIcon, ElDialog, ElForm, ElFormItem, ElInputNumber, ElPagination } from 'element-plus';
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue';
import axios from 'axios';

const formRef = ref(null);
const customerList = ref([]);
const total = ref(0);
const searchKeyword = ref('');
const loading = ref(false);
const pageNum = ref(1);
const pageSize = ref(10);

const dialogVisible = ref(false);
const dialogTitle = ref('新增客户');
const isEdit = ref(false);
const form = ref({
  id: '',
  name: '',
  phone: '',
  address: '',
  balance: 0,
  createdAt: '',
  updatedAt: ''
});

const formRules = ref({
  name: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { max: 100, message: '客户名称不能超过100个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-\d{7,8}$/, message: '请输入正确的手机号或固话格式', trigger: 'blur' }
  ],
  balance: [

    {
      validator: (rule, value, callback) => {
        const num = Number(value);
        if (isNaN(num)) {
          callback(new Error('应收余额必须为数字'));
        } else if (num < 0) {
          callback(new Error('应收余额不能为负数'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});


const fetchCustomers = async () => {
  try {
    loading.value = true;
    console.log('开始查询客户，请求参数：', {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: searchKeyword.value
    });
    const res = await axios.get('/system/customers/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        name: searchKeyword.value,
        t: Date.now()
      }
    });

    if (res.data && Array.isArray(res.data.records)) {
      customerList.value = [...res.data.records]; // 生成新数组引用
      total.value = res.data.total || 0;
      console.log('查询/刷新成功，共', total.value, '条数据', '数据内容：', customerList.value);
    } else {
      ElMessage.error('查询失败：后端返回数据格式异常');
      customerList.value = []; // 异常时清空表格
      total.value = 0;
    }
  } catch (error) {
    console.error('客户查询接口失败：', error);
    console.error('错误详情：', error.config.url, error.response?.status, error.response?.data);
    ElMessage.error(`请求失败：${error.message || '跨域未配置或后端端口错误'}`);
    customerList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const addCustomer = async () => {
  try {
    console.log('开始新增客户，表单数据：', form.value);
    await formRef.value.validate();

    const submitData = {
      ...form.value,
      balance: form.value.balance.toString()
    };
    const res = await axios.post('/system/customers', submitData);

    if (res.data) {
      ElMessage.success('新增客户成功！');
      dialogVisible.value = false;
      fetchCustomers();
      resetForm();
    } else {
      ElMessage.error('新增失败：后端处理失败');
    }
  } catch (error) {
    console.error('新增客户接口失败：', error);
    ElMessage.error(`新增失败：${error.message || '表单校验不通过'}`);
  }
};

const editCustomer = async () => {
  try {
    console.log('开始修改客户，表单数据：', form.value);
    await formRef.value.validate(); // 表单校验

    const submitData = {
      ...form.value,
      balance: form.value.balance.toString()
    };
    const res = await axios.put('/system/customers', submitData);

    if (res.data) {
      ElMessage.success('修改客户成功！');
      dialogVisible.value = false;
      fetchCustomers();
      resetForm();
    } else {
      ElMessage.error('修改失败：后端处理失败');
    }
  } catch (error) {
    console.error('修改客户接口失败：', error);
    ElMessage.error(`修改失败：${error.message || '表单校验不通过'}`);
  }
};

const deleteCustomer = async (id) => {
  try {
    await ElMessageBox.confirm(
        "确定要删除该客户吗？删除后数据不可恢复！",
        "警告",
        { confirmButtonText: "确定", cancelButtonText: "取消", type: "danger" }
    );

    console.log('开始删除客户，ID：', id);
    const res = await axios.delete(`/system/customers/${id}`);
    if (res.data) {
      ElMessage.success('删除客户成功！');
      fetchCustomers();
    } else {
      ElMessage.error('删除失败：后端处理失败');
    }
  } catch (err) {
    if (err.name !== 'Error') {
      ElMessage.info("已取消删除");
    } else {
      console.error('删除客户接口失败：', err);
      ElMessage.error(`删除失败：${err.message || '未知错误'}`);
    }
  }
};

onMounted(() => {
  fetchCustomers();
});

const resetForm = () => {
  form.value = {
    id: '',
    name: '',
    phone: '',
    address: '',
    balance: 0,
    createdAt: '',
    updatedAt: ''
  };
  formRef.value?.resetFields();
  isEdit.value = false;
};

const openAddDialog = () => {
  console.log('触发新增客户弹窗');
  dialogTitle.value = '新增客户';
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const openEditDialog = (row) => {
  console.log('触发编辑客户弹窗，数据回显：', row);
  dialogTitle.value = '编辑客户';
  isEdit.value = true;

  form.value = {
    ...row,
    balance: Number(row.balance) || 0
  };
  dialogVisible.value = true;
};

const searchCustomer = () => {
  console.log('触发搜索客户，关键词：', searchKeyword.value);
  pageNum.value = 1;
  fetchCustomers();
};

const handleSizeChange = (val) => {
  console.log('切换每页条数：', val);
  pageSize.value = val;
  fetchCustomers();
};

const handleCurrentChange = (val) => {
  console.log('切换当前页：', val);
  pageNum.value = val;
  fetchCustomers();
};

const handleRefresh = () => {
  console.log('刷新按钮已点击，开始重置并重新查询');
  pageNum.value = 1;
  searchKeyword.value = '';
  fetchCustomers();
};
</script>

<template>
  <div class="customer-manage">
    <!-- 页面头部：标题+操作栏 -->
    <div class="page-header">
      <h2>客户管理</h2>
      <div class="operate-bar">
        <ElInput
            v-model="searchKeyword"
            placeholder="搜索客户名称"
            @keyup.enter="searchCustomer"
            style="width: 300px; margin-right: 12px;"
        />
        <!-- 刷新按钮绑定专用函数 handleRefresh -->
        <ElButton type="default" :icon="Refresh" @click="handleRefresh">刷新</ElButton>
        <ElButton type="primary" :icon="Plus" @click="openAddDialog" style="margin-left: 10px;">新增客户</ElButton>
      </div>
    </div>

    <ElTable
        :data="customerList"
        border
        :loading="loading"
        row-key="id"
        style="width: 100%; margin-bottom: 16px;"
    >
      <ElTableColumn label="ID" prop="id" width="80" align="center"/>
      <ElTableColumn label="客户名称" prop="name" width="220"/>
      <ElTableColumn label="电话" prop="phone" width="150" align="center"/>
      <ElTableColumn label="地址" prop="address"/>
      <ElTableColumn label="应收余额（元）" prop="balance" width="160" align="center">
        <template #default="scope">{{ Number(scope.row.balance).toFixed(2) }}</template>
      </ElTableColumn>
      <ElTableColumn label="创建时间" prop="createdAt" width="190" align="center"/>
      <ElTableColumn label="操作" width="180" align="center">
        <template #default="scope">
          <ElButton type="text" color="#409eff" @click="openEditDialog(scope.row)">编辑</ElButton>
          <ElButton type="text" color="#f56c6c" @click="deleteCustomer(scope.row.id)">删除</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <!-- 分页组件 -->
    <ElPagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="text-align: right;"
    />

    <ElDialog :title="dialogTitle" v-model="dialogVisible" width="620px" destroy-on-close>
      <ElForm :model="form" :rules="formRules" ref="formRef" label-width="120px" size="default">
        <ElFormItem label="客户名称" prop="name">
          <ElInput v-model="form.name" max-length="100" placeholder="请输入客户名称"/>
        </ElFormItem>
        <ElFormItem label="联系电话" prop="phone">
          <ElInput v-model="form.phone" max-length="20" placeholder="请输入手机号或固话（如：010-12345678）"/>
        </ElFormItem>
        <ElFormItem label="联系地址" prop="address">
          <ElInput v-model="form.address" max-length="200" placeholder="请输入客户地址"/>
        </ElFormItem>
        <ElFormItem label="应收余额" prop="balance">
          <ElInputNumber
              v-model.number="form.balance"
              :min="0"
              :precision="2"
              :step="0.01"
              placeholder="请输入应收余额"
              style="width: 100%;"
              @input="(val) => form.balance = Number(val) || 0"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="isEdit ? editCustomer() : addCustomer()">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.customer-manage {
  padding: 20px;
  background-color: #fff;
  height: 100%;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.page-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.operate-bar {
  display: flex;
  align-items: center;
}

.operate-bar .el-button {
  pointer-events: auto !important;
  opacity: 1 !important;
}

:deep(.el-table__row:hover) {
  background-color: #fafafa !important;
}

:deep(.el-pagination) {
  margin-top: 16px;
}
</style>