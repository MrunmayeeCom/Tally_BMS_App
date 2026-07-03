package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstally.app.model.SalesEntry
import com.bmstally.app.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesTeamScreen(
    entries: List<SalesEntry>,
    onNavigate: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Sales Team", color = Color.White) },
            navigationIcon = { IconButton({}) { Icon(Icons.Default.Menu, "Menu", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            item {
                SalesCard(
                    icon = Icons.Default.ReceiptLong,
                    entry = entries[0],
                    onClick = { onNavigate(Routes.ENTRY_SCREEN) }
                )
            }
            item {
                SalesCard(
                    icon = Icons.Default.CheckCircleOutline,
                    entry = entries[1],
                    onClick = { onNavigate(Routes.CHECK_IN_REPORT) }
                )
            }
            item {
                SalesCard(
                    icon = Icons.Default.Alarm,
                    entry = entries[2],
                    onClick = { onNavigate(Routes.FOLLOW_UPS) }
                )
            }
            item {
                Card(
                    onClick = { onNavigate(Routes.MANAGE_USERS) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(entries[3].title, fontWeight = FontWeight.W600, fontSize = 16.sp) },
                        supportingContent = entries[3].description?.let { { Text(it, fontSize = 13.sp) } },
                        leadingContent = { Icon(Icons.Default.PeopleOutline, null, Modifier.size(28.dp), tint = Color(0xFF3F51B5)) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = Color.Gray) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SalesCard(icon: androidx.compose.ui.graphics.vector.ImageVector, entry: SalesEntry, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row {
                Icon(icon, null, Modifier.size(28.dp), tint = Color(0xFF3F51B5))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(entry.title, fontWeight = FontWeight.W600, fontSize = 16.sp)
                    entry.description?.let { Text(it, color = Color.Gray, fontSize = 13.sp) }
                }
                if (entry.hasArrow) Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
            if (entry.actionLabel != null) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text(entry.actionLabel)
                }
            }
        }
    }
}
