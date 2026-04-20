package com.spendsense.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.data.local.entity.Expense
import com.spendsense.data.local.entity.Goal
import com.spendsense.data.local.entity.Income
import com.spendsense.domain.repository.TransactionRepository
import com.spendsense.domain.service.ReceiptData
import com.spendsense.domain.service.ReceiptScannerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val scannerService: ReceiptScannerService
) : ViewModel() {

    val expenses = repository.getAllExpenses()
    val incomes = repository.getAllIncomes()
    val goals = repository.getAllGoals()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scannedData = MutableStateFlow<ReceiptData?>(null)
    val scannedData = _scannedData.asStateFlow()

    fun detectCategory(input: String): String {
        val lowerInput = input.lowercase()
        return when {
            lowerInput.contains("swiggy") || lowerInput.contains("zomato") || lowerInput.contains("food") || lowerInput.contains("restaurant") -> "Food"
            lowerInput.contains("uber") || lowerInput.contains("ola") || lowerInput.contains("petrol") || lowerInput.contains("fuel") || lowerInput.contains("metro") -> "Travel"
            lowerInput.contains("amazon") || lowerInput.contains("flipkart") || lowerInput.contains("shop") || lowerInput.contains("myntra") -> "Shopping"
            lowerInput.contains("rent") || lowerInput.contains("electricity") || lowerInput.contains("bill") || lowerInput.contains("water") -> "Rent"
            lowerInput.contains("hospital") || lowerInput.contains("doctor") || lowerInput.contains("medicine") || lowerInput.contains("pharmacy") -> "Health"
            else -> "Others"
        }
    }

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

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
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

    fun addGoal(name: String, targetAmount: Double, savedAmount: Double = 0.0) {
        viewModelScope.launch {
            repository.insertGoal(
                Goal(
                    name = name,
                    targetAmount = targetAmount,
                    savedAmount = savedAmount
                )
            )
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun scanReceipt(uri: Uri) {
        viewModelScope.launch {
            _isScanning.value = true
            val data = scannerService.scanReceipt(uri)
            _scannedData.value = data
            _isScanning.value = false
        }
    }
    
    fun clearScannedData() {
        _scannedData.value = null
    }
}
