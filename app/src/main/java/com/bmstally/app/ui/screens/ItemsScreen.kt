package com.bmstally.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstally.app.model.Item
import com.bmstally.app.viewmodel.AggregatedItemData
import com.bmstally.app.viewmodel.ItemFilter
import com.bmstally.app.viewmodel.ItemViewMode
import com.bmstally.app.viewmodel.ItemsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    viewModel: ItemsViewModel,
    onBack: () -> Unit,
    onCreateItem: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val categoryTab by viewModel.categoryTab.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val pageSize by viewModel.pageSize.collectAsState()

    var showSearch by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (categoryTab == 0) {
                FloatingActionButton(
                    onClick = onCreateItem,
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TopAppBar(
                title = { Text("Inventory", color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                actions = {
                    IconButton({ showSearch = !showSearch }) { Icon(Icons.Default.Search, "Search", tint = Color.White) }
                    if (categoryTab == 0) {
                        IconButton({ viewModel.toggleView() }) {
                            Icon(
                                if (viewMode == ItemViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                                "Toggle",
                                tint = Color.White
                            )
                        }
                    }
                    Box {
                        IconButton({ showFilterMenu = true }) { Icon(Icons.Default.FilterList, "Filter", tint = Color.White) }
                        DropdownMenu(showFilterMenu, { showFilterMenu = false }) {
                            ItemFilter.entries.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.label) },
                                    onClick = { viewModel.setFilter(f); showFilterMenu = false },
                                    leadingIcon = if (f == filter) {{ Icon(Icons.Default.Check, null, tint = Color(0xFF3F51B5)) }} else null
                                )
                            }
                        }
                    }
                    IconButton({ showShareSheet = true }) { Icon(Icons.Default.Share, "Share", tint = Color.White) }
                    Box {
                        IconButton({ showMoreMenu = true }) { Icon(Icons.Default.MoreVert, "More", tint = Color.White) }
                        DropdownMenu(showMoreMenu, { showMoreMenu = false }) {
                            listOf("Import", "Export", "Refresh").forEach { action ->
                                DropdownMenuItem(
                                    text = { Text(action) },
                                    onClick = { showMoreMenu = false }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )

            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by name, category or group...", color = Color.White.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.6f)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton({ viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, null, tint = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            InventorySummaryCards(viewModel, filter)

            TabRow(selectedTabIndex = categoryTab) {
                viewModel.tabs.forEachIndexed { i, label ->
                    Tab(
                        selected = categoryTab == i,
                        onClick = {
                            showSearch = false
                            viewModel.setCategoryTab(i)
                        },
                        text = { Text(label) }
                    )
                }
            }

            when (categoryTab) {
                1 -> AggregatedListView(viewModel.groupData, Icons.Default.GroupWork, "Groups")
                2 -> AggregatedListView(viewModel.categoryData, Icons.Default.Category, "Categories")
                else -> {
                    if (items.isEmpty() && viewModel.filtered.isNotEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No items on this page", color = Color.Gray)
                        }
                    } else if (items.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No items found", color = Color.Gray)
                        }
                    } else {
                        if (viewMode == ItemViewMode.LIST) ItemList(items, viewModel)
                        else ItemGridView(items)
                    }
                }
            }
        }
    }

    if (showShareSheet) {
        AlertDialog(
            onDismissRequest = { showShareSheet = false },
            confirmButton = {},
            title = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Select category to share", fontWeight = FontWeight.W600)
                    IconButton({ showShareSheet = false }) { Icon(Icons.Default.Close, null) }
                }
            },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Category Wise Summary") },
                        supportingContent = { Text("Share grouped category information.", fontSize = 12.sp, color = Color.Gray) },
                        leadingContent = { Icon(Icons.Default.Category, null) },
                        modifier = Modifier.clickable { showShareSheet = false }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Items") },
                        supportingContent = { Text("Share item list information.", fontSize = 12.sp, color = Color.Gray) },
                        leadingContent = { Icon(Icons.Default.Inventory2, null) },
                        modifier = Modifier.clickable { showShareSheet = false }
                    )
                }
            }
        )
    }
}

@Composable
private fun InventorySummaryCards(viewModel: ItemsViewModel, currentFilter: ItemFilter) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "Total Items",
                value = "${viewModel.filtered.size}",
                icon = Icons.Default.Inventory2,
                color = Color(0xFF3F51B5)
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "Stock Value",
                value = "₹${String.format("%,d", viewModel.totalStockValue)}",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFF4CAF50)
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "Low Stock",
                value = "${viewModel.lowStockCount}",
                icon = Icons.Default.Warning,
                color = if (viewModel.lowStockCount > 0) Color(0xFFE53935) else Color(0xFF9E9E9E),
                highlighted = currentFilter == ItemFilter.BELOW_REORDER
            )
        }
        if (viewModel.lowStockCount > 0) {
            Spacer(Modifier.height(4.dp))
            FilterChip(
                selected = currentFilter == ItemFilter.BELOW_REORDER,
                onClick = {
                    viewModel.setFilter(
                        if (currentFilter == ItemFilter.BELOW_REORDER) ItemFilter.ALL
                        else ItemFilter.BELOW_REORDER
                    )
                },
                label = { Text("Show low stock only", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFE53935)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE53935).copy(alpha = 0.12f),
                    selectedLabelColor = Color(0xFFC62828)
                )
            )
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    highlighted: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) color.copy(alpha = 0.08f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlighted) 4.dp else 1.dp)
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            Text(label, color = Color.Gray, fontSize = 9.sp)
        }
    }
}

