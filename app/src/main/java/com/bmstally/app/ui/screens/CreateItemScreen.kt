package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstally.app.data.MockDataService
import com.bmstally.app.model.Item
import com.bmstally.app.viewmodel.ItemsViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    tenantId: String,
    viewModel: ItemsViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var openingStock by remember { mutableStateOf("") }
    var purchaseRate by remember { mutableStateOf("") }
    var salesRate by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var selectedUnit by remember { mutableStateOf<String?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Building Material", "Paint", "Plumbing", "Tiles", "Electrical", "Sanitary")
    val groups = listOf("Raw Material", "Finished Good", "Trading Good", "Consumable")
    val units = MockDataService.units

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Item", color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            Text("Item Information", fontWeight = FontWeight.W600, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMsg = null },
                label = { Text("Item Name *") },
                leadingIcon = { Icon(Icons.Default.Inventory2, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(categoryExpanded, { categoryExpanded = it }) {
                OutlinedTextField(
                    value = selectedCategory ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    leadingIcon = { Icon(Icons.Default.Category, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(categoryExpanded, { categoryExpanded = false }) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = { selectedCategory = c; categoryExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(groupExpanded, { groupExpanded = it }) {
                OutlinedTextField(
                    value = selectedGroup ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Group") },
                    leadingIcon = { Icon(Icons.Default.GroupWork, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(groupExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(groupExpanded, { groupExpanded = false }) {
                    groups.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = { selectedGroup = g; groupExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(unitExpanded, { unitExpanded = it }) {
                OutlinedTextField(
                    value = selectedUnit ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit *") },
                    leadingIcon = { Icon(Icons.Default.Scale, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(unitExpanded, { unitExpanded = false }) {
                    units.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = { selectedUnit = u; unitExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = openingStock,
                onValueChange = { openingStock = it },
                label = { Text("Opening Stock") },
                leadingIcon = { Icon(Icons.Default.Storage, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = purchaseRate,
                onValueChange = { purchaseRate = it },
                label = { Text("Purchase Rate") },
                leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = salesRate,
                onValueChange = { salesRate = it },
                label = { Text("Sales Rate") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            if (errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg!!, color = Color(0xFFD32F2F), fontSize = 13.sp)
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isBlank() || selectedCategory == null || selectedUnit == null) {
                        errorMsg = "Please fill all required fields"
                        return@Button
                    }
                    val item = Item(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        category = selectedCategory!!,
                        group = selectedGroup ?: "",
                        unit = selectedUnit!!,
                        quantity = openingStock.toIntOrNull() ?: 0,
                        openingStock = openingStock.toIntOrNull() ?: 0,
                        purchaseRate = purchaseRate.toIntOrNull() ?: 0,
                        salesRate = salesRate.toIntOrNull() ?: 0,
                        reorderLevel = 0
                    )
                    viewModel.addItem(item)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save Item")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}