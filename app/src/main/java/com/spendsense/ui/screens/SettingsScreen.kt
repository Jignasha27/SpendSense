package com.spendsense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsense.data.local.entity.Budget
import com.spendsense.data.local.entity.User
import com.spendsense.ui.theme.BluePrimary
import com.spendsense.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val user by dashboardViewModel.user.collectAsState(initial = null)
    val currentBudget by dashboardViewModel.currentBudget.collectAsState()

    var name by remember { mutableStateOf("") }
    var incomeRange by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("INR") }
    var budgetAmount by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.let {
            name = it.name
            incomeRange = it.incomeRange
            currency = it.currency
        }
    }

    LaunchedEffect(currentBudget) {
        currentBudget?.let {
            budgetAmount = it.limitAmount.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val updatedUser = user?.copy(
                            name = name,
                            incomeRange = incomeRange,
                            currency = currency
                        ) ?: User(name = name, email = "", incomeRange = incomeRange, currency = currency)
                        
                        dashboardViewModel.updateUser(updatedUser)
                        
                        val amount = budgetAmount.toDoubleOrNull() ?: 0.0
                        val month = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                        dashboardViewModel.setBudget(Budget(month = month, limitAmount = amount))
                        
                        onBack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Text("Profile Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = incomeRange,
                onValueChange = { incomeRange = it },
                label = { Text("Monthly Income Range (e.g. 50k-70k)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Currency Preference (e.g. INR, USD)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Budget Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = budgetAmount,
                onValueChange = { budgetAmount = it },
                label = { Text("Monthly Budget Limit") },
                prefix = { Text(currency + " ") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { /* Implement Export logic here if needed */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Export Transactions as CSV")
            }
        }
    }
}
