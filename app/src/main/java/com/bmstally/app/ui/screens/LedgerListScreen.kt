package com.bmstally.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bmstally.app.model.Ledger
import com.bmstally.app.viewmodel.LedgerListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerListScreen(
    onBack: () -> Unit,
    onViewLedger: (String) -> Unit,
    viewModel: LedgerListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadLedgers() }

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ledger List", color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by party name...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton({ viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filters row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type filter chips
                FilterChip(
                    selected = state.filterType == "all",
                    onClick = { viewModel.setFilterType("all") },
                    label = { Text("All", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = state.filterType == "Dr",
                    onClick = { viewModel.setFilterType("Dr") },
                    label = { Text("Debit", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = state.filterType == "Cr",
                    onClick = { viewModel.setFilterType("Cr") },
                    label = { Text("Credit", fontSize = 12.sp) }
                )
                Spacer(Modifier.weight(1f))
                IconButton({ showFilterSheet = true }) {
                    Icon(Icons.Default.FilterList, null)
                }
            }

            // Group filter dropdown
            if (state.groups.size > 1) {
                GroupFilterBar(
                    groups = state.groups,
                    selectedGroup = state.selectedGroup,
                    onSelectGroup = { viewModel.setSelectedGroup(it) }
                )
            }

            HorizontalDivider()

            // Ledger list
            if (state.ledgers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AccountBalance, null, Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("No ledgers found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(state.paginatedLedgers, key = { it.ledger_guid }) { ledger ->
                        LedgerCard(ledger = ledger, onClick = { onViewLedger(ledger.ledger_guid) })
                    }

                    // Pagination
                    item {
                        PaginationBar(
                            currentPage = state.currentPage,
                            totalPages = state.totalPages,
                            pageSize = state.pageSize,
                            totalRecords = state.totalRecords,
                            onPageChange = { viewModel.setPage(it) },
                            onPageSizeChange = { viewModel.setPageSize(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerCard(ledger: Ledger, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (ledger.nature == "Dr") Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFEF5350).copy(alpha = 0.1f)
            ) {
                Icon(
                    if (ledger.nature == "Dr") Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                    null,
                    Modifier.padding(10.dp).size(24.dp),
                    tint = if (ledger.nature == "Dr") Color(0xFF4CAF50) else Color(0xFFEF5350)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ledger.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    ledger.parent_group ?: ledger.category ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${String.format("%,.0f", ledger.outstanding)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (ledger.nature == "Dr") Color(0xFF4CAF50) else Color(0xFFEF5350)
                )
                NatureBadge(ledger.nature)
            }
        }
        if (ledger.dueDays > 0) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        ledger.dueDays <= 7 -> Color(0xFFFFEBEE)
                        ledger.dueDays <= 15 -> Color(0xFFFFF3E0)
                        else -> Color(0xFFE3F2FD)
                    }
                ) {
                    Text(
                        "${ledger.dueDays} days",
                        Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        color = when {
                            ledger.dueDays <= 7 -> Color(0xFFC62828)
                            ledger.dueDays <= 15 -> Color(0xFFEF6C00)
                            else -> Color(0xFF1565C0)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NatureBadge(nature: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (nature == "Dr") Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFEF5350).copy(alpha = 0.15f)
    ) {
        Text(
            if (nature == "Dr") "Dr" else "Cr",
            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (nature == "Dr") Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupFilterBar(
    groups: List<String>,
    selectedGroup: String,
    onSelectGroup: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.FilterAlt, null, Modifier.size(18.dp), tint = Color.Gray)
        Spacer(Modifier.width(4.dp))
        ExposedDropdownMenuBox(expanded, { expanded = it }) {
            Text(
                text = selectedGroup,
                modifier = Modifier.menuAnchor().clickable { expanded = true },
                fontSize = 13.sp,
                color = Color(0xFF3F51B5),
                fontWeight = FontWeight.Medium
            )
            ExposedDropdownMenu(expanded, { expanded = false }) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group) },
                        onClick = { onSelectGroup(group); expanded = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    pageSize: Int,
    totalRecords: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Rows: ", fontSize = 12.sp, color = Color.Gray)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded, { expanded = it }) {
                Text(
                    "$pageSize",
                    modifier = Modifier.menuAnchor().clickable { expanded = true },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    listOf(5, 10, 15, 100).forEach { size ->
                        DropdownMenuItem(
                            text = { Text("$size") },
                            onClick = { onPageSizeChange(size); expanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "${(currentPage - 1) * pageSize + 1}-${minOf(currentPage * pageSize, totalRecords)} of $totalRecords",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 1
            ) { Icon(Icons.Default.ChevronLeft, "Prev", Modifier.size(20.dp)) }
            Text(
                "$currentPage / $totalPages",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages
            ) { Icon(Icons.Default.ChevronRight, "Next", Modifier.size(20.dp)) }
        }
    }
}
