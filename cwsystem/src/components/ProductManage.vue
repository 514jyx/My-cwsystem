<template>
  <div class="product-manage">
    <h2 style="text-align: center; margin: 20px 0;">产品管理</h2>

    <!-- 新增按钮+查询表单 -->
    <div style="margin: 0 20px 20px; display: flex; justify-content: space-between; align-items: center;">
      <el-button type="primary" @click="openAddDialog">新增商品</el-button>

      <el-form :model="searchForm" inline style="display: flex; gap: 10px;">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称" clearable style="width: 180px;"></el-input>
        </el-form-item>
        <el-form-item label="商品分类">
          <el-input v-model="searchForm.category" placeholder="请输入分类" clearable style="width: 180px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getProductList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格：显示商品ID，隐藏计量单位、成本价 -->
    <el-table :data="tableData" border style="width: 95%; margin: 0 auto;" :loading="loading">
      <el-table-column label="商品ID" prop="id" align="center" width="100"></el-table-column> <!-- 新增商品ID -->
      <el-table-column label="商品名称" prop="name" align="center"></el-table-column>
      <el-table-column label="商品分类" prop="category" align="center"></el-table-column>
      <el-table-column label="销售价" prop="salePrice" align="center" :formatter="formatMoney"></el-table-column>
      <el-table-column label="库存数量" prop="stock" align="center"></el-table-column>
      <el-table-column label="创建日期" prop="createdAt" align="center"></el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button type="text" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button type="text" text-color="#ff4d4f" @click="deleteProduct(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin: 20px auto; text-align: center; display: block;"
    ></el-pagination>

    <!-- 弹窗：隐藏计量单位、成本价，商品ID仅展示（不可编辑） -->
    <el-dialog
        :title="isEdit ? '编辑商品' : '新增商品'"
        v-model="dialogVisible"
        width="500px"
        append-to-body
        z-index="9999"
    >
      <el-form :model="formData" label-width="90px">
        <!-- 商品ID：新增，不可编辑（后端自动生成） -->
        <el-form-item label="商品ID">
          <el-input
              v-model="formData.id"
              placeholder="自动生成"
              disabled
              style="background-color: #f5f5f5;"
          ></el-input>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="formData.name" placeholder="请输入商品名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="商品分类">
          <el-input v-model="formData.category" placeholder="请输入商品分类" clearable></el-input>
        </el-form-item>
        <!-- 隐藏计量单位、成本价字段 -->
        <el-form-item label="销售价">
          <el-input v-model="formData.salePrice" type="number" placeholder="请输入销售价" clearable></el-input>
        </el-form-item>
        <el-form-item label="库存数量">
          <el-input v-model="formData.stock" type="number" placeholder="请输入库存数量" clearable></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// ✅ 关键修复：使用全局 $axios，不再 import axios
const { proxy } = getCurrentInstance();
const $axios = proxy.$axios;

// 页面核心状态
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

// 分页参数
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表格数据
const tableData = ref([])

// 查询表单
const searchForm = reactive({
  name: '',
  category: ''
})

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  category: '',
  salePrice: '',
  stock: ''
})

onMounted(() => {
  getProductList()
})

// ---------------------- 核心功能 ----------------------
const getProductList = async () => {
  loading.value = true
  try {
    // ✅ 使用相对路径 + $axios
    const res = await $axios.get('/system/products/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        name: searchForm.name || '',
        category: searchForm.category || ''
      }
    })
    tableData.value = res.data.data || []
    total.value = res.data.total || 0
  } catch (err) {
    ElMessage.error('查询商品失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  pageNum.value = 1
  getProductList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  getProductList()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  getProductList()
}

const submitForm = async () => {
  if (!formData.name) {
    ElMessage.warning('商品名称不能为空')
    return
  }
  if (!formData.salePrice || formData.salePrice <= 0) {
    ElMessage.warning('销售价必须大于0')
    return
  }
  if (formData.stock === '' || formData.stock < 0) {
    ElMessage.warning('库存数量不能为负数')
    return
  }

  try {
    let res
    if (isEdit.value) {
      // ✅ 相对路径
      res = await $axios.put('/system/products/update', formData)
    } else {
      const { id, ...submitData } = formData
      // ✅ 相对路径
      res = await $axios.post('/system/products/add', submitData)
    }
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      getProductList()
    } else {
      ElMessage.error(res.data.msg || (isEdit.value ? '编辑失败' : '新增失败'))
    }
  } catch (err) {
    ElMessage.error('网络异常，请重试')
  }
}

const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后不可恢复！', '警告', {
      type: 'error'
    })
    // ✅ 相对路径
    const res = await $axios.delete(`/system/products/delete/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      getProductList()
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  } catch (err) {
    if (err.name !== 'Error') {
      ElMessage.info('已取消删除')
    } else {
      ElMessage.error('删除操作失败')
    }
  }
}

// ---------------------- 弹窗逻辑 ----------------------
const openAddDialog = () => {
  isEdit.value = false
  formData.id = ''
  formData.name = ''
  formData.category = ''
  formData.salePrice = ''
  formData.stock = ''
  dialogVisible.value = true
}

const openEditDialog = async (row) => {
  isEdit.value = true
  loading.value = true
  try {
    // ✅ 相对路径
    const res = await $axios.get(`/system/products/get/${row.id}`)
    if (res.data.code === 200 && res.data.data) {
      const product = res.data.data
      formData.id = product.id
      formData.name = product.name
      formData.category = product.category
      formData.salePrice = product.salePrice
      formData.stock = product.stock
      dialogVisible.value = true
    } else {
      ElMessage.error('获取商品详情失败')
    }
  } catch (err) {
    ElMessage.error('获取商品详情异常')
  } finally {
    loading.value = false
  }
}

// ---------------------- 工具函数 ----------------------
const formatMoney = (row, column) => {
  const value = row[column.prop]
  return value ? Number(value).toFixed(2) : '0.00'
}
</script>
<style scoped>
.product-manage {
  padding: 15px;
  background-color: #fff;
  min-height: calc(100vh - 64px);
}

.el-form-item {
  margin-bottom: 15px;
}

/* 确保弹窗层级最高 */
:deep(.el-dialog__wrapper) {
  z-index: 9999 !important;
}

:deep(.el-dialog) {
  z-index: 10000 !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 禁用输入框样式优化 */
:deep(.el-input.is-disabled .el-input__inner) {
  background-color: #f5f5f5 !important;
  color: #666 !important;
}
</style>