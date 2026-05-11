import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useTransactionStore = defineStore('transaction', () => {
  const transactions = ref([])
  const pagination = ref({ page: 0, size: 20, totalPages: 0, totalElements: 0 })
  const loading = ref(false)

  async function fetchTransactions(page = 0, size = 20) {
    loading.value = true
    try {
      const response = await api.get(`/transactions?page=${page}&size=${size}`)
      const data = response.data.data
      transactions.value = data.content || []
      pagination.value = {
        page: data.number || 0,
        size: data.size || 20,
        totalPages: data.totalPages || 0,
        totalElements: data.totalElements || 0
      }
      return transactions.value
    } finally {
      loading.value = false
    }
  }

  async function fetchTransactionsByDateRange(startDate, endDate) {
    const response = await api.get(`/transactions/range?startDate=${startDate}&endDate=${endDate}`)
    return response.data.data
  }

  async function createTransaction(data) {
    const response = await api.post('/transactions', data)
    await fetchTransactions(pagination.value.page, pagination.value.size)
    return response.data.data
  }

  async function updateTransaction(id, data) {
    const response = await api.put(`/transactions/${id}`, data)
    await fetchTransactions(pagination.value.page, pagination.value.size)
    return response.data.data
  }

  async function deleteTransaction(id) {
    await api.delete(`/transactions/${id}`)
    await fetchTransactions(pagination.value.page, pagination.value.size)
  }

  return {
    transactions,
    pagination,
    loading,
    fetchTransactions,
    fetchTransactionsByDateRange,
    createTransaction,
    updateTransaction,
    deleteTransaction
  }
})
