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
import com.bmstally.app.model.OutstandingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutstandingScreen(
    summary: String,
    ledgers: List<OutstandingItem>,
    groups: List<Pair<String, String>>,
    onMenu: () -> Unit,
    onReminderBanner: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Column {
                Text("Outstanding", color = Color.White)
                Text("Manage receivables & payables", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            } },
            navigationIcon = { IconButton(onMenu) { Icon(Icons.Default.Menu, "Menu", tint = Color.White) } },
            actions = {
                IconButton({}) { Icon(Icons.Default.Search, "Search", tint = Color.White) }
                IconButton({}) { Icon(Icons.Default.Tune, "Filter", tint = Color.White) }
                IconButton({}) { Icon(Icons.Default.MoreVert, "More", tint = Color.White) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Total Outstanding", color = Color.Gray, fontSize = 13.sp)
            Text(summary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Last updated: 18 Jun 26 @ 05:40 pm", color = Color.Gray, fontSize = 11.sp)
        }

        Card(onClick = onReminderBanner, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5))) {
            ListItem(
                headlineContent = { Text("Manage Auto Reminders", color = Color.White, fontWeight = FontWeight.W600) },
                leadingContent = { Icon(Icons.Default.Notifications, null, tint = Color.White) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = Color.White) }
            )
        }

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Ledgers") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Groups") })
        }

        if (tab == 0) {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(ledgers) { item ->
                    LedgerCard(item)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(groups) { (name, amount) ->
                    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))) {
                        ListItem(
                            headlineContent = { Text(name, fontWeight = FontWeight.W600) },
                            trailingContent = { Text(amount, fontWeight = FontWeight.Bold) },
                            leadingContent = {
                                Surface(shape = MaterialTheme.shapes.extraLarge, color = Color.Blue.copy(alpha = 0.1f)) {
                                    Icon(Icons.Default.Folder, null, Modifier.padding(8.dp), tint = Color.Blue)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerCard(item: OutstandingItem) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            val color = if (item.isCredit) Color.Green else Color.Red
            Surface(shape = MaterialTheme.shapes.extraLarge, color = color.copy(alpha = 0.1f)) {
                Icon(if (item.isCredit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null,
                    Modifier.padding(8.dp).size(20.dp), tint = color)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.partyName, fontWeight = FontWeight.W600)
                Text("${item.creditInfo} | ${item.meta}", fontSize = 11.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(item.amount, fontWeight = FontWeight.Bold)
                Text(item.paymentInfo, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
