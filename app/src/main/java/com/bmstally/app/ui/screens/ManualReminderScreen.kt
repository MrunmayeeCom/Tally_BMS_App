package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
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
fun ManualReminderScreen(
    tenantId: String,
    onBack: () -> Unit
) {
    val parties = remember { MockDataService.getParties(tenantId) }
    var selectedParty by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Manual Reminder", color = Color.White) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
            ) {
                Column(Modifier.padding(16.dp)) {
                    ExposedDropdownMenuBox(expanded, { expanded = it }) {
                        OutlinedTextField(
                            value = selectedParty,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Party") },
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
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Reminder Title") },
                        leadingIcon = { Icon(Icons.Default.Title, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        leadingIcon = { Icon(Icons.Default.Notes, null) },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = { onBack() },
                            modifier = Modifier.weight(1f),
                            enabled = selectedParty.isNotEmpty() && title.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Send Reminder")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, null, Modifier.size(20.dp), tint = Color(0xFFFF9800))
                    Spacer(Modifier.width(8.dp))
                    Text("Reminders will be sent via email and notification to the selected party.", color = Color(0xFFE65100), fontSize = 12.sp)
                }
            }
        }
    }
}
