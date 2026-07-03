package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.bmstally.app.model.LedgerEntry
import com.bmstally.app.viewmodel.VoucherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherCreateScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: VoucherViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()

    LaunchedEffect(Unit) { viewModel.initForm() }

    var showTypeDropdown by remember { mutableStateOf(false) }
    val voucherTypes = listOf("Sales", "Purchase", "Payment", "Receipt", "Journal")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Voucher", color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Voucher type selector
            ExposedDropdownMenuBox(showTypeDropdown, { showTypeDropdown = it }) {
                OutlinedTextField(
                    value = formState.voucherType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Voucher Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showTypeDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(showTypeDropdown, { showTypeDropdown = false }) {
                    voucherTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = { viewModel.setFormVoucherType(type); showTypeDropdown = false }
                        )
                    }
                }
            }

            // Date
            OutlinedTextField(
                value = formState.voucherDate,
                onValueChange = { viewModel.setFormDate(it) },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Reference No
            OutlinedTextField(
                value = formState.referenceNo,
                onValueChange = { viewModel.setFormRefNo(it) },
                label = { Text("Reference No") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Party
            OutlinedTextField(
                value = formState.party,
                onValueChange = { viewModel.setFormParty(it) },
                label = { Text("Party Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Narration
            OutlinedTextField(
                value = formState.narration,
                onValueChange = { viewModel.setFormNarration(it) },
                label = { Text("Narration") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            // Ledger entries
            HorizontalDivider()
            Text("Ledger Entries", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

            formState.entries.forEachIndexed { index, entry ->
                LedgerEntryRow(
                    entry = entry,
                    onUpdate = { viewModel.updateEntry(index, it) },
                    onRemove = { viewModel.removeEntry(index) },
                    canRemove = formState.entries.size > 1
                )
            }

            TextButton(
                onClick = { viewModel.addEntry() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Entry")
            }

            // Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Debit", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "₹${String.format("%,.0f", formState.entries.filter { it.is_debit }.sumOf { it.amount })}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Credit", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "₹${String.format("%,.0f", formState.entries.filter { !it.is_debit }.sumOf { it.amount })}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF5350)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    if (viewModel.saveVoucher()) onSaved()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = formState.isValid(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Text("Save Voucher")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LedgerEntryRow(
    entry: LedgerEntry,
    onUpdate: (LedgerEntry) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = entry.ledger_name,
                    onValueChange = { onUpdate(entry.copy(ledger_name = it)) },
                    label = { Text("Ledger") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.RemoveCircle, null, Modifier.size(20.dp), tint = Color(0xFFEF5350))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dr/Cr toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = entry.is_debit,
                        onClick = { onUpdate(entry.copy(is_debit = true)) },
                        label = { Text("Dr", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF2E7D32)
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    FilterChip(
                        selected = !entry.is_debit,
                        onClick = { onUpdate(entry.copy(is_debit = false)) },
                        label = { Text("Cr", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF5350).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFFC62828)
                        )
                    )
                }
                OutlinedTextField(
                    value = if (entry.amount == 0.0) "" else String.format("%.0f", entry.amount),
                    onValueChange = { onUpdate(entry.copy(amount = it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Amount") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
