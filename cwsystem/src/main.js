import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';

const app = createApp(App);

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
}

axios.defaults.timeout = 5000;
axios.defaults.withCredentials = true;

// 请求拦截器
axios.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        ElMessage.error('请求发送失败');
        return Promise.reject(error);
    }
);

// 响应拦截器
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            ElMessage.error('登录已过期，请重新登录');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            router.push('/login');
        } else {
            ElMessage.error(error.message || '请求失败');
        }
        return Promise.reject(error);
    }
);

app.config.globalProperties.$axios = axios;
app.use(ElementPlus, { locale: zhCn });
app.use(router);
app.mount('#app');