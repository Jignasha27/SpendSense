package com.spendsense.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.spendsense.ui.theme.BluePrimary
import com.spendsense.ui.viewmodel.AnalyticsViewModel
import java.text.DateFormatSymbols

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val remainingBalance by viewModel.remainingBalance.collectAsState()
    val mostSpentCategory by viewModel.mostSpentCategory.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val monthlyTrend by viewModel.monthlyTrend.collectAsState()
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Summary Section
            item {
                SummaryCards(totalIncome, totalExpense, remainingBalance, mostSpentCategory)
            }

            // Pie Chart - Category Breakdown
            item {
                Text(
                    "Category Spending",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryPieChartCard(categoryBreakdown)
            }

            // Bar Chart - Monthly Trend
            item {
                Text(
                    "Monthly Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                MonthlyTrendCard(monthlyTrend)
            }

            // Gemini AI Insights
            item {
                Text(
                    "Smart Insights ✨",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                InsightsCard(aiInsights, isGeneratingInsights) {
                    viewModel.generateInsights()
                }
            }
        }
    }
}

@Composable
fun SummaryCards(income: Double, expense: Double, balance: Double, topCategory: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard(
                title = "Income",
                amount = income,
                icon = Icons.Default.ArrowUpward,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Expense",
                amount = expense,
                icon = Icons.Default.ArrowDownward,
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard(
                title = "Balance",
                amount = balance,
                icon = Icons.Default.AccountBalanceWallet,
                color = BluePrimary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Top Category",
                category = topCategory ?: "None",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double? = null,
    category: String? = null,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(
                text = if (amount != null) "₹${String.format("%.0f", amount)}" else category ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CategoryPieChartCard(breakdown: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (breakdown.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No data available", color = Color.Gray)
            }
        } else {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PieChart(
                    data = breakdown,
                    modifier = Modifier.size(150.dp).weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val colors = listOf(Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFFFFC107), Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFF795548))
                    breakdown.keys.toList().forEachIndexed { index, category ->
                        if (index < colors.size) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(colors[index], RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(data: Map<String, Double>, modifier: Modifier = Modifier) {
    val total = data.values.sum()
    val colors = listOf(Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFFFFC107), Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFF795548))
    
    Canvas(modifier = modifier) {
        var startAngle = -90f
        data.values.forEachIndexed { index, value ->
            val sweepAngle = (value / total).toFloat() * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width - 40.dp.toPx(), size.height - 40.dp.toPx()),
                topLeft = Offset(20.dp.toPx(), 20.dp.toPx())
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun MonthlyTrendCard(trend: Map<Int, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (trend.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No data available", color = Color.Gray)
                }
            } else {
                val chartModel = entryModelOf(*trend.values.map { it.toFloat() }.toTypedArray())
                Chart(
                    chart = columnChart(),
                    model = chartModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            val monthIndex = trend.keys.toList().getOrNull(value.toInt()) ?: 0
                            DateFormatSymbols().shortMonths[monthIndex]
                        }
                    ),
                    modifier = Modifier.height(200.dp)
                )
            }
        }
    }
}

@Composable
fun InsightsCard(insights: String?, isLoading: Boolean, onAnalyze: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else if (insights != null) {
                Text(insights, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onAnalyze, modifier = Modifier.fillMaxWidth()) {
                    Text("Regenerate Insights")
                }
            } else {
                Text(
                    "Get personalized financial advice based on your spending patterns.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAnalyze,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Analyze My Spending")
                }
            }
        }
    }
}
