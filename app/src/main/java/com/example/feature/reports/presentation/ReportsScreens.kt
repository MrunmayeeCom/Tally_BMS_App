package com.example.feature.reports.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

// ==========================================
// CENTRAL REPORT VIEWER HUB COMPOSABLE
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsHubScreen(
    viewModel: ReportsViewModel,
    initialScreen: String = "dashboard",
    onNavigateBack: () -> Unit = {},
    onNavigateToOutstanding: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToFollowUps: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToBuilder: () -> Unit = {},
    onNavigateToExports: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Multi-tenant profile states
    val companyName by viewModel.companyId.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    
    // Dynamic Filter states
    val selectedStart by viewModel.startDate.collectAsState()
    val selectedEnd by viewModel.endDate.collectAsState()

    var showFiltersDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reports & Analytics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tenant ID: $companyName • Role: $userRole",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("reports_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFiltersDialog = true },
                        modifier = Modifier.testTag("reports_filters_trigger")
                    ) {
                        BadgedBox(
                            badge = {
                                if (selectedStart != "2026-06-01" || selectedEnd != "2026-06-30") {
                                    Badge { Text("!") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.List, contentDescription = "Reports Filters")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (initialScreen) {
                "dashboard" -> ReportsDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToOutstanding = onNavigateToOutstanding,
                    onNavigateToReminders = onNavigateToReminders,
                    onNavigateToFollowUps = onNavigateToFollowUps,
                    onNavigateToPerformance = onNavigateToPerformance,
                    onNavigateToBuilder = onNavigateToBuilder,
                    onNavigateToExports = onNavigateToExports
                )
                "outstanding" -> OutstandingReportScreen(
                    viewModel = viewModel,
                    onNavigateToBuilder = onNavigateToBuilder
                )
                "reminders" -> ReminderAnalyticsScreen(
                    viewModel = viewModel,
                    onTriggerDocument = { type, format ->
                        viewModel.triggerExportReport(type, format) {
                            scope.launch { snackbarHostState.showSnackbar("Compiled: $it") }
                        }
                    }
                )
                "followups" -> FollowUpReportScreen(
                    viewModel = viewModel
                )
                "performance" -> SalesTeamPerformanceScreen(
                    viewModel = viewModel
                )
                "builder" -> CustomReportBuilderScreen(
                    viewModel = viewModel,
                    onTriggerExport = { title, format ->
                        viewModel.triggerExportReport(title, format) {
                            scope.launch { snackbarHostState.showSnackbar("Compiled: $it") }
                        }
                    }
                )
                "exports" -> ExportCenterScreen(
                    viewModel = viewModel,
                    onTriggerInstantExport = { type, format ->
                        viewModel.triggerExportReport(type, format) {
                            scope.launch { snackbarHostState.showSnackbar("Export saved: $it") }
                        }
                    }
                )
            }
        }
    }

    if (showFiltersDialog) {
        ReportsGlobalFilterDialog(
            currentStart = selectedStart,
            currentEnd = selectedEnd,
            onDismiss = { showFiltersDialog = false },
            onApply = { start, end ->
                viewModel.setDateRange(start, end)
                showFiltersDialog = false
            }
        )
    }
}

