<template>
  <div class="login-page">
    <div class="login-container">
      <!-- Left Side - Branding -->
      <div class="login-branding">
        <div class="brand-content">
          <div class="brand-icon">💰</div>
          <h1 class="brand-title">智能记账</h1>
          <p class="brand-subtitle">轻松理财，掌控人生</p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">📊</span>
              <span>智能统计分析</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🎯</span>
              <span>预算目标管理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📱</span>
              <span>多端数据同步</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Right Side - Form -->
      <div class="login-form-container">
        <div class="login-form-wrapper">
          <div class="form-header">
            <h2>{{ isRegister ? '创建账户' : '欢迎回来' }}</h2>
            <p>{{ isRegister ? '开启您的智能记账之旅' : '登录以继续使用服务' }}</p>
          </div>
          
          <form @submit.prevent="handleSubmit" class="login-form">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <input 
                v-model="form.username" 
                type="text" 
                class="form-input"
                placeholder="请输入用户名"
                required
              />
            </div>
            
            <div class="form-group" v-if="isRegister">
              <label class="form-label">邮箱（可选）</label>
              <input 
                v-model="form.email" 
                type="email" 
                class="form-input"
                placeholder="请输入邮箱"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">密码</label>
              <input 
                v-model="form.password" 
                type="password" 
                class="form-input"
                placeholder="请输入密码"
                required
              />
            </div>
            
            <div class="form-group" v-if="isRegister">
              <label class="form-label">确认密码</label>
              <input 
                v-model="form.confirmPassword" 
                type="password" 
                class="form-input"
                placeholder="请再次输入密码"
                required
              />
            </div>
            
            <div v-if="error" class="error-message">
              {{ error }}
            </div>
            
            <button type="submit" class="btn btn-primary btn-lg submit-btn" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              {{ loading ? '处理中...' : (isRegister ? '立即注册' : '登录') }}
            </button>
          </form>
          
          <div class="form-footer">
            <span>{{ isRegister ? '已有账户？' : '还没有账户？' }}</span>
            <a href="#" @click.prevent="toggleMode">
              {{ isRegister ? '立即登录' : '立即注册' }}
            </a>
          </div>
          
          <div class="demo-account" v-if="!isRegister">
            <p>演示账号</p>
            <div class="demo-credentials">
              <span>用户名: admin</span>
              <span>密码: Admin123456</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isRegister = ref(false)
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

function toggleMode() {
  isRegister.value = !isRegister.value
  error.value = ''
}

async function handleSubmit() {
  error.value = ''
  
  if (isRegister.value) {
    if (form.password !== form.confirmPassword) {
      error.value = '两次输入的密码不一致'
      return
    }
    if (form.password.length < 6) {
      error.value = '密码长度至少6个字符'
      return
    }
  }
  
  loading.value = true
  
  try {
    if (isRegister.value) {
      await authStore.register({
        username: form.username,
        password: form.password,
        email: form.email || undefined
      })
      // Auto login after register
      await authStore.login(form.username, form.password)
    } else {
      await authStore.login(form.username, form.password)
    }
    router.push('/')
  } catch (err) {
    error.value = err.response?.data?.message || '操作失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 1rem;
}

.login-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  background: white;
  border-radius: var(--radius-2xl);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.login-branding {
  flex: 1;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  padding: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-content {
  text-align: center;
  color: white;
}

.brand-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.brand-subtitle {
  font-size: 1.125rem;
  opacity: 0.8;
  margin-bottom: 2.5rem;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
}

.feature-icon {
  font-size: 1.25rem;
}

.login-form-container {
  flex: 1;
  padding: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-header h2 {
  font-size: 1.75rem;
  margin-bottom: 0.5rem;
}

.form-header p {
  color: var(--color-text-secondary);
}

.login-form {
  margin-bottom: 1.5rem;
}

.submit-btn {
  width: 100%;
  margin-top: 0.5rem;
}

.error-message {
  padding: 0.75rem 1rem;
  background-color: rgba(239, 68, 68, 0.1);
  color: var(--color-danger);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.form-footer {
  text-align: center;
  color: var(--color-text-secondary);
}

.form-footer a {
  color: var(--color-primary);
  font-weight: 500;
  margin-left: 0.25rem;
}

.demo-account {
  margin-top: 2rem;
  padding: 1rem;
  background: var(--color-bg);
  border-radius: var(--radius-lg);
  text-align: center;
}

.demo-account p {
  font-size: 0.8125rem;
  color: var(--color-text-secondary);
  margin-bottom: 0.5rem;
}

.demo-credentials {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  font-size: 0.875rem;
  font-weight: 500;
}

@media (max-width: 768px) {
  .login-branding {
    display: none;
  }
  
  .login-container {
    max-width: 400px;
  }
  
  .login-form-container {
    padding: 2rem;
  }
}
</style>
