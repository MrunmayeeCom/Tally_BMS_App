package com.example.feature.outstanding.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.common.UiState
import com.example.feature.outstanding.domain.OutstandingDashboardData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutstandingDashboardScreen(
    viewModel: OutstandingDashboardViewModel,
    onNavigateToList: () -> Unit,
    onNavigateToAging: () -> Unit,
    onNavigateToPipeline: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val activeCompany by viewModel.activeCompany.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outstanding Ledger", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("dashboard_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        modifier = Modifier.fillMaxSize().testTag("outstanding_dashboard_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(state.message, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadDashboard() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val data = state.data
                    DashboardContent(
                        data = data,
                        userRole = userRole,
                        activeCompany = activeCompany ?: "Main Office",
                        onNavigateToList = onNavigateToList,
                        onNavigateToAging = onNavigateToAging,
                        onNavigateToPipeline = onNavigateToPipeline
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    data: OutstandingDashboardData,
    userRole: String?,
    activeCompany: String,
    onNavigateToList: () -> Unit,
    onNavigateToAging: () -> Unit,
    onNavigateToPipeline: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Tenant Isolation Info Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Home, contentDescription = "Active Company", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Isolated Tenant Profile", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text("Company: $activeCompany", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = userRole ?: "Executive",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Summary KPI Metrics Title
        Text(
            text = "Receivables & Payables Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Receivables KPI Card
        SummaryKpiCard(
            title = "TOTAL RECEIVABLES",
            value = data.totalReceivables,
            icon = Icons.Default.KeyboardArrowUp,
            color = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            label = "Pending payments from customers"
        )

        // Payables KPI Card
        SummaryKpiCard(
            title = "TOTAL PAYABLES",
            value = data.totalPayables,
            icon = Icons.Default.KeyboardArrowDown,
            color = MaterialTheme.colorScheme.error,
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
            label = "Corporate balances due to suppliers"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SmallKpiCard(
                    title = "Overdue Bill Balance",
                    value = data.overdueAmount,
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Warning
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SmallKpiCard(
                    title = "Collections This Month",
                    value = data.collectionsThisMonth,
                    color = Color(0xFF2E7D32),
                    icon = Icons.Default.CheckCircle
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Menu Actions
        Text(
            text = "Quick Operations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        RowMenuCard(
            title = "Outstanding Ledger Directory",
            description = "Explore outstanding balances with granular search & filters.",
            icon = Icons.Default.List,
            onClick = onNavigateToList,
            tag = "nav_outstanding_list"
        )

        RowMenuCard(
            title = "Overdue Ageing Analysis",
            description = "Audit client liabilities divided into 30d, 60d, 90d buckets.",
            icon = Icons.Default.DateRange,
            onClick = onNavigateToAging,
            tag = "nav_aging_analysis"
        )

        RowMenuCard(
            title = "Recovery & Collections Pipeline",
            description = "Kanban pipelines driving receivables to settled states.",
            icon = Icons.Default.ArrowForward,
            onClick = onNavigateToPipeline,
            tag = "nav_recovery_pipeline"
        )
    }
}

@Composable
fun SummaryKpiCard(
    title: String,
    value: Double,
    icon: ImageVector,
    color: Color,
    containerColor: Color,
    label: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = color)
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "₹${formatCurrency(value)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SmallKpiCard(
    title: String,
    value: Double,
    color: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "₹${formatCurrency(value)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun RowMenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.outline)
        }
    }
}

fun formatCurrency(amount: Double): String {
    return String.format("%,.2f", amount)
}
