package com.spendsense.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.data.local.entity.Expense
import com.spendsense.domain.repository.TransactionRepository
import com.spendsense.domain.service.GeminiInsightService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val geminiService: GeminiInsightService
) : ViewModel() {

    val allExpenses: StateFlow<List<Expense>> = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryBreakdown: StateFlow<Map<String, Double>> = allExpenses.map { expenses ->
        expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _aiInsights = MutableStateFlow<String?>(null)
    val aiInsights: StateFlow<String?> = _aiInsights.asStateFlow()

    private val _isGeneratingInsights = MutableStateFlow(false)
    val isGeneratingInsights: StateFlow<Boolean> = _isGeneratingInsights.asStateFlow()

    fun generateInsights() {
        viewModelScope.launch {
            _isGeneratingInsights.value = true
            val currentExpenses = allExpenses.value
            val currentIncome = repository.getTotalIncomeAmount().firstOrNull() ?: 0.0
            
            geminiService.generateFinancialAdvice(currentExpenses, currentIncome)
                .catch { _aiInsights.value = it.localizedMessage }
                .collect { result ->
                    _aiInsights.value = result
                }
            _isGeneratingInsights.value = false
        }
    }
}
