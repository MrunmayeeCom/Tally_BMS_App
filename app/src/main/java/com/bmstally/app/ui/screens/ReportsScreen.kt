package com.bmstally.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstally.app.data.MockDataService
import com.bmstally.app.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onNavigate: (String) -> Unit) {
    val reports = MockDataService.getReports()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Reports", color = Color.White) },
            navigationIcon = { IconButton({}) { Icon(Icons.Default.Menu, "Menu", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(reports.size) { i ->
                val r = reports[i]
                ListItem(
                    headlineContent = { Text(r.title) },
                    leadingContent = { Icon(Icons.Default.Description, null, tint = Color(0xFF3F51B5)) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = Color.Gray) },
                    modifier = Modifier.clickable {
                        if (r.title == "Auto Reminders") onNavigate(Routes.REMINDER_SCHEDULER)
                        else onNavigate(Routes.reportDetail(r.title))
                    }.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(title: String) {
    val i = title.lowercase()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White) },
                navigationIcon = { IconButton({}) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                actions = {
                    IconButton({}) { Icon(Icons.Default.Share, "Share", tint = Color.White) }
                    IconButton({}) { Icon(Icons.Default.MoreVert, "More", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                i.contains("auto reminder") || i.contains("invoice auto share") -> AutoReminderReport(title)
                i.contains("top") -> TopReport()
                i.contains("expense") -> ExpenseReport()
                i.contains("inactive customer") -> InactiveReport("customers")
                i.contains("inactive item") -> InactiveReport("items")
                i.contains("ledger") -> LedgerReport()
                i.contains("day book") -> DayBookReport()
                i.contains("pending sales") -> PendingReport("Sales Order")
                i.contains("pending purchase") -> PendingReport("Purchase Order")
                i.contains("profit") -> PnLReport()
                i.contains("balance sheet") -> BalanceSheetReport()
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(title, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}

@Composable
private fun AutoReminderReport(title: String) {
    val items = listOf(
        Triple("Overdue Reminder", "Sent daily at 10:00 AM", true),
        Triple("Upcoming Payment Alert", "Sent 3 days before due date", true),
        Triple("Invoice Share", "Auto-share via email", false)
    )
    var toggles by remember { mutableStateOf(items.filter { it.third }.map { it.first }.toSet()) }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(items) { (t, sub, _) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null, tint = Color(0xFF3F51B5))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t, fontWeight = FontWeight.W500, fontSize = 14.sp)
                        Text(sub, color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(checked = t in toggles, onCheckedChange = {
                        if (it) toggles = toggles + t else toggles = toggles - t
                    })
                }
            }
        }
    }
}

@Composable
private fun TopReport() {
    val items = listOf(
        Pair("#1", Triple("ABC Enterprises", "₹ 1,23,45,678", true)),
        Pair("#2", Triple("PQR Tradelink", "₹ 89,01,234", false)),
        Pair("#3", Triple("EFG Corporation", "₹ 45,67,890", false)),
        Pair("#4", Triple("XYZ Industries", "₹ 34,56,789", false)),
        Pair("#5", Triple("LMN Brothers", "₹ 12,34,567", false))
    )

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 16.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Top Performer", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("ABC Enterprises", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("₹ 1,23,45,678", color = Color.White, fontSize = 16.sp)
                }
            }
        }

        items(items) { (rank, triple) ->
            val (name, value, isFirst) = triple
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = if (isFirst) Color(0xFFFFF8E1) else if (rank == "#2") Color(0xFFF5F5F5) else Color(0xFFEFEBE9)
                ) {
                    Text(rank, Modifier.padding(12.dp), fontWeight = FontWeight.Bold,
                        color = if (isFirst) Color(0xFFFFC107) else Color.Gray)
                }
                Spacer(Modifier.width(12.dp))
                Text(name, fontWeight = FontWeight.W500, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(value, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExpenseReport() {
    val expenses = listOf(
        "Total Expenses" to "₹ 12,45,678",
        "Office Rent" to "₹ 3,50,000",
        "Salaries" to "₹ 5,20,000",
        "Travel" to "₹ 1,25,000",
        "Utilities" to "₹ 85,000",
        "Marketing" to "₹ 95,678",
        "Miscellaneous" to "₹ 70,000"
    )

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(expenses) { (label, value) ->
            val isTotal = label == "Total Expenses"
            Row(
                Modifier.fillMaxWidth().padding(vertical = if (isTotal) 8.dp else 4.dp)
            ) {
                Text(label, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (isTotal) 16.sp else 14.sp)
                Spacer(Modifier.weight(1f))
                Text(value, fontWeight = FontWeight.Bold,
                    fontSize = if (isTotal) 16.sp else 14.sp,
                    color = if (isTotal) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurface)
            }
            if (isTotal) HorizontalDivider()
        }
    }
}

@Composable
private fun InactiveReport(type: String) {
    val items = if (type == "customers")
        listOf("Crystal Distributors", "Delta Supplies", "Innovative Tech")
    else
        listOf("Sand (Fine)", "Magnetic Tiles (Set)", "CPVC Pipe 1 inch")

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFE65100))
                    Spacer(Modifier.width(12.dp))
                    Text("${items.size} Inactive $type", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        items(items) { item ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = MaterialTheme.shapes.extraLarge, color = Color.Gray.copy(alpha = 0.2f)) {
                    Icon(Icons.Default.PersonOff, null, Modifier.padding(8.dp).size(20.dp), tint = Color.Gray)
                }
                Spacer(Modifier.width(12.dp))
                Text(item, fontSize = 14.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { }) { Text("Reactivate", fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun LedgerReport() {
    val ledgers = listOf(
        Triple("ABC Enterprises", "₹ 1,23,45,678", "Dr"),
        Triple("XYZ Industries", "₹ 56,78,901", "Cr"),
        Triple("PQR Tradelink", "₹ 7,89,01,234", "Dr"),
        Triple("LMN Brothers", "₹ 12,34,567", "Cr"),
        Triple("EFG Corporation", "₹ 3,45,67,890", "Dr")
    )

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(ledgers) { (name, amount, type) ->
            val color = if (type == "Dr") Color(0xFFEF5350) else Color(0xFF4CAF50)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = color.copy(alpha = 0.1f)) {
                        Text(type, Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(name, fontWeight = FontWeight.W500, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(amount, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DayBookReport() {
    val entries = listOf(
        Triple("Sales Invoice", "ABC Enterprises", "₹ 25,000"),
        Triple("Receipt", "XYZ Industries", "₹ 12,000"),
        Triple("Payment", "PQR Tradelink", "₹ 8,500"),
        Triple("Sales Invoice", "LMN Brothers", "₹ 45,000"),
        Triple("Receipt", "EFG Corporation", "₹ 67,890")
    )
    val times = listOf("10:30 AM", "11:15 AM", "02:00 PM", "03:30 PM", "04:45 PM")

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(entries.size) { i ->
            val (type, party, amount) = entries[i]
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFF3F51B5).copy(alpha = 0.1f)) {
                        Icon(Icons.Default.Receipt, null, Modifier.padding(8.dp).size(20.dp), tint = Color(0xFF3F51B5))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(party, fontWeight = FontWeight.W500, fontSize = 14.sp)
                        Text("$type \u2022 ${times[i]}", color = Color.Gray, fontSize = 11.sp)
                    }
                    Text(amount, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PendingReport(type: String) {
    val items = listOf(
        Triple("$type #001", "ABC Enterprises", "25,000"),
        Triple("$type #002", "XYZ Industries", "12,500"),
        Triple("$type #003", "PQR Tradelink", "78,900"),
        Triple("$type #004", "LMN Brothers", "34,000")
    )

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(items) { (id, party, amount) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFFFF9800).copy(alpha = 0.1f)) {
                        Icon(Icons.Default.Pending, null, Modifier.padding(8.dp).size(20.dp), tint = Color(0xFFFF9800))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(id, fontWeight = FontWeight.W500, fontSize = 14.sp)
                            Spacer(Modifier.weight(1f))
                            Text("\u20B9 $amount", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text("$party \u2022 Due: 02 Jul 26", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PnLReport() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        PnLRow("Sales Revenue", "₹ 45,67,890")
        PnLRow("Other Income", "₹ 2,34,567")
        HorizontalDivider()
        PnLRow("Total Income", "₹ 48,02,457", color = Color(0xFF2E7D32))
        Spacer(Modifier.height(16.dp))
        PnLRow("Cost of Goods Sold", "₹ 28,90,123", color = Color(0xFFC62828))
        PnLRow("Operating Expenses", "₹ 12,45,678", color = Color(0xFFC62828))
        HorizontalDivider()
        PnLRow("Total Expenses", "₹ 41,35,801", color = Color(0xFFEF5350))
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF4CAF50))
                Spacer(Modifier.width(12.dp))
                Text("Net Profit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                Text("₹ 6,66,656", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
private fun PnLRow(label: String, value: String, color: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, fontSize = 14.sp, color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.W600, color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun BalanceSheetReport() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("ASSETS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1565C0))
        Spacer(Modifier.height(8.dp))
        BsRow("Current Assets", "₹ 67,89,01,234")
        BsRow("Fixed Assets", "₹ 12,34,56,789")
        BsRow("Investments", "₹ 5,67,89,012")
        HorizontalDivider()
        BsRow("Total Assets", "₹ 85,91,47,035", bold = true)
        Spacer(Modifier.height(24.dp))

        Text("LIABILITIES", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1565C0))
        Spacer(Modifier.height(8.dp))
        BsRow("Current Liabilities", "₹ 34,56,78,901")
        BsRow("Long Term Debt", "₹ 12,34,56,789")
        HorizontalDivider()
        BsRow("Total Liabilities", "₹ 46,91,35,690", bold = true)
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF3F51B5))
                Spacer(Modifier.width(12.dp))
                Text("Shareholder's Equity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                Text("₹ 39,00,11,345", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF3F51B5))
            }
        }
    }
}

@Composable
private fun BsRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Spacer(Modifier.weight(1f))
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.W500)
    }
}