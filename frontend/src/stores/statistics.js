import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useStatisticsStore = defineStore('statistics', () => {
  const overview = ref(null)
  const categoryStats = ref(null)
  const monthlyTrend = ref(null)
  const loading = ref(false)

  async function fetchOverview() {
    loading.value = true
    try {
      const response = await api.get('/statistics/overview')
      overview.value = response.data.data
      return overview.value
    } finally {
      loading.value = false
    }
  }

  async function fetchCategoryStats(year, month) {
    const response = await api.get(`/statistics/category?year=${year}&month=${month}`)
    categoryStats.value = response.data.data
    return categoryStats.value
  }

  async function fetchMonthlyTrend(months = 12) {
    const response = await api.get(`/statistics/trend/monthly?months=${months}`)
    monthlyTrend.value = response.data.data.monthlyTrend
    return monthlyTrend.value
  }

  async function fetchDailyTrend(days = 30) {
    const response = await api.get(`/statistics/trend/daily?days=${days}`)
    return response.data.data.dailyTrend
  }

  function clear() {
    overview.value = null
    categoryStats.value = null
    monthlyTrend.value = null
    loading.value = false
  }

  return {
    overview,
    categoryStats,
    monthlyTrend,
    loading,
    fetchOverview,
    fetchCategoryStats,
    fetchMonthlyTrend,
    fetchDailyTrend,
    clear
  }
})
