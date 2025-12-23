<template>
  <div class="order-list-container">
    <!-- 页面标题 + 新增按钮 -->
    <div class="page-header">
      <h2>销售单管理</h2>
      <el-button type="primary" @click="openAddDialog">新增销售单</el-button>
    </div>

    <!-- 查询条件表单 -->
    <el-card shadow="hover" class="search-form">
      <el-form :model="searchForm" :inline="true" label-width="100px">
        <el-form-item label="销售单编号">
          <el-input v-model="searchForm.orderNo" placeholder="输入销售单编号模糊查询" style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="客户">
          <el-select
              v-model="searchForm.customerId"
              placeholder="选择或搜索客户"
              style="width: 200px;"
              filterable
          >
            <el-option label="请选择客户" value=""></el-option>
            <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="销售单状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 150px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="草稿" value="草稿"></el-option>
            <el-option label="已确认" value="已确认"></el-option>
            <el-option label="已完成" value="已完成"></el-option>
            <el-option label="已取消" value="已取消"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="销售单日期">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyy-MM-dd" style="width: 300px;"></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchOrderList">查询</el-button>
          <el-button @click="resetSearchForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 销售单表格 -->
    <el-card shadow="hover" class="order-table">
      <el-table :data="orderList" border stripe :loading="loading" style="width: 100%;">
        <el-table-column label="销售单编号" prop="orderNo" align="center"></el-table-column>
        <el-table-column label="客户名称" prop="customerName" align="center"></el-table-column>
        <el-table-column label="销售单日期" prop="orderDate" align="center"></el-table-column>
        <el-table-column label="销售单金额" prop="totalAmount" align="center">
          <template #default="scope">{{ scope.row.totalAmount ? Number(scope.row.totalAmount).toFixed(2) : '0.00' }}</template>
        </el-table-column>
        <el-table-column label="已收金额" prop="paidAmount" align="center">
          <template #default="scope">{{ scope.row.paidAmount ? Number(scope.row.paidAmount).toFixed(2) : '0.00' }}</template>
        </el-table-column>
        <el-table-column label="销售单状态" prop="status" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.status || '未知状态' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="300px">
          <template #default="scope">
            <el-button type="text" @click="openDetailDialog(scope.row.id)">查看详情</el-button>

            <el-button type="text" color="#409eff" @click="openEditDialog(scope.row)" v-show="scope.row.status === '草稿'">编辑</el-button>
            <el-button type="text" color="#67c23a" @click="changeOrderStatus(scope.row.id, '已确认')" v-show="scope.row.status === '草稿'">确认</el-button>
            <el-button type="text" color="#909399" @click="deleteOrder(scope.row.id)" v-show="scope.row.status === '草稿'">删除</el-button>

            <el-button type="text" color="#e6a23c" @click="changeOrderStatus(scope.row.id, '已完成')" v-show="scope.row.status === '已确认'">完成</el-button>

            <el-button type="text" color="#f56c6c" @click="changeOrderStatus(scope.row.id, '已取消')" v-show="scope.row.status !== '已完成' && scope.row.status !== '已取消'">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="pageNum" :page-sizes="[10, 20, 50, 100]" :page-size="pageSize" :total="total" layout="total, sizes, prev, pager, next, jumper" class="pagination"></el-pagination>
    </el-card>

    <!-- 新增/编辑销售单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑销售单' : '新增销售单'" width="80%" :close-on-click-modal="false" style="z-index: 9999 !important;">
      <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-width="100px">
        <el-form-item label="销售单编号" prop="orderNo">
          <el-input v-model="orderForm.orderNo" placeholder="输入销售单编号（如：SALE20250101001）" style="width: 100%;"></el-input>
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select
              v-model="orderForm.customerId"
              placeholder="选择客户"
              style="width: 100%;"
              filterable
              @change="handleCustomerChange"
              required
          >
            <el-option label="请选择客户" value=""></el-option>
            <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id + ''"></el-option>
          </el-select>
          <div style="margin-top: 8px; font-size: 12px; color: #666;">
            选中客户：{{ orderForm.customerName || '未选择' }}
          </div>
        </el-form-item>
        <!-- 完全还原订单管理的日期选择器：不添加 value-format，绑定 Date 对象 -->
        <el-form-item label="销售单日期" prop="orderDate">
          <el-date-picker
              v-model="orderForm.orderDate"
              type="date"
              placeholder="选择销售单日期"
              style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="已收金额" prop="paidAmount">
          <el-input
              v-model="orderForm.paidAmount"
              type="number"
              placeholder="输入已收金额（空则默认0）"
              min="0"
              step="0.01"
              style="width: 100%;"
              clearable
              @input="handlePaidAmountInput"
          ></el-input>
        </el-form-item>
        <el-form-item label="销售项" prop="itemList">
          <el-table :data="orderForm.itemList" border style="width: 100%;">
            <el-table-column label="商品ID" align="center" width="120">
              <template #default="scope">
                <el-input v-model.number="scope.row.productId" type="number" placeholder="输入商品ID（≥1）" min="1" style="width: 80%;"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="商品名称" align="center" width="250">
              <template #default="scope">
                <el-input v-model="scope.row.productName" placeholder="输入商品名称（必填）" style="width: 100%;"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="数量" align="center" width="120">
              <template #default="scope">
                <el-input v-model.number="scope.row.quantity" type="number" placeholder="输入数量（≥1）" min="1" style="width: 80%;" @change="calcItemAmount(scope.row)"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="单价（元）" align="center" width="150">
              <template #default="scope">
                <el-input v-model.number="scope.row.unitPrice" type="number" placeholder="输入单价（≥0.01）" min="0.01" step="0.01" style="width: 80%;" @change="calcItemAmount(scope.row)"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="小计金额（元）" align="center" width="150">
              <template #default="scope">{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="80">
              <template #default="scope">
                <el-button type="text" color="#f56c6c" @click="removeOrderItem(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="text" @click="addOrderItem" style="margin-top: 10px;">新增销售项</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrderForm">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="销售单详情" width="80%" :close-on-click-modal="false" style="z-index: 9999 !important;">
      <div class="detail-content">
        <div class="detail-base-info">
          <div class="info-item">
            <span class="info-label">销售单编号：</span>
            <span class="info-value">{{ detailOrder.orderNo || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">客户名称：</span>
            <span class="info-value">{{ detailOrder.customerName || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">销售单日期：</span>
            <span class="info-value">{{ detailOrder.orderDate ? new Date(detailOrder.orderDate).toLocaleDateString() : '无' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">销售单状态：</span>
            <span class="info-value">
              <el-tag :type="getStatusTagType(detailOrder.status)">
                {{ detailOrder.status || '未知状态' }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">销售单金额（元）：</span>
            <span class="info-value">{{ detailOrder.totalAmount ? Number(detailOrder.totalAmount).toFixed(2) : '0.00' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">已收金额（元）：</span>
            <span class="info-value">{{ detailOrder.paidAmount ? Number(detailOrder.paidAmount).toFixed(2) : '0.00' }}</span>
          </div>
        </div>

        <h4 style="margin: 20px 0 10px; font-size: 15px; font-weight: 500;">销售项列表</h4>
        <el-table :data="detailItemList" border stripe style="width: 100%;">
          <el-table-column label="商品ID" align="center">
            <template #default="scope">{{ scope.row.productId || '无' }}</template>
          </el-table-column>
          <el-table-column label="商品名称" align="center">
            <template #default="scope">{{ scope.row.productName || '无' }}</template>
          </el-table-column>
          <el-table-column label="数量" align="center">
            <template #default="scope">{{ scope.row.quantity || 0 }}</template>
          </el-table-column>
          <el-table-column label="单价（元）" align="center">
            <template #default="scope">{{ scope.row.unitPrice ? Number(scope.row.unitPrice).toFixed(2) : '0.00' }}</template>
          </el-table-column>
          <el-table-column label="小计金额（元）" align="center">
            <template #default="scope">{{ scope.row.amount ? Number(scope.row.amount).toFixed(2) : '0.00' }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, unref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import axios from 'axios';

axios.defaults.withCredentials = true;

// 基础数据
const loading = ref(false);
const orderList = ref([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const customerList = ref([]);

// 查询表单
const searchForm = reactive({
  orderNo: '',
  customerId: '',
  status: '',
  dateRange: []
});

// 弹窗相关
const dialogVisible = ref(false);
const isEdit = ref(false);
const orderFormRef = ref(null);
const detailDialogVisible = ref(false);
const detailOrder = ref({});
const detailItemList = ref([]);

// 完全还原订单管理的表单：orderDate 绑定 Date 对象，初始值为当前日期
const orderForm = reactive({
  id: '',
  orderNo: '',
  customerName: '',
  customerId: '',
  orderDate: new Date(), // 初始为当前日期（和订单管理一致）
  paidAmount: '',
  status: '草稿',
  itemList: []
});

// 表单校验规则：完全复制订单管理的日期校验（处理 Date 对象）
const orderRules = reactive({
  orderNo: [
    {required: true, message: '请输入销售单编号', trigger: ['blur', 'change']},
    {type: 'string', min: 3, max: 50, message: '销售单编号长度3-50字符', trigger: ['blur', 'change']}
  ],
  customerId: [
    {required: true, message: '请选择客户', trigger: ['blur', 'change']},
    {type: 'string', message: '请选择有效的客户', trigger: ['blur', 'change']}
  ],
  orderDate: [
    {required: true, message: '请选择销售单日期', trigger: ['blur', 'change']},
    {
      validator: (rule, value, callback) => {
        // 完全和订单管理一致：只校验 Date 对象
        if (value instanceof Date) {
          callback();
        } else {
          callback(new Error('请选择有效的日期'));
        }
      },
      trigger: ['blur', 'change']
    }
  ],
  paidAmount: [
    {
      validator: (rule, value, callback) => {
        if (value === '' || value === null) {
          callback();
        } else {
          const num = Number(value);
          if (isNaN(num) || num < 0) {
            callback(new Error('已收金额必须是大于等于0的数字'));
          } else {
            callback();
          }
        }
      },
      trigger: ['blur', 'change']
    }
  ],
  itemList: [
    {required: true, message: '至少添加1条销售项', trigger: ['blur', 'change']},
    {type: 'array', min: 1, message: '至少添加1条销售项', trigger: ['blur', 'change']},
    {
      validator: (rule, value, callback) => {
        const hasEmptyProductId = value.some(item => !item.productId || item.productId < 1);
        const hasEmptyProductName = value.some(item => !item.productName.trim());
        if (hasEmptyProductId) {
          callback(new Error('所有销售项的商品ID必须是≥1的数字'));
        } else if (hasEmptyProductName) {
          callback(new Error('所有销售项的商品名称不能为空'));
        } else {
          callback();
        }
      },
      trigger: ['blur', 'change']
    }
  ]
});

// 处理已收金额输入
const handlePaidAmountInput = () => {
  const val = orderForm.paidAmount;
  if (val === '' || val === null || isNaN(Number(val))) {
    orderForm.paidAmount = '';
  } else {
    orderForm.paidAmount = Number(val);
  }
};

// 客户名称同步（保持之前的修复，确保显示）
const handleCustomerChange = (customerId) => {
  if (!customerId) {
    orderForm.customerName = '';
    return;
  }
  const selectedCustomer = customerList.value.find(item => item.id === Number(customerId));
  orderForm.customerName = selectedCustomer ? selectedCustomer.name : '';
};

// 页面加载
onMounted(() => {
  fetchCustomerList();
  fetchOrderList();
});

// 拉取客户列表
const fetchCustomerList = async () => {
  try {
    const res = await axios.get('/system/customers/list', {timeout: 10000});
    if (res.data.code === 200 && Array.isArray(res.data.data)) {
      customerList.value = res.data.data;
    } else {
      ElMessage.error('拉取客户失败');
    }
  } catch (err) {
    ElMessage.error('拉取客户异常：' + (err.message || '网络错误'));
  }
};

// 拉取销售单列表
const fetchOrderList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/system/orders/sales/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        orderNo: searchForm.orderNo,
        customerId: searchForm.customerId ? Number(searchForm.customerId) : '',
        status: searchForm.status,
        startDate: searchForm.dateRange[0] || '',
        endDate: searchForm.dateRange[1] || ''
      }
    });
    if (res.data.code === 200) {
      orderList.value = res.data.data.list;
      total.value = res.data.data.total;
      // 表格客户名称匹配
      orderList.value.forEach(order => {
        if (order.customerId && !order.customerName) {
          const customer = customerList.value.find(item => item.id === Number(order.customerId));
          order.customerName = customer ? customer.name : '未知客户';
        }
      });
    } else {
      ElMessage.error('查询销售单失败');
    }
  } catch (err) {
    ElMessage.error('查询销售单异常：' + (err.message || '网络错误'));
  } finally {
    loading.value = false;
  }
};

// 重置查询表单
const resetSearchForm = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'dateRange' ? [] : '';
  });
  fetchOrderList();
};

// 分页方法
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchOrderList();
};
const handleCurrentChange = (val) => {
  pageNum.value = val;
  fetchOrderList();
};

// 打开新增弹窗：完全还原订单管理的日期初始值（当前日期）
const openAddDialog = () => {
  isEdit.value = false;
  const date = new Date();
  const dateStr = date.getFullYear() +
      String(date.getMonth() + 1).padStart(2, '0') +
      String(date.getDate()).padStart(2, '0');
  const randomStr = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
  const defaultOrderNo = `SALE${dateStr}${randomStr}`;

  orderForm.id = '';
  orderForm.orderNo = defaultOrderNo;
  orderForm.customerName = '';
  orderForm.customerId = '';
  orderForm.orderDate = new Date(); // 初始为当前日期（解决默认2-01问题）
  orderForm.paidAmount = '';
  orderForm.status = '草稿';
  orderForm.itemList = [];

  dialogVisible.value = true;
  setTimeout(() => {
    addOrderItem();
  }, 100);
};

// 打开编辑弹窗：日期处理完全和订单管理一致
const openEditDialog = async (row) => {
  isEdit.value = true;
  loading.value = true;
  try {
    const res = await axios.get(`/system/orders/sales/detail/${row.id}`);
    if (res.data.code === 200) {
      const {order, items} = res.data.data;
      orderForm.id = order.id || '';
      orderForm.orderNo = order.orderNo || '';
      orderForm.customerId = order.customerId ? order.customerId + '' : '';
      const selectedCustomer = customerList.value.find(item => item.id === Number(order.customerId));
      orderForm.customerName = selectedCustomer ? selectedCustomer.name : '';
      // 后端返回字符串日期，转成 Date 对象（和订单管理一致）
      orderForm.orderDate = order.orderDate ? new Date(order.orderDate) : new Date();
      orderForm.paidAmount = order.paidAmount !== undefined ? Number(order.paidAmount) : '';
      orderForm.status = order.status || '草稿';
      orderForm.itemList = items.map(item => ({
        productId: Number(item.productId) || 1,
        productName: item.productName || '',
        quantity: Number(item.quantity) || 1,
        unitPrice: Number(item.unitPrice) || 0.01,
        amount: Number(item.amount) || 0.01
      }));
      dialogVisible.value = true;
    }
  } catch (err) {
    ElMessage.error('加载销售单详情失败');
  } finally {
    loading.value = false;
  }
};

// 打开详情弹窗
const openDetailDialog = async (orderId) => {
  loading.value = true;
  try {
    const res = await axios.get(`/system/orders/sales/detail/${orderId}`);
    if (res.data.code === 200) {
      const {order, items} = res.data.data || {};
      // 客户名称匹配
      if (order.customerId && !order.customerName) {
        const customer = customerList.value.find(item => item.id === Number(order.customerId));
        order.customerName = customer ? customer.name : '未知客户';
      }
      detailOrder.value = order || {};
      detailItemList.value = items || [];
      detailDialogVisible.value = true;
    } else {
      ElMessage.error('加载详情失败：' + (res.data.message || '未知错误'));
    }
  } catch (err) {
    ElMessage.error('加载详情异常：' + (err.message || '网络错误'));
  } finally {
    loading.value = false;
  }
};

// 新增销售项
const addOrderItem = () => {
  orderForm.itemList.push({
    productId: 1,
    productName: '',
    quantity: 1,
    unitPrice: 0.01,
    amount: 0.01
  });
  const lastIndex = orderForm.itemList.length - 1;
  const item = orderForm.itemList[lastIndex];
  Object.defineProperty(item, 'productName', {
    get: () => item._productName || '',
    set: (val) => {
      item._productName = val;
      if (!val) item.amount = 0;
      else calcItemAmount(item);
    }
  });
};

// 删除销售项
const removeOrderItem = (index) => {
  if (orderForm.itemList.length <= 1) {
    ElMessage.warning('至少保留1条销售项');
    return;
  }
  orderForm.itemList.splice(index, 1);
};

// 计算小计金额
const calcItemAmount = (row) => {
  const quantity = Number(row.quantity) || 1;
  const unitPrice = Number(row.unitPrice) || 0.01;
  row.quantity = quantity < 1 ? 1 : quantity;
  row.unitPrice = unitPrice < 0.01 ? 0.01 : unitPrice;
  row.amount = row.productName ? row.quantity * row.unitPrice : 0;
};

// 提交销售单：日期转成 yyyy-MM-dd 字符串（和订单管理一致）
const submitOrderForm = async () => {
  try {
    await unref(orderFormRef).validate();

    const submitData = {
      id: orderForm.id,
      orderNo: orderForm.orderNo.trim(),
      customerId: orderForm.customerId ? Number(orderForm.customerId) : '',
      // Date 对象转成字符串（和订单管理完全一致的处理方式）
      orderDate: orderForm.orderDate.toISOString().split('T')[0],
      paidAmount: orderForm.paidAmount === '' || orderForm.paidAmount === null ? 0 : Number(orderForm.paidAmount),
      status: orderForm.status,
      totalAmount: Number(orderForm.itemList.reduce((sum, item) => sum + (item.amount || 0), 0).toFixed(2)),
      itemList: orderForm.itemList.map(item => ({
        productId: Number(item.productId),
        productName: item.productName.trim(),
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice),
        amount: Number(item.amount.toFixed(2))
      }))
    };

    const res = isEdit.value
        ? await axios.put(`/system/orders/sales/update/${orderForm.id}`, submitData)
        : await axios.post('/system/orders/sales/add', submitData);

    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功');
      dialogVisible.value = false;
      fetchOrderList();
    } else {
      ElMessage.error(`操作失败：${res.data.message || '未知错误'}`);
    }
  } catch (err) {
    console.error('提交异常：', err);
    const errorMsg = err.message || '请求格式不正确';
    ElMessage.error(`提交失败：${errorMsg}`);
  }
};

