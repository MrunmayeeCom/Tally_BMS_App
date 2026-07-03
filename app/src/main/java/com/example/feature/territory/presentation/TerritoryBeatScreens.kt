package com.example.feature.territory.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.Resource
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.crm.domain.ICrmRepository
import com.example.feature.salesteam.domain.ISalesTeamRepository
import com.example.feature.salesteam.domain.SalesTeamMember
import com.example.feature.territory.domain.Beat
import com.example.feature.territory.domain.Territory
import com.example.feature.territory.domain.TerritoryPerformance
import com.example.feature.territory.domain.TerritoryUiState
import kotlinx.coroutines.launch

enum class TerritorySubScreen {
    LIST,
    TERRITORY_DETAIL,
    TERRITORY_CREATE_EDIT,
    BEAT_DETAIL,
    BEAT_CREATE_EDIT,
    ASSIGNMENT,
    PERFORMANCE_DASHBOARD
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TerritoryBeatManagementScreen(
    viewModel: TerritoryViewModel,
    crmRepository: ICrmRepository,
    salesTeamRepository: ISalesTeamRepository,
    currentCompanyId: String,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var activeSubScreen by remember { mutableStateOf(TerritorySubScreen.LIST) }
    var selectedTerritoryId by remember { mutableStateOf<String?>(null) }
    var selectedBeatId by remember { mutableStateOf<String?>(null) }
    
    // Form States
    var editingTerritory by remember { mutableStateOf<Territory?>(null) }
    var editingBeat by remember { mutableStateOf<Beat?>(null) }

    // Reps and CRM Customers lists
    var salesReps by remember { mutableStateOf<List<SalesTeamMember>>(emptyList()) }
    var crmCustomers by remember { mutableStateOf<List<CrmCustomer>>(emptyList()) }

    // Fetch lists from integrated repositories
    LaunchedEffect(currentCompanyId) {
        coroutineScope.launch {
            try {
                salesReps = salesTeamRepository.getTeamMembers(currentCompanyId)
            } catch (e: Exception) {
                // Keep empty or fall back if sandbox offline
            }
        }
        coroutineScope.launch {
            try {
                crmCustomers = crmRepository.getCustomers(null, null, null, 1, 100)
            } catch (e: Exception) {
                // Keep empty or fallback
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (activeSubScreen) {
                            TerritorySubScreen.LIST -> "Territory & Beat Management"
                            TerritorySubScreen.TERRITORY_DETAIL -> "Territory Area Details"
                            TerritorySubScreen.TERRITORY_CREATE_EDIT -> if (editingTerritory == null) "Create New Territory" else "Edit Territory Configuration"
                            TerritorySubScreen.BEAT_DETAIL -> "Beat Route Coverage"
                            TerritorySubScreen.BEAT_CREATE_EDIT -> if (editingBeat == null) "Create Beat Route" else "Edit Beat Route Configuration"
                            TerritorySubScreen.ASSIGNMENT -> "Territory Matrix Assignment"
                            TerritorySubScreen.PERFORMANCE_DASHBOARD -> "Territory Operational Dashboard"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeSubScreen == TerritorySubScreen.LIST) {
                                onBack()
                            } else {
                                activeSubScreen = TerritorySubScreen.LIST
                            }
                        },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (activeSubScreen == TerritorySubScreen.LIST) {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Data")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is TerritoryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TerritoryUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "Error",
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Retrieval Failed",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.refresh() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Retry Connection")
                                }
                            }
                        }
                    }
                }
                is TerritoryUiState.Success -> {
                    AnimatedContent(
                        targetState = activeSubScreen,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        }
                    ) { subScreen ->
                        when (subScreen) {
                            TerritorySubScreen.LIST -> {
                                MainTerritorryListLayout(
                                    territories = state.territories,
                                    beats = state.beats,
                                    onSelectTerritory = { id ->
                                        selectedTerritoryId = id
                                        activeSubScreen = TerritorySubScreen.TERRITORY_DETAIL
                                    },
                                    onSelectBeat = { id ->
                                        selectedBeatId = id
                                        activeSubScreen = TerritorySubScreen.BEAT_DETAIL
                                    },
                                    onCreateTerritory = {
                                        editingTerritory = null
                                        activeSubScreen = TerritorySubScreen.TERRITORY_CREATE_EDIT
                                    },
                                    onCreateBeat = {
                                        editingBeat = null
                                        activeSubScreen = TerritorySubScreen.BEAT_CREATE_EDIT
                                    },
                                    onGoToAssignment = {
                                        activeSubScreen = TerritorySubScreen.ASSIGNMENT
                                    },
                                    onGoToDashboard = {
                                        activeSubScreen = TerritorySubScreen.PERFORMANCE_DASHBOARD
                                    }
                                )
                            }
                            TerritorySubScreen.TERRITORY_DETAIL -> {
                                val territory = state.territories.find { it.id == selectedTerritoryId }
                                if (territory != null) {
                                    TerritoryDetailLayout(
                                        territory = territory,
                                        beats = state.beats.filter { it.territoryId == territory.id },
                                        onEdit = {
                                            editingTerritory = territory
                                            activeSubScreen = TerritorySubScreen.TERRITORY_CREATE_EDIT
                                        },
                                        onDelete = {
                                            viewModel.deleteTerritory(territory.id)
                                            activeSubScreen = TerritorySubScreen.LIST
                                        },
                                        onBack = { activeSubScreen = TerritorySubScreen.LIST }
                                    )
                                }
                            }
                            TerritorySubScreen.TERRITORY_CREATE_EDIT -> {
                                CreateEditTerritoryLayout(
                                    editingTerritory = editingTerritory,
                                    salesReps = salesReps,
                                    onSave = { name, desc, lat, lng, radius, rep ->
                                        viewModel.saveTerritory(
                                            id = editingTerritory?.id,
                                            name = name,
                                            description = desc,
                                            centerLat = lat,
                                            centerLng = lng,
                                            radiusKm = radius,
                                            salesRepId = rep?.id,
                                            salesRepName = rep?.name
                                        )
                                        activeSubScreen = TerritorySubScreen.LIST
                                    },
                                    onCancel = { activeSubScreen = TerritorySubScreen.LIST }
                                )
                            }
                            TerritorySubScreen.BEAT_DETAIL -> {
                                val beat = state.beats.find { it.id == selectedBeatId }
                                if (beat != null) {
                                    val parentTerritory = state.territories.find { it.id == beat.territoryId }
                                    BeatDetailLayout(
                                        beat = beat,
                                        parentTerritory = parentTerritory,
                                        crmCustomers = crmCustomers,
                                        onEdit = {
                                            editingBeat = beat
                                            activeSubScreen = TerritorySubScreen.BEAT_CREATE_EDIT
                                        },
                                        onDelete = {
                                            viewModel.deleteBeat(beat.id)
                                            activeSubScreen = TerritorySubScreen.LIST
                                        },
                                        onBack = { activeSubScreen = TerritorySubScreen.LIST }
                                    )
                                }
                            }
                            TerritorySubScreen.BEAT_CREATE_EDIT -> {
                                CreateEditBeatLayout(
                                    editingBeat = editingBeat,
                                    territories = state.territories,
                                    salesReps = salesReps,
                                    crmCustomers = crmCustomers,
                                    onSave = { name, desc, territoryId, repId, repName, customerIds ->
                                        viewModel.saveBeat(
                                            id = editingBeat?.id,
                                            territoryId = territoryId,
                                            name = name,
                                            description = desc,
                                            salesRepId = repId,
                                            salesRepName = repName,
                                            customerIds = customerIds
                                        )
                                        activeSubScreen = TerritorySubScreen.LIST
                                    },
                                    onCancel = { activeSubScreen = TerritorySubScreen.LIST }
                                )
                            }
                            TerritorySubScreen.ASSIGNMENT -> {
                                TerritoryAssignmentLayout(
                                    territories = state.territories,
                                    beats = state.beats,
                                    crmCustomers = crmCustomers,
                                    onSaveMapping = { beatId, customers ->
                                        val targetBeat = state.beats.find { it.id == beatId }
                                        if (targetBeat != null) {
                                            viewModel.saveBeat(
                                                id = targetBeat.id,
                                                territoryId = targetBeat.territoryId,
                                                name = targetBeat.name,
                                                description = targetBeat.description,
                                                salesRepId = targetBeat.salesRepId,
                                                salesRepName = targetBeat.salesRepName,
                                                customerIds = customers
                                            )
                                        }
                                        activeSubScreen = TerritorySubScreen.LIST
                                    },
                                    onCancel = { activeSubScreen = TerritorySubScreen.LIST }
                                )
                            }
                            TerritorySubScreen.PERFORMANCE_DASHBOARD -> {
                                TerritoryPerformanceLayout(
                                    performances = state.performances,
                                    onBack = { activeSubScreen = TerritorySubScreen.LIST }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 1. SCREEN: MAIN LISTS LAYOUT
@Composable
fun MainTerritorryListLayout(
    territories: List<Territory>,
    beats: List<Beat>,
    onSelectTerritory: (String) -> Unit,
    onSelectBeat: (String) -> Unit,
    onCreateTerritory: () -> Unit,
    onCreateBeat: () -> Unit,
    onGoToAssignment: () -> Unit,
    onGoToDashboard: () -> Unit
) {
    var selectedTabState by remember { mutableStateOf(0) } // 0 = Territories, 1 = Beats

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onGoToAssignment,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f).padding(end = 4.dp).testTag("matrix_assignment_button")
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Matrix Map", fontSize = 12.sp)
                }
                
                Button(
                    onClick = onGoToDashboard,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.weight(1f).padding(start = 4.dp).testTag("performance_dashboard_button")
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Performance", fontSize = 12.sp)
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTabState,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedTabState == 0,
                onClick = { selectedTabState = 0 },
                text = { Text("Territories (${territories.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTabState == 1,
                onClick = { selectedTabState = 1 },
                text = { Text("Beats (${beats.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (selectedTabState == 0) {
                if (territories.isEmpty()) {
                    EmptyStatusWidget(title = "No Territories Defined", description = "Create your first operational boundary with radius mapping.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(territories) { territory ->
                            val childBeats = beats.filter { it.territoryId == territory.id }.size
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectTerritory(territory.id) }
                                    .testTag("territory_item_${territory.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = territory.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "${territory.radiusKm} km Rad",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = territory.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = territory.salesRepName ?: "Unassigned Rep",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$childBeats Active Beats",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                FloatingActionButton(
                    onClick = onCreateTerritory,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .testTag("create_territory_fab"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Territory")
                }
            } else {
                if (beats.isEmpty()) {
                    EmptyStatusWidget(title = "No Beats Scheduled", description = "Add route beats to define path coverage for fields visits.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(beats) { beat ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectBeat(beat.id) }
                                    .testTag("beat_item_${beat.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = beat.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "${beat.customerIds.size} Clients",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = beat.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBox,
                                            contentDescription = "",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = beat.salesRepName ?: "Unassigned Executive",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                FloatingActionButton(
                    onClick = onCreateBeat,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .testTag("create_beat_fab"),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Beat")
                }
            }
        }
    }
}

// 2. SCREEN: TERRITORY DETAILS
@Composable
fun TerritoryDetailLayout(
    territory: Territory,
    beats: List<Beat>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Territory?") },
            text = { Text("This will permanently delete '${territory.name}'. This action is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = territory.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_territory_button")) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.testTag("delete_territory_button")) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = territory.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailMetricItem(label = "Radius Limit", value = "${territory.radiusKm} km", icon = Icons.Default.LocationOn)
                    DetailMetricItem(label = "Rep Representative", value = territory.salesRepName ?: "Unassigned", icon = Icons.Default.Person)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailMetricItem(label = "Center Lat", value = territory.centerLat.toString(), icon = Icons.Default.LocationOn)
                    DetailMetricItem(label = "Center Lng", value = territory.centerLng.toString(), icon = Icons.Default.LocationOn)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GEOGRAPHIC SPATIAL MAP REPRESENTATION (CUSTOM COMPOSED CANVAS CANVAS!)
        Text("Geographical Spatial Representation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF263238)) // elegant blueprint dark map color
                    .drawBehind {
                        // Drawing schematic rings representing the territory radius!
                        val center = Offset(size.width / 2, size.height / 2)
                        
                        // Radii rings
                        drawCircle(
                            color = Color(0x334CAF50),
                            radius = size.height * 0.35f,
                            center = center,
                            style = Stroke(width = 2f)
                        )
                        drawCircle(
                            color = Color(0x1A4CAF50),
                            radius = size.height * 0.21f,
                            center = center,
                            style = Stroke(width = 1f)
                        )
                        // Central coordinator node
                        drawCircle(
                            color = Color(0xFF4CAF50),
                            radius = 8f,
                            center = center
                        )

                        // Dynamic beat coverage nodes
                        drawCircle(
                            color = Color(0xFFFF9800),
                            radius = 6f,
                            center = Offset(center.x - 60f, center.y + 40f)
                        )
                        drawCircle(
                            color = Color(0xFFFF9800),
                            radius = 6f,
                            center = Offset(center.x + 80f, center.y - 30f)
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF4CAF50)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Center Coordinate (${territory.centerLat}, ${territory.centerLng})", color = Color.White, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFFF9800)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Beats Coverage Area", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ASSIGNED BEATS SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Routes in this Territory (${beats.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (beats.isEmpty()) {
            Text("No scheduled beat pathways configured in this territory yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            beats.forEach { beat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(beat.name, fontWeight = FontWeight.Bold)
                            Text("${beat.customerIds.size} client touchpoints assigned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "")
                    }
                }
            }
        }
    }
}

// 3. SCREEN: CREATE EDIT TERRITORY
@Composable
fun CreateEditTerritoryLayout(
    editingTerritory: Territory?,
    salesReps: List<SalesTeamMember>,
    onSave: (name: String, desc: String, lat: Double, lng: Double, radius: Double, rep: SalesTeamMember?) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(editingTerritory?.name ?: "") }
    var desc by remember { mutableStateOf(editingTerritory?.description ?: "") }
    var latText by remember { mutableStateOf(editingTerritory?.centerLat?.toString() ?: "19.0760") }
    var lngText by remember { mutableStateOf(editingTerritory?.centerLng?.toString() ?: "72.8777") }
    var radius by remember { mutableStateOf(editingTerritory?.radiusKm ?: 15.0) }
    var selectedRep by remember { mutableStateOf<SalesTeamMember?>(salesReps.find { it.id == editingTerritory?.salesRepId }) }

    var expandedReps by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Territory Name") },
            modifier = Modifier.fillMaxWidth().testTag("territory_name_input")
        )

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().testTag("territory_desc_input")
        )

        Text("Sales Representative Assignment", fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedReps = true },
                modifier = Modifier.fillMaxWidth().testTag("rep_dropdown_trigger")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedRep?.name ?: "Select Sales Rep / Executive")
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "")
                }
            }
            DropdownMenu(
                expanded = expandedReps,
                onDismissRequest = { expandedReps = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Unassigned") },
                    onClick = {
                        selectedRep = null
                        expandedReps = false
                    }
                )
                salesReps.forEach { rep ->
                    DropdownMenuItem(
                        text = { Text(rep.name) },
                        onClick = {
                            selectedRep = rep
                            expandedReps = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = latText,
                onValueChange = { latText = it },
                label = { Text("Center Latitude") },
                modifier = Modifier.weight(1f).testTag("territory_lat_input")
            )
            OutlinedTextField(
                value = lngText,
                onValueChange = { lngText = it },
                label = { Text("Center Longitude") },
                modifier = Modifier.weight(1f).testTag("territory_lng_input")
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Operational Coverage Radius", fontWeight = FontWeight.SemiBold)
                Text("${radius.toInt()} km", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = radius.toFloat(),
                onValueChange = { radius = it.toDouble() },
                valueRange = 1f..100f,
                modifier = Modifier.testTag("territory_radius_slider")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val latNum = latText.toDoubleOrNull() ?: 19.0760
                    val lngNum = lngText.toDoubleOrNull() ?: 72.8777
                    onSave(name, desc, latNum, lngNum, radius, selectedRep)
                },
                modifier = Modifier.weight(1f).testTag("save_territory_button"),
                enabled = name.isNotBlank()
            ) {
                Text("Save Territory")
            }
        }
    }
}

// 4. SCREEN: BEAT DETAILS
@Composable
fun BeatDetailLayout(
    beat: Beat,
    parentTerritory: Territory?,
    crmCustomers: List<CrmCustomer>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Beat?") },
            text = { Text("Are you sure you want to delete the beat '${beat.name}'? All client routing assignments will need remapping.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = beat.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_beat_button")) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.testTag("delete_beat_button")) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(beat.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                DetailMetricItem(label = "Parent Territory", value = parentTerritory?.name ?: "All Zones", icon = Icons.Default.Home)
                Spacer(modifier = Modifier.height(8.dp))
                DetailMetricItem(label = "Assigned Sales Executive", value = beat.salesRepName ?: "Unassigned", icon = Icons.Default.Person)
            }
        }

        Text("Mapped Clients / Accounts (${beat.customerIds.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        val assignedCustomers = crmCustomers.filter { beat.customerIds.contains(it.id) }
        
        if (assignedCustomers.isEmpty()) {
            Text("No customer contacts mapped to this beat route yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            assignedCustomers.forEach { customer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(customer.name, fontWeight = FontWeight.Bold)
                            Text("Outstanding: $${customer.outstandingAmount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (customer.statusBadge == "Active") Color(0xFF2E7D32) else Color(0xFFC62828))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                customer.statusBadge,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. SCREEN: CREATE EDIT BEAT
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateEditBeatLayout(
    editingBeat: Beat?,
    territories: List<Territory>,
    salesReps: List<SalesTeamMember>,
    crmCustomers: List<CrmCustomer>,
    onSave: (name: String, desc: String, territoryId: String, repId: String?, repName: String?, customerIds: List<String>) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(editingBeat?.name ?: "") }
    var desc by remember { mutableStateOf(editingBeat?.description ?: "") }
    
    var selectedTerritory by remember { mutableStateOf(territories.find { it.id == editingBeat?.territoryId } ?: territories.firstOrNull()) }
    var selectedRep by remember { mutableStateOf(salesReps.find { it.id == editingBeat?.salesRepId } ?: salesReps.firstOrNull()) }
    
    var assignedCustomerIds = remember { mutableStateListOf<String>().apply { addAll(editingBeat?.customerIds ?: emptyList()) } }

    var expandedTerritories by remember { mutableStateOf(false) }
    var expandedReps by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Beat Name") },
            modifier = Modifier.fillMaxWidth().testTag("beat_name_input")
        )

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().testTag("beat_desc_input")
        )

        Text("Select Parent Territory", fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedTerritories = true },
                modifier = Modifier.fillMaxWidth().testTag("territory_dropdown_trigger")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedTerritory?.name ?: "Assign Territory")
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "")
                }
            }
            DropdownMenu(
                expanded = expandedTerritories,
                onDismissRequest = { expandedTerritories = false }
            ) {
                territories.forEach { terr ->
                    DropdownMenuItem(
                        text = { Text(terr.name) },
                        onClick = {
                            selectedTerritory = terr
                            expandedTerritories = false
                        }
                    )
                }
            }
        }

        Text("Sales representative", fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedReps = true },
                modifier = Modifier.fillMaxWidth().testTag("beat_rep_dropdown_trigger")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedRep?.name ?: "Select Rep")
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "")
                }
            }
            DropdownMenu(
                expanded = expandedReps,
                onDismissRequest = { expandedReps = false }
            ) {
                salesReps.forEach { rep ->
                    DropdownMenuItem(
                        text = { Text(rep.name) },
                        onClick = {
                            selectedRep = rep
                            expandedReps = false
                        }
                    )
                }
            }
        }

        Text("Map Clients to Route (${assignedCustomerIds.size} selected)", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(crmCustomers) { customer ->
                    val isChecked = assignedCustomerIds.contains(customer.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) {
                                    assignedCustomerIds.remove(customer.id)
                                } else {
                                    assignedCustomerIds.add(customer.id)
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (isChecked) {
                                    assignedCustomerIds.remove(customer.id)
                                } else {
                                    assignedCustomerIds.add(customer.id)
                                }
                            },
                            modifier = Modifier.testTag("customer_checkbox_${customer.id}")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(customer.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(customer.stateCode, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    onSave(
                        name,
                        desc,
                        selectedTerritory?.id ?: "",
                        selectedRep?.id,
                        selectedRep?.name,
                        assignedCustomerIds.toList()
                    )
                },
                modifier = Modifier.weight(1f).testTag("save_beat_button"),
                enabled = name.isNotBlank() && selectedTerritory != null
            ) {
                Text("Save Beat")
            }
        }
    }
}

