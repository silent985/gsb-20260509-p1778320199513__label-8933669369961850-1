<template>
  <div class="transactions-page">
    <div class="page-header">
      <h1 class="page-title">交易记录</h1>
      <button class="btn btn-primary" @click="showModal = true">➕ 记一笔</button>
    </div>
    
    <div class="card">
      <div class="card-body" style="padding: 0;">
        <div v-if="loading" class="loading-overlay">
          <div class="loading-spinner"></div>
        </div>
        
        <div v-if="transactions.length === 0 && !loading" class="empty-state">
          <div class="empty-state-icon">📝</div>
          <div class="empty-state-title">暂无交易记录</div>
          <div class="empty-state-text">点击上方按钮添加第一笔记录</div>
        </div>
        
        <div v-else class="transaction-list">
          <div 
            v-for="tx in transactions"
            :key="tx.id"
            class="transaction-item"
            @click="editTransaction(tx)"
          >
            <div class="transaction-icon" :style="{ backgroundColor: tx.categoryColor + '20' || 'rgba(99, 102, 241, 0.1)' }">
              {{ tx.categoryIcon || '💳' }}
            </div>
            <div class="transaction-info">
              <div class="transaction-category">{{ tx.categoryName }}</div>
              <div class="transaction-note">
                {{ tx.note || tx.accountName }} · {{ formatDate(tx.transactionDate) }}
              </div>
            </div>
            <div class="transaction-right">
              <div class="transaction-amount" :class="tx.type.toLowerCase()">
                {{ tx.type === 'INCOME' ? '+' : '-' }}¥{{ formatNumber(tx.amount) }}
              </div>
              <button class="btn-delete" @click.stop="confirmDelete(tx)">🗑️</button>
            </div>
          </div>
        </div>
        
        <!-- Pagination -->
        <div v-if="pagination.totalPages > 1" class="pagination">
          <button 
            class="btn btn-outline btn-sm"
            :disabled="pagination.page === 0"
            @click="loadPage(pagination.page - 1)"
          >
            上一页
          </button>
          <span class="page-info">
            第 {{ pagination.page + 1 }} / {{ pagination.totalPages }} 页
          </span>
          <button 
            class="btn btn-outline btn-sm"
            :disabled="pagination.page >= pagination.totalPages - 1"
            @click="loadPage(pagination.page + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
    
    <TransactionModal 
      v-if="showModal"
      :transaction="editingTransaction"
      @close="closeModal"
      @saved="onSaved"
    />
    
    <!-- Delete Confirm Modal -->
    <ConfirmModal
      v-model:visible="showDeleteConfirm"
      title="确认删除"
      :message="`确定要删除这笔${deletingTransaction?.type === 'INCOME' ? '收入' : '支出'}记录吗？`"
      type="danger"
      confirmText="删除"
      cancelText="取消"
      @confirm="handleDeleteConfirm"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTransactionStore } from '../stores/transaction'
import TransactionModal from '../components/TransactionModal.vue'
import ConfirmModal from '../components/ConfirmModal.vue'
import dayjs from 'dayjs'

const transactionStore = useTransactionStore()

const showModal = ref(false)
const editingTransaction = ref(null)
const showDeleteConfirm = ref(false)
const deletingTransaction = ref(null)
const transactions = computed(() => transactionStore.transactions)
const pagination = computed(() => transactionStore.pagination)
const loading = computed(() => transactionStore.loading)

function formatNumber(num) {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatDate(date) {
  return dayjs(date).format('MM-DD')
}

function editTransaction(tx) {
  editingTransaction.value = tx
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingTransaction.value = null
}

function onSaved() {
  closeModal()
}

function confirmDelete(tx) {
  deletingTransaction.value = tx
  showDeleteConfirm.value = true
}

async function handleDeleteConfirm() {
  if (deletingTransaction.value) {
    await transactionStore.deleteTransaction(deletingTransaction.value.id)
  }
  showDeleteConfirm.value = false
  deletingTransaction.value = null
}

function loadPage(page) {
  transactionStore.fetchTransactions(page, 20)
}

onMounted(() => {
  transactionStore.fetchTransactions(0, 20)
})
</script>

<style scoped>
.transaction-list {
  max-height: calc(100vh - 280px);
  overflow-y: auto;
}

.transaction-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.btn-delete {
  padding: 0.375rem;
  background: none;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.transaction-item:hover .btn-delete {
  opacity: 1;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid var(--color-border);
}

.page-info {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
</style>
