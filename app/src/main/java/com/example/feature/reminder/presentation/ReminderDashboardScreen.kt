package com.example.feature.reminder.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.feature.reminder.domain.ReminderDashboardData
import com.example.feature.reminder.domain.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDashboardScreen(
    viewModel: ReminderDashboardViewModel,
    onNavigateToManual: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToScheduler: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val role by viewModel.userRole.collectAsState()
    val tenantId by viewModel.companyId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder Engine Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("dashboard_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadSessionAndDashboard() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("dashboard_loading"))
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(state.message, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadSessionAndDashboard() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    DashboardContent(
                        data = state.data,
                        role = role ?: "Super Admin",
                        tenantId = tenantId ?: "All",
                        onNavigateToManual = onNavigateToManual,
                        onNavigateToRules = onNavigateToRules,
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToTemplates = onNavigateToTemplates,
                        onNavigateToScheduler = onNavigateToScheduler
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: ReminderDashboardData,
    role: String,
    tenantId: String,
    onNavigateToManual: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToScheduler: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reminder_dashboard_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Tenant context indicator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Tenant", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Isolated Tenant Profile: $tenantId", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Text("Active Role: $role", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Row of KPI statistics
        item {
            Text("Collection Campaigns Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    KpiCard(
                        title = "Active Reminders",
                        value = "${data.totalActive}",
                        icon = Icons.Default.Send,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    KpiCard(
                        title = "Overdue Today",
                        value = "${data.overdue}",
                        icon = Icons.Default.Notifications,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    KpiCard(
                        title = "Today Scheduled",
                        value = "${data.todayScheduled}",
                        icon = Icons.Default.DateRange,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    KpiCard(
                        title = "Completed",
                        value = "${data.completed}",
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Navigation Menu list
        item {
            Text("Campaign Operations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        item {
            NavigationItemCard(
                title = "1. Ad-hoc Manual Reminder Composer",
                description = "Craft and dispatch instant outstanding warnings to selected customers.",
                icon = Icons.Default.Edit,
                onClick = onNavigateToManual,
                tag = "nav_manual_composer"
            )
        }

        item {
            NavigationItemCard(
                title = "2. Automated Escalation Rules",
                description = "Configure dunning flow thresholds by balance weight or overdue invoice age.",
                icon = Icons.Default.Build,
                onClick = onNavigateToRules,
                tag = "nav_auto_rules"
            )
        }

        item {
            NavigationItemCard(
                title = "3. Reminder Scheduler Profiles",
                description = "Configure interval timers (Daily, Weekly, Monthly) for automated runs.",
                icon = Icons.Default.DateRange,
                onClick = onNavigateToScheduler,
                tag = "nav_scheduler"
            )
        }

        item {
            NavigationItemCard(
                title = "4. Templates Library",
                description = "Draft predefined layouts with dynamic variables for WhatsApp, SMS, and Email.",
                icon = Icons.Default.List,
                onClick = onNavigateToTemplates,
                tag = "nav_templates"
            )
        }

        item {
            NavigationItemCard(
                title = "5. Communication Delivery History",
                description = "Audit delivery parameters, delivery status flags, and failure responses.",
                icon = Icons.Default.Info,
                onClick = onNavigateToHistory,
                tag = "nav_history"
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun NavigationItemCard(
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
