package com.example.feature.outstanding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.common.UiState
import com.example.feature.outstanding.domain.OutstandingDetail
import com.example.feature.outstanding.domain.OutstandingInvoice
import com.example.feature.outstanding.domain.OutstandingPayment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutstandingDetailScreen(
    viewModel: OutstandingDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isActionSubmitting by viewModel.isActionSubmitting.collectAsState()

    var showPromoteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Client 360", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDetail() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload Account")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        modifier = Modifier.fillMaxSize().testTag("outstanding_detail_screen")
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(state.message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadDetail() }) { Text("Retry") }
                        }
                    }
                }
                is UiState.Success -> {
                    val detail = state.data
                    DetailScrollableContent(
                        detail = detail,
                        onPromoteClick = { showPromoteDialog = true }
                    )

                    if (showPromoteDialog) {
                        PromoteStatusDialog(
                            currentStatus = detail.recoveryTracking.recoveryStatus,
                            currentExecutive = detail.recoveryTracking.assignedExecutive,
                            onDismiss = { showPromoteDialog = false },
                            onSubmit = { newStatus, nextAction, exec ->
                                viewModel.promoteRecovery(newStatus, nextAction, exec)
                                showPromoteDialog = false
                            }
                        )
                    }
                }
            }

            if (isActionSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun DetailScrollableContent(
    detail: OutstandingDetail,
    onPromoteClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Customer Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = detail.type.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ID: ${detail.partyId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = detail.partyName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(detail.phone, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(detail.email, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 2. Ledger Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ledger Sheet Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                DetailRow("Ledger Closing Balance", "₹${formatCurrency(detail.ledgerClosingBalance)}")
                DetailRow("Credit Limit Allowed", "₹${formatCurrency(detail.creditLimit)}")
                DetailRow("Immediate Outstanding Debit", "₹${formatCurrency(detail.outstandingAmount)}", highlight = true)
            }
        }

        // 3. Outstanding Aging Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ageing Classification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Simple progress ageing representations
                LabelProgressRow("Current", 0.1f)
                LabelProgressRow("31-60 Days Overdue", 0.4f)
                LabelProgressRow("61-90 Days Overdue", 0.7f, color = MaterialTheme.colorScheme.error)
            }
        }

        // 4. Invoice History List
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Open Invoices History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (detail.billingHistory.isEmpty()) {
                    Text("No pending bills detected for this ledger.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    detail.billingHistory.forEach { invoice ->
                        InvoiceItemRow(invoice = invoice)
                    }
                }
            }
        }

        // 5. Payment History
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recent Payments Cleared", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (detail.paymentHistory.isEmpty()) {
                    Text("No payments registered in the current cycle.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    detail.paymentHistory.forEach { payment ->
                        PaymentItemRow(payment = payment)
                    }
                }
            }
        }

        // 6. Recovery Tracking & Status Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("recovery_tracking_section"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recovery Actions & Work Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = detail.recoveryTracking.recoveryStatus,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                DetailRow("Assigned recovery Executive", detail.recoveryTracking.assignedExecutive)
                DetailRow("Reminders Sent Count", "${detail.recoveryTracking.followUpCount} calls")
                DetailRow("Last follow-up Date", detail.recoveryTracking.lastContactDate ?: "None recorded")
                DetailRow("Next Escalation Target", detail.recoveryTracking.nextActionDate ?: "Not Scheduled")

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onPromoteClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("promote_actions_cta"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Tools")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Promote Recovery & Log Activity State", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun LabelProgressRow(label: String, progress: Float, color: Color = MaterialTheme.colorScheme.primary) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun InvoiceItemRow(invoice: OutstandingInvoice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(invoice.billId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("Date: ${invoice.date} | Due: ${invoice.dueDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("₹${formatCurrency(invoice.pendingAmount)} overdue", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Text("${invoice.daysOverdue} days late", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun PaymentItemRow(payment: OutstandingPayment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(payment.paymentId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("Date: ${payment.date} via ${payment.mode}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("₹${formatCurrency(payment.amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoteStatusDialog(
    currentStatus: String,
    currentExecutive: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var nextActionDate by remember { mutableStateOf("2026-06-30") }
    var executiveName by remember { mutableStateOf(currentExecutive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escalate Account Recovery Status", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select new stage milestone for this client:")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val states = listOf("Normal", "Reminder Sent", "Promised", "Paid")
                    states.forEach { state ->
                        FilterChip(
                            selected = selectedStatus == state,
                            onClick = { selectedStatus = state },
                            label = { Text(state) }
                        )
                    }
                }

                OutlinedTextField(
                    value = executiveName,
                    onValueChange = { executiveName = it },
                    label = { Text("Assigned Collection Officer") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_exec_input")
                )

                OutlinedTextField(
                    value = nextActionDate,
                    onValueChange = { nextActionDate = it },
                    label = { Text("Next Action Target Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_date_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedStatus, nextActionDate, executiveName) },
                modifier = Modifier.testTag("dialog_submit_button")
            ) {
                Text("Confirm Status Level Promotion")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
