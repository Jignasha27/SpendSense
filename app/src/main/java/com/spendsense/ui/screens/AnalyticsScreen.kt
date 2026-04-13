package com.spendsense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsense.ui.theme.BluePrimary
import com.spendsense.ui.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val breakdown by viewModel.categoryBreakdown.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()
    val isGeneratingInsights by viewModel.isGeneratingInsights.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Category Breakdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            // To incorporate Vico Charts here requires additional extensive setup natively, 
            // but for simplicity we show a list representation of the breakdown. 
            // (In a full production this is where the Vico PieChart component would sit).
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(breakdown.entries.toList()) { entry ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.key)
                            Text("$${String.format("%.2f", entry.value)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // AI Insights Section
            Text("Gemini AI Insights ✨", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = BluePrimary)
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    if (isGeneratingInsights) {
                        CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
                    } else if (aiInsights != null) {
                        Text(aiInsights ?: "")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.generateInsights() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Regenerate")
                        }
                    } else {
                        Button(onClick = { viewModel.generateInsights() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Analyze My Spending")
                        }
                        Text("Powered by Google Gemini", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
