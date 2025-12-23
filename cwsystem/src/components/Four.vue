<script setup>
import { ref, computed, onMounted } from 'vue';
import {
  ElTree, ElButton, ElInput, ElDialog, ElForm, ElFormItem,
  ElSelect, ElOption, ElMessage, ElPopconfirm, ElIcon, ElEmpty
} from 'element-plus';

// 图标组件
import { Plus, Edit, Delete, Folder, Document, Refresh } from '@element-plus/icons-vue';

import axios from 'axios';

// ====== API 配置 ======
const endpoints = {
  tree: '/system/accounts/tree',
  add: '/system/accounts/add',
  update: '/system/accounts/update',
  delete: '/system/accounts/delete/',
  check: '/system/accounts/check-code'
};

axios.defaults.baseURL = '/';
axios.defaults.timeout = 5000;

// ====== 状态 ======
const accounts = ref([]);
const searchKeyword = ref('');
const dialogVisible = ref(false);
const isEditMode = ref(false);
const isLoading = ref(false); // 操作loading状态
const accountForm = ref({ id: '', code: '', name: '', type: '', parentId: null, balance: 0 });

const accountTypes = ['资产', '负债', '权益', '收入', '费用'].map(t => ({ label: t, value: t }));

// 默认框架节点（不可编辑/删除）
const framework = [
  { id: -1, code: '1000', name: '资产类', type: '资产' },
  { id: -2, code: '2000', name: '负债类', type: '负债' },
  { id: -3, code: '4000', name: '权益类', type: '权益' },
  { id: -4, code: '6000', name: '收入类', type: '收入' },
  { id: -5, code: '6000', name: '费用类', type: '费用' }
].map(item => ({ ...item, isFramework: true, children: [] }));

// ====== 工具函数 ======
const getTypeColor = (type) => ({
  资产: '#4299e1', 负债: '#9f7aea', 权益: '#38b2ac',
  收入: '#48bb78', 费用: '#ed8936'
}[type] || '#666');

const formatBalance = (v) => (v || 0).toFixed(2);

// 搜索关键词高亮
const highlightKeyword = (text) => {
  const keyword = searchKeyword.value.trim().toLowerCase();
  if (!keyword) return text;
  const reg = new RegExp(`(${keyword})`, 'gi');
  return text.replace(reg, '<span style="color: #4299e1; font-weight: bold;">$1</span>');
};

// 安全过滤树（无副作用）
const filterTree = (nodes, keyword) => {
  if (!keyword) return nodes.map(n => ({ ...n, children: n.children ? [...n.children] : [] }));
  const result = [];
  for (const node of nodes) {
    const match = node.name?.toLowerCase().includes(keyword) || node.code?.includes(keyword);
    const filteredChildren = node.children ? filterTree(node.children, keyword) : [];
    if (match || filteredChildren.length > 0) {
      result.push({ ...node, children: filteredChildren });
    }
  }
  return result;
};

const filteredAccounts = computed(() => {
  return filterTree(accounts.value, searchKeyword.value.trim().toLowerCase());
});

// 校验是否有子科目
const hasChildren = (node) => {
  return Array.isArray(node.children) && node.children.length > 0;
};

// ====== 数据加载 ======
const loadAccounts = async () => {
  try {
    isLoading.value = true;
    const res = await axios.get(endpoints.tree);
    const data = res.data?.code === 200 && Array.isArray(res.data.data) ? res.data.data : [];

    const groups = {};
    accountTypes.forEach(t => groups[t.value] = []);

    data.forEach(acc => {
      if (groups[acc.type]) {
        groups[acc.type].push({ ...acc, isFramework: false });
      }
    });

    accounts.value = framework.map(fw => ({
      ...fw,
      children: [...(groups[fw.type] || [])]
    }));
  } catch (err) {
    if (err.message.includes('Network Error')) {
      ElMessage.error('网络错误，请检查接口连接');
    } else {
      ElMessage.error('数据加载失败');
    }
    accounts.value = framework.map(fw => ({ ...fw, children: [] }));
  } finally {
    isLoading.value = false;
  }
};

