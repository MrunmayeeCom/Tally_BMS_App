package com.bmstally.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.bmstally.app.model.*
import com.bmstally.app.viewmodel.LedgerDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDetailScreen(
    ledgerGuid: String,
    onBack: () -> Unit,
    viewModel: LedgerDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(ledgerGuid) { viewModel.loadLedgerDetail(ledgerGuid) }

    val tabs = listOf("Vouchers", "Invoices", "Bills", "Ageing")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.ledger?.name ?: "Ledger", color = Color.White, maxLines = 1) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val ledger = state.ledger ?: run {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Ledger not found", color = Color.Gray)
            }
            return@Scaffold
        }

        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            // Summary cards
            item {
                SummaryRow(ledger, state.totalDebit, state.totalCredit)
            }

            // Tabs
            item {
                TabRow(selectedTabIndex = state.activeTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = state.activeTab == index,
                            onClick = { viewModel.setActiveTab(index) },
                            text = { Text(title, fontSize = 13.sp) }
                        )
                    }
                }
            }

            // Tab content
            item {
                when (state.activeTab) {
                    0 -> VouchersTab(state.vouchers)
                    1 -> InvoicesTab(state.invoices)
                    2 -> BillsTab(state.bills)
                    3 -> AgeingTab(state.ageing)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(ledger: Ledger, totalDebit: Double, totalCredit: Double) {
    Column(Modifier.padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.weight(1f)) { SummaryCard("Account Type", if (ledger.nature == "Dr") "Debit" else "Credit", Color(0xFF3F51B5)) }
            Box(Modifier.weight(1f)) { SummaryCard("Opening", "₹${String.format("%,.0f", ledger.opening_balance)}", Color(0xFF2196F3)) }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.weight(1f)) { SummaryCard("Total Debit", "₹${String.format("%,.0f", totalDebit)}", Color(0xFF4CAF50)) }
            Box(Modifier.weight(1f)) { SummaryCard("Total Credit", "₹${String.format("%,.0f", totalCredit)}", Color(0xFFEF5350)) }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        }
    }
}

@Composable
private fun VouchersTab(vouchers: List<Voucher>) {
    if (vouchers.isEmpty()) {
        EmptyState("No vouchers found")
        return
    }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        vouchers.forEach { v ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(v.voucher_type ?: "", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(v.reference_no ?: "-", fontSize = 12.sp, color = Color.Gray)
                        Text(v.voucher_date?.take(10) ?: "", fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (v.debit > 0) {
                            Text("₹${String.format("%,.0f", v.debit)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Dr", fontSize = 10.sp, color = Color(0xFF4CAF50))
                        }
                        if (v.credit > 0) {
                            Text("₹${String.format("%,.0f", v.credit)}", color = Color(0xFFEF5350), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Cr", fontSize = 10.sp, color = Color(0xFFEF5350))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoicesTab(invoices: List<Invoice>) {
    if (invoices.isEmpty()) {
        EmptyState("No invoices found")
        return
    }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        invoices.forEach { inv ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(inv.invoice_no ?: "-", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(inv.party_name ?: "", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            "₹${String.format("%,.0f", inv.total_amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF3F51B5)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text(inv.invoice_type ?: "", fontSize = 11.sp, color = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text(inv.invoice_date?.take(10) ?: "", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun BillsTab(bills: List<Bill>) {
    if (bills.isEmpty()) {
        EmptyState("No bills found")
        return
    }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        bills.forEach { b ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(b.bill_name ?: "-", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(b.ledger_name ?: "", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            "₹${String.format("%,.0f", b.pending_amount.takeIf { it > 0 } ?: b.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF3F51B5)
                        )
                    }
                    Text("Due: ${b.due_date?.take(10) ?: "-"}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun AgeingTab(ageing: List<Ageing>) {
    if (ageing.isEmpty()) {
        EmptyState("No ageing data found")
        return
    }
    Column(Modifier.padding(16.dp)) {
        val barColors = mapOf(
            "0-30" to Color(0xFF4F9CF9),
            "31-60" to Color(0xFF22C55E),
            "61-90" to Color(0xFFFACC15),
            "90+" to Color(0xFFFB7185)
        )
        val maxAmount = ageing.maxOfOrNull { it.amount } ?: 1.0

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ageing.forEach { a ->
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(a.period, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("₹${String.format("%,.0f", a.amount)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    val barColor = barColors[a.period] ?: Color(0xFF2563EB)
                    Box(
                        modifier = Modifier.fillMaxWidth().height(24.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (a.amount / maxAmount).toFloat().coerceIn(0f, 1f))
                                .background(barColor, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, null, Modifier.size(36.dp), tint = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(message, color = Color.Gray, fontSize = 14.sp)
        }
    }
}