@Composable
private fun ItemList(items: List<Item>, viewModel: ItemsViewModel) {
    val currentPage by viewModel.currentPage.collectAsState()
    val pageSize by viewModel.pageSize.collectAsState()
    val totalRecords = viewModel.totalRecords
    val totalPages = viewModel.totalPages

    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Item Name", fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.weight(1f))
            Text("Stock Value", fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(80.dp))
            Text("Qty", fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(50.dp))
        }
        LazyColumn(Modifier.weight(1f)) {
            items(items) { item -> ItemTile(item) }
        }
        PaginationBarItems(
            currentPage = currentPage,
            totalPages = totalPages,
            totalRecords = totalRecords,
            pageSize = pageSize,
            onPageChange = { viewModel.setPage(it) },
            onPageSizeChange = { viewModel.setPageSize(it) }
        )
    }
}

@Composable
private fun ItemTile(item: Item) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            val stockColor = if (item.isInStock) Color(0xFF4CAF50) else Color(0xFFEF5350)
            Box {
                Surface(shape = MaterialTheme.shapes.extraLarge, color = stockColor.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Inventory2, null, Modifier.padding(8.dp).size(20.dp), tint = stockColor)
                }
                if (item.isBelowReorderLevel) {
                    Box(
                        modifier = Modifier.size(12.dp).align(Alignment.TopEnd).background(Color(0xFFE53935), shape = MaterialTheme.shapes.extraLarge)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.W600, fontSize = 14.sp)
                    if (item.isBelowReorderLevel) {
                        Spacer(Modifier.width(6.dp))
                        Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFE53935).copy(alpha = 0.12f)) {
                            Text("Low", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFC62828), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(item.category, color = Color.Gray, fontSize = 11.sp)
            }
            Text("₹${String.format("%,d", item.stockValue)}", fontSize = 12.sp, color = Color(0xFF424242), modifier = Modifier.width(80.dp))
            Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(50.dp))
        }
    }
}

@Composable
private fun ItemGridView(items: List<Item>) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items found", color = Color.Gray)
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val stockColor = if (item.isInStock) Color(0xFF4CAF50) else Color(0xFFEF5350)
                        Box {
                            Surface(shape = MaterialTheme.shapes.extraLarge, color = stockColor.copy(alpha = 0.1f)) {
                                Icon(Icons.Default.Inventory2, null, Modifier.padding(6.dp).size(16.dp), tint = stockColor)
                            }
                            if (item.isBelowReorderLevel) {
                                Box(
                                    modifier = Modifier.size(8.dp).align(Alignment.TopEnd).background(Color(0xFFE53935), shape = MaterialTheme.shapes.extraLarge)
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.name, fontWeight = FontWeight.W600, fontSize = 13.sp, maxLines = 2, modifier = Modifier.weight(1f))
                        if (item.isBelowReorderLevel) {
                            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFE53935).copy(alpha = 0.12f)) {
                                Text("Low", Modifier.padding(horizontal = 4.dp, vertical = 1.dp), color = Color(0xFFC62828), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(item.category, color = Color.Gray, fontSize = 11.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.unit, color = Color.Gray, fontSize = 11.sp)
                        Text("₹${String.format("%,d", item.stockValue)}", color = Color(0xFF424242), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AggregatedListView(data: List<AggregatedItemData>, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    if (data.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data found", color = Color.Gray)
        }
        return
    }
    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.weight(1f))
            Text("Items", fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
            Text("Qty", fontWeight = FontWeight.W600, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(80.dp))
        }
        LazyColumn {
            items(data) { d ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFF3F51B5).copy(alpha = 0.1f)) {
                            Icon(icon, null, Modifier.padding(8.dp).size(20.dp), tint = Color(0xFF3F51B5))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(d.name, fontWeight = FontWeight.W600, modifier = Modifier.weight(1f))
                        Text("${d.itemCount}", fontWeight = FontWeight.W600, fontSize = 14.sp, modifier = Modifier.width(60.dp))
                        Text("${d.totalQuantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(80.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaginationBarItems(
    currentPage: Int,
    totalPages: Int,
    totalRecords: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit
) {
    var pageSizeExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$totalRecords records", fontSize = 12.sp, color = Color.Gray)

        Row(verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(
                expanded = pageSizeExpanded,
                onExpandedChange = { pageSizeExpanded = it },
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    "$pageSize/page",
                    modifier = Modifier.menuAnchor().padding(horizontal = 8.dp),
                    fontSize = 12.sp
                )
                ExposedDropdownMenu(
                    expanded = pageSizeExpanded,
                    onDismissRequest = { pageSizeExpanded = false }
                ) {
                    listOf(5, 10, 15, 20, 50, 100).forEach { size ->
                        DropdownMenuItem(
                            text = { Text("$size/page") },
                            onClick = { onPageSizeChange(size); pageSizeExpanded = false }
                        )
                    }
                }
            }

            TextButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 1
            ) { Text("Prev", fontSize = 12.sp) }

            Text("$currentPage / $totalPages", fontSize = 12.sp)

            TextButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages
            ) { Text("Next", fontSize = 12.sp) }
        }
    }
}
