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
fun FollowUpsScreen(
    tenantId: String,
    onBack: () -> Unit
) {
    val followUps = remember { MockDataService.getFollowUps(tenantId) }
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Completed", "In Progress")

    val filtered = remember(followUps, filter) {
        if (filter == "All") followUps
        else followUps.filter { it.status == filter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Follow Ups", color = Color.White) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            actions = { IconButton({}) { Icon(Icons.Default.Add, "Add", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        ScrollableTabRow(selectedTabIndex = filters.indexOf(filter)) {
            filters.forEach { f ->
                Tab(selected = filter == f, onClick = { filter = f }, text = { Text(f) })
            }
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(filtered) { f ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        val statusColor = when (f.status) {
                            "Completed" -> Color(0xFF4CAF50)
                            "In Progress" -> Color(0xFFFF9800)
                            else -> Color(0xFFEF5350)
                        }
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = statusColor.copy(alpha = 0.1f)) {
                            Icon(
                                when (f.status) {
                                    "Completed" -> Icons.Default.CheckCircle
                                    "In Progress" -> Icons.Default.Schedule
                                    else -> Icons.Default.Pending
                                }, null, Modifier.padding(8.dp).size(24.dp), tint = statusColor
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(f.party, fontWeight = FontWeight.W600, fontSize = 14.sp)
                            Text(f.purpose, color = Color.Gray, fontSize = 12.sp)
                            if (f.notes.isNotEmpty()) {
                                Text(f.notes, color = Color.Gray, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, Modifier.size(12.dp), tint = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text(f.date, fontSize = 11.sp, color = Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Surface(shape = MaterialTheme.shapes.small, color = statusColor.copy(alpha = 0.2f)) {
                                    Text(f.status, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = statusColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
