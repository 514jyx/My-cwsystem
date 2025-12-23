<template>
  <div class="login-container">
    <div class="login-card">
      <h2>财务系统</h2>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              placeholder="密码"
              prefix-icon="Lock"
              :type="showPwd ? 'text' : 'password'"
          >
            <template #suffix>
              <el-icon @click="showPwd = !showPwd" style="cursor: pointer">
                <el-icon name="Eye" v-if="showPwd" />
                <el-icon name="EyeOff" v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import {ElMessage, ElIcon} from 'element-plus';
import {useRouter} from 'vue-router';
import {getCurrentInstance} from 'vue';

// 基础配置
const {proxy} = getCurrentInstance();
const $axios = proxy.$axios; // 假设已全局配置 axios
const router = useRouter();

// 表单状态
const formRef = ref(null);
const loading = ref(false);
const showPwd = ref(false);
const form = reactive({username: '', password: ''});

// 表单验证规则
const rules = {
  username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}]
};

// 登录处理逻辑
const handleLogin = async () => {
  try {
    // 表单校验
    await formRef.value.validate();
    loading.value = true;

    // 发送登录请求（后端接口需自行实现）
    const res = await $axios.post('/system/user/login', form);
    if (res.data.code === 200) {
      // 存储 token
      localStorage.setItem('token', res.data.data.token);
      // 关键：登录成功后直接跳首页 /Home
      router.push('/Home');
      ElMessage.success('登录成功');
    } else {
      ElMessage.error(res.data.msg || '登录失败');
    }
  } catch (err) {
    // 表单校验失败会抛出 Error 对象（无 message）
    if (!err.response) {
      ElMessage.warning('请输入账号密码');
    }
    // 其他错误（如 401/500）已在 axios 拦截器中处理（ElMessage.error）
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.login-card {
  width: 350px;
  padding: 30px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.login-card h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #165DFF;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
}
</style>