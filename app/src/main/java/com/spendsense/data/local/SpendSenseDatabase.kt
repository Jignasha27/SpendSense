package com.spendsense.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spendsense.data.local.dao.*
import com.spendsense.data.local.entity.*

@Database(
    entities = [User::class, Expense::class, Income::class, Budget::class, Goal::class],
    version = 2,
    exportSchema = false
)
abstract class SpendSenseDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val expenseDao: ExpenseDao
    abstract val incomeDao: IncomeDao
    abstract val budgetDao: BudgetDao
    abstract val goalDao: GoalDao
}
