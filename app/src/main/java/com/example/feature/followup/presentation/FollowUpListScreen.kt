package com.example.feature.followup.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.feature.followup.domain.FollowUp
import com.example.feature.followup.domain.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpListScreen(
    viewModel: FollowUpListViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.filterType.collectAsState()
    val selectedPriority by viewModel.filterPriority.collectAsState()
    val selectedStatus by viewModel.filterStatus.collectAsState()
    val selectedSort by viewModel.sortBy.collectAsState()

    var showFiltersPanel by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Follow-Up Registry") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("list_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFiltersPanel = !showFiltersPanel },
                        modifier = Modifier.testTag("list_filter_toggle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Toggle Filter Options"
                        )
                    }
                    IconButton(onClick = onNavigateToCreate, modifier = Modifier.testTag("list_go_create_btn")) {
                        Icon(Icons.Default.Add, contentDescription = "Create Follow Up")
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
            // Search Bar & Filter Header
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateFilters(query = it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    .testTag("list_search_input"),
                placeholder = { Text("Search customer name, details...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateFilters(query = "") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear text")
                        }
                    }
                },
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            // Collapsible Advanced Filter Panel
            AnimatedVisibility(
                visible = showFiltersPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Interactive Filters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Type Choice Filter Row
                        Text("Follow-Up Channel", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            items(listOf("All", "Call", "Visit", "Email", "WhatsApp")) { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { viewModel.updateFilters(type = type) },
                                    label = { Text(type) }
                                )
                            }
                        }

                        // Priority Filter Row
                        Text("Action Level Priority", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            items(listOf("All", "High", "Medium", "Low")) { p ->
                                FilterChip(
                                    selected = selectedPriority == p,
                                    onClick = { viewModel.updateFilters(priority = p) },
                                    label = { Text(p) }
                                )
                            }
                        }

                        // Status Filter Row
                        Text("Work Progress Status", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            items(listOf("All", "Open", "In Progress", "Waiting Response", "Promised Payment", "Completed", "Cancelled")) { st ->
                                FilterChip(
                                    selected = selectedStatus == st,
                                    onClick = { viewModel.updateFilters(status = st) },
                                    label = { Text(st) }
                                )
                            }
                        }

                        // Sorting selection dropdown
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sequence: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            var sortMenuExpanded by remember { mutableStateOf(false) }
                            Box {
                                AssistChip(
                                    onClick = { sortMenuExpanded = true },
                                    label = { Text(selectedSort) },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                                )
                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false }
                                ) {
                                    listOf("Due Date (Earliest)", "Due Date (Latest)", "Priority (High to Low)", "Customer (A-Z)").forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                viewModel.updateFilters(sort = item)
                                                sortMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Follow-up scrollable content
            Box(modifier = Modifier.weight(1f)) {
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
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Communication Error", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(state.message, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadFollowUps() }) {
                                    Text("Requery Records")
                                }
                            }
                        }
                    }
                    is UiState.Success -> {
                        val items = state.data
                        if (items.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Empty Registry",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No interaction entries found",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Refine your filters, search keyword or log a new action.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    if (showFiltersPanel || searchQuery.isNotEmpty()) {
                                        Button(
                                            onClick = {
                                                viewModel.updateFilters("", "All", "All", "All", "All", "Due Date (Earliest)")
                                            }
                                        ) {
                                            Text("Clear Active Filters")
                                        }
                                    } else {
                                        Button(onClick = onNavigateToCreate) {
                                            Text("Create Action Schedule")
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("follow_up_lazy_list"),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(items) { item ->
                                    FollowUpItemCard(item = item, onClick = { onNavigateToDetail(item.id) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Fixed arrangement extension because Jetpack Compose standard Arrangement doesn't have interstate padding label
private val LazyColumn_Arrangement = Arrangement.spacedBy(12.dp)
@Composable
fun FollowUpItemCard(item: FollowUp, onClick: () -> Unit) {
    val statusColor = when (item.status) {
        "Completed" -> Color(0xFF2E7D32)
        "Promised Payment" -> Color(0xFFE65100)
        "In Progress" -> Color(0xFFFFB300)
        "Waiting Response" -> Color(0xFF1565C0)
        "Cancelled" -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("follow_up_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Assigned: ${item.assignedUserName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    contentColor = statusColor
                ) {
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body copy
            Text(
                text = item.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            // Footer Row metadatas
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (item.type) {
                            "Call" -> Icons.Default.Phone
                            "Visit" -> Icons.Default.Place
                            "Email" -> Icons.Default.Email
                            else -> Icons.Default.Send
                        },
                        contentDescription = item.type,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.type,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Priority",
                        tint = when (item.priority) {
                            "High" -> Color.Red
                            "Medium" -> Color(0xFFEF6C00)
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.priority} Priority",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Target date",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.dueDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
