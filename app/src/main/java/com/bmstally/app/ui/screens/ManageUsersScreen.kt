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
fun ManageUsersScreen(
    tenantId: String,
    onBack: () -> Unit
) {
    val users = remember { MockDataService.getUsers(tenantId) }
    var activeOnly by remember { mutableStateOf(false) }

    val filtered = remember(users, activeOnly) {
        if (activeOnly) users.filter { it.active } else users
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Manage Users", color = Color.White) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            actions = { IconButton({}) { Icon(Icons.Default.PersonAdd, "Add", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Show active only", fontSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Switch(checked = activeOnly, onCheckedChange = { activeOnly = it })
            Spacer(Modifier.weight(1f))
            Text("${filtered.size} users", fontSize = 12.sp, color = Color.Gray)
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(filtered) { u ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFF3F51B5).copy(alpha = 0.1f)) {
                            Icon(Icons.Default.Person, null, Modifier.padding(8.dp).size(24.dp), tint = Color(0xFF3F51B5))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(u.name, fontWeight = FontWeight.W600, fontSize = 14.sp)
                            Text(u.email, color = Color.Gray, fontSize = 12.sp)
                            Text(u.role, fontSize = 11.sp, color = Color(0xFF3F51B5))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(shape = MaterialTheme.shapes.small, color = if (u.active) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFEF5350).copy(alpha = 0.2f)) {
                                Text(
                                    if (u.active) "Active" else "Inactive",
                                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    color = if (u.active) Color(0xFF4CAF50) else Color(0xFFEF5350)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            IconButton({}, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.MoreVert, null, Modifier.size(18.dp), tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
