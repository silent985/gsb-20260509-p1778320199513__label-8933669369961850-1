# 登出时清空用户数据

> **Workspace:** d:\code\trae\gsb\20260509\B-478-bug修复-1\TestM_2

---

## User
用户登出后重新登录另一个账号时,在数据加载完成前会短暂看到上一个用户的交易记录和账户信息。需要在登出时清空所有业务数据,保持前端状态管理和后端架构的一致性,不引入新的第三方库。

## Assistant
让我检查关键的前端文件，以便理解状态管理和认证流程。

现在我也检查一下预算的存储以及使用这些存储的视图，以了解全貌。

我现在对问题有了全面的理解。让我更新一下我的待办事项，然后着手实施。

现在我对这个 bug 有了清晰的理解。让我来分析一下：

**根本原因**：当用户登出时，`auth.js` 中的 `logout()` 函数只清除了 `token` 和 `user`，但所有其他业务 Pinia 存储（`account`、`transaction`、`category`、`statistics`）的数据都保留在内存中。当新用户登录时，这些视图在获取自己的数据之前，会短暂地渲染出上一个用户的数据。

**需要修改的关键位置**：
1. **`auth.js`** — `logout()` 函数需要重置所有业务 store
2. **`MainLayout.vue`** — `handleLogout()` 调用需要确保在导航前清除数据

Pinia 的组合式 API store 默认没有 `$reset()`。我需要为每个业务 store 添加一个 `reset()` 方法，然后从登出流程中调用它们。

我将为每个 store 添加一个 `reset()` 方法，并更新 `auth` store 的 `logout` 函数来重置所有业务 store：

现在更新 `auth.js` 的 `logout` 函数，以重置所有业务 store：

现在我还需要检查 401 响应拦截器——当由于 401 错误触发 `authStore.logout()` 时，重置也会正确发生，因为它调用了相同的 `logout` 函数。好的。

我还需检查 `BudgetsView.vue` 是否将 `budgets` 作为本地 `ref` 存储（而不是 Pinia `store`）。查看代码，`BudgetsView.vue` 在第 107 行有 `const budgets = ref([])` — 这是一个组件本地的 `ref`。由于它不是一个 Pinia `store`，它会在组件重新挂载时（用户登录并导航时发生）自动重置。因此，那里不需要更改。

现在我还需验证 `api/index.js` 中的 401 拦截器是否也遵循正确的流程——它调用 `authStore.logout()`，现在会重置所有 `store`。很好。

让我再次验证所有修改文件的最终状态：

现在让我运行诊断程序来验证代码是否正确：

所有文件零诊断错误。

修复完成。以下是问题的完整分析和修改说明：

---

## Bug 分析

**根因**：用户登出时，`auth.js` 的 `logout()` 函数仅清除了 `token` 和 `user` 两个认证相关状态，但 Pinia 中的所有业务数据 store（账户、交易、分类、统计）的数据仍然残留在内存中。当新用户登录后、新数据加载完成前，页面会短暂渲染上一个用户的残留数据。

## 修改内容

共修改 **5 个文件**，核心思路是为每个业务 store 添加 `$reset()` 方法，然后在登出时统一调用：

### 1. `frontend/src/stores/account.js`
新增 `$reset()` 方法，将 `accounts` 重置为 `[]`，`loading` 重置为 `false`。

### 2. `frontend/src/stores/transaction.js`
新增 `$reset()` 方法，将 `transactions` 重置为 `[]`，`pagination` 重置为初始值，`loading` 重置为 `false`。

### 3. `frontend/src/stores/category.js`
新增 `$reset()` 方法，将 `categories`、`incomeCategories`、`expenseCategories` 重置为 `[]`，`loading` 重置为 `false`。

### 4. `frontend/src/stores/statistics.js`
新增 `$reset()` 方法，将 `overview`、`categoryStats`、`monthlyTrend` 重置为 `null`，`loading` 重置为 `false`。

### 5. `frontend/src/stores/auth.js`
- 导入 4 个业务 store
- 在 `logout()` 函数末尾调用所有业务 store 的 `$reset()` 方法

**关键设计点**：
- 在 `logout()` 内部通过函数调用 `useXxxStore().$reset()` 而非在模块顶层调用，避免 Pinia 未初始化时的问题
- 401 拦截器（`api/index.js`）同样调用 `authStore.logout()`，因此也会自动清空业务数据
- `BudgetsView.vue` 中的 `budgets` 是组件本地 ref，路由切换后组件卸载会自动重置，无需额外处理

