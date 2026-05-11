<template>
  <div class="accounts-page">
    <div class="page-header">
      <h1 class="page-title">账户管理</h1>
      <button class="btn btn-primary" @click="showModal = true">➕ 添加账户</button>
    </div>
    
    <div class="accounts-grid">
      <div 
        v-for="account in accounts"
        :key="account.id"
        class="account-card"
        :style="{ borderTopColor: account.color || '#6366f1' }"
      >
        <div class="account-header">
          <div class="account-icon-wrapper" :style="{ backgroundColor: (account.color || '#6366f1') + '15' }">
            {{ account.icon || '💳' }}
          </div>
          <div class="account-actions">
            <button class="action-btn" @click="editAccount(account)">✏️</button>
            <button class="action-btn" @click="deleteAccountConfirm(account)" v-if="!account.isDefault">🗑️</button>
          </div>
        </div>
        <div class="account-name">{{ account.name }}</div>
        <div class="account-type-badge">{{ getTypeName(account.type) }}</div>
        <div class="account-balance" :class="account.balance >= 0 ? 'positive' : 'negative'">
          ¥{{ formatNumber(account.balance) }}
        </div>
        <div v-if="account.isDefault" class="default-badge">默认账户</div>
      </div>
    </div>
    
    <!-- Account Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ editingAccount ? '编辑账户' : '添加账户' }}</h3>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div v-if="error" class="error-message">{{ error }}</div>
            <div class="form-group">
              <label class="form-label">账户名称</label>
              <input v-model="form.name" type="text" class="form-input" required />
            </div>
            <div class="form-group">
              <label class="form-label">账户类型</label>
              <select v-model="form.type" class="form-select" required>
                <option value="CASH">现金</option>
                <option value="BANK">银行卡</option>
                <option value="CREDIT_CARD">信用卡</option>
                <option value="ALIPAY">支付宝</option>
                <option value="WECHAT">微信</option>
                <option value="OTHER">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">图标</label>
              <div class="icon-selector">
                <span 
                  v-for="icon in icons" 
                  :key="icon"
                  class="icon-option"
                  :class="{ selected: form.icon === icon }"
                  @click="form.icon = icon"
                >{{ icon }}</span>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">颜色</label>
              <div class="color-selector">
                <span 
                  v-for="color in colors" 
                  :key="color"
                  class="color-option"
                  :class="{ selected: form.color === color }"
                  :style="{ backgroundColor: color }"
                  @click="form.color = color"
                ></span>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">备注</label>
              <input v-model="form.note" type="text" class="form-input" />
            </div>
          </form>
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
      :message="`确定要删除账户 '${deletingAccount?.name || ''}' 吗？删除后无法恢复。`"
      type="danger"
      confirmText="删除"
      cancelText="取消"
      @confirm="handleDeleteConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAccountStore } from '../stores/account'
import ConfirmModal from '../components/ConfirmModal.vue'

const accountStore = useAccountStore()

const showModal = ref(false)
const editingAccount = ref(null)
const error = ref('')
const showDeleteConfirm = ref(false)
const deletingAccount = ref(null)

const accounts = computed(() => accountStore.accounts)

const icons = ['💵', '💳', '🏦', '💰', '📱', '💎', '🪙', '👛']
const colors = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#3b82f6', '#8b5cf6', '#ec4899', '#14b8a6']

const form = reactive({
  name: '',
  type: 'CASH',
  icon: '💵',
  color: '#6366f1',
  note: ''
})

function getTypeName(type) {
  const types = { CASH: '现金', BANK: '银行卡', CREDIT_CARD: '信用卡', ALIPAY: '支付宝', WECHAT: '微信', OTHER: '其他' }
  return types[type] || type
}

function formatNumber(num) {
  return Number(num || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function editAccount(account) {
  editingAccount.value = account
  Object.assign(form, { name: account.name, type: account.type, icon: account.icon || '💵', color: account.color || '#6366f1', note: account.note || '' })
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingAccount.value = null
  error.value = ''
  Object.assign(form, { name: '', type: 'CASH', icon: '💵', color: '#6366f1', note: '' })
}

async function handleSubmit() {
  if (!form.name.trim()) {
    error.value = '请输入账户名称'
    return
  }
  error.value = ''
  
  if (editingAccount.value) {
    await accountStore.updateAccount(editingAccount.value.id, form)
  } else {
    await accountStore.createAccount(form)
  }
  closeModal()
}

function deleteAccountConfirm(account) {
  deletingAccount.value = account
  showDeleteConfirm.value = true
}

async function handleDeleteConfirm() {
  if (deletingAccount.value) {
    await accountStore.deleteAccount(deletingAccount.value.id)
  }
  showDeleteConfirm.value = false
  deletingAccount.value = null
}

onMounted(() => {
  accountStore.fetchAccounts()
})
</script>

<style scoped>
.accounts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.account-card {
  background: white;
  border-radius: var(--radius-xl);
  padding: 1.5rem;
  box-shadow: var(--shadow-md);
  border-top: 4px solid;
  position: relative;
}

.account-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.account-icon-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  border-radius: var(--radius-lg);
}

.account-actions {
  display: flex;
  gap: 0.25rem;
}

.action-btn {
  padding: 0.375rem;
  background: none;
  border: none;
  cursor: pointer;
  opacity: 0.5;
  transition: opacity var(--transition-fast);
}

.action-btn:hover { opacity: 1; }

.account-name {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.account-type-badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  background: var(--color-bg);
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  margin-bottom: 1rem;
}

.account-balance {
  font-size: 1.5rem;
  font-weight: 700;
}

.account-balance.positive { color: var(--color-income); }
.account-balance.negative { color: var(--color-expense); }

.default-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.25rem 0.5rem;
  background: var(--gradient-primary);
  color: white;
  border-radius: var(--radius-full);
  font-size: 0.6875rem;
  font-weight: 500;
}

.icon-selector, .color-selector {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.icon-option {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  background: var(--color-bg);
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.icon-option.selected {
  border-color: var(--color-primary);
  background: rgba(99, 102, 241, 0.1);
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  cursor: pointer;
  border: 3px solid transparent;
}

.color-option.selected {
  border-color: var(--color-text);
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
