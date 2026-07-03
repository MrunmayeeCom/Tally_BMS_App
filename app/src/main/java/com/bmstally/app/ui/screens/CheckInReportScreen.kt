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
fun CheckInReportScreen(
    tenantId: String,
    onBack: () -> Unit
) {
    val checkIns = remember { MockDataService.getCheckIns(tenantId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Check-In Report", color = Color.White) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            actions = { IconButton({}) { Icon(Icons.Default.CalendarMonth, "Calendar", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5).copy(alpha = 0.1f))
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Total", "${checkIns.size}", Icons.Default.People)
                StatItem("On Time", "${checkIns.count { it.status == "Checked In" }}", Icons.Default.CheckCircle)
                StatItem("Late", "${checkIns.count { it.status == "Late" }}", Icons.Default.Warning)
            }
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(checkIns) { c ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        val isLate = c.status == "Late"
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = if (isLate) Color(0xFFFF9800).copy(alpha = 0.1f) else Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ) {
                            Icon(
                                if (isLate) Icons.Default.TimerOff else Icons.Default.CheckCircle,
                                null, Modifier.padding(8.dp).size(24.dp),
                                tint = if (isLate) Color(0xFFFF9800) else Color(0xFF4CAF50)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.user, fontWeight = FontWeight.W600, fontSize = 14.sp)
                            Text(c.location, color = Color.Gray, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(c.time, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(shape = MaterialTheme.shapes.small, color = if (isLate) Color(0xFFFF9800).copy(alpha = 0.2f) else Color(0xFF4CAF50).copy(alpha = 0.2f)) {
                                Text(c.status, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = if (isLate) Color(0xFFFF9800) else Color(0xFF4CAF50))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, Modifier.size(24.dp), tint = Color(0xFF3F51B5))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}
