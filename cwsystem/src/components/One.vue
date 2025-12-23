<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import { ElMessage, ElMessageBox, ElTable, ElTableColumn, ElInput, ElButton, ElIcon, ElDialog, ElForm, ElFormItem, ElInputNumber, ElPagination } from 'element-plus';
import { Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue';

// ✅ 关键修复：不再 import axios，改用全局配置的 $axios
const { proxy } = getCurrentInstance();
const $axios = proxy.$axios;

// 表格数据和分页配置
const formRef = ref(null);
const supplierList = ref([]); // 供应商数据列表
const total = ref(0); // 总条数
const searchKeyword = ref(''); // 搜索关键词
const loading = ref(false); // 加载状态
const pageNum = ref(1); // 当前页（默认第1页）
const pageSize = ref(10); // 每页条数（默认10条）

// 弹窗相关配置
const dialogVisible = ref(false);
const dialogTitle = ref('新增供应商');
const isEdit = ref(false);
const form = ref({
  id: '', // 供应商ID（编辑时必填）
  name: '', // 供应商名称（必填）
  contactPerson: '', // 联系人（和后端字段一致：contactPerson）
  phone: '', // 电话
  address: '', // 地址
  balance: 0, // 应付余额（初始值为数字0）
  createdAt: '', // 创建时间（后端自动生成，前端只读）
  updatedAt: '' // 更新时间（后端自动生成，前端只读）
});

// 表单校验规则（核心修改：balance 用自定义校验彻底解决类型问题）
const formRules = ref({
  name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { max: 100, message: '供应商名称不能超过100个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-\d{7,8}$/, message: '请输入正确的手机号或固话格式', trigger: 'blur' }
  ],
  balance: [
    {
      validator: (rule, value, callback) => {
        console.log('应付余额校验：', value, '类型：', typeof value);
        const num = Number(value);
        if (isNaN(num)) {
          callback(new Error('应付余额必须为数字'));
        } else if (num < 0) {
          callback(new Error('应付余额不能为负数'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});

// -------------- 核心接口请求函数（✅ 全部使用相对路径 + $axios）--------------
const fetchSuppliers = async () => {
  try {
    loading.value = true;
    console.log('开始查询供应商，请求参数：', {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: searchKeyword.value
    });

    // ✅ 修复：使用相对路径，自动拼接 VITE_API_BASE_URL
    const res = await $axios.get('/system/suppliers/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        name: searchKeyword.value
      }
    });

    if (res.data) {
      supplierList.value = res.data.data || [];
      total.value = res.data.total || 0;
      console.log('查询成功，共', total.value, '条数据');
    } else {
      ElMessage.error('查询失败：后端返回数据格式异常');
    }
  } catch (error) {
    console.error('供应商查询接口失败：', error);
    ElMessage.error(`请求失败：${error.message || '未知错误'}`);
  } finally {
    loading.value = false;
  }
};

const addSupplier = async () => {
  try {
    console.log('开始新增供应商，表单数据：', form.value);
    await formRef.value.validate();

    // ✅ 修复：使用相对路径
    const res = await $axios.post('/system/suppliers', form.value);

    if (res.data) {
      ElMessage.success('新增供应商成功！');
      dialogVisible.value = false;
      fetchSuppliers();
      resetForm();
    } else {
      ElMessage.error('新增失败：后端处理失败');
    }
  } catch (error) {
    console.error('新增供应商接口失败：', error);
    ElMessage.error(`新增失败：${error.message || '表单校验不通过'}`);
  }
};

const editSupplier = async () => {
  try {
    console.log('开始修改供应商，表单数据：', form.value);
    await formRef.value.validate();

    // ✅ 修复：使用相对路径
    const res = await $axios.put('/system/suppliers', form.value);

    if (res.data) {
      ElMessage.success('修改供应商成功！');
      dialogVisible.value = false;
      fetchSuppliers();
      resetForm();
    } else {
      ElMessage.error('修改失败：后端处理失败');
    }
  } catch (error) {
    console.error('修改供应商接口失败：', error);
    ElMessage.error(`修改失败：${error.message || '表单校验不通过'}`);
  }
};

const deleteSupplier = async (id) => {
  try {
    await ElMessageBox.confirm(
        "确定要删除该供应商吗？删除后数据不可恢复！",
        "警告",
        { confirmButtonText: "确定", cancelButtonText: "取消", type: "danger" }
    );

    console.log('开始删除供应商，ID：', id);

    // ✅ 修复：使用相对路径
    const res = await $axios.delete(`/system/suppliers/${id}`);

    if (res.data) {
      ElMessage.success('删除供应商成功！');
      fetchSuppliers();
    } else {
      ElMessage.error('删除失败：后端处理失败');
    }
  } catch (err) {
    if (err.name !== 'Error') {
      ElMessage.info("已取消删除");
    } else {
      console.error('删除供应商接口失败：', err);
      ElMessage.error(`删除失败：${err.message || '未知错误'}`);
    }
  }
};

// -------------- 辅助函数 --------------
onMounted(() => {
  fetchSuppliers();
});

const resetForm = () => {
  form.value = {
    id: '',
    name: '',
    contactPerson: '',
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
  console.log('触发新增供应商弹窗，dialogVisible 设置为 true');
  dialogTitle.value = '新增供应商';
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const openEditDialog = (row) => {
  console.log('触发编辑供应商弹窗，原始数据：', row);
  dialogTitle.value = '编辑供应商';
  isEdit.value = true;
  form.value = {
    ...row,
    balance: Number(row.balance) || 0
  };
  dialogVisible.value = true;
};

const searchSupplier = () => {
  console.log('触发搜索供应商，关键词：', searchKeyword.value);
  pageNum.value = 1;
  fetchSuppliers();
};

const handleSizeChange = (val) => {
  console.log('切换每页条数：', val);
  pageSize.value = val;
  fetchSuppliers();
};

const handleCurrentChange = (val) => {
  console.log('切换当前页：', val);
  pageNum.value = val;
  fetchSuppliers();
};
</script>

<template>
  <div class="supplier-manage">
    <!-- 页面头部：标题+操作栏 -->
    <div class="page-header">
      <h2>供应商管理</h2>
      <div class="operate-bar">
        <ElInput
            v-model="searchKeyword"
            placeholder="搜索供应商名称"
            @keyup.enter="searchSupplier"
            style="width: 300px; margin-right: 12px;"
        />
        <ElButton type="default" :icon="Refresh" @click="fetchSuppliers">刷新</ElButton>
        <ElButton type="primary" :icon="Plus" @click="openAddDialog" style="margin-left: 10px;">新增供应商</ElButton>
      </div>
    </div>

    <!-- 供应商表格 -->
    <ElTable
        :data="supplierList"
        border
        :loading="loading"
        row-key="id"
        style="width: 100%; margin-bottom: 16px;"
    >
      <ElTableColumn label="ID" prop="id" width="80" align="center" />
      <ElTableColumn label="供应商名称" prop="name" width="220" />
      <ElTableColumn label="联系人" prop="contactPerson" width="130" align="center" />
      <ElTableColumn label="电话" prop="phone" width="150" align="center" />
      <ElTableColumn label="地址" prop="address" />
      <ElTableColumn label="应付余额（元）" prop="balance" width="160" align="center">
        <template #default="scope">{{ Number(scope.row.balance).toFixed(2) }}</template>
      </ElTableColumn>
      <ElTableColumn label="创建时间" prop="createdAt" width="190" align="center" />
      <ElTableColumn label="操作" width="180" align="center">
        <template #default="scope">
          <ElButton type="text" color="#409eff" @click="openEditDialog(scope.row)">编辑</ElButton>
          <ElButton type="text" color="#f56c6c" @click="deleteSupplier(scope.row.id)">删除</ElButton>
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

    <!-- 新增/编辑弹窗：v-model 绑定（Vue 3 适配） -->
    <ElDialog :title="dialogTitle" v-model="dialogVisible" width="620px" destroy-on-close>
      <ElForm :model="form" :rules="formRules" ref="formRef" label-width="120px" size="default">
        <ElFormItem label="供应商名称" prop="name">
          <ElInput v-model="form.name" max-length="100" placeholder="请输入供应商名称" />
        </ElFormItem>
        <ElFormItem label="联系人" prop="contactPerson">
          <ElInput v-model="form.contactPerson" max-length="50" placeholder="请输入联系人" />
        </ElFormItem>
        <ElFormItem label="联系电话" prop="phone">
          <ElInput v-model="form.phone" max-length="20" placeholder="请输入手机号或固话（如：010-12345678）" />
        </ElFormItem>
        <ElFormItem label="联系地址" prop="address">
          <ElInput v-model="form.address" max-length="200" placeholder="请输入供应商地址" />
        </ElFormItem>
        <!-- 应付余额输入框：核心修改：v-model.number + @input 强制同步数字 -->
        <ElFormItem label="应付余额" prop="balance">
          <ElInputNumber
              v-model.number="form.balance"
              :min="0"
              :precision="2"
              :step="0.01"
              placeholder="请输入应付余额"
              style="width: 100%;"
              @input="(val) => { form.balance = Number(val) || 0; console.log('输入后 balance：', form.balance, '类型：', typeof form.balance); }"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="isEdit ? editSupplier() : addSupplier()">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.supplier-manage {
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

/* 表格hover样式优化 */
:deep(.el-table__row:hover) {
  background-color: #fafafa !important;
}

/* 分页组件样式 */
:deep(.el-pagination) {
  margin-top: 16px;
}
</style>