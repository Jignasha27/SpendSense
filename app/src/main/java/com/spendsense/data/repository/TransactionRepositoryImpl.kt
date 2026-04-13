package com.spendsense.data.repository

import com.spendsense.data.local.dao.BudgetDao
import com.spendsense.data.local.dao.ExpenseDao
import com.spendsense.data.local.dao.IncomeDao
import com.spendsense.data.local.dao.UserDao
import com.spendsense.data.local.entity.Budget
import com.spendsense.data.local.entity.Expense
import com.spendsense.data.local.entity.Income
import com.spendsense.data.local.entity.User
import com.spendsense.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl(
    private val userDao: UserDao,
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val budgetDao: BudgetDao
) : TransactionRepository {

    override fun getUser(): Flow<User?> = userDao.getUser()

    override suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }

    override fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    override fun getTotalExpenseAmount(): Flow<Double?> = expenseDao.getTotalExpenseAmount()

    override suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
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
}
