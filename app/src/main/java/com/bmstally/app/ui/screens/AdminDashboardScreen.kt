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
import androidx.hilt.navigation.compose.hiltViewModel
import com.bmstally.app.data.MockDataService
import com.bmstally.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    tenantId: String,
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val deleteHistory by viewModel.deleteHistory.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val userSearchQuery by viewModel.userSearchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers(tenantId)
        viewModel.loadDeleteHistory()
        viewModel.loadRequests()
    }

    var deleteConfirmUser by remember { mutableStateOf<MockDataService.AppUser?>(null) }

    if (deleteConfirmUser != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmUser = null },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${deleteConfirmUser?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    deleteConfirmUser?.let { viewModel.deleteUser(it.id) }
                    deleteConfirmUser = null
                }) { Text("Delete", color = Color(0xFFE53935)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmUser = null }) { Text("Cancel") }
            }
        )
    }

    var restoreConfirmId by remember { mutableStateOf<Int?>(null) }

    if (restoreConfirmId != null) {
        AlertDialog(
            onDismissRequest = { restoreConfirmId = null },
            title = { Text("Restore Record") },
            text = { Text("Restore this deleted record?") },
            confirmButton = {
                TextButton(onClick = {
                    restoreConfirmId?.let { viewModel.restoreRecord(it) }
                    restoreConfirmId = null
                }) { Text("Restore", color = Color(0xFF3F51B5)) }
            },
            dismissButton = {
                TextButton(onClick = { restoreConfirmId = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { viewModel.setTab(0) },
                    text = { Text("Users") },
                    icon = { Icon(Icons.Default.Group, null, modifier = Modifier.size(18.dp)) }
                )
                Tab(selected = selectedTab == 1, onClick = { viewModel.setTab(1) },
                    text = { Text("Delete History") },
                    icon = { Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp)) }
                )
                Tab(selected = selectedTab == 2, onClick = { viewModel.setTab(2) },
                    text = { Text("Requests") },
                    icon = { Icon(Icons.Default.ForwardToInbox, null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (selectedTab) {
                0 -> UsersTab(viewModel, users, userSearchQuery) { deleteConfirmUser = it }
                1 -> DeleteHistoryTab(viewModel, deleteHistory) { restoreConfirmId = it }
                2 -> RequestsTab(requests)
            }
        }
    }
}

@Composable
private fun UsersTab(
    viewModel: AdminViewModel,
    users: List<MockDataService.AppUser>,
    searchQuery: String,
    onDeleteRequest: (MockDataService.AppUser) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setUserSearchQuery(it) },
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            placeholder = { Text("Search users...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton({ viewModel.setUserSearchQuery("") }) { Icon(Icons.Default.Clear, "Clear") }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors()
        )

        val filtered = viewModel.filteredUsers

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No users found", color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                items(filtered, key = { it.id }) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFF3F51B5).copy(alpha = 0.1f)) {
                                Icon(Icons.Default.Person, null, Modifier.padding(8.dp).size(24.dp), tint = Color(0xFF3F51B5))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.name, fontWeight = FontWeight.W600, fontSize = 14.sp)
                                Text(user.email, color = Color.Gray, fontSize = 12.sp)
                                Text(user.role, fontSize = 11.sp, color = Color(0xFF3F51B5))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(shape = MaterialTheme.shapes.small, color = if (user.active) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFEF5350).copy(alpha = 0.2f)) {
                                    Text(
                                        if (user.active) "Active" else "Inactive",
                                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        color = if (user.active) Color(0xFF4CAF50) else Color(0xFFEF5350)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                IconButton({ onDeleteRequest(user) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = Color(0xFFE53935))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteHistoryTab(
    viewModel: AdminViewModel,
    records: List<com.bmstally.app.model.DeleteHistoryRecord>,
    onRestoreRequest: (Int) -> Unit
) {
    if (records.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DeleteSweep, null, Modifier.size(48.dp), tint = Color.LightGray)
                Spacer(Modifier.height(8.dp))
                Text("No deleted records", color = Color.Gray)
            }
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, Modifier.size(16.dp), tint = Color(0xFFFF9800))
            Spacer(Modifier.width(8.dp))
            Text("Tap restore to undo deletion", fontSize = 12.sp, color = Color(0xFFE65100))
        }

        LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp)) {
            items(records, key = { it.id }) { record ->
                val isLedger = record.entityType == "ledger"
                val icon = if (isLedger) Icons.Default.AccountBalance else Icons.Default.Receipt
                val typeColor = if (isLedger) Color(0xFF3F51B5) else Color(0xFF4CAF50)
                val typeBg = if (isLedger) Color(0xFF3F51B5).copy(alpha = 0.1f) else Color(0xFF4CAF50).copy(alpha = 0.1f)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = typeBg) {
                            Icon(icon, null, Modifier.padding(8.dp).size(20.dp), tint = typeColor)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(record.entityName, fontWeight = FontWeight.W600, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = MaterialTheme.shapes.small, color = typeBg) {
                                    Text(
                                        record.entityType.replaceFirstChar { it.uppercase() },
                                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        color = typeColor
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(record.companyGuid, color = Color.Gray, fontSize = 11.sp)
                            }
                            Text("Deleted: ${record.deletedAt}", color = Color.Gray, fontSize = 10.sp)
                        }
                        TextButton({ onRestoreRequest(record.id) }) {
                            Icon(Icons.Default.Restore, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Restore", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestsTab(requests: List<com.bmstally.app.model.UserRequest>) {
    if (requests.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ForwardToInbox, null, Modifier.size(48.dp), tint = Color.LightGray)
                Spacer(Modifier.height(8.dp))
                Text("No requests yet", color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(12.dp)) {
        items(requests, key = { it.id }) { req ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFFE91E63).copy(alpha = 0.1f)) {
                            Icon(Icons.Default.Person, null, Modifier.padding(8.dp).size(20.dp), tint = Color(0xFFE91E63))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(req.userName, fontWeight = FontWeight.W600, fontSize = 14.sp)
                            Text(req.userEmail, color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(req.createdAt, fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFF5F5F5)) {
                        Text(req.message, Modifier.padding(10.dp), fontSize = 13.sp, color = Color(0xFF424242))
                    }
                }
            }
        }
    }
}