// 变更销售单状态
const changeOrderStatus = async (id, status) => {
  try {
    const statusText = status;
    await ElMessageBox.confirm(`确定要将销售单状态改为「${statusText}」吗？`, '确认', {type: 'warning'});

    let url = '';
    if (status === '已确认') url = `/system/orders/sales/confirm/${id}`;
    else if (status === '已完成') url = `/system/orders/sales/complete/${id}`;
    else if (status === '已取消') url = `/system/orders/sales/cancel/${id}`;

    const res = await axios.put(url);
    if (res.data.code === 200) {
      ElMessage.success(`状态已更新为${statusText}`);
      fetchOrderList();
    } else {
      ElMessage.error(`状态更新失败：${res.data.message || '未知错误'}`);
    }
  } catch (err) {
    console.error('状态变更接口报错：', err);
    ElMessage.info('已取消操作');
  }
};

// 删除销售单
const deleteOrder = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该销售单？删除后不可恢复！', '警告', {type: 'error'});
    const res = await axios.delete(`/system/orders/sales/delete/${id}`);
    if (res.data.code === 200) {
      ElMessage.success('删除成功');
      fetchOrderList();
    } else {
      ElMessage.error('删除失败');
    }
  } catch (err) {
    ElMessage.info('已取消删除');
  }
};

// 销售单状态标签样式
const getStatusTagType = (status) => {
  const map = {
    '草稿': 'info',
    '已确认': 'primary',
    '已完成': 'success',
    '已取消': 'danger'
  };
  return map[status] || 'default';
};
</script>

<style scoped>
.order-list-container {
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

.search-form {
  margin-bottom: 20px;
  padding: 15px;
}

.order-table {
  padding: 15px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-table .el-input) {
  --el-input-height: 32px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-table__header th) {
  background-color: #f8f9fa;
  font-weight: 500;
}

:deep(.el-table-cell .el-input) {
  width: 100% !important;
}

:deep(.el-button--text) {
  margin: 0 8px;
}

.detail-content {
  line-height: 1.6;
  padding: 10px;
}

.detail-base-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: 500;
  width: 120px;
  color: #666;
}

.info-value {
  flex: 1;
  color: #333;
}

@media (max-width: 768px) {
  .detail-base-info {
    grid-template-columns: 1fr;
  }
}
</style>