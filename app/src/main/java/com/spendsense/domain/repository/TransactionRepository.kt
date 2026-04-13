package com.spendsense.domain.repository

import com.spendsense.data.local.entity.Budget
import com.spendsense.data.local.entity.Expense
import com.spendsense.data.local.entity.Income
import com.spendsense.data.local.entity.User
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // User
    fun getUser(): Flow<User?>
    suspend fun saveUser(user: User)

    // Expense
    fun getAllExpenses(): Flow<List<Expense>>
    fun getTotalExpenseAmount(): Flow<Double?>
    suspend fun insertExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)

    // Income
    fun getAllIncomes(): Flow<List<Income>>
    fun getTotalIncomeAmount(): Flow<Double?>
    suspend fun insertIncome(income: Income)
    suspend fun deleteIncome(income: Income)

    // Budget
    fun getBudgetForMonth(month: String): Flow<Budget?>
    suspend fun insertBudget(budget: Budget)
}
