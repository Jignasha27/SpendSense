package com.spendsense.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.data.local.entity.Expense
import com.spendsense.data.local.entity.Income
import com.spendsense.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    fun addExpense(amount: Double, category: String, date: Long, paymentMethod: String, note: String) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = category,
                    date = date,
                    paymentMethod = paymentMethod,
                    note = note
                )
            )
        }
    }

    fun addIncome(amount: Double, date: Long, source: String, note: String) {
        viewModelScope.launch {
            repository.insertIncome(
                Income(
                    amount = amount,
                    date = date,
                    source = source,
                    note = note
                )
            )
        }
    }
}