// ====== 操作处理 ======
const handleAdd = (parentId = null, parentType = '', parentCode = '') => {
  accountForm.value = {
    id: '',
    code: parentCode ? `${parentCode}` : '', // 自动填充父级代码前缀
    name: '',
    type: parentType,
    parentId: parentId && parentId > 0 ? parentId : null,
    balance: 0
  };
  isEditMode.value = false;
  dialogVisible.value = true;
};

const handleEdit = (data) => {
  if (data.isFramework) return ElMessage.warning('分类节点不可编辑');
  accountForm.value = { ...data };
  isEditMode.value = true;
  dialogVisible.value = true;
};

const checkCodeUnique = async () => {
  const code = accountForm.value.code.trim();
  if (!code) return false;
  try {
    const res = await axios.get(endpoints.check, {
      params: { code, id: isEditMode.value ? accountForm.value.id : undefined }
    });
    return res.data?.code === 200 && res.data.data;
  } catch (err) {
    if (err.message.includes('Network Error')) {
      ElMessage.error('网络错误，校验失败');
    } else {
      ElMessage.error('代码唯一性校验失败');
    }
    return false;
  }
};

const submitForm = async () => {
  if (isLoading.value) return; // 防止重复提交

  const { code, name, type, balance } = accountForm.value;

  // 1. 基础必填校验
  if (!code.trim() || !name.trim() || !type) {
    return ElMessage.warning('请填写完整科目信息');
  }

  // 2. 代码格式校验（4-10位纯数字）
  const codeReg = /^\d{4,10}$/;
  if (!codeReg.test(code.trim())) {
    return ElMessage.warning('科目代码必须是4-10位纯数字');
  }

  // 3. 名称格式校验（中文、字母、数字、下划线，1-50字）
  const nameReg = /^[\u4e00-\u9fa5a-zA-Z0-9_]{1,50}$/;
  if (!nameReg.test(name.trim())) {
    return ElMessage.warning('科目名称只能包含中文、字母、数字和下划线（1-50字）');
  }

  // 4. 余额校验（资产/费用类不能为负）
  if (['资产', '费用'].includes(type) && balance < 0) {
    return ElMessage.warning(`${type}类科目余额不能为负数`);
  }

  // 5. 代码唯一性校验
  if (!(await checkCodeUnique())) {
    return ElMessage.warning('科目代码已存在，请更换');
  }

  try {
    isLoading.value = true;
    const url = isEditMode.value ? endpoints.update : endpoints.add;
    const method = isEditMode.value ? 'put' : 'post';
    const res = await axios[method](url, accountForm.value);

    if (res.data?.code === 200) {
      ElMessage.success(isEditMode.value ? '更新成功' : '新增成功');
      dialogVisible.value = false;
      loadAccounts();
    } else {
      throw new Error('操作失败');
    }
  } catch (err) {
    if (err.message.includes('Network Error')) {
      ElMessage.error('网络错误，请检查连接');
    } else {
      ElMessage.error(isEditMode.value ? '更新失败' : '新增失败');
    }
  } finally {
    isLoading.value = false;
  }
};

