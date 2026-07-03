package com.example.feature.inventory.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feature.inventory.domain.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

// Helper currency formatter
fun formatInventoryCurrency(amount: BigDecimal): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return try {
        formatter.format(amount)
    } catch (e: Exception) {
        "₹${String.format("%.2f", amount.toDouble())}"
    }
}

// ==========================================
// 1. ITEM MASTER SCREEN (STOCK ITEM LIST)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemMasterScreen(
    viewModel: InventoryViewModel,
    companyId: String,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.itemMasterState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(companyId) {
        viewModel.loadItemMaster(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Item Master", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadItemMaster(companyId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_item_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Item")
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar & Filter Headers
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search by name or SKU...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Search") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_search_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filters Roll
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Filter Chip
                        FilterChip(
                            selected = state.selectedCategory == null,
                            onClick = { viewModel.selectCategoryFilter(null) },
                            label = { Text("All Categories") }
                        )
                        state.categories.forEach { cat ->
                            FilterChip(
                                selected = state.selectedCategory == cat,
                                onClick = { viewModel.selectCategoryFilter(cat) },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Brand Filter Chip
                        FilterChip(
                            selected = state.selectedBrand == null,
                            onClick = { viewModel.selectBrandFilter(null) },
                            label = { Text("All Brands") }
                        )
                        state.brands.forEach { br ->
                            FilterChip(
                                selected = state.selectedBrand == br,
                                onClick = { viewModel.selectBrandFilter(br) },
                                label = { Text(br) }
                            )
                        }
                    }
                }
            }

            // Results feeds
            if (state.filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "No Items Found",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No stock items match your filter parameters.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredItems) { item ->
                        ItemCardRow(item = item, onClick = { onNavigateToDetail(item.id) })
                    }
                }
            }
        }
    }

    // Modal dialog to add stock item
    if (showAddDialog) {
        AddStockItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, sku, category, brand, uom, desc, buy, sell, min ->
                viewModel.createStockItem(
                    companyId = companyId,
                    name = name,
                    sku = sku,
                    category = category,
                    brand = brand,
                    uom = uom,
                    description = desc,
                    purchasePrice = buy,
                    sellingPrice = sell,
                    minReorderLevel = min,
                    onSuccess = { showAddDialog = false }
                )
            }
        )
    }
}

