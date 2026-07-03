package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstally.app.data.MockDataService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    entryTypes: List<String>,
    onCreateTransaction: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("New Entry", color = Color.White) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Text("Select Entry Type", fontWeight = FontWeight.W600, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text("Choose the type of transaction you want to create", color = Color.Gray, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
            }
            items(entryTypes) { type ->
                Card(
                    onClick = { onCreateTransaction(type) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    ListItem(
                        headlineContent = { Text(type, fontWeight = FontWeight.W500) },
                        leadingContent = {
                            val icon = when {
                                type.contains("Sales") -> Icons.Default.ShoppingCart
                                type.contains("Purchase") -> Icons.Default.ShoppingBag
                                type.contains("Receipt") -> Icons.Default.AccountBalance
                                type.contains("Payment") -> Icons.Default.Payments
                                type.contains("Delivery") -> Icons.Default.LocalShipping
                                type.contains("Quotation") -> Icons.Default.Description
                                type.contains("Journal") -> Icons.Default.Book
                                else -> Icons.Default.ReceiptLong
                            }
                            Icon(icon, null, tint = Color(0xFF3F51B5))
                        },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = Color.Gray) }
                    )
                }
            }
        }
    }
}
