package com.spendsense.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsense.ui.theme.BluePrimary
import com.spendsense.ui.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var isExpense by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var categoryOrSource by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    val isScanning by viewModel.isScanning.collectAsState()
    val scannedData by viewModel.scannedData.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.scanReceipt(it) }
    }

    LaunchedEffect(scannedData) {
        scannedData?.let { data ->
            if (data.amount != null) {
                amount = data.amount.toString()
            }
            viewModel.clearScannedData()
        }
    }

    // Auto-detect category for expenses based on note
    LaunchedEffect(note, isExpense) {
        if (isExpense && note.isNotBlank() && categoryOrSource.isBlank()) {
            categoryOrSource = viewModel.detectCategory(note)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isExpense) "Add Expense" else "Add Income", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                FilterChip(
                    selected = isExpense,
                    onClick = { isExpense = true },
                    label = { Text("Expense") }
                )
                Spacer(modifier = Modifier.width(16.dp))
                FilterChip(
                    selected = !isExpense,
                    onClick = { isExpense = false },
                    label = { Text("Income") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (isExpense) {
                OutlinedCard(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scanning Receipt...")
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Receipt with Smart OCR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note / Description (Try 'Lunch' or 'Uber')") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = categoryOrSource,
                onValueChange = { categoryOrSource = it },
                label = { Text(if (isExpense) "Category (Auto-detected from note)" else "Source") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    val date = System.currentTimeMillis()
                    if (isExpense) {
                        viewModel.addExpense(amountVal, categoryOrSource.ifBlank { "Others" }, date, "Cash", note)
                    } else {
                        viewModel.addIncome(amountVal, date, categoryOrSource.ifBlank { "Others" }, note)
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Save Transaction")
            }
        }
    }
}
