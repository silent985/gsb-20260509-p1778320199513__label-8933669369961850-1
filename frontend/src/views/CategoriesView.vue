<template>
  <div class="categories-page">
    <div class="page-header">
      <h1 class="page-title">分类管理</h1>
      <button class="btn btn-primary" @click="showModal = true">➕ 添加分类</button>
    </div>
    
    <div class="tabs">
      <button class="tab" :class="{ active: activeTab === 'EXPENSE' }" @click="activeTab = 'EXPENSE'">支出分类</button>
      <button class="tab" :class="{ active: activeTab === 'INCOME' }" @click="activeTab = 'INCOME'">收入分类</button>
    </div>
    
    <div class="categories-grid">
      <div 
        v-for="cat in filteredCategories"
        :key="cat.id"
        class="category-card"
      >
        <div class="category-icon-wrapper" :style="{ backgroundColor: (cat.color || '#6366f1') + '20' }">
          {{ cat.icon || '📁' }}
        </div>
        <div class="category-name">{{ cat.name }}</div>
        <div class="category-actions" v-if="!cat.isSystem">
          <button class="action-btn" @click="editCategory(cat)">✏️</button>
          <button class="action-btn" @click="deleteCategoryConfirm(cat)">🗑️</button>
        </div>
        <div v-else class="system-badge">系统</div>
      </div>
    </div>
    
    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ editingCategory ? '编辑分类' : '添加分类' }}</h3>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="error" class="error-message">{{ error }}</div>
          <div class="form-group">
            <label class="form-label">分类类型</label>
            <select v-model="form.type" class="form-select" :disabled="!!editingCategory">
              <option value="EXPENSE">支出</option>
              <option value="INCOME">收入</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">分类名称</label>
            <input v-model="form.name" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">图标</label>
            <div class="icon-selector">
              <span v-for="icon in icons" :key="icon" class="icon-option" :class="{ selected: form.icon === icon }" @click="form.icon = icon">{{ icon }}</span>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">颜色</label>
            <div class="color-selector">
              <span v-for="color in colors" :key="color" class="color-option" :class="{ selected: form.color === color }" :style="{ backgroundColor: color }" @click="form.color = color"></span>
            </div>
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
      :message="`确定要删除分类 '${deletingCategory?.name || ''}' 吗？`"
      type="danger"
      confirmText="删除" 
      cancelText="取消"
      @confirm="handleDeleteConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useCategoryStore } from '../stores/category'
import ConfirmModal from '../components/ConfirmModal.vue'

const categoryStore = useCategoryStore()

const activeTab = ref('EXPENSE')
const showModal = ref(false)
const editingCategory = ref(null)
const error = ref('')
const showDeleteConfirm = ref(false)
const deletingCategory = ref(null)
const icons = ['🍜', '🚗', '🛒', '🎮', '🏠', '💊', '📚', '📱', '🎁', '💼', '🏋️', '✈️', '🎬', '💇', '🐾', '📌']
const colors = ['#ff4d4f', '#1890ff', '#fa8c16', '#722ed1', '#13c2c2', '#eb2f96', '#2f54eb', '#52c41a', '#faad14', '#8c8c8c']

const form = reactive({ name: '', type: 'EXPENSE', icon: '📁', color: '#6366f1' })

const filteredCategories = computed(() => activeTab.value === 'INCOME' ? categoryStore.incomeCategories : categoryStore.expenseCategories)

function editCategory(cat) {
  editingCategory.value = cat
  Object.assign(form, { name: cat.name, type: cat.type, icon: cat.icon || '📁', color: cat.color || '#6366f1' })
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingCategory.value = null
  error.value = ''
  Object.assign(form, { name: '', type: activeTab.value, icon: '📁', color: '#6366f1' })
}

async function handleSubmit() {
  if (!form.name.trim()) {
    error.value = '请输入分类名称'
    return
  }
  error.value = ''

  if (editingCategory.value) {
    await categoryStore.updateCategory(editingCategory.value.id, form)
  } else {
    await categoryStore.createCategory(form)
  }
  closeModal()
}

function deleteCategoryConfirm(cat) {
  deletingCategory.value = cat
  showDeleteConfirm.value = true
}

async function handleDeleteConfirm() {
  if (deletingCategory.value) {
    await categoryStore.deleteCategory(deletingCategory.value.id)
  }
  showDeleteConfirm.value = false
  deletingCategory.value = null
}

onMounted(() => {
  categoryStore.fetchCategories()
})
</script>

<style scoped>
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 1rem;
}

.category-card {
  background: white;
  border-radius: var(--radius-xl);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
  text-align: center;
  position: relative;
  transition: all var(--transition-fast);
}

.category-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.category-icon-wrapper {
  width: 56px;
  height: 56px;
  margin: 0 auto 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  border-radius: var(--radius-lg);
}

.category-name {
  font-weight: 500;
}

.category-actions {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  display: flex;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.category-card:hover .category-actions {
  opacity: 1;
}

.action-btn {
  padding: 0.25rem;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
}

.system-badge {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  padding: 0.125rem 0.375rem;
  background: var(--color-bg);
  color: var(--color-text-secondary);
  border-radius: var(--radius-full);
  font-size: 0.625rem;
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
}

.color-option {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  cursor: pointer;
  border: 2px solid transparent;
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