const confirmDelete = async (data) => {
  if (isLoading.value) return;
  if (data.isFramework) return ElMessage.warning('分类节点不可删除');
  if (hasChildren(data)) return ElMessage.warning('存在子科目，无法删除');

  try {
    isLoading.value = true;
    const res = await axios.delete(endpoints.delete + data.id);
    if (res.data?.code === 200) {
      ElMessage.success('删除成功');
      loadAccounts();
    } else {
      throw new Error('删除失败');
    }
  } catch (err) {
    if (err.message.includes('Network Error')) {
      ElMessage.error('网络错误，请检查连接');
    } else {
      ElMessage.error('删除失败');
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(loadAccounts);
</script>

<template>
  <div class="account-page">
    <!-- 头部 -->
    <div class="header">
      <h3>科目管理</h3>
      <div class="actions">
        <ElInput
            v-model="searchKeyword"
            placeholder="搜索名称或代码"
            style="width: 200px; margin-right: 12px;"
            clearable
            size="small"
            @keyup.enter="searchKeyword = searchKeyword.trim()"
        />
        <ElButton type="primary" size="small" @click="handleAdd()">
          <ElIcon style="margin-right: 4px;">
            <Plus />
          </ElIcon>
          新增顶级
        </ElButton>
        <ElButton size="small" @click="loadAccounts()" style="margin-left: 8px;" :loading="isLoading">
          <ElIcon style="margin-right: 4px;">
            <Refresh />
          </ElIcon>
          刷新
        </ElButton>
      </div>
    </div>

    <!-- 树容器 -->
    <div class="tree-container">
      <ElTree
          v-if="filteredAccounts.length > 0"
          :data="filteredAccounts"
          :props="{ label: 'name', children: 'children', key: 'id' }"
          :indent="40"
      node-key="id"
      default-expand-all
      :expand-on-click-node="false"
      :loading="isLoading"
      class="custom-indent-tree"
      >
      <template #default="{ node, data }">
        <!-- 按节点层级添加不同缩进（核心：子级手动增加左边距） -->
        <div class="custom-node" :style="{ 'padding-left': `${(node.level - 1) * 15}px` }">
          <!-- 左侧信息 -->
          <div class="node-info">
            <ElIcon :style="{ color: getTypeColor(data.type), marginRight: '10px', fontSize: node.level === 1 ? '18px' : '16px' }">
              <component :is="hasChildren(data) ? Folder : Document" />
            </ElIcon>
            <span class="name" :class="{'framework-name': data.isFramework}" v-html="highlightKeyword(data.name)"></span>
            <span class="code">[{{ data.code }}]</span>
            <span v-if="!data.isFramework" class="balance">¥{{ formatBalance(data.balance) }}</span>
          </div>

          <!-- 右侧操作按钮 -->
          <div class="node-actions">
            <ElButton
                type="text"
                size="small"
                @click.stop="handleAdd(data.id, data.type, data.code)"
                title="新增子科目"
                class="btn-add"
            >
              <ElIcon>
                <Plus />
              </ElIcon>
            </ElButton>

            <ElButton
                type="text"
                size="small"
                @click.stop="handleEdit(data)"
                :disabled="data.isFramework"
                title="编辑"
                class="btn-edit"
            >
              <ElIcon>
                <Edit />
              </ElIcon>
            </ElButton>

            <ElPopconfirm
                :title="`确定删除「${data.name}（${data.code}）」？`"
                @confirm="confirmDelete(data)"
                width="200"
                confirm-button-text="删除"
                cancel-button-text="取消"
            >
              <template #reference>
                <ElButton
                    type="text"
                    size="small"
                    :disabled="data.isFramework || hasChildren(data)"
                    title="删除"
                    class="btn-delete"
                >
                  <ElIcon>
                    <Delete />
                  </ElIcon>
                </ElButton>
              </template>
            </ElPopconfirm>
          </div>
        </div>
      </template>
      </ElTree>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <ElEmpty description="暂无匹配的科目数据" />
      </div>
    </div>

    <!-- 对话框 -->
    <ElDialog :title="isEditMode ? '编辑科目' : '新增科目'" v-model="dialogVisible" width="380px" :close-on-click-modal="false">
      <ElForm :model="accountForm" label-width="80px" size="small">
        <ElFormItem label="科目代码" required>
          <ElInput
              v-model="accountForm.code"
              maxlength="20"
              placeholder="4-10位纯数字"
              :disabled="isEditMode"
          />
        </ElFormItem>
        <ElFormItem label="科目名称" required>
          <ElInput
              v-model="accountForm.name"
              maxlength="50"
              placeholder="中文、字母、数字或下划线"
          />
        </ElFormItem>
        <ElFormItem label="科目类型" required>
          <ElSelect v-model="accountForm.type" disabled>
            <ElOption v-for="t in accountTypes" :key="t.value" :label="t.label" :value="t.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="初始余额">
          <ElInput
              v-model.number="accountForm.balance"
              type="number"
              step="0.01"
              :placeholder="['资产', '费用'].includes(accountForm.type) ? '请输入非负金额' : '可输入正负金额'"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitForm()" :loading="isLoading">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.account-page {
  padding: 16px;
  height: 100vh;
  background: #f8f9fa;
  overflow: auto;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header h3 {
  font-size: 18px;
  color: #2d3748;
  margin: 0;
}

.actions {
  display: flex;
  align-items: center;
}

.tree-container {
  background: white;
  border-radius: 6px;
  padding: 16px;
  min-height: calc(100vh - 88px);
  box-sizing: border-box;
}

/* 空状态样式 */
.empty-state {
  padding: 40px 0;
  text-align: center;
}

/* 关键：强化树形缩进，解除默认样式干扰 */
:deep(.custom-indent-tree .el-tree-node__content) {
  height: auto !important;
  overflow: visible !important;
  padding: 4px 0 !important;
}

/* 修复Element Tree默认缩进的连接线位置 */
:deep(.custom-indent-tree .el-tree-node__indent) {
  width: 40px !important; /* 与ElTree的indent属性一致，确保连接线对齐 */
}

:deep(.el-tree-node__label) {
  width: 100% !important;
  display: block !important;
}

.custom-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  min-height: 40px;
  padding: 8px 0;
  box-sizing: border-box;
}

.node-info {
  display: flex;
  align-items: center;
  flex: 1;
  overflow: hidden;
}

/* 名称样式：层级区分+子级淡化 */
.name {
  margin-right: 8px;
  white-space: nowrap;
  font-size: 15px;
  color: #4a5568;
}

/* 顶级框架节点（1级） */
.framework-name {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

/* 二级科目（子级） */
:deep(.el-tree-node.level-2 .name) {
  font-size: 14px;
  color: #718096; /* 颜色稍淡，区分顶级 */
}

/* 三级科目（孙子级） */
:deep(.el-tree-node.level-3 .name) {
  font-size: 14px;
  color: #a0aec0; /* 颜色更淡 */
  font-style: italic; /* 斜体，进一步区分 */
}

/* 代码样式 */
.code {
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  margin-right: 10px;
  white-space: nowrap;
}

/* 余额样式 */
.balance {
  color: #4299e1;
  font-size: 14px;
  white-space: nowrap;
  font-weight: 500;
}

/* 按钮容器 */
.node-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  padding-left: 8px;
}

/* 按钮样式：颜色区分+hover效果 */
:deep(.btn-add.el-button--text) {
  padding: 6px !important;
  font-size: 17px !important;
  color: #48bb78 !important;
}
:deep(.btn-add.el-button--text:hover) {
  color: #38a169 !important;
  background-color: rgba(72, 187, 120, 0.1) !important;
  border-radius: 4px;
}

:deep(.btn-edit.el-button--text) {
  padding: 6px !important;
  font-size: 17px !important;
  color: #4299e1 !important;
}
:deep(.btn-edit.el-button--text:hover) {
  color: #3182ce !important;
  background-color: rgba(66, 153, 225, 0.1) !important;
  border-radius: 4px;
}

:deep(.btn-delete.el-button--text) {
  padding: 6px !important;
  font-size: 17px !important;
  color: #f56c6c !important;
}
:deep(.btn-delete.el-button--text:hover) {
  color: #e53e3e !important;
  background-color: rgba(245, 108, 108, 0.1) !important;
  border-radius: 4px;
}

/* 禁用按钮样式 */
:deep(.el-button--text.is-disabled) {
  color: #c0c4cc !important;
  cursor: not-allowed;
  background: transparent !important;
}

/* 树形连接线样式优化（更清晰） */
:deep(.el-tree-node::before) {
  border-left: 1px solid #ccc !important; /* 虚线改实线，更明显 */
  height: 100% !important;
}

:deep(.el-tree-node::after) {
  border-top: 1px solid #ccc !important; /* 虚线改实线 */
}

/* 加载状态样式 */
:deep(.el-tree__empty-text) {
  color: #999;
}
</style>