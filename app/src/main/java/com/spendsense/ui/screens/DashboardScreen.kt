package com.spendsense.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spendsense.data.local.entity.Expense
import com.spendsense.data.local.entity.Income
import com.spendsense.ui.theme.BluePrimary
import com.spendsense.ui.theme.GreenSuccess
import com.spendsense.ui.theme.RedError
import com.spendsense.ui.viewmodel.AuthState
import com.spendsense.ui.viewmodel.AuthViewModel
import com.spendsense.ui.viewmodel.DashboardViewModel
import com.spendsense.ui.viewmodel.DailyLimitInfo
import com.spendsense.ui.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    transactionViewModel: TransactionViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val remainingBalance by viewModel.remainingBalance.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val dailyLimitInfo by viewModel.dailyLimitInfo.collectAsState()
    val currentBudget by viewModel.currentBudget.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    
    var showQuickAdd by remember { mutableStateOf(false) }

    val userName = (authState as? AuthState.Authenticated)?.user?.name ?: "User"
    val userPhoto = (authState as? AuthState.Authenticated)?.user?.profilePic

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            if (!userPhoto.isNullOrEmpty()) {
                                AsyncImage(
                                    model = userPhoto,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Welcome back,", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.White.copy(alpha = 0.8f))
                            Text(userName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAdd = true }, 
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        if (showQuickAdd) {
            QuickAddBottomSheet(
                onDismiss = { showQuickAdd = false },
                onSave = { amount, category ->
                    transactionViewModel.addExpense(
                        amount = amount,
                        category = category,
                        date = System.currentTimeMillis(),
                        paymentMethod = "Cash",
                        note = "Quick logged"
                    )
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BalanceCard(remainingBalance)
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            item {
                DailyLimitCard(dailyLimitInfo)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryCard("Income", totalIncome, GreenSuccess, Icons.Default.TrendingUp, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    SummaryCard("Expense", totalExpense, RedError, Icons.Default.TrendingDown, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                BudgetProgressSection(currentBudget?.limitAmount ?: 0.0, totalExpense)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3436))
                    TextButton(onClick = onNavigateToHistory) {
                        Text("View History", color = BluePrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No recent transactions", color = Color.Gray)
                    }
                }
            } else {
                items(recentTransactions) { transaction ->
                    TransactionItem(transaction)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun DailyLimitCard(info: DailyLimitInfo) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (info.isOverBudget) RedError.copy(alpha = 0.1f) else BluePrimary.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    if (info.isOverBudget) Icons.Default.Warning else Icons.Default.Speed,
                    contentDescription = null,
                    tint = if (info.isOverBudget) RedError else BluePrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Daily Spending Power",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${String.format("%,.2f", info.dailyLimit)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (info.isOverBudget) RedError else Color(0xFF2D3436)
                )
                Text(
                    text = if (info.isOverBudget) "You've exceeded your limit!" else "Remaining for ${info.daysRemaining} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (info.isOverBudget) RedError else Color.Gray
                )
            }
        }
    }
}

@Composable
fun BudgetProgressSection(budgetAmount: Double, totalExpense: Double) {
    if (budgetAmount <= 0) return
    
    val progress = (totalExpense / budgetAmount).toFloat().coerceIn(0f, 1f)
    val color = progressToColor(progress)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Monthly Budget", fontWeight = FontWeight.Bold, color = Color(0xFF2D3436))
                Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.ExtraBold, color = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = color,
                trackColor = color.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Spent: ₹${String.format("%,.0f", totalExpense)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Limit: ₹${String.format("%,.0f", budgetAmount)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

private fun progressToColor(progress: Float): Color {
    return when {
        progress < 0.6f -> GreenSuccess
        progress < 0.85f -> Color(0xFFF39C12) // Orange
        else -> RedError
    }
}

@Composable
fun BalanceCard(balance: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BluePrimary, Color(0xFF1565C0))
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text("Total Balance", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("₹${String.format("%,.2f", balance)}", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Securely Encrypted", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
        Icon(
            Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.size(120.dp).align(Alignment.CenterEnd).offset(x = 20.dp)
        )
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(6.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("₹${String.format("%,.2f", amount)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun TransactionItem(transaction: Any) {
    val title = when (transaction) {
        is Expense -> transaction.category
        is Income -> transaction.source
        else -> ""
    }
    val amount = when (transaction) {
        is Expense -> "-₹${String.format("%,.2f", transaction.amount)}"
        is Income -> "+₹${String.format("%,.2f", transaction.amount)}"
        else -> ""
    }
    val color = when (transaction) {
        is Expense -> RedError
        is Income -> GreenSuccess
        else -> Color.Black
    }
    val icon = when (transaction) {
        is Expense -> getCategoryIcon(transaction.category)
        is Income -> Icons.Default.Payments
        else -> Icons.Default.QuestionMark
    }
    val date = when (transaction) {
        is Expense -> SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(transaction.date))
        is Income -> SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(transaction.date))
        else -> ""
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2D3436), fontSize = 16.sp)
                    Text(date, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Text(amount, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 16.sp)
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "travel" -> Icons.Default.DirectionsBus
        "shopping" -> Icons.Default.ShoppingCart
        "rent" -> Icons.Default.Home
        "health" -> Icons.Default.MedicalServices
        "education" -> Icons.Default.School
        "entertainment" -> Icons.Default.Movie
        "others" -> Icons.Default.Category
        else -> Icons.Default.Payments
    }
}
