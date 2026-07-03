package com.example.feature.outstanding.presentation

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.common.UiState
import com.example.feature.outstanding.domain.OutstandingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryTrackingScreen(
    viewModel: RecoveryTrackingViewModel,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedWorkflowStage by remember { mutableStateOf("All Stages") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collection Pipeline Feed", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("pipeline_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPipeline() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Pipeline")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        modifier = Modifier.fillMaxSize().testTag("recovery_tracking_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stage Selector Filter row
            ScrollableTabRow(
                selectedTabIndex = when (selectedWorkflowStage) {
                    "All Stages" -> 0
                    "Normal" -> 1
                    "Reminder Sent" -> 2
                    "Promised" -> 3
                    "Paid" -> 4
                    else -> 0
                },
                modifier = Modifier.fillMaxWidth().testTag("stage_scrollable_tab_row")
            ) {
                val stages = listOf("All Stages", "Normal", "Reminder Sent", "Promised", "Paid")
                stages.forEach { stage ->
                    Tab(
                        selected = selectedWorkflowStage == stage,
                        onClick = { selectedWorkflowStage = stage },
                        text = { Text(stage) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                                Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(state.message, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadPipeline() }) { Text("Retry") }
                            }
                        }
                    }
                    is UiState.Success -> {
                        val allItems = state.data
                        val filteredItems = if (selectedWorkflowStage == "All Stages") {
                            allItems
                        } else {
                            allItems.filter { it.billingState?.equals(selectedWorkflowStage, ignoreCase = true) == true }
                        }

                        if (filteredItems.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Empty State: No Pipeline Items Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("There are no registered accounts currently active in stage '$selectedWorkflowStage'. Check other stages of recovery.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                items(filteredItems) { item ->
                                    PipelineItemRow(
                                        item = item,
                                        onCardClick = { onNavigateToDetail(item.partyId) },
                                        onAdvanceStatus = { newStatus ->
                                            viewModel.dragOrUpdateStatus(item.partyId, newStatus)
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
fun PipelineItemRow(
    item: OutstandingItem,
    onCardClick: () -> Unit,
    onAdvanceStatus: (String) -> Unit
) {
    var showActionMenu by remember { mutableStateOf(false) }

    val indicatorColor = when (item.billingState?.lowercase()?.trim()) {
        "paid" -> Color(0xFF2E7D32)
        "reminder sent" -> Color(0xFFE65100)
        "promised" -> Color(0xFF1976D2)
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pipeline_card_${item.partyId}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(indicatorColor, shape = RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.billingState ?: "Normal",
                        style = MaterialTheme.typography.labelMedium,
                        color = indicatorColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box {
                    IconButton(onClick = { showActionMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Pipeline action switcher")
                    }
                    DropdownMenu(
                        expanded = showActionMenu,
                        onDismissRequest = { showActionMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Move to: 'Normal'") },
                            onClick = {
                                onAdvanceStatus("Normal")
                                showActionMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to: 'Reminder Sent'") },
                            onClick = {
                                onAdvanceStatus("Reminder Sent")
                                showActionMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to: 'Promised'") },
                            onClick = {
                                onAdvanceStatus("Promised")
                                showActionMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to: 'Paid'") },
                            onClick = {
                                onAdvanceStatus("Paid")
                                showActionMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.partyName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onCardClick() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Overdue Liability", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${formatCurrency(item.overdueAmount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Rep Icon", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item.assignedExecutive ?: "No Agent", style = MaterialTheme.typography.bodySmall)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last Contact: " + (item.lastContactDate ?: "Never contacted"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(onClick = onCardClick) {
                    Text("Collect & Track", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