@Composable
fun ItemCardRow(item: StockItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("item_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (!item.isSynced) {
                        SuggestionChip(
                            label = { Text("Local", fontSize = 9.sp) },
                            onClick = {}
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SKU: ${item.sku}  |  ${item.category}  |  ${item.brand}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Buy: ${formatInventoryCurrency(item.purchasePrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Sell: ${formatInventoryCurrency(item.sellingPrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Open Detail")
        }
    }
}

@Composable
fun AddStockItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, BigDecimal, BigDecimal, BigDecimal) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Structural Metals") }
    var brand by remember { mutableStateOf("") }
    var uom by remember { mutableStateOf("Metric Tons") }
    var description by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var minReorder by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Register Stock Item",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_item_name")
                )

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU Code") },
                    modifier = Modifier.fillMaxWidth().testTag("add_item_sku")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uom,
                        onValueChange = { uom = it },
                        label = { Text("UOM") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minReorder,
                        onValueChange = { minReorder = it },
                        label = { Text("Min Reorder Level") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("Purchase Price") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Item Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || sku.isBlank() || brand.isBlank()) {
                                errorMsg = "Name, SKU and Brand cannot be blank"
                                return@Button
                            }
                            val buyQty = purchasePrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            val sellQty = sellingPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            val reorderQty = minReorder.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            onConfirm(name, sku, category, brand, uom, description, buyQty, sellQty, reorderQty)
                        },
                        modifier = Modifier.testTag("dialog_confirm_button")
                    ) {
                        Text("Save Product")
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. ITEM DETAIL SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    viewModel: InventoryViewModel,
    itemId: String,
    companyId: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.itemDetailState.collectAsState()

    LaunchedEffect(itemId, companyId) {
        viewModel.loadItemDetail(itemId, companyId)
    }

    val item = state.item

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "Stock Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SKU: ${item.sku}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            Column {
                                Text("Standard Cost", fontSize = 11.sp, color = Color.Gray)
                                Text(formatInventoryCurrency(item.purchasePrice), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column {
                                Text("Selling Price", fontSize = 11.sp, color = Color.Gray)
                                Text(formatInventoryCurrency(item.sellingPrice), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Reorder Trigger", fontSize = 11.sp, color = Color.Gray)
                                Text("${item.minReorderLevel} ${item.uom}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Storage splits
                Text("Warehouse Storage Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                state.stockLevels.forEach { lv ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lv.godownName, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Avail: ${lv.availableStock} ${item.uom}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                    Text("Rsvd: ${lv.reservedStock} ${item.uom}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Text("${lv.currentStock} ${item.uom}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Valuation Tool Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Valuation Simulator", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("FIFO", "Weighted Average", "Standard Cost").forEach { method ->
                                FilterChip(
                                    selected = state.selectedValuationMethod == method,
                                    onClick = { viewModel.updateDetailValuationMethod(item.id, companyId, method) },
                                    label = { Text(method, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        state.valuationReport?.let { vr ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Unit Valuation", fontSize = 11.sp, color = Color.DarkGray)
                                    Text(formatInventoryCurrency(vr.unitValue), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Material Valuation", fontSize = 11.sp, color = Color.DarkGray)
                                    Text(formatInventoryCurrency(vr.totalValuation), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Movements Logs
                Text("Recent Stock Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (state.history.isEmpty()) {
                    Text("No transactions logged for this item yet.", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    state.history.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${tx.type} - ${tx.godownName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Ref: ${tx.referenceId ?: "Direct"} | ${tx.date}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                text = "${if (tx.type in listOf("IN", "PURCHASE", "TRANSFER_IN")) "+" else "-"} ${tx.quantity} ${item.uom}",
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type in listOf("IN", "PURCHASE", "TRANSFER_IN")) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. STOCK SUMMARY SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSummaryScreen(
    viewModel: InventoryViewModel,
    companyId: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.stockSummaryState.collectAsState()

    LaunchedEffect(companyId) {
        viewModel.loadStockSummary(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Stock Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Valuation and method selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Corporate Stock Material Value", fontSize = 11.sp, color = Color.DarkGray)
                    Text(
                        formatInventoryCurrency(state.totalValuation),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Valuation Standard:", fontSize = 11.sp, color = Color.DarkGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Weighted Average", "FIFO", "Standard Cost").forEach { method ->
                            FilterChip(
                                selected = state.selectedValuationMethod == method,
                                onClick = { viewModel.updateSummaryValuationMethod(companyId, method) },
                                label = { Text(method, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }

            // Categories roll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { viewModel.selectSummaryCategoryFilter(companyId, null) },
                    label = { Text("All Categories") }
                )
                // Distinct categories
                val categories = state.summaryItems.map { it.item.category }.distinct()
                categories.forEach { cat ->
                    FilterChip(
                        selected = state.selectedCategory == cat,
                        onClick = { viewModel.selectSummaryCategoryFilter(companyId, cat) },
                        label = { Text(cat) }
                    )
                }
            }

            // Results summary
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filteredSummary) { summaryItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(summaryItem.item.name, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Stock: ${summaryItem.totalCurrentStock} ${summaryItem.item.uom}", fontSize = 11.sp)
                                    Text("Avail: ${summaryItem.totalAvailableStock} ${summaryItem.item.uom}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatInventoryCurrency(summaryItem.totalValue), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text("at method rate", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. WAREHOUSE / GODOWN SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseScreen(
    viewModel: InventoryViewModel,
    companyId: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.warehouseState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(companyId) {
        viewModel.loadGodowns(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Godown / Warehouse Master", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_warehouse_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Godown")
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Slider list of godowns
            ScrollableTabRow(
                selectedTabIndex = state.godowns.indexOf(state.selectedGodown).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.godowns.forEach { gd ->
                    Tab(
                        selected = state.selectedGodown?.id == gd.id,
                        onClick = { viewModel.selectGodown(companyId, gd.id) },
                        text = { Text(gd.name) }
                    )
                }
            }

            // Selected Godown Detail Card
            state.selectedGodown?.let { gd ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, "Location", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(gd.location, style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, "Manager", tint = Color.Gray)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manager: ${gd.manager}  |  Status: ${gd.status}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Stock Levels at this Godown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(4.dp))

                if (state.selectedGodownLevels.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No stock items located in this warehouse currently.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.selectedGodownLevels) { lv ->
                            // Fetch product name or details based on itemId
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Product Ref: ${lv.itemId}", fontWeight = FontWeight.Bold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text("Available: ${lv.availableStock}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                            Text("Reserved: ${lv.reservedStock}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                    Text("${lv.currentStock}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    if (showAddDialog) {
        AddGodownDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, loc, mgr ->
                viewModel.createGodown(
                    companyId = companyId,
                    name = name,
                    location = loc,
                    manager = mgr,
                    onSuccess = { showAddDialog = false }
                )
            }
        )
    }
}

@Composable
fun AddGodownDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var manager by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Add Corporate Godown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Godown Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_warehouse_name")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Full Address / Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = manager,
                    onValueChange = { manager = it },
                    label = { Text("Warehouse Manager Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || location.isBlank()) {
                                errorMsg = "Name and Location cannot be blank"
                                return@Button
                            }
                            onConfirm(name, location, manager)
                        },
                        modifier = Modifier.testTag("dialog_confirm_button")
                    ) {
                        Text("Add Godown")
                    }
                }
            }
        }
    }
}


// ==========================================
// 5. STOCK TRANSFER SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferScreen(
    viewModel: InventoryViewModel,
    companyId: String,
    onNavigateBack: () -> Unit
) {
    val masterState by viewModel.itemMasterState.collectAsState()
    val warehouseState by viewModel.warehouseState.collectAsState()

    var selectedItem by remember { mutableStateOf<StockItem?>(null) }
    var sourceGodown by remember { mutableStateOf<Godown?>(null) }
    var destGodown by remember { mutableStateOf<Godown?>(null) }
    var quantityStr by remember { mutableStateOf("") }
    var dateVal by remember { mutableStateOf("2026-06-20") }
    var feedbackMessage by remember { mutableStateOf("") }

    LaunchedEffect(companyId) {
        viewModel.loadItemMaster(companyId)
        viewModel.loadGodowns(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Warehouse Stock Transfer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Inter-Godown Transfer Application", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

            if (feedbackMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(feedbackMessage, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Dropdowns
            Text("1. Select Stock Item", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                masterState.items.forEach { item ->
                    FilterChip(
                        selected = selectedItem?.id == item.id,
                        onClick = { selectedItem = item },
                        label = { Text(item.name) }
                    )
                }
            }

            Text("2. Select Source Godown", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                warehouseState.godowns.forEach { gd ->
                    FilterChip(
                        selected = sourceGodown?.id == gd.id,
                        onClick = { sourceGodown = gd },
                        label = { Text(gd.name) }
                    )
                }
            }

            Text("3. Select Destination Godown", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                warehouseState.godowns.forEach { gd ->
                    FilterChip(
                        selected = destGodown?.id == gd.id,
                        onClick = { destGodown = gd },
                        label = { Text(gd.name) }
                    )
                }
            }

            Text("4. Set Quantities & Timestamps", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = quantityStr,
                onValueChange = { quantityStr = it },
                label = { Text("Transfer Quantity") },
                modifier = Modifier.fillMaxWidth().testTag("transfer_qty_input")
            )

            OutlinedTextField(
                value = dateVal,
                onValueChange = { dateVal = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val item = selectedItem
                    val src = sourceGodown
                    val dest = destGodown
                    val qty = quantityStr.toBigDecimalOrNull() ?: BigDecimal.ZERO

                    if (item == null || src == null || dest == null) {
                        feedbackMessage = "Please ensure all dropdown elements are selected."
                        return@Button
                    }
                    if (src.id == dest.id) {
                        feedbackMessage = "Source and destination godowns cannot be identical."
                        return@Button
                    }
                    if (qty <= BigDecimal.ZERO) {
                        feedbackMessage = "Please specify a positive non-zero transfer qty."
                        return@Button
                    }

                    feedbackMessage = ""
                    viewModel.transferStock(
                        companyId = companyId,
                        itemId = item.id,
                        sourceGodownId = src.id,
                        destinationGodownId = dest.id,
                        quantity = qty,
                        date = dateVal,
                        onSuccess = {
                            onNavigateBack()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("transfer_submit_button")
            ) {
                Text("Process Inter-Warehouse Transfer")
            }
        }
    }
}


// ==========================================
// 6. INVENTORY TRANSACTIONS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryTransactionsScreen(
    viewModel: InventoryViewModel,
    companyId: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.transactionState.collectAsState()
    val masterState by viewModel.itemMasterState.collectAsState()
    val warehouseState by viewModel.warehouseState.collectAsState()

    var showActionDialog by remember { mutableStateOf(false) }
    var dialogActionType by remember { mutableStateOf("IN") } // "IN", "OUT", "ADJUSTMENT"

    LaunchedEffect(companyId) {
        viewModel.loadTransactions(companyId)
        viewModel.loadItemMaster(companyId)
        viewModel.loadGodowns(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Transactions Log", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Actions Toolbar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Register Stock Entry Voucher", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                dialogActionType = "IN"
                                showActionDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1f).testTag("action_stock_in")
                        ) {
                            Text("Stock In", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                dialogActionType = "OUT"
                                showActionDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            modifier = Modifier.weight(1f).testTag("action_stock_out")
                        ) {
                            Text("Stock Out", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                dialogActionType = "ADJUSTMENT"
                                showActionDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).testTag("action_adjust")
                        ) {
                            Text("Adjust (Qty)", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Filters Roll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedType == null,
                    onClick = { viewModel.selectTransactionTypeFilter(null) },
                    label = { Text("All Vouchers") }
                )
                listOf("PURCHASE", "SALES", "IN", "OUT", "TRANSFER_IN", "TRANSFER_OUT", "ADJUSTMENT").forEach { ty ->
                    FilterChip(
                        selected = state.selectedType == ty,
                        onClick = { viewModel.selectTransactionTypeFilter(ty) },
                        label = { Text(ty) }
                    )
                }
            }

            // List feeds
            if (state.filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No transactions logged in this ledger timeframe.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredTransactions) { tx ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(tx.itemName, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SuggestionChip(label = { Text(tx.type, fontSize = 9.sp) }, onClick = {})
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Godown: ${tx.godownName}  |  Ref: ${tx.referenceId ?: "Manual"}  |  Date: ${tx.date}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val isAdd = tx.type in listOf("IN", "PURCHASE", "TRANSFER_IN")
                                    Text(
                                        text = "${if (isAdd) "+" else "-"} ${tx.quantity}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAdd) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                    Text("at Rate ${formatInventoryCurrency(tx.unitPrice)}", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showActionDialog) {
        StockActionFormDialog(
            actionType = dialogActionType,
            items = masterState.items,
            godowns = warehouseState.godowns,
            onDismiss = { showActionDialog = false },
            onConfirm = { itemId, godownId, qty, rate, ref, date, adjType ->
                when (dialogActionType) {
                    "IN" -> viewModel.addStockIn(companyId, itemId, godownId, qty, rate, ref, date, onSuccess = { showActionDialog = false })
                    "OUT" -> viewModel.addStockOut(companyId, itemId, godownId, qty, rate, ref, date, onSuccess = { showActionDialog = false })
                    "ADJUSTMENT" -> viewModel.adjustStock(companyId, itemId, godownId, adjType, qty, ref ?: "Inventory Reconciliation", date, onSuccess = { showActionDialog = false })
                }
            }
        )
    }
}

@Composable
fun StockActionFormDialog(
    actionType: String,
    items: List<StockItem>,
    godowns: List<Godown>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, BigDecimal, BigDecimal, String?, String, String) -> Unit
) {
    var selectedItem by remember { mutableStateOf<StockItem?>(null) }
    var selectedGodown by remember { mutableStateOf<Godown?>(null) }
    var quantity by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var referenceId by remember { mutableStateOf("") }
    var dateVal by remember { mutableStateOf("2026-06-20") }
    var adjType by remember { mutableStateOf("IN_ADJUSTMENT") } // "IN_ADJUSTMENT" or "OUT_ADJUSTMENT"

    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 550.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Post Stock Entry [$actionType]",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Dropdowns
                Text("Select Product:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { pr ->
                        FilterChip(
                            selected = selectedItem?.id == pr.id,
                            onClick = { selectedItem = pr; priceStr = pr.purchasePrice.toPlainString() },
                            label = { Text(pr.name) }
                        )
                    }
                }

                Text("Select Godown:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    godowns.forEach { gd ->
                        FilterChip(
                            selected = selectedGodown?.id == gd.id,
                            onClick = { selectedGodown = gd },
                            label = { Text(gd.name) }
                        )
                    }
                }

                if (actionType == "ADJUSTMENT") {
                    Text("Reconciliation Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = adjType == "IN_ADJUSTMENT",
                            onClick = { adjType = "IN_ADJUSTMENT" },
                            label = { Text("Add Qty (Found)") }
                        )
                        FilterChip(
                            selected = adjType == "OUT_ADJUSTMENT",
                            onClick = { adjType = "OUT_ADJUSTMENT" },
                            label = { Text("Subtract Qty (Shortage)") }
                        )
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_qty_input")
                )

                if (actionType != "ADJUSTMENT") {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Rate Per Unit (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = referenceId,
                    onValueChange = { referenceId = it },
                    label = { Text(if (actionType == "ADJUSTMENT") "Reason / Remarks" else "Reference ID / Voucher No.") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_ref_input")
                )

                OutlinedTextField(
                    value = dateVal,
                    onValueChange = { dateVal = it },
                    label = { Text("Tx Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val item = selectedItem
                            val gd = selectedGodown
                            val qty = quantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            val rate = priceStr.toBigDecimalOrNull() ?: BigDecimal.ZERO

                            if (item == null || gd == null) {
                                errorMsg = "Must select both an item and a warehouse."
                                return@Button
                            }
                            if (qty <= BigDecimal.ZERO) {
                                errorMsg = "Quantity must be greater than zero."
                                return@Button
                            }
                            onConfirm(item.id, gd.id, qty, rate, referenceId.takeIf { it.isNotBlank() }, dateVal, adjType)
                        },
                        modifier = Modifier.testTag("dialog_confirm_button")
                    ) {
                        Text("Post Entry")
                    }
                }
            }
        }
    }
}


// ==========================================
// 7. INVENTORY REPORTS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryReportsScreen(
    viewModel: InventoryViewModel,
    companyId: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.reportsState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Low Stock, 1: Dead Stock, 2: Fast Moving, 3: Ageing

    LaunchedEffect(companyId) {
        viewModel.loadReports(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Performance Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Header Bar
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Low Stock", fontSize = 11.sp) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Dead Stock", fontSize = 11.sp) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Fast-Moving", fontSize = 11.sp) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Ageing", fontSize = 11.sp) })
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                0 -> {
                    // Low Stock Alert Report
                    if (state.lowStock.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("All items are well above safety threshold levels.", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.lowStock) { itemRep ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(itemRep.item.name, fontWeight = FontWeight.Bold)
                                            Text("Trigger Safety Stock: ${itemRep.reorderLevel} ${itemRep.item.uom}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Current: ${itemRep.currentStock} ${itemRep.item.uom}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                            Text("Shortage: ${itemRep.shortage}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Dead Stock Report
                    if (state.deadStock.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No inert/unmoved materials recorded in last 90 days.", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.deadStock) { itemRep ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(itemRep.item.name, fontWeight = FontWeight.Bold)
                                            Text("Last Move: ${itemRep.lastMovementDate}", fontSize = 11.sp, color = Color.Gray)
                                            Text("Unmoved For: ${itemRep.daysInactive} days", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Stock: ${itemRep.currentStock}", fontWeight = FontWeight.Bold)
                                            Text(formatInventoryCurrency(itemRep.totalValue), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Fast Moving items
                    if (state.fastMoving.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No items recorded with sales/turnovers yet.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.fastMoving) { itemRep ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row {
                                                Text("#${itemRep.popularityRank}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 6.dp))
                                                Text(itemRep.item.name, fontWeight = FontWeight.Bold)
                                            }
                                            Text("Turnover Ratio Rate: ${itemRep.turnOverRate}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${itemRep.totalUnitsSold} Sold", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Ageing Analysis report
                    if (state.ageingReport.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No stock items quantified currently.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.ageingReport) { ageingRep ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(ageingRep.item.name, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Total Qty: ${ageingRep.totalStock}", fontSize = 12.sp, color = Color.DarkGray)
                                            Text("Est Value: ${formatInventoryCurrency(ageingRep.totalValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // Colored Age breakdown bar
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(24.dp)
                                                .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
                                        ) {
                                            Box(modifier = Modifier.fillMaxHeight().weight(0.4f).background(Color(0xFF81C784)), contentAlignment = Alignment.Center) {
                                                Text("<30d: ${ageingRep.ageing.lessThan30Days}", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Box(modifier = Modifier.fillMaxHeight().weight(0.3f).background(Color(0xFFFFB74D)), contentAlignment = Alignment.Center) {
                                                Text("30-60d: ${ageingRep.ageing.age30To60Days}", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Box(modifier = Modifier.fillMaxHeight().weight(0.2f).background(Color(0xFFE57373)), contentAlignment = Alignment.Center) {
                                                Text("61-90d: ${ageingRep.ageing.age61To90Days}", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Box(modifier = Modifier.fillMaxHeight().weight(0.1f).background(Color(0xFFBA68C8)), contentAlignment = Alignment.Center) {
                                                Text(">90d: ${ageingRep.ageing.over90Days}", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
}
