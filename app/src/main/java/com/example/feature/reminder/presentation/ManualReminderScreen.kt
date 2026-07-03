package com.example.feature.reminder.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.outstanding.domain.OutstandingItem
import com.example.feature.reminder.domain.ReminderTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualReminderScreen(
    viewModel: ManualReminderViewModel,
    onBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val invoices by viewModel.outstandingInvoices.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val assigneeName by viewModel.assigneeName.collectAsState()

    // Form field states
    var selectedCustomer by remember { mutableStateOf<CrmCustomer?>(null) }
    var selectedInvoice by remember { mutableStateOf<OutstandingItem?>(null) }
    var reminderType by remember { mutableStateOf("WhatsApp") }
    var messageText by remember { mutableStateOf("") }
    var scheduleDate by remember { mutableStateOf("2026-06-20") }
    var scheduleTime by remember { mutableStateOf("10:00 AM") }

    // Dropdown expansion states
    var customerExpanded by remember { mutableStateOf(false) }
    var invoiceExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var templateExpanded by remember { mutableStateOf(false) }

    // Set initial values if passed in
    LaunchedEffect(viewModel.initialPartyId, customers, invoices) {
        if (viewModel.initialPartyId.isNotBlank()) {
            val customerMatched = customers.find { it.id == viewModel.initialPartyId }
            if (customerMatched != null) {
                selectedCustomer = customerMatched
            }
        }
        if (viewModel.initialBillId != null && invoices.isNotEmpty()) {
            val invoiceMatched = invoices.find { it.partyId == viewModel.initialPartyId }
            if (invoiceMatched != null) {
                selectedInvoice = invoiceMatched
            }
        }
    }

    // Handle template autofill
    fun applyTemplate(template: ReminderTemplate) {
        val custName = selectedCustomer?.name ?: "[Customer Name]"
        val invNo = selectedInvoice?.partyName ?: selectedInvoice?.partyId ?: "[Invoice No]"
        val amount = selectedInvoice?.outstandingAmount?.let { "₹$it" } ?: "[Outstanding Amount]"
        
        var body = template.body
        body = body.replace("[Customer Name]", custName)
        body = body.replace("[Customer]", custName)
        body = body.replace("[Invoice No]", invNo)
        body = body.replace("[Invoice Number]", invNo)
        body = body.replace("[Outstanding Amount]", amount)
        body = body.replace("[Due Date]", "2026-06-30")
        body = body.replace("[Company Name]", "TallyBMS Cloud Store")
        body = body.replace("[Action Date]", "2026-07-05")
        
        messageText = body
        reminderType = template.type
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manual Reminder Composer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("composer_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("manual_composer_form"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success/Error Feedback Alerts
                item {
                    if (successMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Reminder Dispatched", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text(successMessage ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }

                // 1. SELECT CUSTOMER (CRM Integration)
                item {
                    Text("1. Target Customer (CRM Portal)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: "Tap to choose CRM Customer...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_customer_trigger")
                                .clickable { customerExpanded = true },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            enabled = true
                        )
                        DropdownMenu(
                            expanded = customerExpanded,
                            onDismissRequest = { customerExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(customer.name, fontWeight = FontWeight.SemiBold)
                                            Text("CRM Balance: ₹${customer.outstandingAmount} (${customer.statusBadge})", style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        selectedCustomer = customer
                                        customerExpanded = false
                                        // Auto-filter corresponding invoices
                                        selectedInvoice = invoices.find { it.partyId == customer.id }
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. SELECT OUTSTANDING INVOICE (Outstanding Module Integration)
                item {
                    Text("2. Select Overdue Outstanding Code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredInvoices = if (selectedCustomer != null) {
                        invoices.filter { it.partyId == selectedCustomer!!.id }
                    } else {
                        invoices
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedInvoice?.let { "${it.partyName} - ₹${it.outstandingAmount}" } ?: "Select billing entries...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_invoice_trigger")
                                .clickable { invoiceExpanded = true },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                        )
                        DropdownMenu(
                            expanded = invoiceExpanded,
                            onDismissRequest = { invoiceExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            filteredInvoices.forEach { inv ->
                                DropdownMenuItem(
                                    text = {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${inv.partyId} Details")
                                            Text("₹${inv.overdueAmount}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        selectedInvoice = inv
                                        invoiceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. APPLY PRESET TEMPLATES
                item {
                    Text("Optional: Auto-populate with Templated Copy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "Select layout template...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { templateExpanded = true },
                            trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                        )
                        DropdownMenu(
                            expanded = templateExpanded,
                            onDismissRequest = { templateExpanded = false }
                        ) {
                            templates.forEach { temp ->
                                DropdownMenuItem(
                                    text = { Text("${temp.name} (${temp.type})") },
                                    onClick = {
                                        applyTemplate(temp)
                                        templateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 4. REMINDER TYPE
                item {
                    Text("3. Communication Channel Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val types = listOf("WhatsApp", "SMS", "Email", "Custom/Call")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { type ->
                            FilterChip(
                                selected = (reminderType == type),
                                onClick = { reminderType = type },
                                label = { Text(type) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 5. MESSAGE TEXT
                item {
                    Text("4. Reminder Message Content Layout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("message_text_input"),
                        placeholder = { Text("Draft critical collections content warnings or payment terms...") }
                    )
                }

                // 6. SCHEDULE CALENDAR
                item {
                    Text("5. Configuration Dispatch Parameters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = scheduleDate,
                            onValueChange = { scheduleDate = it },
                            label = { Text("Schedule Date") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                        )
                        OutlinedTextField(
                            value = scheduleTime,
                            onValueChange = { scheduleTime = it },
                            label = { Text("Schedule Time") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                        )
                    }
                }

                // 7. ROLE DETAILS & SUBMIT
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = "Role Status", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Dispatcher Profile: $assigneeName ($userRole)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            viewModel.submitReminder(
                                partyId = selectedCustomer?.id ?: "",
                                partyName = selectedCustomer?.name ?: "",
                                billId = selectedInvoice?.partyId,
                                billNo = selectedInvoice?.partyName,
                                type = reminderType,
                                message = messageText,
                                scheduleDate = scheduleDate,
                                scheduleTime = scheduleTime
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reminder_send_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Schedule & Deploy Reminder", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
