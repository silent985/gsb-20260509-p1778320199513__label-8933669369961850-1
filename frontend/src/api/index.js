import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { useAccountStore } from '../stores/account'
import { useTransactionStore } from '../stores/transaction'
import { useCategoryStore } from '../stores/category'
import { useStatisticsStore } from '../stores/statistics'
import router from '../router'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

function clearAllStores() {
  const accountStore = useAccountStore()
  const transactionStore = useTransactionStore()
  const categoryStore = useCategoryStore()
  const statisticsStore = useStatisticsStore()
  const authStore = useAuthStore()
  
  accountStore.clear()
  transactionStore.clear()
  categoryStore.clear()
  statisticsStore.clear()
  authStore.logout()
}

// Request interceptor
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      clearAllStores()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default api
