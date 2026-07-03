package com.example.feature.outstanding.presentation

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
fun OutstandingListScreen(
    viewModel: OutstandingListViewModel,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showGroupMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outstanding Registry", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("list_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Items")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        modifier = Modifier.fillMaxSize().testTag("outstanding_list_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Tab Selector Toggles between Receivables and Payables
            TabRow(
                selectedTabIndex = if (selectedType == "Receivable") 0 else 1,
                modifier = Modifier.fillMaxWidth().testTag("outstanding_type_tab_row")
            ) {
                Tab(
                    selected = selectedType == "Receivable",
                    onClick = { viewModel.updateSelectedType("Receivable") },
                    text = { Text("Receivables") },
                    modifier = Modifier.testTag("receivables_tab")
                )
                Tab(
                    selected = selectedType == "Payable",
                    onClick = { viewModel.updateSelectedType("Payable") },
                    text = { Text("Payables") },
                    modifier = Modifier.testTag("payables_tab")
                )
            }

            // 2. Search Text Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search clients or ledger groups...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("outstanding_search_input"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true
            )

            // 3. Dropdowns Row for filtering/sorting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group selector
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showGroupMenu = true },
                        modifier = Modifier.fillMaxWidth().testTag("group_filter_button")
                    ) {
                        Text(
                            text = "Group: $selectedGroup",
                            maxLines = 1
                        )
                    }
                    DropdownMenu(
                        expanded = showGroupMenu,
                        onDismissRequest = { showGroupMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Groups") },
                            onClick = {
                                viewModel.updateSelectedGroup("All")
                                showGroupMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sundry Debtors") },
                            onClick = {
                                viewModel.updateSelectedGroup("Sundry Debtors")
                                showGroupMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sundry Creditors") },
                            onClick = {
                                viewModel.updateSelectedGroup("Sundry Creditors")
                                showGroupMenu = false
                            }
                        )
                    }
                }

                // Sort Order selector
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.fillMaxWidth().testTag("sort_filter_button")
                    ) {
                        Text(
                            text = "Sort: ${selectedSort.uppercase()}",
                            maxLines = 1
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Overdue (Highest First)") },
                            onClick = {
                                viewModel.updateSelectedSort("overdue desc")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Outstanding (Highest First)") },
                            onClick = {
                                viewModel.updateSelectedSort("outstanding desc")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Outstanding (Lowest First)") },
                            onClick = {
                                viewModel.updateSelectedSort("outstanding asc")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Client Name") },
                            onClick = {
                                viewModel.updateSelectedSort("party name")
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            // Scoped Boundary Role Note
            userRole?.let { role ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Viewing as $role. Balance allocations isolated by company unit.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 4. Data states representation
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Oops, something went wrong.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                                Text(state.message, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.loadItems() }) { Text("Retry") }
                            }
                        }
                    }
                    is UiState.Success -> {
                        val items = state.data
                        if (items.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Info, contentDescription = "Empty", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No Pending Balance Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Try adjusting your lookup query or selecting a different company accounting group.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(items) { item ->
                                    OutstandingItemRow(
                                        item = item,
                                        onClick = { onNavigateToDetail(item.partyId) }
                                    )
                                }

                                // Load More Pagination button state
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        TextButton(onClick = { viewModel.incrementPage() }) {
                                            Text("Pagination: Load Next Page Content", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
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
fun OutstandingItemRow(
    item: OutstandingItem,
    onClick: () -> Unit
) {
    val stateColor = when (item.billingState?.lowercase()?.trim()) {
        "paid" -> Color(0xFF2E7D32)
        "reminder sent", "dunning" -> Color(0xFFE65100)
        "promised" -> Color(0xFF1976D2)
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("outstanding_item_row_${item.partyId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.groupName,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Surface(
                    color = stateColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.billingState ?: "Normal",
                        style = MaterialTheme.typography.labelSmall,
                        color = stateColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.partyName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Outstanding Balance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "₹${formatCurrency(item.outstandingAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Overdue amount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "₹${formatCurrency(item.overdueAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        color = if (item.overdueAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (item.assignedExecutive != null || item.lastContactDate != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = "Executive", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rep: ${item.assignedExecutive ?: "Unassigned"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = "Last Contact", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Contact: ${item.lastContactDate ?: "Never"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}
