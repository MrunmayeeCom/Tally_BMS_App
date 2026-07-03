package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.bmstally.app.data.MockDataService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionScreen(
    tenantId: String,
    type: String,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val parties = remember { MockDataService.getParties(tenantId) }
    var selectedParty by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-06-20") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("New $type", color = Color.White) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            actions = {
                TextButton(onClick = onSave, enabled = selectedParty.isNotEmpty() && amount.isNotEmpty()) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Transaction Details", fontWeight = FontWeight.W600, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))

                    ExposedDropdownMenuBox(expanded, { expanded = it }) {
                        OutlinedTextField(
                            value = selectedParty,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Party Name") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            leadingIcon = { Icon(Icons.Default.People, null) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(expanded, { expanded = false }) {
                            parties.forEach { p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = { selectedParty = p; expanded = false })
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Description") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp), tint = Color(0xFF4CAF50))
                    Spacer(Modifier.width(8.dp))
                    Text("All transactions are recorded in the ledger and can be reviewed before posting.", color = Color(0xFF2E7D32), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = selectedParty.isNotEmpty() && amount.isNotEmpty()
            ) {
                Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save $type")
            }
        }
    }
}
