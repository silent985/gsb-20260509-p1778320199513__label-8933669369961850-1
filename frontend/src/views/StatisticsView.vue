<template>
  <div class="statistics-page">
    <div class="page-header">
      <h1 class="page-title">统计报表</h1>
      <div class="period-selector">
        <button class="btn btn-outline btn-sm" @click="prevMonth">←</button>
        <span class="current-period">{{ currentYear }}年{{ currentMonth }}月</span>
        <button class="btn btn-outline btn-sm" @click="nextMonth">→</button>
      </div>
    </div>
    
    <!-- Summary Cards -->
    <div class="summary-row">
      <div class="summary-card income">
        <div class="summary-icon">📈</div>
        <div class="summary-content">
          <div class="summary-label">本月收入</div>
          <div class="summary-value">¥{{ formatNumber(monthData.income) }}</div>
        </div>
      </div>
      <div class="summary-card expense">
        <div class="summary-icon">📉</div>
        <div class="summary-content">
          <div class="summary-label">本月支出</div>
          <div class="summary-value">¥{{ formatNumber(monthData.expense) }}</div>
        </div>
      </div>
      <div class="summary-card net" :class="{ positive: monthData.net >= 0 }">
        <div class="summary-icon">💰</div>
        <div class="summary-content">
          <div class="summary-label">本月结余</div>
          <div class="summary-value">¥{{ formatNumber(monthData.net) }}</div>
        </div>
      </div>
    </div>
    
    <!-- Charts -->
    <div class="charts-grid">
      <div class="card">
        <div class="card-header"><h3>支出分类占比</h3></div>
        <div class="card-body chart-container">
          <canvas ref="expenseChartRef"></canvas>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3>收入分类占比</h3></div>
        <div class="card-body chart-container">
          <canvas ref="incomeChartRef"></canvas>
        </div>
      </div>
    </div>
    
    <div class="card">
      <div class="card-header"><h3>近6个月收支趋势</h3></div>
      <div class="card-body chart-container-wide">
        <canvas ref="trendChartRef"></canvas>
      </div>
    </div>
    
    <!-- Category Details -->
    <div class="card">
      <div class="card-header">
        <h3>支出明细</h3>
      </div>
      <div class="card-body">
        <div v-if="!categoryStats?.expenseByCategory?.length" class="empty-state" style="padding: 2rem;">
          <p class="text-secondary">本月暂无支出记录</p>
        </div>
        <div v-else class="category-list">
          <div v-for="cat in categoryStats.expenseByCategory" :key="cat.categoryId" class="category-row">
            <div class="category-info">
              <span class="category-icon" :style="{ backgroundColor: cat.categoryColor + '20' }">{{ cat.categoryIcon }}</span>
              <span class="category-name">{{ cat.categoryName }}</span>
            </div>
            <div class="category-stats">
              <span class="category-count">{{ cat.count }}笔</span>
              <span class="category-amount">¥{{ formatNumber(cat.amount) }}</span>
              <span class="category-percent">{{ cat.percentage }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Chart, registerables } from 'chart.js'
import { useStatisticsStore } from '../stores/statistics'

Chart.register(...registerables)

const statisticsStore = useStatisticsStore()

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)

const expenseChartRef = ref(null)
const incomeChartRef = ref(null)
const trendChartRef = ref(null)

let expenseChart = null
let incomeChart = null
let trendChart = null

const categoryStats = computed(() => statisticsStore.categoryStats)
const monthData = reactive({ income: 0, expense: 0, net: 0 })

function formatNumber(num) {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function prevMonth() {
  if (currentMonth.value === 1) { currentMonth.value = 12; currentYear.value-- }
  else { currentMonth.value-- }
}

function nextMonth() {
  if (currentMonth.value === 12) { currentMonth.value = 1; currentYear.value++ }
  else { currentMonth.value++ }
}

async function loadData() {
  await statisticsStore.fetchCategoryStats(currentYear.value, currentMonth.value)
  await statisticsStore.fetchMonthlyTrend(6)
  
  // Calculate month totals
  const stats = categoryStats.value
  monthData.income = stats?.incomeByCategory?.reduce((sum, c) => sum + Number(c.amount), 0) || 0
  monthData.expense = stats?.expenseByCategory?.reduce((sum, c) => sum + Number(c.amount), 0) || 0
  monthData.net = monthData.income - monthData.expense
  
  renderCharts()
}

function renderCharts() {
  // Expense chart
  if (expenseChartRef.value && categoryStats.value?.expenseByCategory?.length) {
    const ctx = expenseChartRef.value.getContext('2d')
    if (expenseChart) expenseChart.destroy()
    const data = categoryStats.value.expenseByCategory
    expenseChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: data.map(c => c.categoryName),
        datasets: [{ data: data.map(c => c.amount), backgroundColor: data.map(c => c.categoryColor || '#6366f1') }]
      },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
    })
  }
  
  // Income chart
  if (incomeChartRef.value && categoryStats.value?.incomeByCategory?.length) {
    const ctx = incomeChartRef.value.getContext('2d')
    if (incomeChart) incomeChart.destroy()
    const data = categoryStats.value.incomeByCategory
    incomeChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: data.map(c => c.categoryName),
        datasets: [{ data: data.map(c => c.amount), backgroundColor: data.map(c => c.categoryColor || '#10b981') }]
      },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
    })
  }
  
  // Trend chart
  if (trendChartRef.value && statisticsStore.monthlyTrend?.length) {
    const ctx = trendChartRef.value.getContext('2d')
    if (trendChart) trendChart.destroy()
    const trend = statisticsStore.monthlyTrend
    trendChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: trend.map(t => t.label.substring(5) + '月'),
        datasets: [
          { label: '收入', data: trend.map(t => t.income), backgroundColor: 'rgba(16, 185, 129, 0.8)' },
          { label: '支出', data: trend.map(t => t.expense), backgroundColor: 'rgba(239, 68, 68, 0.8)' }
        ]
      },
      options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'top' } } }
    })
  }
}

watch([currentYear, currentMonth], loadData)

onMounted(loadData)
</script>

<style scoped>
.period-selector {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.current-period {
  font-size: 1.125rem;
  font-weight: 600;
  min-width: 120px;
  text-align: center;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
}

.summary-card.income { border-left: 4px solid var(--color-income); }
.summary-card.expense { border-left: 4px solid var(--color-expense); }
.summary-card.net { border-left: 4px solid var(--color-primary); }
.summary-card.net.positive { border-left-color: var(--color-income); }

.summary-icon {
  font-size: 2rem;
}

.summary-label {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  margin-bottom: 0.25rem;
}

.summary-value {
  font-size: 1.5rem;
  font-weight: 700;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.chart-container {
  height: 280px;
}

.chart-container-wide {
  height: 320px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.category-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem;
  background: var(--color-bg);
  border-radius: var(--radius-lg);
}

.category-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.category-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  border-radius: var(--radius-md);
}

.category-name {
  font-weight: 500;
}

.category-stats {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.category-count {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

.category-amount {
  font-weight: 600;
  min-width: 100px;
  text-align: right;
}

.category-percent {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  min-width: 50px;
  text-align: right;
}

@media (max-width: 768px) {
  .summary-row, .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
