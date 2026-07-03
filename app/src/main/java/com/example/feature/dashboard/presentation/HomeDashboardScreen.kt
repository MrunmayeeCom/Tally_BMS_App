package com.example.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.TallyBMSApp
import com.example.core.common.UiState
import com.example.core.common.ViewModelFactory
import com.example.feature.dashboard.domain.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    onNavigateToLedgers: () -> Unit,
    onNavigateToVouchers: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToOutstanding: () -> Unit,
    onNavigateToFollowUps: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToApprovals: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToInventory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext as TallyBMSApp
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = ViewModelFactory(context.container) { container ->
            DashboardViewModel(
                dashboardRepository = container.dashboardRepository,
                sessionManager = container.sessionManager
            )
        }
    )

    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val userName by dashboardViewModel.userNameFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome, ${userName ?: "Administrator"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { dashboardViewModel.fetchDashboardData() }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Loading dashboard...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(
                                Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("Failed to load", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text((uiState as UiState.Error).message, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { dashboardViewModel.fetchDashboardData() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val data = (uiState as UiState.Success<DashboardData>).data
                    DashboardContent(
                        data = data,
                        onNavigateToLedgers = onNavigateToLedgers,
                        onNavigateToVouchers = onNavigateToVouchers,
                        onNavigateToOrders = onNavigateToOrders,
                        onNavigateToInventory = onNavigateToInventory,
                        onNavigateToOutstanding = onNavigateToOutstanding
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardData,
    onNavigateToLedgers: () -> Unit,
    onNavigateToVouchers: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToOutstanding: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("dashboard_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Dashboard Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Welcome back! Here's what's happening today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Date display
        item {
            val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
            val today = remember { dateFormat.format(Date()) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(today, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // 4 Stats Cards
        item {
            val stats = listOf(
                StatCardData("Total Receivables", formatAmount(data.summary.receivables), Icons.Default.TrendingUp, Blue),
                StatCardData("Total Payables", formatAmount(data.summary.payables), Icons.Default.TrendingDown, Orange),
                StatCardData("Pending Bills", data.summary.pendingBills.toString(), Icons.Default.Info, Red),
                StatCardData("Cleared Bills", data.summary.clearedBills.toString(), Icons.Default.CheckCircle, Green)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    stats[0].let { StatCard(it, Modifier.weight(1f)) }
                    stats[1].let { StatCard(it, Modifier.weight(1f)) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    stats[2].let { StatCard(it, Modifier.weight(1f)) }
                    stats[3].let { StatCard(it, Modifier.weight(1f)) }
                }
            }
        }

        // Charts section
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Income vs Expense Bar Chart
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Income vs Expense", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        IncomeExpenseChart(data.incomeExpense)
                    }
                }

                // Outstanding Trends
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Outstanding Trends", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        OutstandingTrendsChart(data.outstandingTrends)
                    }
                }
            }
        }

        // Quick Links + Upcoming Dues
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Quick Links
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Quick Links", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickLinkButton("Ledger List", Icons.Default.AccountBalance, Blue, onNavigateToLedgers)
                            QuickLinkButton("Voucher Explorer", Icons.Default.Description, Color(0xFF7B1FA2), onNavigateToVouchers)
                            QuickLinkButton("Order Book", Icons.Default.ShoppingCart, Orange, onNavigateToOrders)
                            QuickLinkButton("Inventory", Icons.Default.Inventory, Green, onNavigateToInventory)
                        }
                    }
                }

                // Upcoming Due Dates
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Upcoming Dues", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            TextButton(
                                onClick = onNavigateToOutstanding,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("View All", fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        data.upcomingDues.take(4).forEach { due ->
                            DueRow(due)
                            if (due != data.upcomingDues.take(4).last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                        if (data.upcomingDues.isEmpty()) {
                            Text("No upcoming dues", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Bottom spacer
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// --- Data class for stat cards ---
private data class StatCardData(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

private val Blue = Color(0xFF3B82F6)
private val Orange = Color(0xFFF97316)
private val Red = Color(0xFFEF4444)
private val Green = Color(0xFF10B981)

// --- Stat Card ---
@Composable
private fun StatCard(data: StatCardData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(data.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.color,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// --- Income vs Expense Mini Bar Chart ---
@Composable
private fun IncomeExpenseChart(entries: List<IncomeExpenseEntry>) {
    val displayEntries = remember(entries) {
        if (entries.size > 6) entries.takeLast(6) else entries
    }
    val maxVal = remember(displayEntries) {
        displayEntries.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(Green, "Income")
            LegendDot(Red, "Expense")
        }
        Spacer(Modifier.height(4.dp))
        displayEntries.forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.monthName.take(3),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                val incomeRatio = (entry.income / maxVal).toFloat().coerceIn(0f, 1f)
                val expenseRatio = (entry.expense / maxVal).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(incomeRatio.coerceAtLeast(0.01f))
                                .background(Green.copy(alpha = 0.7f), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                        )
                        if (expenseRatio > 0) {
                            Spacer(Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(expenseRatio.coerceAtLeast(0.01f))
                                    .background(Red.copy(alpha = 0.7f), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Outstanding Trends Area-like Chart ---
@Composable
private fun OutstandingTrendsChart(trends: List<OutstandingTrend>) {
    val displayEntries = remember(trends) {
        if (trends.size > 5) trends.take(5) else trends
    }
    val maxVal = remember(displayEntries) {
        displayEntries.maxOfOrNull { it.amount } ?: 1.0
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Party-wise", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        displayEntries.forEach { trend ->
            Row(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trend.partyName.take(8),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(64.dp)
                )
                Spacer(Modifier.width(4.dp))
                val ratio = (trend.amount / maxVal).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(ratio.coerceAtLeast(0.01f))
                            .background(Blue.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = formatCompact(trend.amount),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(48.dp)
                )
            }
        }
    }
}

// --- Legend Dot ---
@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// --- Quick Link Button ---
@Composable
private fun QuickLinkButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// --- Due Row ---
@Composable
private fun DueRow(due: OutstandingTrend) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = due.partyName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Due: ${due.dueDate}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCompact(due.amount),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${due.daysLeft} days left",
                fontSize = 10.sp,
                color = Orange
            )
        }
    }
}

// --- Formatting ---
private fun formatAmount(amount: Double): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        format.format(amount)
    } catch (e: Exception) {
        "\u20B9${amount.toInt()}"
    }
}

private fun formatCompact(value: Double): String {
    val abs = kotlin.math.abs(value)
    return when {
        abs >= 1_00_00_000 -> "\u20B9${(value / 1_00_00_000).toInt()}Cr"
        abs >= 1_00_000 -> "\u20B9${(value / 1_00_000).toInt()}L"
        abs >= 1_000 -> "${(value / 1_000).toInt()}K"
        else -> "\u20B9${value.toInt()}"
    }
}
