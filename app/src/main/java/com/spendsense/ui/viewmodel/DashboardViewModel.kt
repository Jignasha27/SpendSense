package com.spendsense.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.data.local.entity.Expense
import com.spendsense.data.local.entity.Income
import com.spendsense.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    val totalIncome: StateFlow<Double> = repository.getTotalIncomeAmount()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.getTotalExpenseAmount()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val remainingBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // For recent transactions, combine both and sort
    val recentTransactions: StateFlow<List<Any>> = combine(
        repository.getAllExpenses(),
        repository.getAllIncomes()
    ) { expenses, incomes ->
        val all = mutableListOf<Any>()
        all.addAll(expenses)
        all.addAll(incomes)
        // Sort by date descending
        all.sortByDescending {
            when (it) {
                is Expense -> it.date
                is Income -> it.date
                else -> 0L
            }
        }
        all.take(5) // Just show top 5
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
