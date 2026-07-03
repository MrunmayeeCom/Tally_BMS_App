package com.example.feature.reminder.presentation

import androidx.compose.animation.AnimatedVisibility
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
import com.example.feature.reminder.domain.ReminderTemplate
import com.example.feature.reminder.domain.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTemplatesScreen(
    viewModel: ReminderTemplatesViewModel,
    onBack: () -> Unit
) {
    val templatesState by viewModel.templatesState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<ReminderTemplate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates Library", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("templates_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("template_add_btn")) {
                        Icon(Icons.Default.Add, contentDescription = "New Template")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_template_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Template")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = templatesState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("templates_loading"))
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(state.message, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadTemplates() }) {
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
                                Icon(Icons.Default.Edit, contentDescription = "Empty", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No Layout Templates Customizer Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Build templates for automated or ad-hoc campaigns.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("templates_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Text("Global Message Presets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Rich text documents injected with dynamic database parameters.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            items(list) { temp ->
                                TemplateItemRow(
                                    template = temp,
                                    onEdit = { editingTemplate = temp }
                                )
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                TemplateEditorDialog(
                    template = null,
                    onDismiss = { showAddDialog = false },
                    onSubmit = { name, type, subject, body ->
                        viewModel.addTemplate(name, type, subject, body)
                        showAddDialog = false
                    }
                )
            }

            if (editingTemplate != null) {
                TemplateEditorDialog(
                    template = editingTemplate,
                    onDismiss = { editingTemplate = null },
                    onSubmit = { name, type, subject, body ->
                        viewModel.editTemplate(editingTemplate!!.id, name, type, subject, body)
                        editingTemplate = null
                    }
                )
            }
        }
    }
}

@Composable
fun TemplateItemRow(
    template: ReminderTemplate,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("template_card_${template.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
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
                        imageVector = when(template.type) {
                            "WhatsApp" -> Icons.Default.Info
                            "Email" -> Icons.Default.Email
                            "SMS" -> Icons.Default.List
                            else -> Icons.Default.Phone
                        },
                        contentDescription = template.type,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(template.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp).testTag("edit_template_${template.id}")) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Template", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (template.type == "Email" && !template.subject.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("Subject: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(template.subject ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Text("Text Body Copy Format:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(template.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun TemplateEditorDialog(
    template: ReminderTemplate?,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String?, String) -> Unit
) {
    var name by remember { mutableStateOf(template?.name ?: "") }
    var type by remember { mutableStateOf(template?.type ?: "WhatsApp") }
    var subject by remember { mutableStateOf(template?.subject ?: "") }
    var body by remember { mutableStateOf(template?.body ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (template == null) "Create Message Template" else "Edit Template: ${template.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Template Description") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_temp_name_input")
                    )
                }
                item {
                    Text("Outbound Channel Category", style = MaterialTheme.typography.labelSmall)
                    val types = listOf("WhatsApp", "SMS", "Email", "Custom")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { t ->
                            FilterChip(
                                selected = (type == t),
                                onClick = { type = t },
                                label = { Text(t) }
                            )
                        }
                    }
                }
                item {
                    AnimatedVisibility(visible = (type == "Email")) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject Line") },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_temp_subject_input")
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Message Body Copy Structure") },
                        modifier = Modifier.fillMaxWidth().height(120.dp).testTag("dialog_temp_body_input"),
                        placeholder = { Text("e.g., Dear [Customer Name], invoice [Invoice No] of [Outstanding Amount] is due on [Due Date]...") }
                    )
                }
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Variable Helper Tooltip", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                "Inject tags like [Customer Name], [Invoice No], [Outstanding Amount] to dynamically resolve balance fields.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && body.isNotBlank()) {
                        onSubmit(name, type, subject, body)
                    }
                },
                modifier = Modifier.testTag("dialog_temp_submit_btn")
            ) {
                Text("Commit Draft")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
