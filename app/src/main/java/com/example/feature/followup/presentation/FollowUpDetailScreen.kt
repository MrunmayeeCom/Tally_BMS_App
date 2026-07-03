package com.example.feature.followup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.feature.followup.domain.FollowUpDetail
import com.example.feature.followup.domain.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpDetailScreen(
    viewModel: FollowUpDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Engagement", "Financials", "Communications", "Audit")

    // Modals controlling states
    var showStatusUpdateDialog by remember { mutableStateOf(false) }
    var quickNoteText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Follow-Up Workspace") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDetail() }, modifier = Modifier.testTag("detail_sync_btn")) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Details")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Database Sync Failed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.message, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadDetail() }) {
                                Text("Retry Workspace Initializer")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val detail = state.data
                    val fo = detail.followUp

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header info slice
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(fo.customerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("Assigned Rep: ${fo.assignedUserName} (${viewModel.getUserRole()})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = getStatusBgColor(fo.status),
                                        modifier = Modifier.testTag("status_badge_detail")
                                    ) {
                                        Text(
                                            fo.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                if (!fo.outcome.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Latest Outcome: ${fo.outcome}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }

                        // Module tabs index
                        TabRow(
                            selectedTabIndex = activeTab,
                            modifier = Modifier.fillMaxWidth().testTag("detail_tab_bar")
                        ) {
                            tabTitles.forEachIndexed { idx, title ->
                                Tab(
                                    selected = activeTab == idx,
                                    onClick = { activeTab = idx },
                                    text = { Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }

                        // Dynamic Tab rendering
                        Box(modifier = Modifier.weight(1f)) {
                            when (activeTab) {
                                0 -> EngagementTab(
                                    detail = detail,
                                    quickNoteText = quickNoteText,
                                    onNoteChange = { quickNoteText = it },
                                    onSubmitNote = {
                                        viewModel.addTimelineNote(quickNoteText)
                                        quickNoteText = ""
                                    },
                                    onUpdateWorkflow = { showStatusUpdateDialog = true }
                                )
                                1 -> FinancialsTab(detail = detail)
                                2 -> CommunicationsTab(detail = detail)
                                3 -> AuditTab(detail = detail)
                            }
                        }
                    }

                    // Status Workflow modal launcher block
                    if (showStatusUpdateDialog) {
                        StatusUpdateWorkflowDialog(
                            currentStatus = fo.status,
                            onDismiss = { showStatusUpdateDialog = false },
                            onConfirm = { status, notes, amt, dt ->
                                viewModel.updateStatus(status, notes, amt, dt)
                                showStatusUpdateDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ====================== Engagement Tab (Chronologies & note actions) ======================
@Composable
fun EngagementTab(
    detail: FollowUpDetail,
    quickNoteText: String,
    onNoteChange: (String) -> Unit,
    onSubmitNote: () -> Unit,
    onUpdateWorkflow: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("detail_engagement_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Direct Directives card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Campaign Objective Guidelines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(detail.followUp.notes, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Action Level: ${detail.followUp.priority}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Target: ${detail.followUp.dueDate}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    }
                }
            }
        }

        // Action workflow buttons trigger
        item {
            Button(
                onClick = onUpdateWorkflow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("action_btn_update_status"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transition Workflow Progress State", fontWeight = FontWeight.SemiBold)
            }
        }

        // Quick Node Addition Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Record Ongoing Interaction Note", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quickNoteText,
                        onValueChange = onNoteChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_note_input"),
                        placeholder = { Text("e.g. Spoke to Mr. Suresh, check is printed. Will pick up tomorrow.") },
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = onSubmitNote,
                            enabled = quickNoteText.isNotBlank(),
                            modifier = Modifier.testTag("quick_note_submit_btn")
                        ) {
                            Text("Post note")
                        }
                    }
                }
            }
        }

        // Timeline Chronicles title
        item {
            Text("Operation Timeline Chronicles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        // Timeline Chronicles elements
        if (detail.notesTimeline.isEmpty()) {
            item {
                Text("No chronicles logged on this campaign.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        } else {
            items(detail.notesTimeline) { node ->
                TimelineNodeItem(node = node)
            }
        }
    }
}

// Timeline node row card
@Composable
fun TimelineNodeItem(node: com.example.feature.followup.domain.TimelineNode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (node.statusColor) {
                            "Completed" -> Color(0xFF2E7D32)
                            "Promised Payment" -> Color(0xFFE65100)
                            "In Progress" -> Color(0xFFFFB300)
                            "Cancelled" -> Color(0xFFC62828)
                            "NoteAdded" -> Color.Gray
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
            )
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(65.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(node.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("By ${node.author}  |  ${node.date}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    node.description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

// ====================== Financials Tab (Aging report and CRM profiles) ======================
@Composable
fun FinancialsTab(detail: FollowUpDetail) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("detail_financials_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // CRM Contact Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CRM Registry Customer Directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    CrmContactItem(Icons.Default.Person, "Contact Executive", detail.customerInfo.keyContactPerson)
                    CrmContactItem(Icons.Default.Phone, "Mobile/Phone", detail.customerInfo.phone)
                    CrmContactItem(Icons.Default.Email, "Corporate Email", detail.customerInfo.email)
                    CrmContactItem(Icons.Default.Home, "Billing Address", detail.customerInfo.address)
                }
            }
        }

        // Outstanding analysis
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE65100))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Outstanding Ledger Assessment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Credit Ceiling Limit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        Text("₹5,00,000.00", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Combined Core Outstanding", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "₹${detail.outstandingSummary.totalOutstanding}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Aging Outstanding Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    AgingRowComponent("0 - 30 days cycle", detail.outstandingSummary.overdue30Days, Color(0xFF4CAF50))
                    AgingRowComponent("31 - 60 days cycle", detail.outstandingSummary.overdue60Days, Color(0xFF8BC34A))
                    AgingRowComponent("61 - 90 days cycle", detail.outstandingSummary.overdue90Days, Color(0xFFFF9800))
                    AgingRowComponent("Over 90 days breach", detail.outstandingSummary.overdueOver90Days, Color(0xFFF44336))
                }
            }
        }
    }
}

@Composable
fun CrmContactItem(icon: ImageVector, label: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
        }
        Text(content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 22.dp))
    }
}