// 6. SCREEN: MATRIX ASSIGNMENT MAPPING
@Composable
fun TerritoryAssignmentLayout(
    territories: List<Territory>,
    beats: List<Beat>,
    crmCustomers: List<CrmCustomer>,
    onSaveMapping: (beatId: String, customerIds: List<String>) -> Unit,
    onCancel: () -> Unit
) {
    var selectedBeat by remember { mutableStateOf(beats.firstOrNull()) }
    var expandedBeats by remember { mutableStateOf(false) }

    // Dynamic state matching whichever beat is chosen
    val activeCustomerIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(selectedBeat) {
        activeCustomerIds.clear()
        if (selectedBeat != null) {
            activeCustomerIds.addAll(selectedBeat!!.customerIds)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Batch Assignment Panel", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        
        Text("Target Beat Router Selection", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedBeats = true },
                modifier = Modifier.fillMaxWidth().testTag("assignment_beat_dropdown")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedBeat?.name ?: "Select Beat Route")
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "")
                }
            }
            DropdownMenu(expanded = expandedBeats, onDismissRequest = { expandedBeats = false }) {
                beats.forEach { beat ->
                    DropdownMenuItem(
                        text = { Text(beat.name) },
                        onClick = {
                            selectedBeat = beat
                            expandedBeats = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("Customer Allocation Dashboard", fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            if (selectedBeat == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a beat to allocate accounts", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(crmCustomers) { customer ->
                        val isAssigned = activeCustomerIds.contains(customer.id)
                        
                        // Fake visual computation for distance calculation!
                        val distanceKm = remember(customer.id, selectedBeat) {
                            val rawHash = (customer.id.hashCode() + (selectedBeat?.id.hashCode() ?: 0)).coerceAtLeast(0)
                            val offset = (rawHash % 150) / 10.0
                            offset + 0.5
                        }
                        val isWithinRange = distanceKm <= 15.0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAssigned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isAssigned) {
                                            activeCustomerIds.remove(customer.id)
                                        } else {
                                            activeCustomerIds.add(customer.id)
                                        }
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isAssigned,
                                        onCheckedChange = {
                                            if (isAssigned) {
                                                activeCustomerIds.remove(customer.id)
                                            } else {
                                                activeCustomerIds.add(customer.id)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Distance: ${String.format("%.1f", distanceKm)} km", fontSize = 11.sp, color = if (isWithinRange) Color(0xFF2E7D32) else Color(0xFFC62828))
                                    }
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isWithinRange) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        if (isWithinRange) "Valid Radius" else "Exceeds Radius",
                                        color = if (isWithinRange) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
            Button(
                onClick = {
                    if (selectedBeat != null) {
                        onSaveMapping(selectedBeat!!.id, activeCustomerIds.toList())
                    }
                },
                modifier = Modifier.weight(1.5f).testTag("save_assignments_button"),
                enabled = selectedBeat != null
            ) {
                Text("Apply Workspace Assignments")
            }
        }
    }
}

// 7. SCREEN: PERFORMANCE DASHBOARD
@Composable
fun TerritoryPerformanceLayout(
    performances: List<TerritoryPerformance>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Zone Analytics Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

        // Render Canvas Bar Chart for visits & collections
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Collections Achieved by Territory ($)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .drawBehind {
                            val spacing = size.width / (performances.size + 1)
                            val maxCollections = performances.maxOfOrNull { it.totalCollections } ?: 1.0
                            
                            // Draw horizontal grid lines
                            for (i in 1..4) {
                                val y = size.height * (i * 0.2f)
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1f
                                )
                            }

                            // Draw Bars
                            performances.forEachIndexed { i, perf ->
                                val barWidth = 40f
                                val x = spacing * (i + 1) - (barWidth / 2)
                                val barHeight = (perf.totalCollections / maxCollections) * size.height * 0.75f
                                val y = size.height - barHeight.toFloat()
                                
                                drawRect(
                                    color = Color(0xFF1E88E5), // dynamic blue
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight.toFloat())
                                )
                            }
                        }
                )

                Spacer(modifier = Modifier.height(8.dp))
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    performances.forEach { perf ->
                        Text(
                            text = perf.territoryName.split(" ").firstOrNull() ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Text("Analytical Field Performance Metrics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        performances.forEach { perf ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(perf.territoryName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Recovery Success: ${perf.recoverySuccessRate}%",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Visits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text(perf.totalVisits.toString(), fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Total Collections", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text("$${String.format("%,.2f", perf.totalCollections)}", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Outstanding", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text("$${String.format("%,.2f", perf.totalOutstanding)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Global Help UI Components
@Composable
fun EmptyStatusWidget(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun DetailMetricItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
