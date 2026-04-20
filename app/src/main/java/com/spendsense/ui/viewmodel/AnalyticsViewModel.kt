package com.spendsense.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.data.local.entity.Expense
import com.spendsense.domain.repository.TransactionRepository
import com.spendsense.domain.service.GeminiInsightService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val geminiService: GeminiInsightService
) : ViewModel() {

    val allExpenses: StateFlow<List<Expense>> = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense: StateFlow<Double> = repository.getTotalExpenseAmount()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = repository.getTotalIncomeAmount()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val remainingBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categoryBreakdown: StateFlow<Map<String, Double>> = allExpenses.map { expenses ->
        expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val mostSpentCategory: StateFlow<String?> = categoryBreakdown.map { breakdown ->
        breakdown.maxByOrNull { it.value }?.key
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val monthlyTrend: StateFlow<Map<Int, Double>> = allExpenses.map { expenses ->
        val calendar = Calendar.getInstance()
        val trend = mutableMapOf<Int, Double>()
        // Initialize last 6 months with 0.0
        val currentMonth = calendar.get(Calendar.MONTH)
        for (i in 0..5) {
            val m = (currentMonth - i + 12) % 12
            trend[m] = 0.0
        }
        
        expenses.forEach {
            calendar.timeInMillis = it.date
            val m = calendar.get(Calendar.MONTH)
            if (trend.containsKey(m)) {
                trend[m] = (trend[m] ?: 0.0) + it.amount
            }
        }
        trend.toSortedMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _aiInsights = MutableStateFlow<String?>(null)
    val aiInsights: StateFlow<String?> = _aiInsights.asStateFlow()

    private val _isGeneratingInsights = MutableStateFlow(false)
    val isGeneratingInsights: StateFlow<Boolean> = _isGeneratingInsights.asStateFlow()

    fun generateInsights() {
        viewModelScope.launch {
            _isGeneratingInsights.value = true
            val currentExpenses = allExpenses.value
            val currentIncome = totalIncome.value
            
            geminiService.generateFinancialAdvice(currentExpenses, currentIncome)
                .catch { _aiInsights.value = "Error: ${it.localizedMessage}" }
                .collect { result ->
                    _aiInsights.value = result
                }
            _isGeneratingInsights.value = false
        }
    }
}
