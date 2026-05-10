<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <h3 class="modal-title">{{ editMode ? '编辑交易' : '记一笔' }}</h3>
        <button class="modal-close" @click="$emit('close')">✕</button>
      </div>
      
      <div class="modal-body">
        <!-- Type Tabs -->
        <div class="type-tabs">
          <button 
            class="type-tab" 
            :class="{ active: form.type === 'EXPENSE' }"
            @click="form.type = 'EXPENSE'"
          >
            💸 支出
          </button>
          <button 
            class="type-tab" 
            :class="{ active: form.type === 'INCOME' }"
            @click="form.type = 'INCOME'"
          >
            💰 收入
          </button>
        </div>
        
        <form @submit.prevent="handleSubmit">
          <!-- Amount -->
          <div class="form-group">
            <label class="form-label">金额</label>
            <div class="amount-input-wrapper">
              <span class="currency-symbol">¥</span>
              <input 
                v-model="form.amount"
                type="number"
                step="0.01"
                min="0.01"
                class="form-input amount-input"
                placeholder="0.00"
                required
              />
            </div>
          </div>
          
          <!-- Category -->
          <div class="form-group">
            <label class="form-label">分类</label>
            <div class="category-grid">
              <div 
                v-for="cat in filteredCategories"
                :key="cat.id"
                class="category-item"
                :class="{ selected: form.categoryId === cat.id }"
                @click="form.categoryId = cat.id"
              >
                <span class="category-icon">{{ cat.icon }}</span>
                <span class="category-name">{{ cat.name }}</span>
              </div>
            </div>
          </div>
          
          <!-- Account -->
          <div class="form-group">
            <label class="form-label">账户</label>
            <select v-model="form.accountId" class="form-select" required>
              <option v-for="acc in accounts" :key="acc.id" :value="acc.id">
                {{ acc.icon }} {{ acc.name }}
              </option>
            </select>
          </div>
          
          <!-- Date -->
          <div class="form-group">
            <label class="form-label">日期</label>
            <input 
              v-model="form.transactionDate"
              type="date"
              class="form-input"
              required
            />
          </div>
          
          <!-- Note -->
          <div class="form-group">
            <label class="form-label">备注</label>
            <input 
              v-model="form.note"
              type="text"
              class="form-input"
              placeholder="添加备注..."
            />
          </div>
          
          <div v-if="error" class="error-message">{{ error }}</div>
        </form>
      </div>
      
      <div class="modal-footer">
        <button class="btn btn-outline" @click="$emit('close')">取消</button>
        <button class="btn btn-primary" @click="handleSubmit" :disabled="loading">
          {{ loading ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useTransactionStore } from '../stores/transaction'
import { useCategoryStore } from '../stores/category'
import { useAccountStore } from '../stores/account'
import dayjs from 'dayjs'

const props = defineProps({
  transaction: { type: Object, default: null }
})

const emit = defineEmits(['close', 'saved'])

const transactionStore = useTransactionStore()
const categoryStore = useCategoryStore()
const accountStore = useAccountStore()

const loading = ref(false)
const error = ref('')
const editMode = computed(() => !!props.transaction)

const form = reactive({
  type: props.transaction?.type || 'EXPENSE',
  amount: props.transaction?.amount || '',
  categoryId: props.transaction?.categoryId || null,
  accountId: props.transaction?.accountId || null,
  transactionDate: props.transaction?.transactionDate || dayjs().format('YYYY-MM-DD'),
  note: props.transaction?.note || ''
})

const accounts = computed(() => accountStore.accounts)
const filteredCategories = computed(() => {
  return form.type === 'INCOME' 
    ? categoryStore.incomeCategories 
    : categoryStore.expenseCategories
})

async function handleSubmit() {
  if (!form.amount || !form.categoryId || !form.accountId) {
    error.value = '请填写完整信息'
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    const data = {
      type: form.type,
      amount: Number(form.amount),
      categoryId: form.categoryId,
      accountId: form.accountId,
      transactionDate: form.transactionDate,
      note: form.note
    }
    
    if (editMode.value) {
      await transactionStore.updateTransaction(props.transaction.id, data)
    } else {
      await transactionStore.createTransaction(data)
    }
    
    emit('saved')
  } catch (err) {
    error.value = err.response?.data?.message || '保存失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // 强制刷新分类和账户数据
  await categoryStore.fetchCategories()
  await accountStore.fetchAccounts()
  
  // Set default account
  if (!form.accountId && accounts.value.length > 0) {
    const defaultAccount = accounts.value.find(a => a.isDefault) || accounts.value[0]
    form.accountId = defaultAccount.id
  }
  
  // Set default category based on type
  setDefaultCategory()
})

// 监听类型变化，自动切换到对应分类
watch(() => form.type, () => {
  setDefaultCategory()
})

function setDefaultCategory() {
  const cats = form.type === 'INCOME' 
    ? categoryStore.incomeCategories 
    : categoryStore.expenseCategories
  if (cats.length > 0) {
    form.categoryId = cats[0].id
  }
}
</script>

<style scoped>
.type-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.type-tab {
  flex: 1;
  padding: 0.875rem;
  background: var(--color-bg);
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  font-size: 0.9375rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.type-tab:hover {
  border-color: var(--color-border);
}

.type-tab.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.amount-input-wrapper {
  position: relative;
}

.currency-symbol {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.amount-input {
  padding-left: 2.5rem;
  font-size: 1.5rem;
  font-weight: 600;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.375rem;
  padding: 0.75rem 0.5rem;
  background: var(--color-bg);
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.category-item:hover {
  border-color: var(--color-border);
}

.category-item.selected {
  background: rgba(99, 102, 241, 0.1);
  border-color: var(--color-primary);
}

.category-icon {
  font-size: 1.5rem;
}

.category-name {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.error-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: rgba(239, 68, 68, 0.1);
  color: var(--color-danger);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
}
</style>
