import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus'; // 导入 ElMessage，用于提示

// 只导入你实际存在的组件（严格匹配你的目录结构）
import Login from '../components/Login.vue';
import Index from '../components/Index.vue'; // 布局壳子
import Home from '../components/Home.vue';   // 首页
import One from '../components/One.vue';     // 供应商管理
import Two from '../components/Two.vue';     // 客户管理
// 新增：导入产品管理组件（已放在 components 目录下）
import ProductManage from '../components/ProductManage.vue';
import Four from '../components/Four.vue';   // 科目管理
import Five from '../components/Five.vue';   // 订单管理
import Six from '../components/Six.vue';     // 销售单管理
import Seven from '../components/Seven.vue'; // 财务报表
import UserContainer from '../components/UserContainer.vue';

// 导入凭证相关组件
import VoucherMain from '../components/VoucherMain.vue';
import VoucherCreate from '../components/VoucherCreate.vue';
import VoucherEdit from '../components/VoucherEdit.vue';
import VoucherWriteOff from '../components/VoucherWriteOff.vue';

// 新增：导入订单详情组件（放到 components 文件夹下，和 Five.vue 同级）
import OrderDetail from '../components/OrderDetail.vue';

const routes = [
    // 登录页
    {
        path: '/login',
        component: Login,
        name: 'Login',
        meta: { title: '登录' }
    },

    // 默认重定向
    { path: '/', redirect: '/Home' },

    // 主布局（嵌套所有业务页面）
    {
        path: '/',
        component: Index,
        meta: { requiresAuth: true },
        children: [
            // 首页
            {
                path: 'Home',
                name: 'Home',
                component: Home,
                meta: { title: '首页' }
            },
            // 供应商管理
            {
                path: 'One',
                name: 'SupplierManage',
                component: One,
                meta: { title: '供应商管理' }
            },
            // 客户管理
            {
                path: 'Two',
                name: 'CustomerManage',
                component: Two,
                meta: { title: '客户管理' }
            },
            // 新增：产品管理路由（放在客户管理和订单管理之间）
            {
                path: 'ProductManage', // 路由路径（和菜单 index 一致）
                name: 'ProductManage', // 路由名称
                component: ProductManage, // 对应 components/ProductManage.vue
                meta: { title: '产品管理' } // 页面标题（路由守卫会拼接为「产品管理 - 管理系统」）
            },
            // 科目管理
            {
                path: 'Four',
                name: 'SubjectManage',
                component: Four,
                meta: { title: '科目管理' }
            },
            // 订单管理（核心修正：路径还是 /Five，组件是你的 Five.vue）
            {
                path: 'Five',
                name: 'OrderManage',
                component: Five,
                meta: { title: '订单管理' }
            },
            // 新增：订单详情页（嵌套在主布局下，路径和之前一致）
            {
                path: 'orders/detail/:id',
                name: 'OrderDetail',
                component: OrderDetail,
                meta: { title: '订单详情' }
            },
            // 销售单管理
            {
                path: 'Six',
                name: 'SalesOrderManage',
                component: Six,
                meta: { title: '销售单管理' }
            },
            // 财务报表
            {
                path: 'Seven', // 父路由路径不变
                // name: 'FinancialReport', // 移除父路由的name
                component: Seven, // 父布局壳子不变
                meta: { title: '财务报表' },
                children: [
                    {
                        path: 'balance-sheet',
                        name: 'FinancialReport', // 把父路由的name移到这里（默认显示的子路由）
                        component: () => import('../components/report/BalanceSheet.vue'),
                        meta: { title: '资产负债表' }
                    },
                    {
                        path: 'income-statement',
                        name: 'IncomeStatement',
                        component: () => import('../components/report/IncomeStatement.vue'),
                        meta: { title: '利润表' }
                    },
                    {
                        path: 'cash-flow',
                        name: 'CashFlow',
                        component: () => import('../components/report/CashFlow.vue'),
                        meta: { title: '现金流量表' }
                    },
                    { path: '', redirect: 'balance-sheet' } // 空路径重定向不变，无需加name
                ]
            },
            {
                path: 'Main',
                name: 'UserManage',
                component: UserContainer,
                meta: { title: '用户管理', requiresAuth: true }
            },
            // 凭证管理（你的配置正确，无需修改）
            {
                path: 'voucher/list',
                name: 'VoucherManage',
                component: VoucherMain,
                meta: { title: '凭证管理' }
            },
            {
                path: 'voucher/create',
                component: VoucherCreate,
                meta: { title: '新增凭证' }
            },
            {
                path: 'voucher/edit/:id',
                component: VoucherEdit,
                props: true,
                meta: { title: '编辑凭证' }
            },
            {
                path: 'voucher/write-off/:id',
                component: VoucherWriteOff,
                props: true,
                meta: { title: '红字冲销' }
            }
        ]
    },

    // 404页面（优化：重定向更友好）
    {
        path: '/:pathMatch(.*)*',
        redirect: (to) => {
            const token = localStorage.getItem('token');
            return token ? '/Home' : '/login';
        }
    }
];

// 创建路由实例
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior: () => ({ top: 0 }) // 路由切换时回到顶部（优化体验）
});

// 路由守卫（核心优化：添加 50ms 延迟，确保路由切换完成）
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token');
    const requiresAuth = to.meta.requiresAuth;

    if (token && to.path === '/login') {
        next('/Home'); // 已登录，访问登录页跳首页
    } else if (!token && requiresAuth) {
        ElMessage.warning('请先登录'); // 优化：添加未登录提示
        next('/login'); // 未登录，访问需授权页面跳登录页
    } else {
        // 优化：如果没有设置title，默认显示「管理系统」
        document.title = to.meta.title ? `${to.meta.title} - 管理系统` : '管理系统';
        // 关键修改：加 50ms 延迟，确保路由切换完成后再渲染页面
        setTimeout(() => {
            next();
        }, 50);
    }
});

export default router;