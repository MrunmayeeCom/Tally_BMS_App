package com.example.feature.accounting.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.Resource
import com.example.feature.accounting.data.db.LocalOrder
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBookScreen(
    orderViewModel: OrderViewModel,
    companyId: String,
    onNavigateToCreate: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("All") }
    var selectedOrderForDetail by remember { mutableStateOf<LocalOrder?>(null) }
    
    LaunchedEffect(companyId) {
        orderViewModel.loadOrders(companyId, forceRefresh = true)
    }

    val ordersState by orderViewModel.ordersState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Book Module", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { orderViewModel.loadOrders(companyId, forceRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh List")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_order_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Order")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // TAB ROW SELECTOR FOR STATUS FILTERS
            ScrollableTabRow(
                selectedTabIndex = listOf("All", "Draft", "Approved", "Shipped", "Canceled").indexOf(selectedTab),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                listOf("All", "Draft", "Approved", "Shipped", "Canceled").forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            when (val state = ordersState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Failed to retrieve orders.", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.message ?: "Unknown Error")
                        }
                    }
                }
                is Resource.Success -> {
                    val rawOrders = state.data ?: emptyList()
                    val filteredOrders = if (selectedTab == "All") {
                        rawOrders
                    } else {
                        rawOrders.filter { it.status.equals(selectedTab, ignoreCase = true) }
                    }

                    if (filteredOrders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No $selectedTab orders registered.", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap the '+' button below to add your first sales quote.", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredOrders) { order ->
                                OrderItemCard(
                                    order = order,
                                    onClick = { selectedOrderForDetail = order }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // BOTTOM DETAIL SHEET
    if (selectedOrderForDetail != null) {
        val order = selectedOrderForDetail!!
        ModalBottomSheet(
            onDismissRequest = { selectedOrderForDetail = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ORDER ID: ${order.orderId}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatusBadge(status = order.status)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Party / Customer", color = Color.Gray, fontSize = 12.sp)
                Text(order.partyName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                Spacer(modifier = Modifier.height(12.dp))

                Text("Order Date", color = Color.Gray, fontSize = 12.sp)
                Text(order.date, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Items Summary", color = Color.Gray, fontSize = 12.sp)
                Text(order.itemsSummary, fontWeight = FontWeight.Normal, fontSize = 15.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Remarks", color = Color.Gray, fontSize = 12.sp)
                Text(order.remarks.ifBlank { "No additional comments." }, fontSize = 15.sp)

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Amount Due", color = Color.Gray, fontSize = 14.sp)
                    val formattedPrice = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(order.amount)
                    Text(formattedPrice, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1B5E20))
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // QUICK ACTION ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (order.status == "Draft") {
                        Button(
                            onClick = {
                                orderViewModel.updateOrderStatus(companyId, order.orderId, "Approved")
                                selectedOrderForDetail = selectedOrderForDetail?.copy(status = "Approved")
                            },
                            modifier = Modifier.weight(1f).testTag("action_approve_order")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve")
                        }
                    }

                    if (order.status != "Canceled" && order.status != "Shipped") {
                        OutlinedButton(
                            onClick = {
                                orderViewModel.updateOrderStatus(companyId, order.orderId, "Canceled")
                                selectedOrderForDetail = selectedOrderForDetail?.copy(status = "Canceled")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).testTag("action_cancel_order")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel")
                        }
                    }
                    
                    if (order.status == "Approved") {
                        Button(
                            onClick = {
                                orderViewModel.updateOrderStatus(companyId, order.orderId, "Shipped")
                                selectedOrderForDetail = selectedOrderForDetail?.copy(status = "Shipped")
                            },
                            modifier = Modifier.weight(1f).testTag("action_ship_order")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Shipped")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(order: LocalOrder, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("order_item_${order.orderId}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    Text(
                        text = order.orderId,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (order.pendingSync) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Pending Sync",
                            tint = Color(0xFFEF6C00),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Synchronized",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(order.partyName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(order.itemsSummary, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = order.status)
                Spacer(modifier = Modifier.height(6.dp))
                val formattedAmount = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(order.amount)
                Text(formattedAmount, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val containerColor = when (status.lowercase()) {
        "draft" -> Color(0xFFFFF3E0)
        "approved" -> Color(0xFFE3F2FD)
        "shipped" -> Color(0xFFE8F5E9)
        "delivered" -> Color(0xFFE8F5E9)
        "canceled" -> Color(0xFFFFEBEE)
        else -> Color(0xFFF5F5F5)
    }

    val contentColor = when (status.lowercase()) {
        "draft" -> Color(0xFFE65100)
        "approved" -> Color(0xFF0D47A1)
        "shipped" -> Color(0xFF1B5E20)
        "delivered" -> Color(0xFF1B5E20)
        "canceled" -> Color(0xFFB71C1C)
        else -> Color(0xFF616161)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = contentColor)
    }
}
