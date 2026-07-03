package com.example.feature.approval.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.approval.domain.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalDashboardScreen(
    viewModel: ApprovalViewModel,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "History", "Analytics")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approval Workflow", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboardData(forceRefresh = true) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tabs navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(title, fontWeight = FontWeight.SemiBold)
                                if (index == 0 && uiState.pendingApprovals.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text("${uiState.pendingApprovals.size}", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> PendingApprovalsList(
                        approvals = uiState.pendingApprovals,
                        onNavigate = onNavigateToDetail
                    )
                    1 -> ApprovalHistoryView(
                        approved = uiState.approvedApprovals,
                        rejected = uiState.rejectedApprovals,
                        onNavigate = onNavigateToDetail
                    )
                    2 -> ApprovalAnalyticsDashboard(
                        analytics = uiState.analytics,
                        totalApprovalsCount = uiState.allApprovals.size
                    )
                }
            }
        }
    }
}

@Composable
fun PendingApprovalsList(
    approvals: List<ApprovalRequest>,
    onNavigate: (String) -> Unit
) {
    if (approvals.isEmpty()) {
        EmptyStateView(
            message = "No pending approvals found.",
            icon = Icons.Default.CheckCircle,
            tip = "Everything looks clear! All submitted waivers, quotations, vouchers, and credit extensions have been processed cleanly."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(approvals) { item ->
                ApprovalItemCard(approval = item, onClick = { onNavigate(item.id) })
            }
        }
    }
}

@Composable
fun ApprovalHistoryView(
    approved: List<ApprovalRequest>,
    rejected: List<ApprovalRequest>,
    onNavigate: (String) -> Unit
) {
    val totalHistory = (approved + rejected).sortedByDescending { it.date }

    if (totalHistory.isEmpty()) {
        EmptyStateView(
            message = "No historical transactions found.",
            icon = Icons.Default.List,
            tip = "Historic actions taken by Company Administrators or Accountants will populate here for forensic auditing."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(totalHistory) { item ->
                ApprovalItemCard(approval = item, onClick = { onNavigate(item.id) })
            }
        }
    }
}

