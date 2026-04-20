package com.spendsense.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.data.local.entity.*
import com.spendsense.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

    val user: StateFlow<User?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentBudget: StateFlow<Budget?> = repository.getBudgetForMonth(currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalIncome: StateFlow<Double> = repository.getTotalIncomeAmount()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.getTotalExpenseAmount()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val remainingBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dailyLimitInfo: StateFlow<DailyLimitInfo> = combine(currentBudget, totalExpense) { budget, expense ->
        val calendar = Calendar.getInstance()
        val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysRemaining = (totalDaysInMonth - currentDay + 1).coerceAtLeast(1)
        
        val budgetAmount = budget?.limitAmount ?: 0.0
        val remainingBudget = (budgetAmount - expense).coerceAtLeast(0.0)
        
        DailyLimitInfo(
            dailyLimit = if (budgetAmount > 0) remainingBudget / daysRemaining else 0.0,
            daysRemaining = daysRemaining,
            isOverBudget = expense > budgetAmount && budgetAmount > 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyLimitInfo(0.0, 1, false))

    val recentTransactions: StateFlow<List<Any>> = combine(
        repository.getAllExpenses(),
        repository.getAllIncomes()
    ) { expenses, incomes ->
        val all = mutableListOf<Any>()
        all.addAll(expenses)
        all.addAll(incomes)
        all.sortByDescending {
            when (it) {
                is Expense -> it.date
                is Income -> it.date
                else -> 0L
            }
        }
        all.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val moneyLeaks: StateFlow<List<MoneyLeak>> = repository.getAllExpenses().map { expenses ->
        expenses.filter { it.amount < 500 }
            .groupBy { it.category }
            .filter { it.value.size >= 3 }
            .map { (category, list) ->
                MoneyLeak(category, list.sumOf { it.amount }, list.size)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val healthScore: StateFlow<Int> = combine(totalIncome, totalExpense, currentBudget) { income, expense, budget ->
        if (income == 0.0) return@combine 50
        var score = 100
        val savingsRatio = (income - expense) / income
        if (savingsRatio < 0.2) score -= 20
        if (savingsRatio < 0.0) score -= 30
        budget?.let {
            val usage = expense / it.limitAmount
            if (usage > 1.0) score -= 25
            else if (usage > 0.8) score -= 10
        }
        score.coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 70)

    val mostSpentCategory: StateFlow<String> = repository.getAllExpenses().map { expenses ->
        expenses.groupBy { it.category }
            .maxByOrNull { entry -> entry.value.sumOf { it.amount } }?.key ?: "N/A"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "N/A")

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.saveUser(user)
        }
    }

    fun setBudget(budget: Budget) {
        viewModelScope.launch {
            repository.insertBudget(budget)
        }
    }
}

data class DailyLimitInfo(
    val dailyLimit: Double,
    val daysRemaining: Int,
    val isOverBudget: Boolean
)

data class MoneyLeak(
    val category: String,
    val totalAmount: Double,
    val frequency: Int
)
