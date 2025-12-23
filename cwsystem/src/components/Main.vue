<script setup>
import { ref, onMounted, reactive } from "vue";
import { getCurrentInstance } from "vue";
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElMessageBox } from "element-plus";
import { Search } from "@element-plus/icons-vue";

const { proxy } = getCurrentInstance();
const $axios = proxy.$axios;

// 姓名查询关键词
const name = ref("");

// 角色ID（-1=全部，0=超级管理员，1=管理员，2=普通用户）
const roleId = ref(-1);
const roleIds = ref([
  { label: "全部角色", value: -1 },
  { label: "超级管理员", value: 0 },
  { label: "管理员", value: 1 },
  { label: "普通用户", value: 2 },
]);

const tableData = ref([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);

const size = ref('default');
const disabled = ref(false);
const background = ref(true);

const centerDialogVisible = ref(false);
const addForm = reactive({
  no: "", // 账号
  name: "", // 姓名
  phone: "", // 电话
  roleId: 2,
  password: "" // 密码
});

// 编辑功能核心变量
const editDialogVisible = ref(false);
const editForm = reactive({
  id: "",
  no: "",
  name: "",
  phone: "",
  roleId: 2,
  password: ""
});
const editFormRef = ref(null);

// 账号唯一性校验（✅ 修复：移除 localhost）
const checkAccountExists = async (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入账号'));
  }
  try {
    const res = await $axios.post('/system/user/checkAccount', {
      no: value
    });
    if (res.data.code === 200) {
      callback(new Error('该账号已被使用，请更换'));
    } else {
      callback();
    }
  } catch (error) {
    callback(new Error('网络异常，无法验证账号'));
  }
};

const addFormRules = reactive({
  no: [
    { required: true, message: "请输入账号", trigger: "blur" },
    { validator: checkAccountExists, trigger: "blur" }
  ],
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [{ required: true, message: "请输入电话", trigger: "blur" }],
  roleId: [{ required: true, message: "请选择角色", trigger: "change" }],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 3, message: "密码长度不能少于3位", trigger: "blur" }
  ]
});

const editFormRules = reactive({
  no: [{ required: true, message: "请输入账号", trigger: "blur" }],
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  phone: [{ required: true, message: "请输入电话", trigger: "blur" }],
  roleId: [{ required: true, message: "请选择角色", trigger: "change" }]
});

const addFormRef = ref(null);

// 加载数据（✅ 修复：移除 localhost）
const loadPost = async () => {
  try {
    loading.value = true;
    const reqData = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      param: {
        name: name.value.trim(),
        roleId: roleId.value
      }
    };
    console.log("请求参数：", reqData);
    const res = await $axios.post('/system/user/listPage1', reqData);
    console.log('listPage1 响应：', res.data);

    if (res.data.code === 200) {
      tableData.value = res.data.data || [];
      total.value = res.data.dataTotal || res.data.total || 0;

      const roleItem = roleIds.value.find(item => item.value === roleId.value);
      const roleName = roleItem?.label || '';
      let tip = '';
      if (name.value && roleId.value !== -1) {
        tip = `查询到 ${total.value} 条「${roleName}」中姓名包含「${name.value}」的数据`;
      } else if (name.value) {
        tip = `查询到 ${total.value} 条姓名包含「${name.value}」的数据`;
      } else if (roleId.value !== -1) {
        tip = `查询到 ${total.value} 条「${roleName}」数据`;
      } else {
        tip = '数据加载成功';
      }
      ElMessage.success(res.data.msg || tip);
    } else {
      tableData.value = [];
      total.value = 0;
      ElMessage.error(res.data.msg || '数据加载失败');
    }
  } catch (error) {
    tableData.value = [];
    total.value = 0;
    ElMessage.error('网络异常，请检查后端服务');
    console.error('请求失败：', error.message);
  } finally {
    loading.value = false;
  }
};

const resetSearch = () => {
  name.value = "";
  roleId.value = -1;
  pageNum.value = 1;
  loadPost();
  ElMessage.info("已重置所有查询条件");
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  pageNum.value = 1;
  loadPost();
};

const handleCurrentChange = (val) => {
  pageNum.value = val;
  loadPost();
};

const add = () => {
  addForm.no = "";
  addForm.name = "";
  addForm.phone = "";
  addForm.roleId = 2;
  addForm.password = "";

  if (addFormRef.value) {
    addFormRef.value.clearValidate();
  }

  centerDialogVisible.value = true;
};

// 新增用户（✅ 修复：移除 localhost）
const submitAdd = async () => {
  if (!addFormRef.value) return;
  try {
    await addFormRef.value.validate();
  } catch (error) {
    ElMessage.error("请完善表单必填项！");
    return;
  }

  try {
    const res = await $axios.post('/system/user/add', addForm);
    if (res.data.code === 200) {
      ElMessage.success("新增用户成功！");
      centerDialogVisible.value = false;
      loadPost();
    } else {
      ElMessage.error(res.data.msg || "新增用户失败！");
    }
  } catch (error) {
    ElMessage.error("网络异常，新增失败！");
    console.error("新增失败：", error.message);
  }
};

