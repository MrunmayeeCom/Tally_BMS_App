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
fun ReminderSchedulerScreen(
    tenantId: String,
    onBack: () -> Unit
) {
    val reminders = remember { MockDataService.getReminders(tenantId) }
    var enabledSet by remember { mutableStateOf(reminders.filter { it.enabled }.map { it.id }.toMutableSet()) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Auto Reminder Scheduler", color = Color.White) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5).copy(alpha = 0.1f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, Modifier.size(32.dp), tint = Color(0xFF3F51B5))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Automated Reminders", fontWeight = FontWeight.W600, fontSize = 16.sp)
                            Text("System sends reminders automatically based on schedule", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            items(reminders) { r ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, null, Modifier.size(24.dp), tint = Color(0xFF3F51B5))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.title, fontWeight = FontWeight.W500, fontSize = 14.sp)
                            Text(r.description, color = Color.Gray, fontSize = 11.sp)
                            Text("Scheduled: ${r.time}", fontSize = 11.sp, color = Color(0xFF3F51B5))
                        }
                        Switch(
                            checked = r.id in enabledSet,
                            onCheckedChange = { if (it) enabledSet.add(r.id) else enabledSet.remove(r.id) }
                        )
                    }
                }
            }
        }
    }
}
