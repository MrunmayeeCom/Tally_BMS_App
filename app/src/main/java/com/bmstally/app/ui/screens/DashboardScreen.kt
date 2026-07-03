package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.bmstally.app.model.Company
import com.bmstally.app.model.DashboardStat
import com.bmstally.app.model.Tenant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    tenant: Tenant?,
    selectedCompany: Company?,
    companies: List<Company>,
    stats: List<DashboardStat>,
    onSelectCompany: (Company) -> Unit,
    onOpenDrawer: () -> Unit,
    onItems: () -> Unit,
    onWallet: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                if (companies.isEmpty()) Text("Dashboard")
                else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded, { expanded = it }) {
                        TextField(
                            value = selectedCompany?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(expanded, { expanded = false }) {
                            companies.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = { onSelectCompany(c); expanded = false }
                                )
                            }
                        }
                    }
                }
            },
            navigationIcon = { IconButton(onOpenDrawer) { Icon(Icons.Default.Menu, "Menu", tint = Color.White) } },
            actions = { IconButton({}) { Icon(Icons.Default.HelpOutline, "Help", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Business, null, Modifier.size(14.dp), tint = Color.Gray)
            Spacer(Modifier.width(4.dp))
            Text(tenant?.name ?: "", fontSize = 11.sp, color = Color.Gray)
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Default.Sync, null, Modifier.size(12.dp), tint = Color.Gray)
            Spacer(Modifier.width(4.dp))
            Text("18 Jun 26", fontSize = 10.sp, color = Color.Gray)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ErrorOutline, null, Modifier.size(14.dp), tint = Color(0xFFEF5350))
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            item { QuickActions(onItems, onWallet) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
                ) {
                    ListItem(
                        headlineContent = { Text("Email verification pending", color = Color.White, fontWeight = FontWeight.W600) },
                        supportingContent = { Text("Please open the mail from us and tap on 'Verify Email'.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) },
                        leadingContent = { Icon(Icons.Default.Campaign, null, tint = Color.White) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ArrowBackIos, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("01 Apr 26 - 31 Mar 27", fontWeight = FontWeight.W600, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForwardIos, null, Modifier.size(14.dp))
                }
            }

            stats.forEach { stat ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                    ) {
                        ListItem(
                            headlineContent = { Text(stat.amount, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                            supportingContent = { Text(stat.label, fontSize = 12.sp) },
                            leadingContent = {
                                Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFF3F51B5).copy(alpha = 0.1f)) {
                                    Icon(Icons.Default.Circle, null, Modifier.padding(8.dp).size(20.dp), tint = Color(0xFF3F51B5))
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
private fun QuickActions(onItems: () -> Unit, onWallet: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Card(onClick = onItems, modifier = Modifier.weight(1f)) {
            Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inventory2, null, Modifier.size(36.dp), tint = Color(0xFF3F51B5))
                Spacer(Modifier.height(8.dp))
                Text("Items")
            }
        }
        Spacer(Modifier.width(16.dp))
        Card(onClick = onWallet, modifier = Modifier.weight(1f)) {
            Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.People, null, Modifier.size(36.dp), tint = Color(0xFF3F51B5))
                Spacer(Modifier.height(8.dp))
                Text("Party")
            }
        }
    }
}
