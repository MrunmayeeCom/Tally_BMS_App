package com.example.feature.accounting.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.crm.domain.ICrmRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditOrderScreen(
    orderViewModel: OrderViewModel,
    crmRepository: ICrmRepository,
    companyId: String,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var partyList by remember { mutableStateOf<List<CrmCustomer>>(emptyList()) }
    var selectedParty by remember { mutableStateOf<CrmCustomer?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var rawAmount by remember { mutableStateOf("") }
    var itemsSummary by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    val createOrderSuccess by orderViewModel.createOrderSuccess.collectAsState()

    LaunchedEffect(Unit) {
        // Fetch parties from CRM repository to fill the customer select list
        coroutineScope.launch {
            try {
                val customers = crmRepository.getCustomers(null, "All", "Name (A-Z)", 1, 50)
                partyList = customers
                if (customers.isNotEmpty()) {
                    selectedParty = customers[0]
                }
            } catch (e: Exception) {
                // Ignore fallback to empty list
            }
        }
    }

    LaunchedEffect(createOrderSuccess) {
        if (createOrderSuccess) {
            orderViewModel.resetCreateState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Sales Order", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), shape = MaterialTheme.shapes.medium)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Register Local Sales Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Stored locally in database and queued for cloud sync.", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Customer selector field
            Text("Select Client / Customer *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedParty?.name ?: "No customers available",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_select_field")
                        .clickable { dropdownExpanded = true },
                    trailingIcon = {
                        IconButton(onClick = { dropdownExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open selector")
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = false
                )
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    if (partyList.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No custom registries offline") },
                            onClick = { dropdownExpanded = false }
                        )
                    } else {
                        partyList.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text(customer.name) },
                                onClick = {
                                    selectedParty = customer
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Order Amount Input
            OutlinedTextField(
                value = rawAmount,
                onValueChange = { rawAmount = it },
                label = { Text("Total Amount (₹) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                singleLine = true
            )

            // Items Summary text field (quotes specific items count etc.)
            OutlinedTextField(
                value = itemsSummary,
                onValueChange = { itemsSummary = it },
                label = { Text("Items / Products Quote *") },
                placeholder = { Text("e.g. Paracetamol 500mg x200; Amoxicillin x50") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("items_summary_input")
            )

            // Remarks field
            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Remarks / Memo") },
                placeholder = { Text("Enter special requests or shipping requirements") },
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remarks_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            val isFormValid = selectedParty != null && rawAmount.isNotBlank() && itemsSummary.isNotBlank()
            Button(
                onClick = {
                    if (isFormValid) {
                        val parsedAmount = BigDecimal(rawAmount.trim())
                        orderViewModel.createOrder(
                            companyId = companyId,
                            partyId = selectedParty!!.id,
                            partyName = selectedParty!!.name,
                            amount = parsedAmount,
                            remarks = remarks,
                            itemsSummary = itemsSummary
                        )
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("commit_order_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Commit Order State", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
