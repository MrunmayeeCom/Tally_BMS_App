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
import com.example.feature.reminder.domain.ReminderSchedulerRule
import com.example.feature.reminder.domain.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSchedulerScreen(
    viewModel: ReminderSchedulerViewModel,
    onBack: () -> Unit
) {
    val schedulersState by viewModel.schedulersState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dunning Schedulers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("scheduler_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("scheduler_add_btn")) {
                        Icon(Icons.Default.Add, contentDescription = "Add Scheduler")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_scheduler_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Scheduler Profile")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = schedulersState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("schedulers_loading"))
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(state.message, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadSchedulers() }) {
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
                                Icon(Icons.Default.DateRange, contentDescription = "Empty", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No Active Scheduler Profile Configured", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Configure interval sequences for automated collection sweeps.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("scheduler_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text("Campaign Run Schedulers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Automatic background cron executions sweeping overdue accounting registers.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            items(list) { scheduler ->
                                SchedulerCardRow(
                                    scheduler = scheduler,
                                    onToggle = { active -> viewModel.toggleScheduler(scheduler.id, active) }
                                )
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                AddSchedulerDialog(
                    onDismiss = { showAddDialog = false },
                    onSubmit = { name, freq, time, day ->
                        viewModel.addScheduler(name, freq, time, day)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun SchedulerCardRow(
    scheduler: ReminderSchedulerRule,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("scheduler_card_${scheduler.id}"),
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
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Event Run",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(scheduler.ruleName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                
                Switch(
                    checked = scheduler.isActive,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("scheduler_toggle_${scheduler.id}")
                )
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text("Execution Cadence: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(scheduler.frequency, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Row {
                    Text("Trigger Time: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(scheduler.timeOfDay, style = MaterialTheme.typography.bodySmall)
                }
                if (!scheduler.dayOfWeekOrMonth.isNullOrBlank()) {
                    Row {
                        Text("Trigger Recurrence Day: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text(scheduler.dayOfWeekOrMonth, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSchedulerDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }
    var timeOfDay by remember { mutableStateOf("09:00 AM") }
    var dayOfWeekOrMonth by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Automated Scheduler Rule", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Automation Profile Description") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_sched_name_input")
                    )
                }
                item {
                    Text("Execution Cadence Frequency", style = MaterialTheme.typography.labelSmall)
                    val freqs = listOf("Daily", "Weekly", "Monthly")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        freqs.forEach { f ->
                            FilterChip(
                                selected = (frequency == f),
                                onClick = { frequency = f },
                                label = { Text(f) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = timeOfDay,
                        onValueChange = { timeOfDay = it },
                        label = { Text("Trigger Time of Day") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_sched_time_input"),
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = dayOfWeekOrMonth,
                        onValueChange = { dayOfWeekOrMonth = it },
                        label = {
                            Text(
                                text = when(frequency) {
                                    "Weekly" -> "Trigger Day of Week (e.g. Monday)"
                                    "Monthly" -> "Trigger Day of Month (e.g. Day 28)"
                                    else -> "Option offset parameters (optional)"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_sched_day_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSubmit(
                            name,
                            frequency,
                            timeOfDay,
                            if (dayOfWeekOrMonth.isBlank()) null else dayOfWeekOrMonth
                        )
                    }
                },
                modifier = Modifier.testTag("dialog_sched_submit_btn")
            ) {
                Text("Deploy Scheduler")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
