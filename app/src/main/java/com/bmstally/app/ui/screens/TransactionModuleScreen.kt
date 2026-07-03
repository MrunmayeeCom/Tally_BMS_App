package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.bmstally.app.data.MockDataService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionModuleScreen(
    tenantId: String,
    title: String,
    onBack: () -> Unit,
    onCreateNew: () -> Unit
) {
    val transactions = remember { MockDataService.getTransactions(tenantId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title, color = Color.White) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            actions = {
                IconButton(onCreateNew) { Icon(Icons.Default.Add, "Create", tint = Color.White) }
                IconButton({}) { Icon(Icons.Default.Search, "Search", tint = Color.White) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF3F51B5).copy(alpha = 0.1f)) {
                Text("All", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, color = Color(0xFF3F51B5))
            }
            Spacer(Modifier.width(8.dp))
            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF4CAF50).copy(alpha = 0.1f)) {
                Text("Posted", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, color = Color(0xFF4CAF50))
            }
            Spacer(Modifier.width(8.dp))
            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFFF9800).copy(alpha = 0.1f)) {
                Text("Pending", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, color = Color(0xFFFF9800))
            }
            Spacer(Modifier.weight(1f))
            IconButton({}) { Icon(Icons.Default.FilterList, "Filter", tint = Color.Gray) }
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(transactions) { t ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        val icon = when {
                            t.type.contains("Sales") -> Icons.Default.ShoppingCart
                            t.type.contains("Purchase") -> Icons.Default.ShoppingBag
                            t.type.contains("Receipt") -> Icons.Default.AccountBalance
                            t.type.contains("Payment") -> Icons.Default.Payments
                            t.type.contains("Delivery") -> Icons.Default.LocalShipping
                            t.type.contains("Order") -> Icons.Default.Assignment
                            else -> Icons.Default.ReceiptLong
                        }
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFF3F51B5).copy(alpha = 0.1f)) {
                            Icon(icon, null, Modifier.padding(8.dp).size(24.dp), tint = Color(0xFF3F51B5))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.party, fontWeight = FontWeight.W600, fontSize = 14.sp)
                            Text(t.type, color = Color.Gray, fontSize = 12.sp)
                            Text(t.date, fontSize = 11.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(t.amount, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val statusColor = when (t.status) {
                                "Posted" -> Color(0xFF4CAF50)
                                "Confirmed" -> Color(0xFF3F51B5)
                                else -> Color(0xFFFF9800)
                            }
                            Surface(shape = MaterialTheme.shapes.small, color = statusColor.copy(alpha = 0.2f)) {
                                Text(t.status, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = statusColor)
                            }
                        }
                    }
                }
            }
        }
    }
}
