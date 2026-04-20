package com.spendsense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsense.ui.theme.BluePrimary
import com.spendsense.ui.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsState(initial = emptyList())
    var selectedRange by remember { mutableStateOf("Last 30 Days") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Export Logic */ }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = BluePrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BluePrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = BluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedRange, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = { /* Range Picker */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = BluePrimary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Category Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            val categorySummary = expenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categorySummary) { (category, amount) ->
                    ReportRow(category, amount)
                }
            }
        }
    }
}

@Composable
fun ReportRow(category: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(category, fontWeight = FontWeight.Medium)
        Text("₹${String.format("%.2f", amount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
    }
}