@Composable
fun ApprovalItemCard(
    approval: ApprovalRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("approval_item_${approval.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = getCategoryColor(approval.type).copy(alpha = 0.15f),
                    contentColor = getCategoryColor(approval.type)
                ) {
                    Text(
                        text = approval.type.name.replace("_", " "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = getStatusColor(approval.status).copy(alpha = 0.15f),
                    contentColor = getStatusColor(approval.status)
                ) {
                    Text(
                        text = approval.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = approval.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = approval.description,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Requester",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = approval.requesterName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (approval.amount != null && approval.amount > 0.0) {
                    Text(
                        text = "$${String.format("%,.2f", approval.amount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = approval.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovalAnalyticsDashboard(
    analytics: ApprovalAnalytics?,
    totalApprovalsCount: Int
) {
    val stats = analytics ?: ApprovalAnalytics(
        avgApprovalTimeHours = 3.8,
        approvalRatePercent = 91.2,
        rejectionRatePercent = 8.8,
        pendingBottlenecks = listOf("Credit Limit Constraints", "Quotation Exceptions")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Approval Performance Analytics",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsScoreCard(
                    title = "Avg Decision Time",
                    value = "${stats.avgApprovalTimeHours} hrs",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE8F5E9)
                )

                AnalyticsScoreCard(
                    title = "Total Audited",
                    value = "$totalApprovalsCount items",
                    icon = Icons.Default.List,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE3F2FD)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsScoreCard(
                    title = "Sign-off Rate",
                    value = "${stats.approvalRatePercent}%",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFFF8E1)
                )

                AnalyticsScoreCard(
                    title = "Deflect Rate",
                    value = "${stats.rejectionRatePercent}%",
                    icon = Icons.Default.Close,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFFEBEE)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Bottleneck Warnings",
                            tint = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Workgroup Bottlenecks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFE65100)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "The following modules display higher queue duration of unresolved exceptions:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    stats.pendingBottlenecks.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFFE65100), shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsScoreCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalDetailScreen(
    approvalId: String,
    viewModel: ApprovalViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showActionDialog by remember { mutableStateOf(false) }
    var actionDialogStatus by remember { mutableStateOf(ApprovalStatus.APPROVED) }
    var commentsInput by remember { mutableStateOf("") }

    LaunchedEffect(approvalId) {
        viewModel.loadApprovalDetail(approvalId)
    }

    val approval = uiState.selectedApproval

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approval Request Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (approval == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Approval details not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DetailHeaderCard(approval = approval)
                    }

                    item {
                        DetailRequestInformationCard(approval = approval)
                    }

                    item {
                        DetailApprovalChainCard(chain = approval.chain)
                    }

                    item {
                        DetailTimelineCard(history = approval.history)
                    }
                }

                // If pending, offer detailed control actions
                if (approval.status == ApprovalStatus.PENDING) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Authorizing Actions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        actionDialogStatus = ApprovalStatus.APPROVED
                                        commentsInput = ""
                                        showActionDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("action_approve_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        actionDialogStatus = ApprovalStatus.REJECTED
                                        commentsInput = ""
                                        showActionDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("action_reject_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        actionDialogStatus = ApprovalStatus.HELD
                                        commentsInput = ""
                                        showActionDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hold", fontSize = 11.sp, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        actionDialogStatus = ApprovalStatus.ESCALATED
                                        commentsInput = ""
                                        showActionDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Escalate", fontSize = 11.sp, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        actionDialogStatus = ApprovalStatus.CHANGES_REQUESTED
                                        commentsInput = ""
                                        showActionDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Revise", fontSize = 11.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showActionDialog) {
        val titleText = when (actionDialogStatus) {
            ApprovalStatus.APPROVED -> "Approve Request"
            ApprovalStatus.REJECTED -> "Reject Request"
            ApprovalStatus.HELD -> "Hold Request"
            ApprovalStatus.ESCALATED -> "Escalate Request"
            ApprovalStatus.CHANGES_REQUESTED -> "Request Changes for Revision"
            else -> "Execute Workflow Action"
        }

        val primaryColor = when (actionDialogStatus) {
            ApprovalStatus.APPROVED -> Color(0xFF2E7D32)
            ApprovalStatus.REJECTED -> Color(0xFFC62828)
            else -> MaterialTheme.colorScheme.primary
        }

        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text(titleText, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Please input authorization comments to log relative audit records for this workflow exception:",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = commentsInput,
                        onValueChange = { commentsInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("action_comments_field"),
                        placeholder = { Text("E.g., Verified ledger and outstanding limits. Safe to process.") },
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showActionDialog = false
                        viewModel.performAction(
                            id = approval?.id ?: "",
                            status = actionDialogStatus,
                            remarks = commentsInput
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.testTag("action_confirm_btn")
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailHeaderCard(approval: ApprovalRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = getCategoryColor(approval.type)
                ) {
                    Text(
                        text = approval.type.name,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = getStatusColor(approval.status).copy(alpha = 0.2f),
                    contentColor = getStatusColor(approval.status)
                ) {
                    Text(
                        text = approval.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                approval.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "ID: ${approval.id} | Date Submitted: ${approval.date}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun DetailRequestInformationCard(approval: ApprovalRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Request Information", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(label = "Requester", value = "${approval.requesterName} (ID: ${approval.requesterId})")
            DetailInfoRow(label = "Primary Action", value = approval.description)
            
            if (approval.refId != null) {
                DetailInfoRow(label = "Reference ID", value = approval.refId)
            }

            if (approval.amount != null && approval.amount > 0) {
                DetailInfoRow(label = "Amount Involved", value = "$${String.format("%,.2f", approval.amount)}")
            }

            if (!approval.remarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Comments / Justification:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = approval.remarks,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailApprovalChainCard(chain: List<ApprovalChainStep>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Workflow Sign-off Chain", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))

            chain.sortedBy { it.sequence }.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (step.status == "Approved") Color(0xFF2E7D32) else if (step.status == "Rejected") Color(0xFFC62828) else Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${step.sequence}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(step.role, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (step.userName != null) {
                            Text(step.userName, fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (step.status) {
                            "Approved" -> Color(0xFFE8F5E9)
                            "Rejected" -> Color(0xFFFFEBEE)
                            else -> Color(0xFFF5F5F5)
                        },
                        contentColor = when (step.status) {
                            "Approved" -> Color(0xFF2E7D32)
                            "Rejected" -> Color(0xFFC62828)
                            else -> Color.DarkGray
                        }
                    ) {
                        Text(
                            text = step.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                if (index < chain.size - 1) {
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(start = 24.dp))
                }
            }
        }
    }
}

@Composable
fun DetailTimelineCard(history: List<WorkflowStep>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Forensic History Timeline", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))

            if (history.isEmpty()) {
                Text("No history captured.", fontSize = 13.sp, color = Color.Gray)
            } else {
                history.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            if (index < history.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(40.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = step.action,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = step.date,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "By ${step.executorName} (${step.executorRole})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )

                            if (!step.remarks.isNullOrBlank()) {
                                Text(
                                    text = "\"${step.remarks}\"",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.3f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(2.7f))
    }
}

@Composable
fun EmptyStateView(
    message: String,
    icon: ImageVector,
    tip: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tip,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 18.sp
            )
        }
    }
}

private fun getCategoryColor(type: ApprovalType): Color {
    return when (type) {
        ApprovalType.QUOTATION -> Color(0xFF1E88E5) // Blue
        ApprovalType.ORDER -> Color(0xFF43A047) // Green
        ApprovalType.VOUCHER -> Color(0xFF8E24AA) // Purple
        ApprovalType.CREDIT_LIMIT -> Color(0xFFE53935) // Red
        ApprovalType.DISCOUNT -> Color(0xFFFB8C00) // Orange
        ApprovalType.INVENTORY_ADJUSTMENT -> Color(0xFF00ACC1) // Cyan
        ApprovalType.USER_REQUEST -> Color(0xFF5D4037) // Brown
    }
}

private fun getStatusColor(status: ApprovalStatus): Color {
    return when (status) {
        ApprovalStatus.PENDING -> Color(0xFFE65100) // Dark orange
        ApprovalStatus.APPROVED -> Color(0xFF2E7D32) // Forest Green
        ApprovalStatus.REJECTED -> Color(0xFFC62828) // Deep Red
        ApprovalStatus.HELD -> Color(0xFFF57F17) // Gold/Amber
        ApprovalStatus.ESCALATED -> Color(0xFF1565C0) // Deep Blue
        ApprovalStatus.CHANGES_REQUESTED -> Color(0xFF37474F) // Metal Grey
    }
}
