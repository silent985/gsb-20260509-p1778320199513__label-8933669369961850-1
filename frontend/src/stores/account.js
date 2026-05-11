import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useAccountStore = defineStore('account', () => {
  const accounts = ref([])
  const loading = ref(false)

  async function fetchAccounts() {
    loading.value = true
    try {
      const response = await api.get('/accounts')
      accounts.value = response.data.data
      return accounts.value
    } finally {
      loading.value = false
    }
  }

  async function createAccount(data) {
    const response = await api.post('/accounts', data)
    await fetchAccounts()
    return response.data.data
  }

  async function updateAccount(id, data) {
    const response = await api.put(`/accounts/${id}`, data)
    await fetchAccounts()
    return response.data.data
  }

  async function deleteAccount(id) {
    await api.delete(`/accounts/${id}`)
    await fetchAccounts()
  }

  function clear() {
    accounts.value = []
    loading.value = false
  }

  return {
    accounts,
    loading,
    fetchAccounts,
    createAccount,
    updateAccount,
    deleteAccount,
    clear
  }
})