@Composable
fun AgingRowComponent(label: String, value: Double, indicatorColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text("₹${String.format("%.2f", value)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

// ====================== Communications Tab (Reminder blasts + history) ======================
@Composable
fun CommunicationsTab(detail: FollowUpDetail) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("detail_communications_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Auto-Dunning Broadcasts History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (detail.reminderHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No reminder engine logs registered for this ledger.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(detail.reminderHistory) { rem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (rem.type) {
                                        "WhatsApp" -> Icons.Default.Phone
                                        "SMS" -> Icons.Default.Send
                                        else -> Icons.Default.Email
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(rem.type, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (rem.status == "Sent") Color(0xFFC8E6C9) else Color(0xFFFFCDD2),
                                contentColor = if (rem.status == "Sent") Color(0xFF2E7D32) else Color(0xFFC62828)
                            ) {
                                Text(rem.status, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(rem.message, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Sent at: ${rem.scheduleDateTime} By ${rem.assigneeName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Past Interaction Schedule Runs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (detail.interactionHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No prior custom follow-ups recorded.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(detail.interactionHistory) { hx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(hx.type, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(hx.dueDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(hx.notes, style = MaterialTheme.typography.bodySmall)
                        if (!hx.outcome.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Outcome: ${hx.outcome}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// ====================== Audit Tab (Trace sequences) ======================
@Composable
fun AuditTab(detail: FollowUpDetail) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("detail_audit_tab"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Secured Multi-Tenant Operational Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Traceability log required for ISO 27001 / dunning auditing.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (detail.activityLog.isEmpty()) {
            item {
                Text("No audited movements in this transaction session scope.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        } else {
            items(detail.activityLog) { audit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(audit.action, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(audit.timestamp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(audit.details, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Signature: ${audit.operatorName} (Audit Verified)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

// ====================== Workflow Status transition model dialog ======================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusUpdateWorkflowDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (status: String, outcome: String, promisedAmt: Double?, promisedDt: String?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var outcomeNotes by remember { mutableStateOf("") }
    var promisedAmount by remember { mutableStateOf("") }
    var promisedDate by remember { mutableStateOf("2026-06-27") } // prefill to target task context (today is June 20, prefill 27th)

    val workflowStages = listOf("Open", "In Progress", "Waiting Response", "Promised Payment", "Completed", "Cancelled")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Progress Flow transition") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("status_workflow_dialog_scroll"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pick status
                item {
                    Text("Select Target Workflow State:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                value = selectedStatus,
                                onValueChange = {},
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                workflowStages.forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st) },
                                        onClick = {
                                            selectedStatus = st
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // If Paid/Promised status chosen, show financials commitment inputs
                if (selectedStatus == "Promised Payment") {
                    item {
                        Column {
                            Text("Promised Commitment Amount:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = promisedAmount,
                                onValueChange = { promisedAmount = it },
                                placeholder = { Text("e.g. 42000.0") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("dialog_promised_amt_input")
                            )
                        }
                    }
                    item {
                        Column {
                            Text("Promised Settlement Date:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = promisedDate,
                                onValueChange = { promisedDate = it },
                                placeholder = { Text("YYYY-MM-DD") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("dialog_promised_dt_input")
                            )
                        }
                    }
                }

                // Interaction outcome summary notes
                item {
                    Column {
                        Text("Directives / Outcome Summary:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = outcomeNotes,
                            onValueChange = { outcomeNotes = it },
                            placeholder = { Text("e.g. client agreed to pay, check is processed, dunning email acknowledged.") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("dialog_outcome_input")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtDouble = promisedAmount.toDoubleOrNull()
                    onConfirm(selectedStatus, outcomeNotes, amtDouble, promisedDate)
                },
                modifier = Modifier.testTag("dialog_confirm_btn")
            ) {
                Text("Apply Transition")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_cancel_btn")) {
                Text("Cancel")
            }
        }
    )
}

// Global color mapper helper
fun getStatusBgColor(status: String): Color {
    return when (status) {
        "Completed" -> Color(0xFF2E7D32)
        "Promised Payment" -> Color(0xFFE65100)
        "In Progress" -> Color(0xFFFFB300)
        "Waiting Response" -> Color(0xFF1565C0)
        "Cancelled" -> Color(0xFFC62828)
        else -> Color(0xFF6200EE)
    }
}
