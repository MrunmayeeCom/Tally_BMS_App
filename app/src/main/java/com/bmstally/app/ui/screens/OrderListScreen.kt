package com.bmstally.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import com.bmstally.app.model.Order
import com.bmstally.app.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    onBack: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOrders() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Book", color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp)) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by order no. or customer") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton({ viewModel.setSearchQuery("") }) { Icon(Icons.Default.Clear, "Clear") }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Sales", "Purchase").forEach { type ->
                    FilterChip(
                        selected = state.selectedType == type,
                        onClick = { viewModel.setSelectedType(type) },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (type == "Sales") Color(0xFF4CAF50).copy(alpha = 0.15f)
                                else if (type == "Purchase") Color(0xFFE53935).copy(alpha = 0.15f)
                                else Color(0xFF3F51B5).copy(alpha = 0.15f),
                            selectedLabelColor = if (type == "Sales") Color(0xFF2E7D32)
                                else if (type == "Purchase") Color(0xFFC62828)
                                else Color(0xFF3F51B5)
                        )
                    )
                }

                Spacer(Modifier.width(8.dp))

                var statusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    modifier = Modifier.width(160.dp)
                ) {
                    OutlinedTextField(
                        value = state.selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.menuAnchor(),
                        textStyle = MaterialTheme.typography.bodySmall,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        listOf("All Status", "Pending", "Completed", "Cancelled").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = { viewModel.setSelectedStatus(s); statusExpanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (state.filteredOrders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.paginatedOrders, key = { it.id }) { order ->
                        OrderCard(order)
                    }
                }

                Spacer(Modifier.height(4.dp))
                PaginationBarOrder(
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    totalRecords = state.totalRecords,
                    pageSize = state.pageSize,
                    onPageChange = { viewModel.setPage(it) },
                    onPageSizeChange = { viewModel.setPageSize(it) }
                )
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order) {
    val typeColor = if (order.type == "Sales") Color(0xFF2E7D32) else Color(0xFFC62828)
    val typeBg = if (order.type == "Sales") Color(0xFF4CAF50).copy(alpha = 0.12f) else Color(0xFFE53935).copy(alpha = 0.12f)

    val statusColor = when (order.status) {
        "Completed" -> Color(0xFF2E7D32)
        "Pending" -> Color(0xFFE65100)
        else -> Color(0xFFC62828)
    }
    val statusBg = when (order.status) {
        "Completed" -> Color(0xFF4CAF50).copy(alpha = 0.12f)
        "Pending" -> Color(0xFFFF9800).copy(alpha = 0.12f)
        else -> Color(0xFFE53935).copy(alpha = 0.12f)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.small, color = typeBg) {
                    Text(
                        order.type,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = typeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(order.orderNo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Surface(shape = MaterialTheme.shapes.small, color = statusBg) {
                    Text(
                        order.status,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(order.customer, fontSize = 14.sp, color = Color(0xFF424242))
            }

            Spacer(Modifier.height(4.dp))

            Row {
                Column(Modifier.weight(1f)) {
                    Text("Order Date", fontSize = 11.sp, color = Color.Gray)
                    Text(order.date, fontSize = 13.sp)
                }
                Column(Modifier.weight(1f)) {
                    Text("Due Date", fontSize = 11.sp, color = Color.Gray)
                    Text(order.dueDate, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Amount", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "₹ ${String.format("%,.0f", order.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF212121)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaginationBarOrder(
    currentPage: Int,
    totalPages: Int,
    totalRecords: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit
) {
    var pageSizeExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                    listOf(5, 10, 15, 20).forEach { size ->
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
