package com.spendsense.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spendsense.data.local.dao.BudgetDao
import com.spendsense.data.local.dao.ExpenseDao
import com.spendsense.data.local.dao.IncomeDao
import com.spendsense.data.local.dao.UserDao
import com.spendsense.data.local.entity.Budget
import com.spendsense.data.local.entity.Expense
import com.spendsense.data.local.entity.Income
import com.spendsense.data.local.entity.User

@Database(
    entities = [User::class, Expense::class, Income::class, Budget::class],
    version = 1,
    exportSchema = false
)
abstract class SpendSenseDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val expenseDao: ExpenseDao
    abstract val incomeDao: IncomeDao
    abstract val budgetDao: BudgetDao
}
