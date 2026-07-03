package com.example.feature.followup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.feature.followup.presentation.CreateFollowUpViewModel.FormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpCreateScreen(
    viewModel: CreateFollowUpViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val creationState by viewModel.creationState.collectAsState()
    val availableUsers = viewModel.availableUsers

    // Form Field States
    var selectedCustomerIndex by remember { mutableStateOf(-1) }
    var selectedType by remember { mutableStateOf("Call") }
    var notes by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("High") }
    var dueDate by remember { mutableStateOf("2026-06-20") } // Default pre-fill matching current task context
    var selectedUserIndex by remember { mutableStateOf(0) }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var userDropdownExpanded by remember { mutableStateOf(false) }

    // Navigation and completion trigger
    LaunchedEffect(creationState) {
        if (creationState is FormState.Success) {
            viewModel.resetState()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Action") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("create_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("create_form_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Lookup Box
                item {
                    Column {
                        Text(
                            text = "1. Customer Relationship Profile",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = customerDropdownExpanded,
                            onExpandedChange = { customerDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth().testTag("customer_lookup_box")
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                value = if (selectedCustomerIndex in customers.indices) customers[selectedCustomerIndex].name else "Please select customer...",
                                onValueChange = {},
                                label = { Text("Customer Ledger Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = customerDropdownExpanded,
                                onDismissRequest = { customerDropdownExpanded = false }
                            ) {
                                customers.forEachIndexed { index, customer ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(customer.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                                Text("Bal: ₹${customer.outstandingAmount}  |  Overdue: ₹${customer.overdueAmount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                            }
                                        },
                                        onClick = {
                                            selectedCustomerIndex = index
                                            customerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Interaction Channel Type
                item {
                    Column {
                        Text(
                            text = "2. Follow-Up Method",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = typeDropdownExpanded,
                            onExpandedChange = { typeDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth().testTag("type_spinner_box")
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                value = selectedType,
                                onValueChange = {},
                                label = { Text("Communication Channel") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = typeDropdownExpanded,
                                onDismissRequest = { typeDropdownExpanded = false }
                            ) {
                                listOf("Call", "Visit", "WhatsApp", "Email").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            selectedType = type
                                            typeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Priority Selection
                item {
                    Column {
                        Text(
                            text = "3. Escalation Urgency Priority",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("High", "Medium", "Low").forEach { p ->
                                val isSelected = selectedPriority == p
                                val chipColor = if (isSelected) {
                                    when (p) {
                                        "High" -> Color(0xFFD32F2F)
                                        "Medium" -> Color(0xFFF57C00)
                                        else -> Color(0xFF1976D2)
                                    }
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }

                                val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                                Button(
                                    onClick = { selectedPriority = p },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("priority_chip_$p"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = chipColor,
                                        contentColor = contentColor
                                    ),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(p, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Target Due Date Input
                item {
                    Column {
                        Text(
                            text = "4. Target Completion Date",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_due_date_input"),
                            label = { Text("Task Due Date (YYYY-MM-DD)") },
                            singleLine = true,
                            placeholder = { Text("e.g. 2026-06-25") }
                        )
                    }
                }

                // Assigned Field User
                item {
                    Column {
                        Text(
                            text = "5. Rep Assignment",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = userDropdownExpanded,
                            onExpandedChange = { userDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth().testTag("assignee_lookup_box")
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                value = availableUsers[selectedUserIndex].second,
                                onValueChange = {},
                                label = { Text("Assigned Representative") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = userDropdownExpanded,
                                onDismissRequest = { userDropdownExpanded = false }
                            ) {
                                availableUsers.forEachIndexed { index, user ->
                                    DropdownMenuItem(
                                        text = { Text(user.second) },
                                        onClick = {
                                            selectedUserIndex = index
                                            userDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes Description Body
                item {
                    Column {
                        Text(
                            text = "6. Campaign Strategy Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("create_notes_input"),
                            label = { Text("Task directives and discussion milestones...") },
                            maxLines = 5,
                            placeholder = { Text("Write down the collection points, payment plans agreements or instructions...") }
                        )
                    }
                }

                // State feedback rendering
                item {
                    if (creationState is FormState.Error) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = (creationState as FormState.Error).message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // Submission Buttons
                item {
                    Button(
                        onClick = {
                            if (selectedCustomerIndex in customers.indices) {
                                val cust = customers[selectedCustomerIndex]
                                viewModel.submitFollowUp(
                                    customerId = cust.id,
                                    customerName = cust.name,
                                    type = selectedType,
                                    notes = notes,
                                    priority = selectedPriority,
                                    dueDate = dueDate,
                                    assignedUserIndex = selectedUserIndex
                                )
                            } else {
                                // Empty Selection Error trigger
                                viewModel.submitFollowUp("", "", selectedType, notes, selectedPriority, dueDate, selectedUserIndex)
                            }
                        },
                        enabled = creationState !is FormState.Submitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("create_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (creationState is FormState.Submitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Deploy Operational Campaign", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

// Extracted modifier wrapper to avoid undefined param checks
private fun ButtonColors.isDisabled(boolean: Boolean): ButtonColors = this