const modify = async (row) => {
  if (editFormRef.value) {
    editFormRef.value.clearValidate();
  }
  Object.assign(editForm, { ...row });
  editDialogVisible.value = true;
};

// 编辑用户（✅ 修复：移除 localhost）
const submitEdit = async () => {
  if (!editFormRef.value) return;
  try {
    await editFormRef.value.validate();
  } catch (error) {
    ElMessage.error("请完善表单必填项！");
    return;
  }

  try {
    const res = await $axios.post('/system/user/modify', editForm);
    if (res.data.code === 200) {
      ElMessage.success("修改成功！");
      editDialogVisible.value = false;
      loadPost();
    } else {
      ElMessage.error(res.data.msg || "修改失败！");
    }
  } catch (error) {
    ElMessage.error("网络异常，修改失败！");
    console.error("修改失败：", error.message);
  }
};

// 删除用户（✅ 修复：移除 localhost）
const deleteUser = async (id) => {
  try {
    await ElMessageBox.confirm(
        "确定要删除这条数据吗？",
        "确认删除",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
    );

    const res = await $axios.get(`/system/user/delete?id=${id}`);
    if (res.data.code === 200) {
      ElMessage.success("删除成功！");
      loadPost();
    } else {
      ElMessage.error(res.data.msg || "删除失败！");
    }
  } catch (error) {
    if (error.name !== 'CanceledError') {
      ElMessage.error("删除失败：" + (error.message || '未知错误'));
    }
  }
};

onMounted(() => {
  loadPost();
});
</script>

<template>
  <div>
    <div style="margin-bottom: 10px; display: flex; align-items: center; gap: 10px;">
      <el-input
          v-model="name"
          placeholder="请输入姓名关键词"
          style="width: 200px"
          @keyup.enter="loadPost"
      >
        <template #suffix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select
          v-model="roleId"
          filterable
          placeholder="请选择角色"
          style="width: 180px"
      >
        <el-option
            v-for="item in roleIds"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>

      <el-button
          type="primary"
          @click="loadPost"
          :loading="loading">查询</el-button>

      <el-button
          type="success"
          @click="resetSearch">重置</el-button>
      <el-button
          type="success"
          @click="add">新增</el-button>
    </div>

    <el-scrollbar style="height: 500px; padding: 0;">
      <el-table
          :data="tableData"
          border
          stripe
          fit
          style="width: 100%;"
          :header-cell-style="{ background: '#f3f6fd', color: '#555'}"
          :empty-text="loading ? '加载中...' : (total === 0 ? (name.value || roleId.value !== -1 ? '未查询到相关数据' : '暂无数据') : '')"
      >
        <el-table-column prop="id" label="ID" width="100"/>
        <el-table-column prop="no" label="账号"/>
        <el-table-column prop="name" label="姓名"/>
        <el-table-column prop="phone" label="电话"/>
        <el-table-column prop="roleId" label="角色">
          <template #default="scope">
            <el-tag
                :type="scope.row.roleId === 0 ? 'danger' : (scope.row.roleId === 1 ? 'primary' : 'success')"
                disable-transitions
            >
              {{ scope.row.roleId === 0 ? '超级管理员' : (scope.row.roleId === 1 ? '管理员' : '普通用户') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="success" @click="modify(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteUser(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
          v-if="total > 0"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[2,5,10, 20, 50]"
          :size="size"
          :disabled="disabled || loading"
          :background="background"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          style="margin-top: 16px; text-align: right;"
      />
    </el-scrollbar>

    <!-- 新增用户弹窗 -->
    <el-dialog v-model="centerDialogVisible" title="新增用户" width="500px" center>
      <el-form
          ref="addFormRef"
          :model="addForm"
          :rules="addFormRules"
          label-width="80px"
          style="margin-top: 10px;"
      >
        <el-form-item label="账号" prop="no">
          <el-input v-model="addForm.no" placeholder="请输入用户账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入用户姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="addForm.password"
              placeholder="请输入密码"
              type="password"/>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="addForm.roleId" placeholder="请选择角色">
            <el-option label="超级管理员" :value="0" />
            <el-option label="管理员" :value="1" />
            <el-option label="普通用户" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="centerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdd">确认新增</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px" center>
      <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editFormRules"
          label-width="80px"
          style="margin-top: 10px;"
      >
        <el-form-item label="ID" disabled>
          <el-input v-model="editForm.id" disabled />
        </el-form-item>
        <el-form-item label="账号" prop="no">
          <el-input v-model="editForm.no" placeholder="请输入用户账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入用户姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="editForm.password"
              placeholder="不输入则不修改密码"
              type="password"/>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="editForm.roleId" placeholder="请选择角色">
            <el-option label="超级管理员" :value="0" />
            <el-option label="管理员" :value="1" />
            <el-option label="普通用户" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">确认修改</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
:deep(.el-select .el-input__wrapper) {
  border-radius: 4px;
}
:deep(.el-input__suffix) {
  color: var(--el-text-color-placeholder);
}
/* 弹窗表单样式优化 */
:deep(.el-form-item) {
  margin-bottom: 15px;
}
:deep(.el-dialog__body) {
  padding: 20px;
}
</style>