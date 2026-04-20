package com.spendsense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsense.ui.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsState(initial = emptyList())
    val incomes by viewModel.incomes.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    val allTransactions = (expenses.map { it.toUiModel() } + incomes.map { it.toUiModel() })
        .sortedByDescending { it.date }
        .filter { it.note.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by note or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allTransactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

data class TransactionUiModel(
    val amount: Double,
    val category: String,
    val date: Long,
    val note: String,
    val isExpense: Boolean
)

fun com.spendsense.data.local.entity.Expense.toUiModel() = TransactionUiModel(amount, category, date, note, true)
fun com.spendsense.data.local.entity.Income.toUiModel() = TransactionUiModel(amount, source, date, note, false)

@Composable
fun TransactionItem(transaction: TransactionUiModel) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = transaction.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = transaction.note, style = MaterialTheme.typography.bodySmall)
                Text(text = sdf.format(Date(transaction.date)), style = MaterialTheme.typography.labelSmall)
            }
            Text(
                text = "${if (transaction.isExpense) "-" else "+"} ₹${transaction.amount}",
                style = MaterialTheme.typography.titleLarge,
                color = if (transaction.isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
