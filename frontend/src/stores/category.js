import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref([])
  const incomeCategories = ref([])
  const expenseCategories = ref([])
  const loading = ref(false)

  async function fetchCategories() {
    loading.value = true
    try {
      const response = await api.get('/categories')
      categories.value = response.data.data
      
      incomeCategories.value = categories.value.filter(c => c.type === 'INCOME')
      expenseCategories.value = categories.value.filter(c => c.type === 'EXPENSE')
      
      return categories.value
    } finally {
      loading.value = false
    }
  }

  async function fetchCategoriesByType(type) {
    const response = await api.get(`/categories?type=${type}`)
    return response.data.data
  }

  async function createCategory(data) {
    const response = await api.post('/categories', data)
    await fetchCategories()
    return response.data.data
  }

  async function updateCategory(id, data) {
    const response = await api.put(`/categories/${id}`, data)
    await fetchCategories()
    return response.data.data
  }

  async function deleteCategory(id) {
    await api.delete(`/categories/${id}`)
    await fetchCategories()
  }

  function reset() {
    categories.value = []
    incomeCategories.value = []
    expenseCategories.value = []
    loading.value = false
  }

  return {
    categories,
    incomeCategories,
    expenseCategories,
    loading,
    fetchCategories,
    fetchCategoriesByType,
    createCategory,
    updateCategory,
    deleteCategory,
    reset
  }
})
