package com.example.feature.auth.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.TallyBMSApp
import com.example.core.common.UiState
import com.example.core.common.ViewModelFactory
import com.example.feature.auth.domain.CompanyDomain

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TenantCompanySwitcherScreen(
    onCompanySelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext as TallyBMSApp
    val switcherViewModel: CompanySwitcherViewModel = viewModel(
        factory = ViewModelFactory(context.container) { container ->
            CompanySwitcherViewModel(container.authRepository)
        }
    )

    val uiState by switcherViewModel.uiState.collectAsStateWithLifecycle()
    val activeCompanyId by switcherViewModel.activeCompanyId.collectAsStateWithLifecycle()
    val activeCompanyGuid by switcherViewModel.activeCompanyGuid.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Database Switcher") },
                actions = {
                    IconButton(
                        onClick = { switcherViewModel.fetchCompanies() },
                        modifier = Modifier.testTag("company_refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh list")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (activeCompanyGuid != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Active Company",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Active Company Context",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "GUID: ${activeCompanyGuid ?: "Unknown"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Search Bar Filter Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Filter database profiles") },
                placeholder = { Text("Enter company name or GUID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("company_search_input")
            )

            // Dynamic State Switcher View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is UiState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = (uiState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { switcherViewModel.fetchCompanies() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Text("Retry Synchronisation", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                    is UiState.Success -> {
                        val allCompanies = (uiState as UiState.Success<List<CompanyDomain>>).data
                        val filteredCompanies = allCompanies.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.companyGuid.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredCompanies.isEmpty()) {
                            Text(
                                text = if (searchQuery.isEmpty()) "No affiliated company databases discovered under your active session profile." 
                                       else "No corporate profile matches string '$searchQuery'.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredCompanies) { company ->
                                    val isActive = company.companyGuid == activeCompanyId
                                    CompanyItemCard(
                                        company = company,
                                        isActive = isActive,
                                        onSelected = {
                                            switcherViewModel.switchCompany(company.companyGuid) {
                                                onCompanySelected()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        val mockCompanies = listOf(
                            CompanyDomain(
                                companyGuid = "guid-01",
                                name = "Acme Global Manufacturing Ltd."
                            ),
                            CompanyDomain(
                                companyGuid = "guid-02",
                                name = "Starlight Retail & Logistics"
                            )
                        )
                        val filteredCompanies = mockCompanies.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.companyGuid.contains(searchQuery, ignoreCase = true)
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredCompanies) { company ->
                                val isActive = company.companyGuid == activeCompanyId
                                CompanyItemCard(
                                    company = company,
                                    isActive = isActive,
                                    onSelected = {
                                        switcherViewModel.switchCompany(company.companyGuid) {
                                            onCompanySelected()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyItemCard(
    company: CompanyDomain,
    isActive: Boolean,
    onSelected: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .testTag("company_profile_card_${company.companyGuid}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = company.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "GUID: ${company.companyGuid.take(18)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary
                )
            }

            if (isActive) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Active database status",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("active_indicator_${company.companyGuid}")
                )
            }
        }
    }
}
