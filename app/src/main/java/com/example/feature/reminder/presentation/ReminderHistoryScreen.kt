package com.example.feature.reminder.presentation

import androidx.compose.foundation.background
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
import com.example.feature.reminder.domain.ReminderItem
import com.example.feature.reminder.domain.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderHistoryScreen(
    viewModel: ReminderHistoryViewModel,
    onBack: () -> Unit
) {
    val historyState by viewModel.historyState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var snackbarHostState = remember { SnackbarHostState() }
    var coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Delivery History Logs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshHistory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp)
                    .testTag("history_search_input"),
                placeholder = { Text("Search by customer, bill number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            // Status Multi filters
            Text(
                "Filter Status",
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            val statuses = listOf("All", "Pending", "Completed", "Failed")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { status ->
                    FilterChip(
                        selected = (selectedStatus == status),
                        onClick = { viewModel.updateStatusFilter(status) },
                        label = { Text(status) },
                        modifier = Modifier.testTag("status_chip_$status")
                    )
                }
            }

            // Communication Type filters
            Text(
                "Filter Channel",
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            val types = listOf("All", "WhatsApp", "SMS", "Email", "Custom/Call")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                types.forEach { type ->
                    FilterChip(
                        selected = (selectedType == type),
                        onClick = { viewModel.updateTypeFilter(type) },
                        label = { Text(type) },
                        modifier = Modifier.testTag("type_chip_$type")
                    )
                }
            }

            // Results UI state
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (val state = historyState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.testTag("history_loading"))
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(state.message, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.refreshHistory() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is UiState.Success -> {
                        val items = state.data
                        if (items.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                    Icon(Icons.Default.Info, contentDescription = "Empty", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No Dispatch Records found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Refine filters or schedule new campaigns.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("history_list"),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(items) { item ->
                                    HistoryItemRow(
                                        item = item,
                                        onResend = {
                                            // Handle simulate resend
                                        }
                                    )
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
fun HistoryItemRow(
    item: ReminderItem,
    onResend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("history_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(item.partyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (item.billNumber != null) {
                        Text("Bill Linked: ${item.billNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text("General Account Reminder", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
                
                // Status Badge
                val badgeColor = when(item.status) {
                    "Completed" -> Color(0xFF2E7D32) // green
                    "Pending" -> Color(0xFF1565C0) // blue
                    else -> Color(0xFFC62828) // red
                }
                Surface(
                    color = badgeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).testTag("status_badge_${item.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            // Text Message Body box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(item.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when(item.type) {
                                "WhatsApp" -> Icons.Default.Info
                                "SMS" -> Icons.Default.List
                                "Email" -> Icons.Default.Email
                                else -> Icons.Default.Phone
                            },
                            contentDescription = item.type,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                    }
                    Text("Scheduled: ${item.scheduleDateTime}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("By: ${item.assigneeName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    if (item.status == "Failed") {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onResend,
                            modifier = Modifier.size(24.dp).testTag("resend_btn_${item.id}")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Retry Delivery", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
