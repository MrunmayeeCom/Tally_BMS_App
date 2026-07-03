package com.example.feature.reminder.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.feature.reminder.domain.AutoReminderRule
import com.example.feature.reminder.domain.ReminderTemplate
import com.example.feature.reminder.domain.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoReminderRulesScreen(
    viewModel: AutoReminderRulesViewModel,
    onBack: () -> Unit
) {
    val rulesState by viewModel.rulesState.collectAsState()
    val templates by viewModel.templates.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Escalation Rules", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("rules_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("rule_add_btn")) {
                        Icon(Icons.Default.Add, contentDescription = "Add Rule")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_rule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Auto Rule")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = rulesState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("rules_loading"))
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(state.message, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadRulesAndTemplates() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val list = state.data
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                Icon(Icons.Default.Settings, contentDescription = "No Rules", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No Active Escalation Rules Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Configure automated channels for overdue dunning targets.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("auto_rules_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text("Operational Rule Matrices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Automated chron-escalation workflows triggered on invoice weight.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(list) { rule ->
                                RuleItemRow(
                                    rule = rule,
                                    templates = templates,
                                    onToggle = { active -> viewModel.toggleRule(rule.id, active) }
                                )
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                AddRuleDialog(
                    templates = templates,
                    onDismiss = { showAddDialog = false },
                    onSubmit = { name, type, value, comm, templateId ->
                        viewModel.createRule(name, type, value, comm, templateId)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun RuleItemRow(
    rule: AutoReminderRule,
    templates: List<ReminderTemplate>,
    onToggle: (Boolean) -> Unit
) {
    val templateName = templates.find { it.id == rule.templateId }?.name ?: "Unknown Template"

    Card(
        modifier = Modifier.fillMaxWidth().testTag("rule_card_${rule.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(rule.communicationType) {
                            "WhatsApp" -> Icons.Default.Info
                            "Email" -> Icons.Default.Email
                            else -> Icons.Default.List
                        },
                        contentDescription = rule.communicationType,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(rule.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = rule.isActive,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("rule_toggle_${rule.id}")
                )
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text("Trigger Metric: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = when(rule.triggerType) {
                            "outstanding_age" -> "Overdue Age > ${rule.triggerValue} Days"
                            "amount_based" -> "Outstanding Balance > ₹${rule.triggerValue}"
                            "customer_category" -> "Customer Class = ${rule.triggerValue}"
                            else -> "Recovery Status = ${rule.triggerValue}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Row {
                    Text("Outbound Channel: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(rule.communicationType, style = MaterialTheme.typography.bodySmall)
                }
                Row {
                    Text("Linked Copy: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(templateName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleDialog(
    templates: List<ReminderTemplate>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var triggerType by remember { mutableStateOf("outstanding_age") }
    var triggerValue by remember { mutableStateOf("") }
    var commType by remember { mutableStateOf("WhatsApp") }
    var selectedTemplateId by remember { mutableStateOf(templates.firstOrNull()?.id ?: "") }

    var templateExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Auto Escalation Rule", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Workflow Name") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_rule_name_input")
                    )
                }
                item {
                    Text("Establish Outbound Channel", style = MaterialTheme.typography.labelMedium)
                    val comms = listOf("WhatsApp", "SMS", "Email")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        comms.forEach { channels ->
                            FilterChip(
                                selected = (commType == channels),
                                onClick = { commType = channels },
                                label = { Text(channels) }
                            )
                        }
                    }
                }
                item {
                    Text("Dunning Trigger Metric", style = MaterialTheme.typography.labelMedium)
                    val metrics = listOf(
                        "outstanding_age" to "Age (Days)",
                        "amount_based" to "Amount (₹)",
                        "customer_category" to "CRM Segment"
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        metrics.forEach { (mCode, mValue) ->
                            FilterChip(
                                selected = (triggerType == mCode),
                                onClick = { triggerType = mCode },
                                label = { Text(mValue) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = triggerValue,
                        onValueChange = { triggerValue = it },
                        label = {
                            Text(
                                text = when(triggerType) {
                                    "outstanding_age" -> "Ages past due (e.g. 45)"
                                    "amount_based" -> "Values exceeding (e.g. 50000)"
                                    else -> "CRM status badge (e.g. Risky)"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_rule_val_input")
                    )
                }
                item {
                    Text("Linked Copy Document", style = MaterialTheme.typography.labelMedium)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = templates.find { it.id == selectedTemplateId }?.name ?: "No template matched",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { templateExpanded = true },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                        )
                        DropdownMenu(
                            expanded = templateExpanded,
                            onDismissRequest = { templateExpanded = false }
                        ) {
                            templates.forEach { temp ->
                                DropdownMenuItem(
                                    text = { Text("${temp.name} (${temp.type})") },
                                    onClick = {
                                        selectedTemplateId = temp.id
                                        templateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && triggerValue.isNotBlank()) {
                        onSubmit(name, triggerType, triggerValue, commType, selectedTemplateId)
                    }
                },
                modifier = Modifier.testTag("dialog_rule_submit_btn")
            ) {
                Text("Commit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
