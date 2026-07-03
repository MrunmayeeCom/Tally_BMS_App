package com.bmstally.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.bmstally.app.model.Voucher
import com.bmstally.app.viewmodel.VoucherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherListScreen(
    onBack: () -> Unit,
    onCreateVoucher: () -> Unit,
    viewModel: VoucherViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsState()
    val deleteId by viewModel.deleteConfirmId.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadVouchers() }

    // Delete confirmation dialog
    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Voucher") },
            text = { Text("Are you sure you want to delete this voucher?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voucher Explorer", color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                actions = {
                    IconButton(onCreateVoucher) { Icon(Icons.Default.Add, "Create", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Search
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by party, ref no...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton({ viewModel.setSearchQuery("") }) { Icon(Icons.Default.Clear, null) }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter chips row
            val types = listOf("All", "Sales", "Purchase", "Payment", "Receipt", "Journal")
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                types.forEach { type ->
                    FilterChip(
                        selected = state.selectedType == type,
                        onClick = { viewModel.setSelectedType(type) },
                        label = { Text(type, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Status + date filters
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statuses = listOf("All Status", "Approved", "Cancelled")
                var statusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(statusExpanded, { statusExpanded = it }) {
                    OutlinedTextField(
                        value = state.selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f).menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                        textStyle = MaterialTheme.typography.bodySmall,
                        singleLine = true
                    )
                    ExposedDropdownMenu(statusExpanded, { statusExpanded = false }) {
                        statuses.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = { viewModel.setSelectedStatus(s); statusExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = state.fromDate,
                    onValueChange = { viewModel.setFromDate(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("From", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = state.toDate,
                    onValueChange = { viewModel.setToDate(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("To", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            // Voucher list
            if (state.vouchers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, null, Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("No vouchers found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(state.paginatedVouchers, key = { it.id }) { voucher ->
                        VoucherCard(
                            voucher = voucher,
                            onDelete = { viewModel.requestDelete(it) }
                        )
                    }
                    item {
                        PaginationBarVoucher(
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
private fun VoucherCard(voucher: Voucher, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (voucher.voucher_type) {
                        "Sales", "Receipt" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        "Purchase", "Payment" -> Color(0xFFEF5350).copy(alpha = 0.15f)
                        else -> Color(0xFF3F51B5).copy(alpha = 0.15f)
                    }
                ) {
                    Icon(
                        when (voucher.voucher_type) {
                            "Sales" -> Icons.Default.ShoppingCart
                            "Purchase" -> Icons.Default.ShoppingBag
                            "Receipt" -> Icons.Default.AccountBalance
                            "Payment" -> Icons.Default.Payment
                            else -> Icons.Default.SwapHoriz
                        },
                        null, Modifier.padding(8.dp).size(20.dp),
                        tint = when (voucher.voucher_type) {
                            "Sales", "Receipt" -> Color(0xFF4CAF50)
                            "Purchase", "Payment" -> Color(0xFFEF5350)
                            else -> Color(0xFF3F51B5)
                        }
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(voucher.voucher_type ?: "", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(voucher.party ?: "", fontSize = 12.sp, color = Color.Gray)
                    Text(voucher.reference_no ?: "", fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${String.format("%,.0f", maxOf(voucher.debit, voucher.credit))}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if ((voucher.debit > 0)) Color(0xFF4CAF50) else Color(0xFFEF5350)
                    )
                    StatusBadge(voucher.status ?: "")
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(voucher.voucher_date?.take(10) ?: "", fontSize = 11.sp, color = Color.Gray)
                if (voucher.narration != null) {
                    Text(voucher.narration, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
                }
                IconButton(
                    onClick = { onDelete(voucher.id) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete", Modifier.size(18.dp), tint = Color(0xFFEF5350))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (status == "Approved") Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFEF5350).copy(alpha = 0.15f)
    ) {
        Text(
            status,
            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (status == "Approved") Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaginationBarVoucher(
    currentPage: Int, totalPages: Int, pageSize: Int, totalRecords: Int,
    onPageChange: (Int) -> Unit, onPageSizeChange: (Int) -> Unit
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
                Text("$pageSize", modifier = Modifier.menuAnchor().clickable { expanded = true }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    listOf(5, 10, 15, 100).forEach { size ->
                        DropdownMenuItem(text = { Text("$size") }, onClick = { onPageSizeChange(size); expanded = false })
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("$totalRecords records", fontSize = 11.sp, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onPageChange(currentPage - 1) }, enabled = currentPage > 1) { Icon(Icons.Default.ChevronLeft, "Prev", Modifier.size(20.dp)) }
            Text("$currentPage / $totalPages", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = { onPageChange(currentPage + 1) }, enabled = currentPage < totalPages) { Icon(Icons.Default.ChevronRight, "Next", Modifier.size(20.dp)) }
        }
    }
}
