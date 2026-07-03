package com.example.feature.salesteam.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.salesteam.domain.*
import java.text.NumberFormat
import java.util.Locale

// === Formatting Utility ===
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(amount)
}

// ==========================================
// 1. SALES TEAM DASHBOARD SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesTeamDashboardScreen(
    viewModel: SalesTeamDashboardViewModel,
    onNavigateToManageUsers: () -> Unit,
    onNavigateToUserDetail: (String) -> Unit,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToVisits: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToActivityFeed: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activities by viewModel.activities.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Team Operations", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("dashboard_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboardData() }, modifier = Modifier.testTag("dashboard_refresh_btn")) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh data", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).testTag("dashboard_loading"))
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp).testTag("dashboard_error"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadDashboardData() }) {
                            Text("Try Again")
                        }
                    }
                }
                is UiState.Success -> {
                    val stats = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Quick Action Shortcuts Row
                        Text("Operation Center", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DashboardActionCard(
                                title = "Check-In",
                                icon = Icons.Default.Place,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f).testTag("dashboard_shortcut_checkin"),
                                onClick = onNavigateToCheckIn
                            )
                            DashboardActionCard(
                                title = "Log Visit",
                                icon = Icons.Default.Place,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.weight(1f).testTag("dashboard_shortcut_visit"),
                                onClick = onNavigateToVisits
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DashboardActionCard(
                                title = "Manage Users",
                                icon = Icons.Default.Person,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.weight(1f).testTag("dashboard_shortcut_users"),
                                onClick = onNavigateToManageUsers
                            )
                            DashboardActionCard(
                                title = "Performance",
                                icon = Icons.Default.Star,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f).testTag("dashboard_shortcut_perf"),
                                onClick = onNavigateToPerformance
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Stats Grid Section
                        Text("Operational Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(title = "On-Field Force", value = "${stats.activeUsersCount} Active", subtext = "Checked-in", icon = Icons.Default.Person, modifier = Modifier.weight(1f).testTag("metric_active_users"))
                            MetricCard(title = "Visits Today", value = "${stats.todayVisitsCount}", subtext = "Completed", icon = Icons.Default.Place, modifier = Modifier.weight(1f).testTag("metric_today_visits"))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(title = "Collections Today", value = formatCurrency(stats.collectionsTodayAmount), subtext = "Against Overdues", icon = Icons.Default.Star, modifier = Modifier.weight(1f).testTag("metric_today_collections"))
                            MetricCard(title = "Pending Task SLA", value = "${stats.pendingTasksCount}", subtext = "Attention Required", icon = Icons.Default.Warning, modifier = Modifier.weight(1f).testTag("metric_pending_tasks"))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Recent Activity Feed Summary Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Real-Time Operations Stream", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = onNavigateToActivityFeed, modifier = Modifier.testTag("dashboard_view_all_activities")) {
                                Text("View Feed")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (activities.isEmpty()) {
                            Text("No recent team operations logged today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            activities.take(5).forEach { event ->
                                ActivityEventRow(event = event)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(96.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, sizePlaceholder(), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtext, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

fun sizePlaceholder(): Modifier = Modifier.size(20.dp)

// ==========================================
// 2. MANAGE USERS SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    viewModel: ManageUsersViewModel,
    onNavigateToUserDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTerritory by viewModel.selectedTerritory.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()

    var editingMember by remember { mutableStateOf<SalesTeamMember?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Field Force Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("users_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadMembers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync force")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("users_search_field"),
                placeholder = { Text("Search by name, state code...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filtering Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Role Filters
                listOf("All", "Sales Executive", "Sales Manager", "Recovery Agent").forEach { role ->
                    FilterChip(
                        selected = (selectedRole == role),
                        onClick = { viewModel.setRole(role) },
                        label = { Text(role) },
                        modifier = Modifier.testTag("filter_role_$role")
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Territory Filters
                listOf("All", "Mumbai South", "Mumbai West", "Delhi NCR", "Bangalore Zone 1", "Kolkata H.Q.").forEach { terr ->
                    FilterChip(
                        selected = (selectedTerritory == terr),
                        onClick = { viewModel.setTerritory(terr) },
                        label = { Text(terr) },
                        modifier = Modifier.testTag("filter_terr_$terr")
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).testTag("users_loading"))
                    }
                    is UiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp).testTag("users_error"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.message, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadMembers() }) {
                                Text("Retry")
                            }
                        }
                    }
                    is UiState.Success -> {
                        val members = state.data
                        if (members.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center).padding(24.dp).testTag("users_empty"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No sales representative fits the filter constraints.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().testTag("users_lazy_list"),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(members, key = { it.id }) { member ->
                                    MemberCard(
                                        member = member,
                                        onClick = { onNavigateToUserDetail(member.id) },
                                        onEditClick = { editingMember = member }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Edit user role/territory dialog
        editingMember?.let { member ->
            var territoryInput by remember { mutableStateOf(member.territory) }
            var roleInput by remember { mutableStateOf(member.role) }
            var statusInput by remember { mutableStateOf(member.status) }

            Dialog(onDismissRequest = { editingMember = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("user_edit_dialog"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Update Scope: ${member.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Territory field
                        OutlinedTextField(
                            value = territoryInput,
                            onValueChange = { territoryInput = it },
                            label = { Text("Territory Assignment") },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_territory_field"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Role Selector Header
                        Text("Assigned Role", style = MaterialTheme.typography.labelLarge)
                        listOf("Sales Executive", "Sales Manager", "Recovery Agent").forEach { r ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { roleInput = r }.padding(vertical = 4.dp).testTag("dialog_role_$r")
                            ) {
                                RadioButton(selected = (roleInput == r), onClick = { roleInput = r })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(r)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Option Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Force Status")
                            Switch(
                                checked = (statusInput == "Active"),
                                onCheckedChange = { statusInput = if (it) "Active" else "Inactive" },
                                modifier = Modifier.testTag("dialog_status_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { editingMember = null }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateMember(member.id, statusInput, territoryInput, roleInput)
                                    editingMember = null
                                },
                                modifier = Modifier.testTag("dialog_save_btn")
                            ) {
                                Text("Save Modifications")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(
    member: SalesTeamMember,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (member.status == "Active") Color(0xFF2E7D32) else Color.Gray,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    }
                    Text(member.role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Text("Territory: ${member.territory}", style = MaterialTheme.typography.bodySmall)
                }

                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp).testTag("edit_member_btn_${member.id}")) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit representative scope", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today's Status badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (member.todayCheckInStatus) {
                        "Checked-In" -> Color(0xFF2E7D32)
                        "Checked-Out" -> Color(0xFFE65100)
                        else -> Color.DarkGray
                    }
                    Icon(
                        imageVector = if (member.todayCheckInStatus == "Checked-In") Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = statusColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (member.todayCheckInStatus) {
                            "Checked-In" -> "Checked In at ${member.todayCheckInTime}"
                            "Checked-Out" -> "Day Closed ${member.todayCheckOutTime}"
                            else -> "No Log Entered"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }

                // Target performance
                Column(horizontalAlignment = Alignment.End) {
                    Text("Monthly Targets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    LinearProgressIndicator(
                        progress = { member.performanceProgress },
                        modifier = Modifier.width(64.dp).height(4.dp).clip(CircleShape),
                        color = if (member.performanceProgress > 0.7f) Color(0xFF2E7D32) else Color(0xFFE65100),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. USER DETAIL SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    viewModel: UserDetailViewModel,
    onBack: () -> Unit
) {
    val memberState by viewModel.memberState.collectAsState()
    val performanceState by viewModel.performanceState.collectAsState()
    val checkInHistory by viewModel.checkInHistory.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Executive Profile & Targets") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val user = memberState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).testTag("detail_loading"))
                }
                is UiState.Error -> {
                    Text("Error: ${user.message}", modifier = Modifier.align(Alignment.Center).testTag("detail_error"))
                }
                is UiState.Success -> {
                    val m = user.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Info Section Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(m.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(m.role, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                        Text("Territory: ${m.territory}", style = MaterialTheme.typography.bodySmall)
                                        Text("Active: ${m.status}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        // Call / Message shortcuts row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { /* trigger telephony call intent in real app */ },
                                    modifier = Modifier.weight(1f).testTag("dial_phone_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Call ${m.phone}")
                                }
                                Button(
                                    onClick = { /* trigger SMS compose */ },
                                    modifier = Modifier.weight(1f).testTag("send_sms_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SMS Chat")
                                }
                            }
                        }

                        // Target vs Achievement visual segment
                        item {
                            Text("Operational Performance KPI Indexes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        item {
                            when (val perf = performanceState) {
                                is UiState.Loading -> {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                                is UiState.Error -> {
                                    Text("Metrics calculations delayed or unavailable.", color = MaterialTheme.colorScheme.error)
                                }
                                is UiState.Success -> {
                                    val data = perf.data
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.testTag("detail_metrics_board")
                                    ) {
                                        LinearPerformanceGauge(title = "Customer Visits Completed", achieved = data.visitsCompleted.toDouble(), target = data.visitsTarget.toDouble(), metricString = "${data.visitsCompleted} / ${data.visitsTarget}")
                                        LinearPerformanceGauge(title = "Sales Collections Collected", achieved = data.collectionsAchieved, target = data.collectionsTarget, metricString = "${formatCurrency(data.collectionsAchieved)} / ${formatCurrency(data.collectionsTarget)}")
                                        LinearPerformanceGauge(title = "Assigned Follow-ups Resolved", achieved = data.followUpsCompleted.toDouble(), target = data.followUpsTarget.toDouble(), metricString = "${data.followUpsCompleted} / ${data.followUpsTarget}")

                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("Bad/Overdue recovery rate", fontWeight = FontWeight.SemiBold)
                                                Text("${data.recoverySuccessRate}%", style = MaterialTheme.typography.titleLarge, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Coordinate / Check In History Logs segment
                        item {
                            Text("Recent GPS Verification Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (checkInHistory.isEmpty()) {
                            item {
                                Text("No checkins logged for this field executive yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            items(checkInHistory) { record ->
                                GPSHistoryRecordCard(record = record)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LinearPerformanceGauge(
    title: String,
    achieved: Double,
    target: Double,
    metricString: String
) {
    val progress = if (target > 0) (achieved / target).toFloat().coerceIn(0.0f, 1.0f) else 0.0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(metricString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = if (progress >= 0.8f) Color(0xFF2E7D32) else Color(0xFFE65100),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun GPSHistoryRecordCard(record: CheckInRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(record.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                if (record.isLocationValid) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = "Valid", tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Area Matched", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Invalid", tint = Color.Red, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Out of Bounds", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("In: ${record.checkInTime} (${record.checkInLocation.address})", style = MaterialTheme.typography.bodySmall)
            record.checkOutTime?.let { outTime ->
                Text("Out: $outTime (${record.checkOutLocation?.address ?: ""})", style = MaterialTheme.typography.bodySmall)
            }
            record.remarks?.let { r ->
                if (r.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Executive Remarks: \"$r\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ==========================================
// 4. CHECK-IN / CHECK-OUT SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInCheckOutScreen(
    viewModel: CheckInCheckOutViewModel,
    onBack: () -> Unit
) {
    val currentCheckIn by viewModel.currentCheckIn.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val submittingState by viewModel.submittingState.collectAsState()

    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val address by viewModel.address.collectAsState()

    var remarks by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CrmCustomer?>(null) }
    var showDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("On-Field GPS Logging") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("checkin_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Simulated Area coordinates details card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("gps_info_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = "GPS Lock", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active GPS Telemetry Validation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Coordinates: $latitude, $longitude", style = MaterialTheme.typography.bodyMedium)
                    Text("Parsed Address: $address", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                    Spacer(modifier = Modifier.height(8.dp))
                    // Buttons to change coordinate and simulate check-in at boundary bounds
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { viewModel.simulateNewLocation(18.9224, 72.8341, "Reliance Retail outlet, Gateway, Mumbai") },
                            modifier = Modifier.testTag("simulate_pos_1")
                        ) {
                            Text("Simulate Outlet A")
                        }
                        TextButton(
                            onClick = { viewModel.simulateNewLocation(19.0762, 72.8773, "Bandra Kurla Center Commercial, Mumbai") },
                            modifier = Modifier.testTag("simulate_pos_2")
                        ) {
                            Text("Simulate Outlet B")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentCheckIn == null) {
                // Check-in view
                Text("Register Visit Arrival Check-In", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Customer lookup button dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("select_customer_btn")
                    ) {
                        Text(selectedCustomer?.name ?: "Tap to Target CRM Registry Customer")
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text(cust.name) },
                                onClick = {
                                    selectedCustomer = cust
                                    showDropdown = false
                                },
                                modifier = Modifier.testTag("customer_option_${cust.id}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Visit Purpose / Initial Status remarks") },
                    modifier = Modifier.fillMaxWidth().testTag("checkin_remarks_field"),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val c = selectedCustomer
                        if (c != null) {
                            viewModel.checkIn(c.id, c.name, remarks) {
                                remarks = ""
                                selectedCustomer = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_checkin_btn"),
                    enabled = (selectedCustomer != null && !submittingState)
                ) {
                    if (submittingState) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Clock-In Arrival At Location")
                    }
                }
            } else {
                // Check-out view
                val active = currentCheckIn!!
                Text("Visits Log: Active Session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Customer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        Text(active.customerName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Arrival Time: ${active.checkInTime}", style = MaterialTheme.typography.bodyMedium)
                        active.remarks?.let {
                            if (it.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Check-In Objective: \"$it\"", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Logging Visit Action Outcomes / Outstanding summary") },
                    modifier = Modifier.fillMaxWidth().testTag("checkout_remarks_field"),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.checkOut(remarks) {
                            remarks = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_checkout_btn"),
                    enabled = !submittingState
                ) {
                    if (submittingState) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Clock-Out & Close Customer Visit")
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. CUSTOMER VISIT SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerVisitScreen(
    viewModel: CustomerVisitViewModel,
    onBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val visitHistory by viewModel.visitHistory.collectAsState()
    val submittingState by viewModel.submittingState.collectAsState()

    var selectedCustomer by remember { mutableStateOf<CrmCustomer?>(null) }
    var notes by remember { mutableStateOf("") }
    var outcomeBadge by remember { mutableStateOf("Promise to Pay") }
    var collectedAmountInput by remember { mutableStateOf("") }
    var nextActionPlanned by remember { mutableStateOf("") }
    var nextVisitDate by remember { mutableStateOf("") }

    var showCustDropdown by remember { mutableStateOf(false) }
    var showOutcomeDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Customer Visit") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("visit_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Create Visit Activity Outcome Record", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Select Customer
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showCustDropdown = true },
                    modifier = Modifier.fillMaxWidth().testTag("visit_select_customer")
                ) {
                    Text(selectedCustomer?.name ?: "Tap to Target CRM Registry Customer")
                }

                DropdownMenu(expanded = showCustDropdown, onDismissRequest = { showCustDropdown = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                    customers.forEach { cust ->
                        DropdownMenuItem(
                            text = { Text(cust.name) },
                            onClick = {
                                selectedCustomer = cust
                                showCustDropdown = false
                            },
                            modifier = Modifier.testTag("visit_cust_option_${cust.id}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Select Outcome
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showOutcomeDropdown = true },
                    modifier = Modifier.fillMaxWidth().testTag("visit_select_outcome")
                ) {
                    Text("Outcome: $outcomeBadge")
                }

                DropdownMenu(expanded = showOutcomeDropdown, onDismissRequest = { showOutcomeDropdown = false }) {
                    listOf("Promise to Pay", "Collected", "Not Available", "Rescheduled").forEach { label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                outcomeBadge = label
                                showOutcomeDropdown = false
                            },
                            modifier = Modifier.testTag("visit_outcome_option_$label")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Collected amount (optional)
            OutlinedTextField(
                value = collectedAmountInput,
                onValueChange = { collectedAmountInput = it },
                label = { Text("Outstanding Collected Amount (₹)") },
                modifier = Modifier.fillMaxWidth().testTag("visit_amount_field"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Visit summary notes / meeting minutes details") },
                modifier = Modifier.fillMaxWidth().testTag("visit_notes_field"),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nextActionPlanned,
                onValueChange = { nextActionPlanned = it },
                label = { Text("Next Action planned") },
                modifier = Modifier.fillMaxWidth().testTag("visit_next_action"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nextVisitDate,
                onValueChange = { nextVisitDate = it },
                label = { Text("Next visit Date (e.g., 2026-06-25)") },
                modifier = Modifier.fillMaxWidth().testTag("visit_next_date"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val c = selectedCustomer
                    if (c != null) {
                        val amt = collectedAmountInput.toDoubleOrNull() ?: 0.0
                        viewModel.createVisit(
                            customerId = c.id,
                            customerName = c.name,
                            notes = notes,
                            outcomeBadge = outcomeBadge,
                            collectedAmount = amt,
                            nextActionPlanned = nextActionPlanned.ifBlank { null },
                            nextVisitDate = nextVisitDate.ifBlank { null },
                            lat = 18.922,
                            lon = 72.834,
                            address = "Colaba Gateway outlet location"
                        ) {
                            selectedCustomer = null
                            notes = ""
                            collectedAmountInput = ""
                            nextActionPlanned = ""
                            nextVisitDate = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("visit_submit_btn"),
                enabled = (selectedCustomer != null && !submittingState)
            ) {
                if (submittingState) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Customer Visit details")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Visit History
            Text("Logged Customer Visits History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (visitHistory.isEmpty()) {
                Text("No visit history reports recorded.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            } else {
                visitHistory.forEach { visit ->
                    LoggedVisitCard(visit = visit)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LoggedVisitCard(visit: CustomerVisit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(visit.customerName, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (visit.outcomeBadge) {
                        "Collected" -> Color(0xFFE8F5E9)
                        "Promise to Pay" -> Color(0xFFE3F2FD)
                        else -> Color(0xFFECEFF1)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = visit.outcomeBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (visit.outcomeBadge) {
                            "Collected" -> Color(0xFF2E7D32)
                            "Promise to Pay" -> Color(0xFF1565C0)
                            else -> Color.DarkGray
                        },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Logged by ${visit.performedByUserName} at ${visit.visitStartTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(6.dp))
            Text(visit.notes, style = MaterialTheme.typography.bodyMedium)

            if (visit.collectedAmount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Collected Amount: ${formatCurrency(visit.collectedAmount)}", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
            }
            if (!visit.nextActionPlanned.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Next Planned Step: ${visit.nextActionPlanned} (Duedate: ${visit.nextVisitDate ?: "Not set"})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ==========================================
// 6. ACTIVITY FEED SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFeedScreen(
    viewModel: ActivityFeedViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("On-Field Operations Stream") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("feed_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadFeed() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync feed")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Type filters row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Check-In", "Check-Out", "Collection", "Follow-Up", "Visit").forEach { type ->
                    FilterChip(
                        selected = (filterType == type),
                        onClick = { viewModel.setFilter(type) },
                        label = { Text(type) },
                        modifier = Modifier.testTag("feed_filter_$type")
                    )
                }
            }

            HorizontalDivider()

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).testTag("feed_loading"))
                    }
                    is UiState.Error -> {
                        Text("Error loading stream: ${state.message}", modifier = Modifier.align(Alignment.Center).testTag("feed_error"))
                    }
                    is UiState.Success -> {
                        val list = state.data
                        if (list.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center).testTag("feed_empty"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Stream Clear", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No recent operations correspond to this sub-category filters.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().testTag("feed_lazy_list"),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(list, key = { it.id }) { event ->
                                    ActivityEventRow(event = event)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityEventRow(event: SalesTeamActivityEvent) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("activity_row_${event.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon according to type
            val badgeColor = when (event.type) {
                "Check-In" -> Color(0xFFC8E6C9)
                "Check-Out" -> Color(0xFFFFCC80)
                "Collection" -> Color(0xFFFFE082)
                else -> Color(0xFFCFD8DC)
            }
            val badgeIcon = when (event.type) {
                "Check-In" -> Icons.Default.Place
                "Check-Out" -> Icons.Default.Info
                "Collection" -> Icons.Default.Star
                "Follow-Up" -> Icons.Default.Info
                else -> Icons.Default.Place
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(badgeIcon, contentDescription = null, sizePlaceholder(), tint = Color.DarkGray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(event.performedByUserName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(event.timestamp.substringAfter("T").take(5), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(event.description, style = MaterialTheme.typography.bodyMedium)
                if (event.amount != null && event.amount > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(formatCurrency(event.amount), color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 7. PERFORMANCE DASHBOARD SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceDashboardScreen(
    viewModel: PerformanceDashboardViewModel,
    onBack: () -> Unit
) {
    val timeframe by viewModel.timeframe.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Field Productivity Board") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("perf_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Timeframe Segmented selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Daily", "Weekly", "Monthly").forEach { tf ->
                    ElevatedAssistChip(
                        onClick = { viewModel.setTimeframe(tf) },
                        label = { Text(tf) },
                        colors = if (timeframe == tf) {
                            AssistChipDefaults.elevatedAssistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            AssistChipDefaults.elevatedAssistChipColors()
                        },
                        modifier = Modifier.weight(1f).testTag("perf_timeframe_$tf")
                    )
                }
            }

            HorizontalDivider()

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).testTag("perf_loading"))
                    }
                    is UiState.Error -> {
                        Text("Calculation error: ${state.message}", modifier = Modifier.align(Alignment.Center).testTag("perf_error"))
                    }
                    is UiState.Success -> {
                        val metrics = state.data
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Operational Achievement Index Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            LinearPerformanceGauge(title = "Total Customer Visits", achieved = metrics.visitsCompleted.toDouble(), target = metrics.visitsTarget.toDouble(), metricString = "${metrics.visitsCompleted} / ${metrics.visitsTarget}")
                            LinearPerformanceGauge(title = "Collections Achieved", achieved = metrics.collectionsAchieved, target = metrics.collectionsTarget, metricString = "${formatCurrency(metrics.collectionsAchieved)} / ${formatCurrency(metrics.collectionsTarget)}")
                            LinearPerformanceGauge(title = "Follow-Ups Resolved", achieved = metrics.followUpsCompleted.toDouble(), target = metrics.followUpsTarget.toDouble(), metricString = "${metrics.followUpsCompleted} / ${metrics.followUpsTarget}")

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom Bar Chart representation of Target vs Achievement
                            Text("Interactive Target Trend Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            val points = when (timeframe) {
                                "Daily" -> metrics.dailyProgress
                                "Weekly" -> metrics.weeklyProgress
                                else -> metrics.monthlyProgress
                            }

                            CustomTargetBarChart(points = points)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTargetBarChart(
    points: List<PerformanceChartPoint>
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("target_bar_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                points.forEach { pt ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Multi-bar representation side-by-side
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val targetHeightShare = (pt.target / (points.maxOfOrNull { maxOf(it.target, it.achievement) } ?: 1.0)).coerceIn(0.0, 1.0).toFloat()
                                val achHeightShare = (pt.achievement / (points.maxOfOrNull { maxOf(it.target, it.achievement) } ?: 1.0)).coerceIn(0.0, 1.0).toFloat()

                                // Target red block
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(targetHeightShare)
                                        .padding(horizontal = 2.dp)
                                        .background(Color(0xFFEF5350).copy(alpha = 0.5f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                                // Achievement green block
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(achHeightShare)
                                        .padding(horizontal = 2.dp)
                                        .background(Color(0xFF66BB6A), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pt.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFFEF5350).copy(alpha = 0.5f), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Targets Threshold", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFF66BB6A), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("My Achievement Log", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
