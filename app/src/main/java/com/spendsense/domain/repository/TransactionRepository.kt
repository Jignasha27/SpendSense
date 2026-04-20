package com.spendsense.domain.repository

import com.spendsense.data.local.entity.*
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // User
    fun getUser(): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun deleteUser()

    // Expense
    fun getAllExpenses(): Flow<List<Expense>>
    fun getTotalExpenseAmount(): Flow<Double?>
    suspend fun insertExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)

    // Income
    fun getAllIncomes(): Flow<List<Income>>
    fun getTotalIncomeAmount(): Flow<Double?>
    suspend fun insertIncome(income: Income)
    suspend fun deleteIncome(income: Income)

    // Budget
    fun getBudgetForMonth(month: String): Flow<Budget?>
    suspend fun insertBudget(budget: Budget)

    // Goals
    fun getAllGoals(): Flow<List<Goal>>
    suspend fun insertGoal(goal: Goal)
    suspend fun deleteGoal(goal: Goal)
}