// ==========================================
// 1. REPORTS DASHBOARD SCREEN COMPOSABLE
// ==========================================
@Composable
fun ReportsDashboardScreen(
    viewModel: ReportsViewModel,
    onNavigateToOutstanding: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToFollowUps: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToBuilder: () -> Unit = {},
    onNavigateToExports: () -> Unit = {}
) {
    val executiveState by viewModel.executiveState.collectAsState()
    val dateStart by viewModel.startDate.collectAsState()
    val dateEnd by viewModel.endDate.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("reports_dashboard_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Executive Briefing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reporting Cycle: $dateStart to $dateEnd",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Analysis Info",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        when (val state = executiveState) {
            is ExecutiveUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.testTag("executive_loading"))
                    }
                }
            }
            is ExecutiveUiState.Error -> {
                item {
                    ReportErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadExecutiveReport(viewModel.companyId.value, dateStart, dateEnd) }
                    )
                }
            }
            is ExecutiveUiState.Success -> {
                val r = state.report
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCompactMetricCard(
                            title = "Total Revenue",
                            value = formatCurrency(r.totalRevenue),
                            subtext = "+14.2% MoM",
                            icon = Icons.Default.Star,
                            modifier = Modifier.weight(1f).testTag("kpi_revenue")
                        )
                        KpiCompactMetricCard(
                            title = "Collections",
                            value = formatCurrency(r.collectionsAchieved),
                            subtext = "Achieved",
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f).testTag("kpi_collections")
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCompactMetricCard(
                            title = "Total Outstanding",
                            value = formatCurrency(r.totalOutstanding),
                            subtext = "Needs attention",
                            icon = Icons.Default.Warning,
                            modifier = Modifier.weight(1f).testTag("kpi_outstanding")
                        )
                        KpiCompactMetricCard(
                            title = "Recovery Rate",
                            value = "${r.recoverySuccessRate}%",
                            subtext = "Success indicator",
                            icon = Icons.Default.Star,
                            modifier = Modifier.weight(1f).testTag("kpi_recovery_rate")
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Revenue vs Collections Trend",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // BEAUTIFUL SMOOTH LINE GRAPH VIA CANVAS
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("revenue_collections_chart")
                        ) {
                            val steps = listOf(0.12, 0.28, 0.42, 0.58, 0.72, 0.95)
                            val revY = listOf(0.3f, 0.45f, 0.4f, 0.65f, 0.55f, 0.85f)
                            val colY = listOf(0.15f, 0.3f, 0.35f, 0.42f, 0.52f, 0.68f)

                            val pathRevenue = Path()
                            val pathCollections = Path()

                            steps.forEachIndexed { index, xFactor ->
                                val px = size.width * xFactor.toFloat()
                                val pyRev = size.height * (1f - revY[index])
                                val pyCol = size.height * (1f - colY[index])

                                if (index == 0) {
                                    pathRevenue.moveTo(px, pyRev)
                                    pathCollections.moveTo(px, pyCol)
                                } else {
                                    pathRevenue.lineTo(px, pyRev)
                                    pathCollections.lineTo(px, pyCol)
                                }
                            }

                            drawPath(
                                path = pathRevenue,
                                color = Color(0xFF4CAF50),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawPath(
                                path = pathCollections,
                                color = Color(0xFF2196F3),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            LegendIndicator(label = "Revenue Projection (₹)", color = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(16.dp))
                            LegendIndicator(label = "Collections Met (₹)", color = Color(0xFF2196F3))
                        }
                    }
                }

                item {
                    Text(
                        text = "Module View Navigation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToOutstanding,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        ListItem(
                            headlineContent = { Text("Outstanding Reports", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("Ageing analyses, forecast metrics & customer lists") },
                            leadingContent = { Icon(Icons.Default.List, contentDescription = "Outstanding", tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = { Icon(Icons.Default.ArrowForward, contentDescription = "Arrow") },
                            modifier = Modifier.testTag("nav_outstanding_report")
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToReminders,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        ListItem(
                            headlineContent = { Text("Reminder Effectiveness Reports", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("Sent count, delivery feedback, conversion indicators") },
                            leadingContent = { Icon(Icons.Default.Notifications, contentDescription = "Reminders", tint = Color(0xFFFF9800)) },
                            trailingContent = { Icon(Icons.Default.ArrowForward, contentDescription = "Arrow") },
                            modifier = Modifier.testTag("nav_reminder_report")
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToFollowUps,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        ListItem(
                            headlineContent = { Text("Follow-Up Compliance Reports", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("Open tasks, overdue compliance, representative rankings") },
                            leadingContent = { Icon(Icons.Default.Info, contentDescription = "Followups", tint = Color(0xFF9C27B0)) },
                            trailingContent = { Icon(Icons.Default.ArrowForward, contentDescription = "Arrow") },
                            modifier = Modifier.testTag("nav_followup_report")
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToPerformance,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        ListItem(
                            headlineContent = { Text("Sales Team Accomplishments", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("Visits ratios, collection indices & beat records") },
                            leadingContent = { Icon(Icons.Default.Star, contentDescription = "Sales", tint = Color(0xFF4CAF50)) },
                            trailingContent = { Icon(Icons.Default.ArrowForward, contentDescription = "Arrow") },
                            modifier = Modifier.testTag("nav_salesteam_report")
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToBuilder,
                            modifier = Modifier.weight(1f).testTag("dashboard_custom_builder_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Builder")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Custom Builder")
                        }

                        Button(
                            onClick = onNavigateToExports,
                            modifier = Modifier.weight(1f).testTag("dashboard_export_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Download")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Center")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. OUTSTANDING REPORTS SCREEN COMPOSABLE
// ==========================================
@Composable
fun OutstandingReportScreen(
    viewModel: ReportsViewModel,
    onNavigateToBuilder: () -> Unit = {}
) {
    val outstandingState by viewModel.outstandingState.collectAsState()
    var selectedAgeingBucket by remember { mutableStateOf<String?>(null) }

    when (val state = outstandingState) {
        is OutstandingUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("outstanding_loading"))
            }
        }
        is OutstandingUiState.Error -> {
            ReportErrorView(
                message = state.message,
                onRetry = { viewModel.loadOutstandingReport(viewModel.companyId.value, viewModel.selectedCustomerId.value, viewModel.selectedTerritory.value) }
            )
        }
        is OutstandingUiState.Success -> {
            val r = state.report
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("outstanding_report_column"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Receivables Ageing Analysis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Understand your uncollected dues, separated by date ranges & collection probability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // RADIAL RING OR DONUT CHART REPRESENTING AGEING
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Outstanding Segment Share",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier.size(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(140.dp).testTag("ageing_donut_chart")) {
                                    var currentAngle = 0f
                                    val colors = listOf(Color(0xFF8BC34A), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFF44336))
                                    
                                    r.ageingBuckets.forEachIndexed { idx, item ->
                                        val sweep = (item.percentage.toFloat() / 100f) * 360f
                                        drawArc(
                                            color = colors[idx % colors.size],
                                            startAngle = currentAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                                            size = size
                                        )
                                        currentAngle += sweep
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Receivables", style = MaterialTheme.typography.labelSmall)
                                    Text("₹Ageing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                val colors = listOf(Color(0xFF8BC34A), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFF44336))
                                r.ageingBuckets.forEachIndexed { i, item ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            selectedAgeingBucket = if (selectedAgeingBucket == item.bucketLabel) null else item.bucketLabel
                                        }
                                    ) {
                                        LegendIndicator(label = item.bucketLabel, color = colors[i % colors.size])
                                        Text(
                                            text = formatCurrency(item.amount),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Cash Flow Collection Forecast",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // HORIZONTAL PROJECTIONS METRIC ROW
                items(r.collectionForecast) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceElevated(2.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.timeframe, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Confidence Probability: ${(item.probabilityIndex * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatCurrency(item.projectedAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                LinearProgressIndicator(
                                    progress = { item.probabilityIndex.toFloat() },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(4.dp)
                                        .padding(top = 4.dp),
                                    color = if (item.probabilityIndex > 0.75) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Customer Ledger Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onNavigateToBuilder) {
                            Icon(Icons.Default.Build, contentDescription = "Custom Query View")
                        }
                    }
                }

                items(r.customerOutstanding) { item ->
                    ListItem(
                        headlineContent = { Text(item.customerName, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Territory: ${item.territory} • Last Visited: ${item.lastActiveDate}") },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatCurrency(item.totalOutstanding), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("Overdue: ${formatCurrency(item.overdueAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        tonalElevation = 1.dp,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

// ==========================================
// 3. REMINDER ANALYTICS SCREEN COMPOSABLE
// ==========================================
@Composable
fun ReminderAnalyticsScreen(
    viewModel: ReportsViewModel,
    onTriggerDocument: (String, String) -> Unit = { _, _ -> }
) {
    val reminderState by viewModel.reminderState.collectAsState()

    when (val state = reminderState) {
        is ReminderUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("reminders_loading"))
            }
        }
        is ReminderUiState.Error -> {
            ReportErrorView(
                message = state.message,
                onRetry = { viewModel.loadReminderAnalytics(viewModel.companyId.value, viewModel.startDate.value, viewModel.endDate.value) }
            )
        }
        is ReminderUiState.Success -> {
            val r = state.report
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("reminders_report_column"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Reminder Effectiveness",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track reminder notifications sent and evaluate how many converted to actual bill settlements.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Settlement Collections Via Reminders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text(formatCurrency(r.totalCollectionRealized), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            CircularConversionBadge(rate = r.conversionRate)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReminderSmallStatCard(title = "Sent", count = r.sentCount, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        ReminderSmallStatCard(title = "Delivered", count = r.deliveredCount, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                        ReminderSmallStatCard(title = "Failed", count = r.failedCount, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    }
                }

                // CHART BAR SIMULATING PERFORMANCE ACTIVITY
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Deliveries & Collections Timeline",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .testTag("timeline_canvas")
                        ) {
                            val count = r.dailyActivity.size
                            if (count > 0) {
                                val spacing = size.width / count
                                val maxSent = r.dailyActivity.maxOf { it.sent }.toFloat().coerceAtLeast(1f)
                                r.dailyActivity.forEachIndexed { idx, point ->
                                    val barWidth = 14.dp.toPx()
                                    val x = (idx * spacing) + (spacing / 2) - (barWidth / 2)
                                    val heightSent = (point.sent / maxSent) * size.height * 0.8f
                                    val heightDelivered = (point.delivered / maxSent) * size.height * 0.8f

                                    // Sent (gray background container)
                                    drawRect(
                                        color = Color.LightGray.copy(alpha = 0.3f),
                                        topLeft = Offset(x, size.height - heightSent),
                                        size = Size(barWidth, heightSent)
                                    )
                                    // Delivered (colored accent)
                                    drawRect(
                                        color = Color(0xFFFF9800),
                                        topLeft = Offset(x, size.height - heightDelivered),
                                        size = Size(barWidth, heightDelivered)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            LegendIndicator(label = "Total Sent Notifications", color = Color.LightGray)
                            Spacer(modifier = Modifier.width(16.dp))
                            LegendIndicator(label = "Successfully Delivered", color = Color(0xFFFF9800))
                        }
                    }
                }

                item {
                    Text("Daily Payment Recoveries via Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(r.dailyActivity) { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(point.date, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Sent: ${point.sent} • Converted payments: ${point.paymentsReceivedCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatCurrency(point.paymentsAmount), fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            Text("Conversion: ${((point.paymentsReceivedCount.toDouble() / point.sent.toDouble().coerceAtLeast(1.0)) * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onTriggerDocument("Reminders Effectiveness", "PDF") },
                            modifier = Modifier.weight(1f).testTag("reminder_export_pdf_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Compile Document PDF")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export PDF")
                        }
                        
                        OutlinedButton(
                            onClick = { onTriggerDocument("Reminders Effectiveness", "XLSX") },
                            modifier = Modifier.weight(1f).testTag("reminder_export_excel_btn")
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Compile Document Excel")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Excel")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. FOLLOW-UP REPORTS SCREEN COMPOSABLE
// ==========================================
@Composable
fun FollowUpReportScreen(
    viewModel: ReportsViewModel
) {
    val followUpState by viewModel.followUpState.collectAsState()

    when (val state = followUpState) {
        is FollowUpUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("followups_loading"))
            }
        }
        is FollowUpUiState.Error -> {
            ReportErrorView(
                message = state.message,
                onRetry = { viewModel.loadFollowUpReport(viewModel.companyId.value, viewModel.selectedUserId.value, viewModel.startDate.value, viewModel.endDate.value) }
            )
        }
        is FollowUpUiState.Success -> {
            val r = state.report
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("followups_report_column"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Follow-Up & Visit Compliance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track active customer communication loops, resolve overdue tasks, and review staff response rates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiIndicatorBubbleCard(title = "Open Visits", value = "${r.openCount}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        KpiIndicatorBubbleCard(title = "Completed", value = "${r.completedCount}", color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                        KpiIndicatorBubbleCard(title = "Overdue", value = "${r.overdueCount}", color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Text(
                        text = "Agent Action Ratios & Ratings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(r.teamFollowUpEfficiency) { performer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(performer.userName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(performer.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Total Assigned: ${performer.assignedCount} schedules", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${performer.completionRate}% Met", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { performer.completionRate.toFloat() / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Settled: ${performer.completedCount}", style = MaterialTheme.typography.labelSmall)
                                Text("Breached/Overdue: ${performer.overdueCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SALES TEAM REPORTS SCREEN COMPOSABLE
// ==========================================
@Composable
fun SalesTeamPerformanceScreen(
    viewModel: ReportsViewModel
) {
    val salesTeamState by viewModel.salesTeamState.collectAsState()

    when (val state = salesTeamState) {
        is SalesTeamUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("salesteam_loading"))
            }
        }
        is SalesTeamUiState.Error -> {
            ReportErrorView(
                message = state.message,
                onRetry = { viewModel.loadSalesTeamReport(viewModel.companyId.value, viewModel.selectedTerritory.value, viewModel.selectedUserId.value, viewModel.startDate.value, viewModel.endDate.value) }
            )
        }
        is SalesTeamUiState.Success -> {
            val r = state.report
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("salesteam_report_column"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Territory & Performance Metrics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Review completed physical visits count, collections achieved, and overall quota targets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Physical Visits", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${r.visitsCompleted}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Follow-Ups Met", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${r.followUpsCompleted}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF9C27B0))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tally Recovered", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatCurrency(r.collectionsAchieved), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Territorial Revenue Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(r.territoryPerformance) { territory ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = "Territory Zone",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Region: ${territory.territory}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Active reps: ${territory.activeForceCount} • Visits: ${territory.visitsCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatCurrency(territory.collectionAmount), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("${territory.targetAchievementsPercent}% Target", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. CUSTOM REPORT BUILDER SCREEN COMPOSABLE
// ==========================================
@Composable
fun CustomReportBuilderScreen(
    viewModel: ReportsViewModel,
    onTriggerExport: (String, String) -> Unit = { _, _ -> }
) {
    val customReportState by viewModel.customReportState.collectAsState()

    var sourceModule by remember { mutableStateOf("Outstanding") }
    val modules = listOf("Outstanding", "Collections", "Reminders", "Follow-Ups", "Sales")

    val allFields = mapOf(
        "Outstanding" to listOf("Customer Info", "Total Outstanding", "Overdue Amount", "Payment Status", "Territory Segment"),
        "Collections" to listOf("Customer Name", "Voucher Hash", "Amount Paid", "Settled Date", "Collector Name"),
        "Reminders" to listOf("Sent Timestamp", "Target Customer", "Alert Route", "Delivery Status", "Action Outcome"),
        "Follow-Ups" to listOf("Scheduled Date", "Customer Account", "Representative", "Resolution Metric", "Latency Hours"),
        "Sales" to listOf("Beat Route", "Location Coordinates", "Duration Minutes", "Signature Status", "Validation Lock")
    )

    val selectedFields = remember { mutableStateMapOf<String, Boolean>() }

    // Synchronize checklist whenever the module target changes
    LaunchedEffect(sourceModule) {
        selectedFields.clear()
        allFields[sourceModule]?.forEach { selectedFields[it] = true }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("custom_report_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dynamic Report Builder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Compose custom tables instantly. Define query fields, apply custom filters and extract data spreadsheets.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("Select Master Module Source", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modules.forEach { mod ->
                        FilterChip(
                            selected = sourceModule == mod,
                            onClick = { sourceModule = mod },
                            label = { Text(mod) },
                            modifier = Modifier.testTag("builder_module_chip_$mod")
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceLevel(1.dp)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Columns to Measure", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    allFields[sourceModule]?.forEach { col ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFields[col] = !(selectedFields[col] ?: false) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedFields[col] ?: false,
                                onCheckedChange = { selectedFields[col] = it },
                                modifier = Modifier.testTag("box_$col")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(col, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val activeCols = selectedFields.filter { it.value }.keys.toList()
                    viewModel.executeCustomReport(sourceModule, activeCols)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("builder_compile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Build, contentDescription = "Compile Analytics")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compile Custom Report")
            }
        }

        // COMPILED AD-HOC DATA PREVIEW BLOCK
        when (val result = customReportState) {
            is CustomReportUiState.Idle -> {
                item {
                    Text(
                        text = "Customize parameters above and click Compile to view data preview.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                }
            }
            is CustomReportUiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("builder_compiling_state"))
                    }
                }
            }
            is CustomReportUiState.Error -> {
                item {
                    Text(result.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
            is CustomReportUiState.Success -> {
                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Interactive Data Grid Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { viewModel.clearCustomReport() }) {
                            Text("Clear")
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        // Headers Row
                        Row(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                            result.spec.headers.forEach { header ->
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = header,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Grid Row Contents
                        result.spec.resultRows.forEach { rowCells ->
                            Row(modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)) {
                                rowCells.forEach { cellText ->
                                    Box(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = cellText,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onTriggerExport(result.spec.title, "PDF") },
                            modifier = Modifier.weight(1f).testTag("custom_pdf_export_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "PDF")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Export")
                        }

                        Button(
                            onClick = { onTriggerExport(result.spec.title, "XLSX") },
                            modifier = Modifier.weight(1f).testTag("custom_xlsx_export_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Excel")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel Export")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. EXPORT CENTER SCREEN COMPOSABLE
// ==========================================
@Composable
fun ExportCenterScreen(
    viewModel: ReportsViewModel,
    onTriggerInstantExport: (String, String) -> Unit = { _, _ -> }
) {
    val exportHistoryState by viewModel.exportHistoryState.collectAsState()
    var showQuickExportMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("export_center_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Export Document Center",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Access generated audits, balance sheets, compliance histories and dynamic XLS file backups.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Button(
                onClick = { showQuickExportMenu = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("quick_export_trigger"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Plus")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compile New Document Backups")
            }
        }

        item {
            Text(
                text = "Past Exports History Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        when (val state = exportHistoryState) {
            is ExportHistoryUiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("exports_loading"))
                    }
                }
            }
            is ExportHistoryUiState.Error -> {
                item {
                    ReportErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadExportHistory(viewModel.companyId.value) }
                    )
                }
            }
            is ExportHistoryUiState.Success -> {
                if (state.exports.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No document backlogs found yet. Build a report to list histories.")
                        }
                    }
                } else {
                    items(state.exports) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceElevated(1.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icon = when (record.format) {
                                        "PDF" -> Icons.Default.Info
                                        else -> Icons.Default.Info
                                    }
                                    val iconColor = when (record.format) {
                                        "PDF" -> Color(0xFFEF5350)
                                        else -> Color(0xFF4CAF50)
                                    }
                                    Icon(icon, contentDescription = record.format, tint = iconColor, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(record.fileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${record.scopeText} • ${formatBytes(record.sizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Generated: ${record.timestamp}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    }
                                }

                                IconButton(
                                    onClick = { /* Simulated File Download to disk */ },
                                    modifier = Modifier.testTag("download_record_${record.id}")
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = "Pull file to system", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQuickExportMenu) {
        QuickExportSelectionDialog(
            onDismiss = { showQuickExportMenu = false },
            onSelect = { name, format ->
                onTriggerInstantExport(name, format)
                showQuickExportMenu = false
            }
        )
    }
}

// ==========================================
// DECORATIVE AND REUSABLE UI STYLING ACCENTS
// ==========================================

@Composable
fun KpiCompactMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceElevated(2.dp)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ReminderSmallStatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$count", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun KpiIndicatorBubbleCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun LegendIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CircularConversionBadge(rate: Double) {
    Box(
        modifier = Modifier.size(76.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color.White.copy(alpha = 0.4f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx())
            )
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = (rate.toFloat() / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${rate.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Conv.", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ReportErrorView(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, contentDescription = "Alert error", tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Text("Retry Connection")
            }
        }
    }
}

@Composable
fun ReportsGlobalFilterDialog(
    currentStart: String,
    currentEnd: String,
    onDismiss: () -> Unit,
    onApply: (String, String) -> Unit
) {
    var startInput by remember { mutableStateOf(currentStart) }
    var endInput by remember { mutableStateOf(currentEnd) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Reports Period") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startInput,
                    onValueChange = { startInput = it },
                    label = { Text("Start Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().testTag("filter_start_date_input")
                )

                OutlinedTextField(
                    value = endInput,
                    onValueChange = { endInput = it },
                    label = { Text("End Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().testTag("filter_end_date_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(startInput, endInput) },
                modifier = Modifier.testTag("apply_filters_dialog_btn")
            ) {
                Text("Apply Filters")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickExportSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    val reportTypes = listOf("Outstanding Ageing Summary", "Reminder Sent Statistics", "Collections Audits Logs", "Sales Target Quota Ledger")
    val formats = listOf("PDF", "XLSX", "CSV")

    var selectedType by remember { mutableStateOf(reportTypes[0]) }
    var selectedFormat by remember { mutableStateOf(formats[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compile Backup Copy") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Select Target Template", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    reportTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = type }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(type, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Divider()

                Column {
                    Text("Select File Format", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        formats.forEach { form ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedFormat = form }) {
                                RadioButton(selected = selectedFormat == form, onClick = { selectedFormat = form })
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(form, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelect(selectedType, selectedFormat) },
                modifier = Modifier.testTag("apply_quick_export_btn")
            ) {
                Text("Trigger Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// MATHEMATICAL AND TEXT FORMATTING UTILITY FNS
// ==========================================

fun formatCurrency(amount: Double): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        format.format(amount)
    } catch (e: Exception) {
        "₹${amount.toInt()}"
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1] + "B"
    return String.format(Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}

// Utility extension for elevating surface values nicely
fun ColorScheme.surfaceLevel(elevation: androidx.compose.ui.unit.Dp): Color {
    return surfaceColorAtElevation(elevation)
}

fun ColorScheme.surfaceElevated(elevation: androidx.compose.ui.unit.Dp): Color {
    return surfaceColorAtElevation(elevation)
}
