package com.spendsense.data.repository

import com.spendsense.data.local.dao.*
import com.spendsense.data.local.entity.*
import com.spendsense.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject // Added import

// Added @Inject constructor to allow Hilt to provide this dependency
class TransactionRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao
) : TransactionRepository {

    override fun getUser(): Flow<User?> = userDao.getUser()

    override suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }

    override suspend fun deleteUser() {
        userDao.deleteUser()
    }

    override fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    override fun getTotalExpenseAmount(): Flow<Double?> = expenseDao.getTotalExpenseAmount()

    override suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    override fun getAllIncomes(): Flow<List<Income>> = incomeDao.getAllIncomes()

    override fun getTotalIncomeAmount(): Flow<Double?> = incomeDao.getTotalIncomeAmount()

    override suspend fun insertIncome(income: Income) {
        incomeDao.insertIncome(income)
    }

    override suspend fun deleteIncome(income: Income) {
        incomeDao.deleteIncome(income)
    }

    override fun getBudgetForMonth(month: String): Flow<Budget?> = budgetDao.getBudgetForMonth(month)

    override suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    override fun getAllGoals(): Flow<List<Goal>> = goalDao.getAllGoals()

    override suspend fun insertGoal(goal: Goal) {
        goalDao.insertGoal(goal)
    }

    override suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }
}