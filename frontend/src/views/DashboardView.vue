<template>
  <div class="dashboard">
    <div class="page-header">
      <h1 class="page-title">仪表盘</h1>
      <div class="header-actions">
        <button class="btn btn-primary" @click="showAddTransaction = true">
          ➕ 记一笔
        </button>
      </div>
    </div>
    
    <!-- Overview Stats -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-card-icon" style="background: rgba(16, 185, 129, 0.1);">💵</div>
        <div class="stat-card-value text-income">¥{{ formatNumber(overview?.totalBalance || 0) }}</div>
        <div class="stat-card-label">总资产</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-icon" style="background: rgba(16, 185, 129, 0.1);">📈</div>
        <div class="stat-card-value text-income">¥{{ formatNumber(overview?.monthIncome || 0) }}</div>
        <div class="stat-card-label">本月收入</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-icon" style="background: rgba(239, 68, 68, 0.1);">📉</div>
        <div class="stat-card-value text-expense">¥{{ formatNumber(overview?.monthExpense || 0) }}</div>
        <div class="stat-card-label">本月支出</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-icon" style="background: rgba(99, 102, 241, 0.1);">💰</div>
        <div class="stat-card-value" :class="overview?.monthNet >= 0 ? 'text-income' : 'text-expense'">
          ¥{{ formatNumber(overview?.monthNet || 0) }}
        </div>
        <div class="stat-card-label">本月结余</div>
      </div>
    </div>
    
    <!-- Charts Row -->
    <div class="charts-row">
      <!-- Monthly Trend Chart -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>收支趋势</h3>
        </div>
        <div class="card-body">
          <canvas ref="trendChartRef"></canvas>
        </div>
      </div>
      
      <!-- Category Stats -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>本月支出分布</h3>
        </div>
        <div class="card-body">
          <canvas ref="categoryChartRef"></canvas>
        </div>
      </div>
    </div>
    
    <!-- Recent Transactions & Accounts -->
    <div class="bottom-row">
      <!-- Recent Transactions -->
      <div class="card">
        <div class="card-header flex justify-between items-center">
          <h3>最近交易</h3>
          <router-link to="/transactions" class="text-sm text-primary">查看全部 →</router-link>
        </div>
        <div class="card-body" style="padding: 0;">
          <div v-if="transactions.length === 0" class="empty-state" style="padding: 2rem;">
            <p class="text-secondary">暂无交易记录</p>
          </div>
          <div v-else class="transaction-list">
            <div 
              v-for="tx in transactions.slice(0, 5)" 
              :key="tx.id"
              class="transaction-item"
            >
              <div class="transaction-icon" :style="{ backgroundColor: getCategoryColor(tx.categoryIcon) }">
                {{ tx.categoryIcon || '💳' }}
              </div>
              <div class="transaction-info">
                <div class="transaction-category">{{ tx.categoryName }}</div>
                <div class="transaction-note">{{ tx.note || tx.accountName }}</div>
              </div>
              <div class="transaction-amount" :class="tx.type.toLowerCase()">
                {{ tx.type === 'INCOME' ? '+' : '-' }}¥{{ formatNumber(tx.amount) }}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Account Balances -->
      <div class="card">
        <div class="card-header flex justify-between items-center">
          <h3>账户余额</h3>
          <router-link to="/accounts" class="text-sm text-primary">管理账户 →</router-link>
        </div>
        <div class="card-body">
          <div v-if="accounts.length === 0" class="empty-state" style="padding: 1rem;">
            <p class="text-secondary">暂无账户</p>
          </div>
          <div v-else class="account-list">
            <div 
              v-for="account in accounts" 
              :key="account.id"
              class="account-item"
            >
              <div class="account-icon" :style="{ backgroundColor: account.color + '20' }">
                {{ account.icon || '💳' }}
              </div>
              <div class="account-info">
                <div class="account-name">{{ account.name }}</div>
                <div class="account-type">{{ getAccountTypeName(account.type) }}</div>
              </div>
              <div class="account-balance" :class="account.balance >= 0 ? 'positive' : 'negative'">
                ¥{{ formatNumber(account.balance) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Add Transaction Modal -->
    <TransactionModal 
      v-if="showAddTransaction"
      @close="showAddTransaction = false"
      @saved="onTransactionSaved"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Chart, registerables } from 'chart.js'
import { useStatisticsStore } from '../stores/statistics'
import { useTransactionStore } from '../stores/transaction'
import { useAccountStore } from '../stores/account'
import { useCategoryStore } from '../stores/category'
import TransactionModal from '../components/TransactionModal.vue'

Chart.register(...registerables)

const statisticsStore = useStatisticsStore()
const transactionStore = useTransactionStore()
const accountStore = useAccountStore()
const categoryStore = useCategoryStore()

const showAddTransaction = ref(false)
const trendChartRef = ref(null)
const categoryChartRef = ref(null)
let trendChart = null
let categoryChart = null

const overview = computed(() => statisticsStore.overview)
const transactions = computed(() => transactionStore.transactions)
const accounts = computed(() => accountStore.accounts)

function formatNumber(num) {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getCategoryColor(icon) {
  return 'rgba(99, 102, 241, 0.1)'
}

function getAccountTypeName(type) {
  const types = {
    CASH: '现金',
    BANK: '银行卡',
    CREDIT_CARD: '信用卡',
    ALIPAY: '支付宝',
    WECHAT: '微信',
    OTHER: '其他'
  }
  return types[type] || type
}

async function loadData() {
  await Promise.all([
    statisticsStore.fetchOverview(),
    transactionStore.fetchTransactions(0, 10),
    accountStore.fetchAccounts(),
    categoryStore.fetchCategories()
  ])
  
  // Load charts
  const now = new Date()
  await statisticsStore.fetchCategoryStats(now.getFullYear(), now.getMonth() + 1)
  await statisticsStore.fetchMonthlyTrend(6)
  
  renderCharts()
}

function renderCharts() {
  // Trend chart
  if (trendChartRef.value && statisticsStore.monthlyTrend) {
    const ctx = trendChartRef.value.getContext('2d')
    if (trendChart) trendChart.destroy()
    
    trendChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: statisticsStore.monthlyTrend.map(t => t.label.substring(5)),
        datasets: [
          {
            label: '收入',
            data: statisticsStore.monthlyTrend.map(t => t.income),
            borderColor: '#10b981',
            backgroundColor: 'rgba(16, 185, 129, 0.1)',
            fill: true,
            tension: 0.4
          },
          {
            label: '支出',
            data: statisticsStore.monthlyTrend.map(t => t.expense),
            borderColor: '#ef4444',
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            fill: true,
            tension: 0.4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'top' }
        },
        scales: {
          y: { beginAtZero: true }
        }
      }
    })
  }
  
  // Category chart
  if (categoryChartRef.value && statisticsStore.categoryStats?.expenseByCategory) {
    const ctx = categoryChartRef.value.getContext('2d')
    if (categoryChart) categoryChart.destroy()
    
    const data = statisticsStore.categoryStats.expenseByCategory
    categoryChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: data.map(c => c.categoryName),
        datasets: [{
          data: data.map(c => c.amount),
          backgroundColor: data.map(c => c.categoryColor || '#6366f1')
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'right' }
        }
      }
    })
  }
}

async function onTransactionSaved() {
  showAddTransaction.value = false
  await loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.charts-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.chart-card .card-body {
  height: 280px;
}

.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.transaction-list {
  max-height: 360px;
  overflow-y: auto;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.account-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.875rem;
  background: var(--color-bg);
  border-radius: var(--radius-lg);
}

.account-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  border-radius: var(--radius-lg);
}

.account-info {
  flex: 1;
}

.account-name {
  font-weight: 500;
  margin-bottom: 0.125rem;
}

.account-type {
  font-size: 0.8125rem;
  color: var(--color-text-secondary);
}

.account-balance {
  font-weight: 600;
}

.account-balance.positive {
  color: var(--color-income);
}

.account-balance.negative {
  color: var(--color-expense);
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-row,
  .bottom-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
