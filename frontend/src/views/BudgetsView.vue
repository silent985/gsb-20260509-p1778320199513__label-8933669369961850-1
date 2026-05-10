<template>
  <div class="budgets-page">
    <div class="page-header">
      <h1 class="page-title">预算管理</h1>
      <button class="btn btn-primary" @click="showModal = true">➕ 设置预算</button>
    </div>
    
    <div class="month-selector">
      <button class="btn btn-outline btn-sm" @click="prevMonth">← 上月</button>
      <span class="current-month">{{ currentYear }}年{{ currentMonth }}月</span>
      <button class="btn btn-outline btn-sm" @click="nextMonth">下月 →</button>
    </div>
    
    <div v-if="budgets.length === 0" class="card">
      <div class="empty-state">
        <div class="empty-state-icon">🎯</div>
        <div class="empty-state-title">暂无预算</div>
        <div class="empty-state-text">设置预算帮助您控制支出</div>
      </div>
    </div>
    
    <div v-else class="budgets-list">
      <div v-for="budget in budgets" :key="budget.id" class="budget-card">
        <div class="budget-header">
          <div class="budget-category">
            <span class="budget-icon">{{ budget.categoryIcon || '🎯' }}</span>
            <span class="budget-name">{{ budget.categoryName }}</span>
          </div>
          <button class="action-btn" @click="deleteBudget(budget)">🗑️</button>
        </div>
        <div class="budget-progress">
          <div class="progress-bar">
            <div 
              class="progress-fill"
              :class="{ warning: budget.isAlertTriggered, danger: budget.isOverBudget }"
              :style="{ width: Math.min(budget.usagePercentage, 100) + '%' }"
            ></div>
          </div>
          <div class="progress-info">
            <span>已用 ¥{{ formatNumber(budget.spentAmount) }}</span>
            <span>预算 ¥{{ formatNumber(budget.amount) }}</span>
          </div>
        </div>
        <div class="budget-footer">
          <span class="remaining" :class="{ negative: budget.isOverBudget }">
            {{ budget.isOverBudget ? '超支' : '剩余' }} ¥{{ formatNumber(budget.remainingAmount) }}
          </span>
          <span class="percentage" :class="{ warning: budget.isAlertTriggered, danger: budget.isOverBudget }">
            {{ budget.usagePercentage }}%
          </span>
        </div>
      </div>
    </div>
    
    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">设置预算</h3>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="error" class="error-message">{{ error }}</div>
          <div class="form-group">
            <label class="form-label">分类</label>
            <select v-model="form.categoryId" class="form-select" required>
              <option v-for="cat in expenseCategories" :key="cat.id" :value="cat.id">
                {{ cat.icon }} {{ cat.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">预算金额</label>
            <input v-model="form.amount" type="number" step="0.01" min="1" class="form-input" required />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="handleSubmit">保存</button>
        </div>
      </div>
    </div>
    
    <!-- Delete Confirm Modal -->
    <ConfirmModal
      v-model:visible="showDeleteConfirm"
      title="确认删除"
      :message="`确定要删除 '${deletingBudget?.categoryName || ''}' 的预算吗？`"
      type="danger"
      confirmText="删除"
      cancelText="取消"
      @confirm="handleDeleteConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useCategoryStore } from '../stores/category'
import api from '../api'
import ConfirmModal from '../components/ConfirmModal.vue'

const categoryStore = useCategoryStore()

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const budgets = ref([])
const showModal = ref(false)
const error = ref('')
const showDeleteConfirm = ref(false)
const deletingBudget = ref(null)
const form = reactive({ categoryId: null, amount: '' })

const expenseCategories = computed(() => categoryStore.expenseCategories)

function formatNumber(num) {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function prevMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

async function fetchBudgets() {
  const res = await api.get(`/budgets?year=${currentYear.value}&month=${currentMonth.value}`)
  budgets.value = res.data.data || []
}

function closeModal() {
  showModal.value = false
  form.categoryId = null
  form.amount = ''
  error.value = ''
}

async function handleSubmit() {
  if (!form.categoryId) {
    error.value = '请选择分类'
    return
  }
  if (!form.amount || Number(form.amount) <= 0) {
    error.value = '请输入有效的预算金额'
    return
  }
  error.value = ''
  
  await api.post('/budgets', {
    categoryId: form.categoryId,
    amount: Number(form.amount),
    year: currentYear.value,
    month: currentMonth.value
  })
  closeModal()
  fetchBudgets()
}

function deleteBudget(budget) {
  deletingBudget.value = budget
  showDeleteConfirm.value = true
}

async function handleDeleteConfirm() {
  if (deletingBudget.value) {
    await api.delete(`/budgets/${deletingBudget.value.id}`)
    fetchBudgets()
  }
  showDeleteConfirm.value = false
  deletingBudget.value = null
}

watch([currentYear, currentMonth], fetchBudgets)

onMounted(() => {
  categoryStore.fetchCategories()
  fetchBudgets()
})
</script>

<style scoped>
.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.current-month {
  font-size: 1.25rem;
  font-weight: 600;
}

.budgets-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.budget-card {
  background: white;
  border-radius: var(--radius-xl);
  padding: 1.5rem;
  box-shadow: var(--shadow-md);
}

.budget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.budget-category {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.budget-icon {
  font-size: 1.5rem;
}

.budget-name {
  font-weight: 600;
}

.action-btn {
  padding: 0.25rem;
  background: none;
  border: none;
  cursor: pointer;
  opacity: 0.5;
}

.action-btn:hover {
  opacity: 1;
}

.budget-progress {
  margin-bottom: 1rem;
}

.progress-bar {
  height: 8px;
  background: var(--color-bg);
  border-radius: var(--radius-full);
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  transition: width var(--transition-normal);
}

.progress-fill.warning {
  background: var(--color-warning);
}

.progress-fill.danger {
  background: var(--color-danger);
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.8125rem;
  color: var(--color-text-secondary);
}

.budget-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remaining {
  font-weight: 500;
  color: var(--color-income);
}

.remaining.negative {
  color: var(--color-expense);
}

.percentage {
  font-weight: 600;
  color: var(--color-primary);
}

.percentage.warning {
  color: var(--color-warning);
}

.percentage.danger {
  color: var(--color-danger);
}

.error-message {
  background-color: #fee2e2;
  color: #ef4444;
  padding: 0.75rem;
  border-radius: var(--radius-md);
  margin-bottom: 1rem;
  font-size: 0.875rem;
  text-align: center;
}
</style>
