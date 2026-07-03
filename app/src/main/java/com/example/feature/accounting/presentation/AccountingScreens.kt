package com.example.feature.accounting.presentation

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.UiState
import com.example.core.database.LocalLedger
import com.example.core.database.LocalVoucher
import com.example.core.database.LocalBill
import com.example.feature.accounting.domain.*
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// Helper currency formatter
fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return try {
        formatter.format(amount)
    } catch (e: Exception) {
        "₹${String.format("%.2f", amount)}"
    }
}

fun formatCurrency(amount: java.math.BigDecimal): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return try {
        formatter.format(amount)
    } catch (e: Exception) {
        "₹${String.format("%.2f", amount.toDouble())}"
    }
}

// ==========================================
// 1. LEDGER LIST SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerListScreen(
    viewModel: LedgerViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val contextCompanyId = viewModel.companyId.collectAsState(initial = "COMP001").value ?: "COMP001"
    val userRole = viewModel.userRole.collectAsState(initial = "Employee").value ?: "Employee"
    val ledgersState = viewModel.ledgersState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf("All") }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(contextCompanyId) {
        viewModel.loadLedgers(contextCompanyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tally Accounts / Ledgers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("ledger_list_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadLedgers(contextCompanyId, forceRefresh = true) },
                        modifier = Modifier.testTag("ledger_list_refresh_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            // Role Permission gate: Super Admin, Company Admin, Accountant can build ledgers
            val isAuthorized = userRole == "Super Admin" || userRole == "Company Admin" || userRole == "Accountant"
            if (isAuthorized) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("create_ledger_fab"),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Ledger")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Input Block
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Ledgers") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ledger_search_input")
                    .padding(bottom = 12.dp)
            )

            // Horizontal scrolling group selectors
            val groupsList = listOf("All", "Sundry Debtors", "Sundry Creditors", "Direct Expenses", "Sales Account", "Bank Accounts")
            ScrollableTabRow(
                selectedTabIndex = groupsList.indexOf(selectedGroupFilter).coerceAtLeast(0),
                modifier = Modifier.padding(bottom = 12.dp),
                edgePadding = 0.dp
            ) {
                groupsList.forEach { group ->
                    Tab(
                        selected = selectedGroupFilter == group,
                        onClick = { selectedGroupFilter = group },
                        text = { Text(group) },
                        modifier = Modifier.testTag("group_tab_$group")
                    )
                }
            }

            // Results UI state switch
            when (val state = ledgersState.value) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val filteredList = state.data.filter { ledger ->
                        val matchesSearch = ledger.name.contains(searchQuery, ignoreCase = true) ||
                                            ledger.group.contains(searchQuery, ignoreCase = true)
                        val matchesGroup = selectedGroupFilter == "All" || ledger.group.equals(selectedGroupFilter, ignoreCase = true)
                        matchesSearch && matchesGroup
                    }

                    if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No ledgers found for your query Filters.", color = Color.Gray, modifier = Modifier.testTag("ledger_empty_view"))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredList) { ledger ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToDetail(ledger.ledgerId) }
                                        .testTag("ledger_item_${ledger.ledgerId}"),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ledger.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(ledger.group, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                                        }
                                        Text(
                                            formatCurrency(ledger.balance),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (ledger.balance >= java.math.BigDecimal.ZERO) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.testTag("ledger_item_balance_${ledger.ledgerId}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error loading data: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Idle -> {}
            }
        }
    }

    // Modal Create dialog
    if (showCreateDialog) {
        var ledgerName by remember { mutableStateOf("") }
        var ledgerGroup by remember { mutableStateOf("Sundry Debtors") }
        var initialBal by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Generate Local Tally Ledger") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ledgerName,
                        onValueChange = { ledgerName = it },
                        label = { Text("Account Name") },
                        modifier = Modifier.fillMaxWidth().testTag("create_ledger_name")
                    )
                    
                    val groups = listOf("Sundry Debtors", "Sundry Creditors", "Sales Account", "Purchase Account", "Indirect Expenses", "Capital Account")
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ledgerGroup,
                            onValueChange = {},
                            label = { Text("Group Classification") },
                            readOnly = true,
                            trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.List, contentDescription = null) } },
                            modifier = Modifier.fillMaxWidth().testTag("create_ledger_group_selector")
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            groups.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g) },
                                    onClick = {
                                        ledgerGroup = g
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = initialBal,
                        onValueChange = { initialBal = it },
                        label = { Text("Opening Balance (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("create_ledger_opening_bal")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedBal = initialBal.toDoubleOrNull() ?: 0.0
                        viewModel.createLedger(contextCompanyId, ledgerName, ledgerGroup, parsedBal) {
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.testTag("create_ledger_submit_dialog_btn")
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// 2. LEDGER DETAIL SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDetailScreen(
    ledgerId: String,
    viewModel: LedgerViewModel,
    onNavigateBack: () -> Unit
) {
    val contextCompanyId = viewModel.companyId.collectAsState(initial = "COMP001").value ?: "COMP001"
    val ledgerDetailState = viewModel.ledgerDetailState.collectAsState()
    val ledgerVouchersState = viewModel.ledgerVouchersState.collectAsState()

    var filterStartDate by remember { mutableStateOf("") }
    var filterEndDate by remember { mutableStateOf("") }

    LaunchedEffect(ledgerId, contextCompanyId) {
        viewModel.loadLedgerDetail(ledgerId, contextCompanyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ledger Statement Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("ledger_detail_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // General Ledger Info Card
            when (val state = ledgerDetailState.value) {
                is UiState.Success -> {
                    val ledger = state.data
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("ledger_detail_info_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(ledger.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Group: ${ledger.group}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tally GUID: ${ledger.tallyGuid}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Current Balance:", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(
                                    formatCurrency(ledger.balance),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (ledger.balance >= java.math.BigDecimal.ZERO) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
                is UiState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is UiState.Error -> Text("Failed to render general ledger metadata: ${state.message}", color = Color.Red)
                is UiState.Idle -> {}
            }

            // Quick Date parameters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = filterStartDate,
                    onValueChange = { filterStartDate = it },
                    label = { Text("From Date (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f).testTag("detail_from_date")
                )
                OutlinedTextField(
                    value = filterEndDate,
                    onValueChange = { filterEndDate = it },
                    label = { Text("To Date (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f).testTag("detail_to_date")
                )
            }

            // Interactive chronologically categorized statements list
            Text("Ledger Account Timeline / Vouchers", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))

            when (val state = ledgerVouchersState.value) {
                is UiState.Success -> {
                    val vouchers = state.data.filter { v ->
                        val afterStart = filterStartDate.isEmpty() || v.date >= filterStartDate
                        val beforeEnd = filterEndDate.isEmpty() || v.date <= filterEndDate
                        afterStart && beforeEnd
                    }

                    if (vouchers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No voucher transactions found matching parameters.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("ledger_detail_records_list"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(vouchers) { voucher ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(voucher.type, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(voucher.date, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Text(
                                            formatCurrency(voucher.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (voucher.type == "Receipt" || voucher.type == "Contra") Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Loading -> Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is UiState.Error -> Text("Failed to query vouchers balance timeline: ${state.message}", color = Color.Red)
                is UiState.Idle -> {}
            }
        }
    }
}

// ==========================================
// 3. VOUCHER LIST SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherListScreen(
    viewModel: VoucherViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val contextCompanyId = viewModel.companyId.collectAsState(initial = "COMP001").value ?: "COMP001"
    val userRole = viewModel.userRole.collectAsState(initial = "Employee").value ?: "Employee"
    val vouchersState = viewModel.vouchersState.collectAsState()

    var activeTab by remember { mutableStateOf("All") }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(contextCompanyId) {
        viewModel.loadVouchers(contextCompanyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tally Accounting Vouchers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("voucher_list_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadVouchers(contextCompanyId, forceRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            val canWrite = userRole == "Super Admin" || userRole == "Company Admin" || userRole == "Accountant" || userRole == "Sales Manager"
            if (canWrite) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("create_voucher_fab"),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Voucher")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Select Voucher sub types
            val tabs = listOf("All", "Sales", "Purchase", "Payment", "Receipt", "Journal", "Contra")
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(activeTab).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                tabs.forEach { t ->
                    Tab(
                        selected = activeTab == t,
                        onClick = { activeTab = t },
                        text = { Text(t) },
                        modifier = Modifier.testTag("voucher_tab_$t")
                    )
                }
            }

            when (val state = vouchersState.value) {
                is UiState.Success -> {
                    val filtered = state.data.filter {
                        activeTab == "All" || it.type.equals(activeTab, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No vouchers in this classification classification yet.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered) { voucher ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToDetail(voucher.voucherId) }
                                        .testTag("voucher_item_${voucher.voucherId}"),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(voucher.type, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (voucher.pendingSync) {
                                                    Icon(Icons.Default.Warning, contentDescription = "Pending Sync", tint = Color(0xFFFFA500), modifier = Modifier.size(14.dp))
                                                } else {
                                                    Icon(Icons.Default.Check, contentDescription = "Synced", tint = Color.Green, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            Text("Party: ${voucher.partyName}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            Text("Date: ${voucher.date}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Text(
                                            formatCurrency(voucher.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (voucher.type == "Receipt" || voucher.type == "Contra") Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${state.message}") }
                is UiState.Idle -> {}
            }
        }
    }

    if (showCreateDialog) {
        var recType by remember { mutableStateOf("Sales") }
        var targetPartyId by remember { mutableStateOf("") }
        var targetPartyName by remember { mutableStateOf("") }
        var stringAmount by remember { mutableStateOf("") }
        var dateField by remember { mutableStateOf("2026-06-20") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Generate Voucher document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val vTypes = listOf("Sales", "Purchase", "Payment", "Receipt", "Journal", "Contra")
                    var expandedT by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = recType,
                            onValueChange = {},
                            label = { Text("Voucher Classification") },
                            readOnly = true,
                            trailingIcon = { IconButton(onClick = { expandedT = true }) { Icon(Icons.Default.List, contentDescription = null) } },
                            modifier = Modifier.fillMaxWidth().testTag("add_voucher_type")
                        )
                        DropdownMenu(expanded = expandedT, onDismissRequest = { expandedT = false }) {
                            vTypes.forEach { vt ->
                                DropdownMenuItem(text = { Text(vt) }, onClick = { recType = vt; expandedT = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = targetPartyId,
                        onValueChange = { targetPartyId = it },
                        label = { Text("Party Account ID") },
                        modifier = Modifier.fillMaxWidth().testTag("add_voucher_party_id")
                    )
                    OutlinedTextField(
                        value = targetPartyName,
                        onValueChange = { targetPartyName = it },
                        label = { Text("Party / Client Name") },
                        modifier = Modifier.fillMaxWidth().testTag("add_voucher_party_name")
                    )
                    OutlinedTextField(
                        value = stringAmount,
                        onValueChange = { stringAmount = it },
                        label = { Text("Voucher Sum Value (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_voucher_amount")
                    )
                    OutlinedTextField(
                        value = dateField,
                        onValueChange = { dateField = it },
                        label = { Text("Record Date") },
                        modifier = Modifier.fillMaxWidth().testTag("add_voucher_date")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val numeric = stringAmount.toDoubleOrNull() ?: 1.0
                        viewModel.createVoucher(
                            contextCompanyId, recType, targetPartyId.ifEmpty { "L001" },
                            targetPartyName.ifEmpty { "Generic Party" }, numeric, dateField
                        ) {
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.testTag("add_voucher_submit_confirm")
                ) {
                    Text("Register Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Retract")
                }
            }
        )
    }
}

// ==========================================
// 4. VOUCHER DETAIL SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherDetailScreen(
    voucherId: String,
    viewModel: VoucherViewModel,
    onNavigateBack: () -> Unit
) {
    val contextCompanyId = viewModel.companyId.collectAsState(initial = "COMP001").value ?: "COMP001"
    val voucherState = viewModel.voucherDetailState.collectAsState()

    LaunchedEffect(voucherId, contextCompanyId) {
        viewModel.loadVoucherDetail(voucherId, contextCompanyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voucher Statement Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("voucher_detail_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = voucherState.value) {
                is UiState.Success -> {
                    val voucher = state.data
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voucher_detail_card"),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(voucher.type, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                if (voucher.pendingSync) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Local Pending Sync") },
                                        icon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    )
                                } else {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Synced Cloud State") },
                                        icon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Ledger Account Reference", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                            Text(voucher.partyName, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            Text("Ledger ID: ${voucher.partyLedgerId}", fontSize = 12.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Registered Document ID", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                            Text(voucher.voucherId, fontSize = 15.sp)

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Transaction Document Date", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                            Text(voucher.date, fontSize = 15.sp)

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Statement value", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    formatCurrency(voucher.amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = if (voucher.type == "Receipt" || voucher.type == "Contra") Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> Text("Failed to render voucher: ${state.message}", color = Color.Red, modifier = Modifier.testTag("voucher_detail_error"))
                is UiState.Idle -> {}
            }
        }
    }
}

// ==========================================
// 5. BILL LIST SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    viewModel: BillViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val contextCompanyId = viewModel.companyId.collectAsState(initial = "COMP001").value ?: "COMP001"
    val billsState = viewModel.billsState.collectAsState()

    var activeTab by remember { mutableStateOf("Open") }

    LaunchedEffect(contextCompanyId) {
        viewModel.loadBills(contextCompanyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tally Bills & Receivables", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("bill_list_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadBills(contextCompanyId, forceRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = if (activeTab == "Open") 0 else if (activeTab == "Closed") 1 else 2) {
                Tab(selected = activeTab == "Open", onClick = { activeTab = "Open" }, text = { Text("Open Bills") }, modifier = Modifier.testTag("bill_tab_open"))
                Tab(selected = activeTab == "Closed", onClick = { activeTab = "Closed" }, text = { Text("Closed") }, modifier = Modifier.testTag("bill_tab_closed"))
                Tab(selected = activeTab == "All", onClick = { activeTab = "All" }, text = { Text("All Bills") }, modifier = Modifier.testTag("bill_tab_all"))
            }

            when (val state = billsState.value) {
                is UiState.Success -> {
                    val filtered = state.data.filter {
                        when (activeTab) {
                            "Open" -> it.status.equals("open", ignoreCase = true) || it.status.equals("partial", ignoreCase = true)
                            "Closed" -> it.status.equals("closed", ignoreCase = true)
                            else -> true
                        }
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No bills matching selected state.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered) { bill ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToDetail(bill.billId) }
                                        .testTag("bill_item_${bill.billId}"),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Bill ID: ${bill.billId}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            StatusTag(bill.status)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Party Name: ${bill.partyName}", fontSize = 14.sp)
                                        Text("Due Date: ${bill.dueDate}", fontSize = 12.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SuggestionChip(onClick = {}, label = { Text("Recovery: ${bill.recoveryState}") })
                                            Text(
                                                formatCurrency(bill.amount),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (bill.status == "closed") Color.Gray else Color(0xFFC62828)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${state.message}") }
                is UiState.Idle -> {}
            }
        }
    }
}

@Composable
fun StatusTag(valStr: String) {
    val clean = valStr.lowercase()
    val text = clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    val color = when (clean) {
        "open" -> Color(0xFFD32F2F)
        "partial" -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ==========================================
// 6. BILL DETAIL SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailScreen(
    billId: String,
    viewModel: BillViewModel,
    onNavigateBack: () -> Unit
) {
    val contextCompanyId = viewModel.companyId.collectAsState(initial = "COMP001").value ?: "COMP001"
    val billState = viewModel.billDetailState.collectAsState()

    LaunchedEffect(billId, contextCompanyId) {
        viewModel.loadBillDetail(billId, contextCompanyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outstanding Bill Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("bill_detail_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = billState.value) {
                is UiState.Success -> {
                    val bill = state.data
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(modifier = Modifier.fillMaxWidth().testTag("bill_detail_card")) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Outstanding Bill info", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    StatusTag(bill.status)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Bill Reference:", color = Color.Gray, fontSize = 14.sp)
                                    Text(bill.billId, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Customer / Vendor:", color = Color.Gray, fontSize = 14.sp)
                                    Text(bill.partyName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Payment Due Date:", color = Color.Gray, fontSize = 14.sp)
                                    Text(bill.dueDate, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Total Outstandings Sum", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(formatCurrency(bill.amount), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD32F2F))
                                }
                            }
                        }

                        // Outstanding Tracker Section
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Outstanding Recovery Tracker", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Recovery Status: ${bill.recoveryState}", fontWeight = FontWeight.SemiBold)
                                if (!bill.promisedDate.isNullOrEmpty()) {
                                    Text("Target Promised Date: ${bill.promisedDate}", fontSize = 13.sp, color = Color.Blue)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Update Recovery state:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.updateBillRecoveryState(bill.billId, "Reminder Sent", null, contextCompanyId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.weight(1f).testTag("action_remind_sent")
                                    ) {
                                        Text("Sent Reminder", fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.updateBillRecoveryState(bill.billId, "Promised", "2026-07-01", contextCompanyId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                        modifier = Modifier.weight(1f).testTag("action_promised")
                                    ) {
                                        Text("Promised", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> Text("Failed: ${state.message}", color = Color.Red)
                is UiState.Idle -> {}
            }
        }
    }
}

// ==========================================
// 7. TRANSACTION EXPLORER SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionExplorerScreen(
    viewModel: TransactionExplorerViewModel,
    onNavigateBack: () -> Unit
) {
    val contextCompanyId = viewModel.companyId.collectAsState(initial = "COMP001").value ?: "COMP001"
    val transactionsState = viewModel.transactionsState.collectAsState()

    val searchQuery = viewModel.searchQuery.collectAsState()
    val selectedType = viewModel.selectedType.collectAsState()
    val currPage = viewModel.currentPage.collectAsState()
    val totalPages = viewModel.totalPages.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(contextCompanyId) {
        viewModel.runQuery(contextCompanyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tally Transaction Explorer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("explorer_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }, modifier = Modifier.testTag("explorer_filter_toggle")) {
                        Icon(Icons.Default.List, contentDescription = "Toggle Filters")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("filter_sheet")
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Interactive Filters Options", fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(
                            value = searchQuery.value,
                            onValueChange = { viewModel.searchQuery.value = it },
                            label = { Text("Search Party/Owner") },
                            modifier = Modifier.fillMaxWidth().testTag("filter_search")
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var exp by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = selectedType.value,
                                    onValueChange = {},
                                    label = { Text("Voucher Classification") },
                                    readOnly = true,
                                    trailingIcon = { IconButton(onClick = { exp = true }) { Icon(Icons.Default.List, contentDescription = null) } },
                                    modifier = Modifier.fillMaxWidth().testTag("filter_type_select")
                                )
                                DropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                                    listOf("All", "Sales", "Purchase", "Payment", "Receipt").forEach { t ->
                                        DropdownMenuItem(text = { Text(t) }, onClick = {
                                            viewModel.selectedType.value = t
                                            exp = false
                                        })
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.runQuery(contextCompanyId) },
                                modifier = Modifier.weight(1f).testTag("apply_explorer_filters")
                            ) {
                                Text("Apply")
                            }
                            TextButton(
                                onClick = { viewModel.resetFilters(contextCompanyId) },
                                modifier = Modifier.weight(1f).testTag("clear_explorer_filters")
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            // Results List
            when (val state = transactionsState.value) {
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("No transactions match criteria.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.data) { item ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(item.type, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(item.partyName, fontSize = 14.sp)
                                            Text(item.date, fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Text(formatCurrency(item.amount), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Loading -> Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is UiState.Error -> Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("Error: ${state.message}") }
                is UiState.Idle -> {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pagination Controls Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.prevPage(contextCompanyId) },
                    enabled = currPage.value > 1,
                    modifier = Modifier.testTag("prev_page_btn")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Prev Page")
                }
                Text("Page ${currPage.value} of ${totalPages.value}", fontWeight = FontWeight.Medium)
                IconButton(
                    onClick = { viewModel.nextPage(contextCompanyId) },
                    enabled = currPage.value < totalPages.value,
                    modifier = Modifier.testTag("next_page_btn")
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Page")
                }
            }
        }
    }
}

// ==========================================
// 8. FINANCIAL SUMMARY SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialSummaryScreen(
    viewModel: FinancialSummaryViewModel,
    onNavigateToLedger: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val contextCompanyId = viewModel.companyId.collectAsState(initial = "COMP001").value ?: "COMP001"

    val trialBalanceState = viewModel.trialBalanceState.collectAsState()
    val profitLossState = viewModel.profitLossState.collectAsState()
    val balanceSheetState = viewModel.balanceSheetState.collectAsState()
    val cashFlowState = viewModel.cashFlowState.collectAsState()

    var activeReportTab by remember { mutableStateOf(0) }

    LaunchedEffect(contextCompanyId) {
        viewModel.loadAllFinancials(contextCompanyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tally Financial Books", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("financial_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAllFinancials(contextCompanyId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = activeReportTab) {
                Tab(selected = activeReportTab == 0, onClick = { activeReportTab = 0 }, text = { Text("Trial Bal") }, modifier = Modifier.testTag("tab_trial"))
                Tab(selected = activeReportTab == 1, onClick = { activeReportTab = 1 }, text = { Text("P&L Summary") }, modifier = Modifier.testTag("tab_pl"))
                Tab(selected = activeReportTab == 2, onClick = { activeReportTab = 2 }, text = { Text("Bal Sheet") }, modifier = Modifier.testTag("tab_bs"))
                Tab(selected = activeReportTab == 3, onClick = { activeReportTab = 3 }, text = { Text("Cash Flow") }, modifier = Modifier.testTag("tab_cf"))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (activeReportTab) {
                    0 -> TrialBalanceView(trialBalanceState.value)
                    1 -> ProfitLossView(profitLossState.value)
                    2 -> BalanceSheetView(balanceSheetState.value)
                    3 -> CashFlowView(cashFlowState.value)
                }
            }
        }
    }
}

// Sub components for financial reports views
@Composable
fun TrialBalanceView(state: UiState<TrialBalanceSummary>) {
    when (state) {
        is UiState.Success -> {
            val data = state.data
            Column {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Debits Amount:", fontSize = 12.sp, color = Color.Gray)
                            Text(formatCurrency(data.totalDebit), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2E7D32))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Credits Amount:", fontSize = 12.sp, color = Color.Gray)
                            Text(formatCurrency(data.totalCredit), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFC62828))
                        }
                    }
                }

                Text("Ledger Account Sums Classification", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.testTag("trial_balance_list")) {
                    items(data.items) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.ledgerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(item.groupName, fontSize = 11.sp, color = Color.Gray)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (item.debitAmount > java.math.BigDecimal.ZERO) {
                                        Text("Dr ${formatCurrency(item.debitAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    } else if (item.creditAmount > java.math.BigDecimal.ZERO) {
                                        Text("Cr ${formatCurrency(item.creditAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    } else {
                                        Text("0.0", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Text("Calculation Error: ${state.message}", color = Color.Red)
        is UiState.Idle -> {}
    }
}

@Composable
fun ProfitLossView(state: UiState<ProfitLossSummary>) {
    when (state) {
        is UiState.Success -> {
            val data = state.data
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Main Net Profit card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Net Operations Income (Net Profit)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(data.netProfit), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Gross Margins Profit: ${formatCurrency(data.grossProfit)}", fontSize = 12.sp)
                    }
                }

                Text("Statement Breakdown Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.testTag("pl_breakdown_list")) {
                    item {
                        ExpandableSectionHeader("Revenues & Inflows Summary", data.revenue.amount, true)
                    }
                    items(data.revenue.items) { item ->
                        ListItem(
                            headlineContent = { Text(item.first) },
                            trailingContent = { Text(formatCurrency(item.second), fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32)) }
                        )
                    }
                    item {
                        ExpandableSectionHeader("Purchasing & Operations Expenses", data.expense.amount, false)
                    }
                    items(data.expense.items) { item ->
                        ListItem(
                            headlineContent = { Text(item.first) },
                            trailingContent = { Text(formatCurrency(item.second), fontWeight = FontWeight.SemiBold, color = Color(0xFFC62828)) }
                        )
                    }
                }
            }
        }
        is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Text("Calculation Error: ${state.message}", color = Color.Red)
        is UiState.Idle -> {}
    }
}

@Composable
fun ExpandableSectionHeader(title: String, sum: java.math.BigDecimal, isPositive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(formatCurrency(sum), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828))
    }
}

@Composable
fun BalanceSheetView(state: UiState<BalanceSheetSummary>) {
    when (state) {
        is UiState.Success -> {
            val data = state.data
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Assets Sum", fontSize = 11.sp, color = Color.Gray)
                            Text(formatCurrency(data.totalAssets), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Liabs & Capital", fontSize = 11.sp, color = Color.Gray)
                            Text(formatCurrency(data.totalLiabilitiesAndEquity), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFC62828))
                        }
                    }
                }

                Text("Aesthetic Ledger Balances", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        ExpandableSectionHeader("Liquid / Fixed Assets", data.assets.amount, true)
                    }
                    items(data.assets.items) { item ->
                        ListItem(
                            headlineContent = { Text(item.first) },
                            trailingContent = { Text(formatCurrency(item.second), fontSize = 13.sp) }
                        )
                    }
                    item {
                        ExpandableSectionHeader("Corporate Liabilities", data.liabilities.amount, false)
                    }
                    items(data.liabilities.items) { item ->
                        ListItem(
                            headlineContent = { Text(item.first) },
                            trailingContent = { Text(formatCurrency(item.second), fontSize = 13.sp) }
                        )
                    }
                    item {
                        ExpandableSectionHeader("Capital Accounts & Equities", data.equity.amount, false)
                    }
                }
            }
        }
        is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Text("Calculation Error: ${state.message}", color = Color.Red)
        is UiState.Idle -> {}
    }
}

@Composable
fun CashFlowView(state: UiState<CashFlowSnapshot>) {
    when (state) {
        is UiState.Success -> {
            val data = state.data
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Net Cash Movement Current Month", fontSize = 11.sp, color = Color.Gray)
                            Text(formatCurrency(data.netCashFlow), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (data.netCashFlow >= java.math.BigDecimal.ZERO) Color(0xFF2E7D32) else Color(0xFFC62828))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Fluid Reserves", fontSize = 11.sp, color = Color.Gray)
                            Text(formatCurrency(data.closingBalance), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                Text("Fluid Statements Logs History", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.testTag("cashflow_timeline")) {
                    items(data.items) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.description, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(item.date, fontSize = 11.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if (item.inflow > java.math.BigDecimal.ZERO) {
                                        Text("+ ${formatCurrency(item.inflow)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    } else {
                                        Text("- ${formatCurrency(item.outflow)}", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Text("Cum: ${formatCurrency(item.balance)}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
        is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Text("Calculation Error: ${state.message}", color = Color.Red)
        is UiState.Idle -> {}
    }
}
