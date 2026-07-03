package com.example.feature.security.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.common.Resource
import com.example.feature.security.domain.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPermissionsParentScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableStateOf(0) } // 0: Roles List, 1: User Assignments, 2: Audit Logs
    var showFormScreen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "RBAC Security Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Centralized policy, user clearances, and forensic logs",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("security_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh data")
                    }
                }
            )
        },
        floatingActionButton = {
            if (activeTab == 0 && !showFormScreen) {
                ExtendedFloatingActionButton(
                    text = { Text("New Role") },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        viewModel.startCreateRole()
                        showFormScreen = true
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("create_role_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector Row
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = {
                        activeTab = 0
                        showFormScreen = false
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Roles & Policies", fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_roles_matrix")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = {
                        activeTab = 1
                        showFormScreen = false
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Assignments", fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_assignments")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = {
                        activeTab = 2
                        showFormScreen = false
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Audits", fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_audit_logs")
                )
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.error?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onErrorContainer)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> {
                        AnimatedVisibility(
                            visible = showFormScreen,
                            enter = slideInHorizontally(initialOffsetX = { it }),
                            exit = slideOutHorizontally(targetOffsetX = { it })
                        ) {
                            RoleFormScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onDismiss = { showFormScreen = false }
                            )
                        }

                        AnimatedVisibility(
                            visible = !showFormScreen,
                            enter = slideInHorizontally(initialOffsetX = { -it }),
                            exit = slideOutHorizontally(targetOffsetX = { -it })
                        ) {
                            RolesListScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onEditRole = { role ->
                                    viewModel.startEditRole(role)
                                    showFormScreen = true
                                },
                                onCloneRole = { role ->
                                    viewModel.startCloneRole(role)
                                    showFormScreen = true
                                }
                            )
                        }
                    }
                    1 -> UserAssignmentScreen(viewModel = viewModel, uiState = uiState)
                    2 -> RoleAuditHistoryScreen(viewModel = viewModel, uiState = uiState)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: Roles List Screen
// ==========================================
@Composable
fun RolesListScreen(
    viewModel: SecurityViewModel,
    uiState: SecurityUiState,
    onEditRole: (Role) -> Unit,
    onCloneRole: (Role) -> Unit
) {
    var expandedRoleId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Stat summaries
        OutlinedTextField(
            value = uiState.searchRolesQuery,
            onValueChange = { viewModel.updateSearchRolesQuery(it) },
            placeholder = { Text("Search system or custom roles...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("roles_search_input"),
            singleLine = true
        )

        val filteredRoles = uiState.roles.filter {
            it.name.contains(uiState.searchRolesQuery, ignoreCase = true) ||
                    it.description.contains(uiState.searchRolesQuery, ignoreCase = true)
        }

        if (filteredRoles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No credentials profiles detected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Try clearing modifiers search criteria or press refresh sync.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredRoles) { role ->
                    RoleRowItem(
                        role = role,
                        isExpanded = expandedRoleId == role.id,
                        onToggleExpand = {
                            expandedRoleId = if (expandedRoleId == role.id) null else role.id
                        },
                        onEdit = { onEditRole(role) },
                        onClone = { onCloneRole(role) },
                        onDelete = { viewModel.deleteRole(role) }
                    )
                }
            }
        }
    }
}

@Composable
fun RoleRowItem(
    role: Role,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onToggleExpand() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (role.isSystem) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (role.isSystem) Icons.Default.Build else Icons.Default.Settings,
                        contentDescription = null,
                        tint = if (role.isSystem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = role.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (role.isSystem) {
                            Text(
                                "SYSTEM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        } else {
                            Text(
                                "CUSTOM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = role.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isExpanded) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Module and Permission quick breakdown list
                Text(
                    text = "Permission Profile Breakdown",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                role.permissions.filter { it.actions.isNotEmpty() }.forEach { modPerm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = modPerm.module.name.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = modPerm.actions.joinToString(", ") { it.name },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onClone,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Clone", fontSize = 12.sp)
                    }

                    if (!role.isSystem) {
                        IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_role_${role.id}")) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete role",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Button(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_role_${role.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Configure Matrix", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2 & 3: Create/Edit Role & Permission Matrix
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleFormScreen(
    viewModel: SecurityViewModel,
    uiState: SecurityUiState,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (uiState.editingRole == null) "Create Custom Role" else if (uiState.isCloneMode) "Clone Existing Profile" else "Edit Permissions Security Schema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Configure precise visual and functional vectors across security matrix clearances.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Basic inputs
        Text(text = "Role Parameters", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.formRoleName,
            onValueChange = { viewModel.updateRoleName(it) },
            label = { Text("Unique Role Name") },
            placeholder = { Text("e.g., Regional Ledger Auditor") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_role_name_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.formRoleDescription,
            onValueChange = { viewModel.updateRoleDescription(it) },
            label = { Text("Public Security Description") },
            placeholder = { Text("Explain authorization clearances and scope definitions...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("form_role_desc_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // THE PERMISSION MATRIX WORKSPACE SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Granular Modules Permissions Matrix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Fine-tune system features and physical activities clearance keys.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Matrix editor cards
        SecurityModule.values().forEach { module ->
            ModuleMatrixCard(
                module = module,
                selectedActions = uiState.formRolePermissions[module] ?: emptySet(),
                onToggleAction = { action -> viewModel.togglePermission(module, action) },
                onSetAll = { enableAll -> viewModel.setAllPermissionsForModule(module, enableAll) }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Form buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text("Discard Actions")
            }

            Button(
                onClick = {
                    viewModel.saveRole(onSuccess = onDismiss)
                },
                modifier = Modifier.testTag("save_role_button"),
                enabled = uiState.formRoleName.isNotBlank()
            ) {
                Text("Commit Parameters")
            }
        }
    }
}

@Composable
fun ModuleMatrixCard(
    module: SecurityModule,
    selectedActions: Set<PermissionAction>,
    onToggleAction: (PermissionAction) -> Unit,
    onSetAll: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = module.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedActions.size} / ${PermissionAction.values().size} actions enabled",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedActions.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action controls inside row header
                Switch(
                    checked = selectedActions.size == PermissionAction.values().size,
                    onCheckedChange = { onSetAll(it) },
                    modifier = Modifier.scale(0.8f)
                )

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (isExpanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Actions matrix grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    PermissionAction.values().forEach { action ->
                        val isSelected = selectedActions.contains(action)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleAction(action) },
                            label = { Text(action.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

// Custom flow container to support side-by-side chips in Compose cleanly
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        val maxRowWidth = constraints.maxWidth

        placeables.forEach { placeable ->
            val spacing = if (currentRow.isEmpty()) 0 else mainAxisSpacing.roundToPx()
            if (currentRowWidth + spacing + placeable.width <= maxRowWidth) {
                currentRow.add(placeable)
                currentRowWidth += spacing + placeable.width
            } else {
                rows.add(currentRow)
                currentRow = mutableListOf(placeable)
                currentRowWidth = placeable.width
            }
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        var totalHeight = 0
        rows.forEachIndexed { index, row ->
            val rowHeight = row.maxOfOrNull { it.height } ?: 0
            val spacing = if (index == 0) 0 else crossAxisSpacing.roundToPx()
            totalHeight += spacing + rowHeight
        }

        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                val rowHeight = row.maxOfOrNull { it.height } ?: 0
                val crossSpacing = if (rowIndex == 0) 0 else crossAxisSpacing.roundToPx()
                y += crossSpacing
                var x = 0
                row.forEachIndexed { itemIndex, placeable ->
                    val mainSpacing = if (itemIndex == 0) 0 else mainAxisSpacing.roundToPx()
                    x += mainSpacing
                    placeable.placeRelative(x, y)
                    x += placeable.width
                }
                y += rowHeight
            }
        }
    }
}

// ==========================================
// SCREEN 4: User Assignment Screen
// ==========================================
@Composable
fun UserAssignmentScreen(
    viewModel: SecurityViewModel,
    uiState: SecurityUiState
) {
    var showDialog by remember { mutableStateOf(false) }

    // User Assignment state fields
    var userIdField by remember { mutableStateOf("") }
    var userNameField by remember { mutableStateOf("") }
    var userEmailField by remember { mutableStateOf("") }
    var roleIdField by remember { mutableStateOf("") }

    var expandedRoleMenu by remember { mutableStateOf(false) }
    var showPreviewUserDialog by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Workspace User Assignations",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Affix multi-role credential matrices and preview overall secure clearances.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        userIdField = ""
                        userNameField = ""
                        userEmailField = ""
                        roleIdField = uiState.roles.firstOrNull()?.id ?: ""
                        showDialog = true
                    },
                    modifier = Modifier.testTag("create_assignment_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Assign", fontSize = 12.sp)
                }
            }
        }

        OutlinedTextField(
            value = uiState.searchAssignmentsQuery,
            onValueChange = { viewModel.updateSearchAssignmentsQuery(it) },
            placeholder = { Text("Filter assignments by email, user, or role...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("assignments_search_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        val filteredAssignments = uiState.assignments.filter {
            it.userName.contains(uiState.searchAssignmentsQuery, ignoreCase = true) ||
                    it.userEmail.contains(uiState.searchAssignmentsQuery, ignoreCase = true) ||
                    it.roleName.contains(uiState.searchAssignmentsQuery, ignoreCase = true)
        }

        if (filteredAssignments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active user clearances profiles match search parameters", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn {
                items(filteredAssignments) { asg ->
                    AssignmentRowItem(
                        assignment = asg,
                        onDelete = { viewModel.revokeAssignment(asg) },
                        onPreview = { showPreviewUserDialog = asg.userId }
                    )
                }
            }
        }
    }

    // Modal Assignment Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Affix Credentials Role") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = userIdField,
                        onValueChange = { userIdField = it },
                        label = { Text("Physical User ID Code") },
                        placeholder = { Text("e.g. user_amit") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("assign_user_id_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userNameField,
                        onValueChange = { userNameField = it },
                        label = { Text("Physical User Name") },
                        placeholder = { Text("e.g. Amit Verma") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("assign_user_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userEmailField,
                        onValueChange = { userEmailField = it },
                        label = { Text("Operational Email Address") },
                        placeholder = { Text("e.g. amit@tallybms.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("assign_user_email_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role selector Dropdown mock
                    Text("Select Target clearance Policy Role", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    var selectedRoleName = uiState.roles.firstOrNull { it.id == roleIdField }?.name ?: "Select Role"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { expandedRoleMenu = true }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedRoleName, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = expandedRoleMenu,
                            onDismissRequest = { expandedRoleMenu = false }
                        ) {
                            uiState.roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.name) },
                                    onClick = {
                                        roleIdField = role.id
                                        expandedRoleMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAssignForm(userIdField, userNameField, userEmailField, roleIdField)
                        viewModel.assignUserRole()
                        showDialog = false
                    },
                    modifier = Modifier.testTag("confirm_assign_button"),
                    enabled = userIdField.isNotEmpty() && userNameField.isNotEmpty() && userEmailField.isNotEmpty() && roleIdField.isNotEmpty()
                ) {
                    Text("Secure Mapping")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Preview Dialog
    if (showPreviewUserDialog != null) {
        val targetUid = showPreviewUserDialog!!
        val userAsgs = uiState.assignments.filter { it.userId == targetUid }
        val userName = userAsgs.firstOrNull()?.userName ?: "Unknown User"
        val userEmail = userAsgs.firstOrNull()?.userEmail ?: "No functional email address"
        val effectivePermissions = uiState.getEffectivePermissionsForUser(targetUid)

        Dialog(onDismissRequest = { showPreviewUserDialog = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(2).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                userName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Clearances Preview Matrix (Multi-Role Resolved)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Resolved capabilities accumulated through permissions mapping blocks:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(effectivePermissions.entries.toList()) { (module, actions) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (actions.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.2f
                                    ) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            module.name.replace("_", " "),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        if (actions.isNotEmpty()) {
                                            Text(
                                                actions.joinToString(", ") { it.name },
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Text(
                                                "ACCESS DENIED (No Roles Assigned)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }

                                    if (actions.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Cleared",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Blocked",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showPreviewUserDialog = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss Analyzer Map")
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentRowItem(
    assignment: UserAssignment,
    onDelete: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assignment.userName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = assignment.userEmail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = assignment.roleName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Assigned: ${assignment.assignedAt}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onPreview) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Preview Clearance Matrix",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_assignment_${assignment.id}")) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Revoke Clearance Map",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


// ==========================================
// SCREEN 5: Role Audit History Screen
// ==========================================
@Composable
fun RoleAuditHistoryScreen(
    viewModel: SecurityViewModel,
    uiState: SecurityUiState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchAuditQuery,
            onValueChange = { viewModel.updateSearchAuditQuery(it) },
            placeholder = { Text("Forensics filter (Target, Actor, Action category)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("audits_search_input"),
            singleLine = true
        )

        val filteredAudits = uiState.auditLogs.filter {
            it.actorName.contains(uiState.searchAuditQuery, ignoreCase = true) ||
                    it.targetName.contains(uiState.searchAuditQuery, ignoreCase = true) ||
                    it.actionType.contains(uiState.searchAuditQuery, ignoreCase = true) ||
                    it.details.contains(uiState.searchAuditQuery, ignoreCase = true)
        }

        if (filteredAudits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching security audit trails indexed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredAudits) { record ->
                    AuditRowItem(record = record)
                }
            }
        }
    }
}

@Composable
fun AuditRowItem(record: SecurityAuditRecord) {
    val chipColor = when (record.actionType) {
        "ROLE_CREATED" -> Color(0xFFC8E6C9) // Clean light green
        "ROLE_UPDATED" -> Color(0xFFFFE0B2) // Light orange
        "ROLE_DELETED" -> Color(0xFFFFCDD2) // Light red
        "USER_ASSIGNED" -> Color(0xFFB3E5FC) // Light blue
        "USER_REMOVED" -> Color(0xFFD1C4E9) // Light purple
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val chipTextColor = when (record.actionType) {
        "ROLE_CREATED" -> Color(0xFF1B5E20)
        "ROLE_UPDATED" -> Color(0xFFE65100)
        "ROLE_DELETED" -> Color(0xFFB71C1C)
        "USER_ASSIGNED" -> Color(0xFF01579B)
        "USER_REMOVED" -> Color(0xFF4A148C)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.actionType.replace("_", " "),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = chipTextColor,
                    modifier = Modifier
                        .background(chipColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = record.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body text describing actions details
            Text(
                text = record.details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Operator: ${record.actorName} (${record.actorRole})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (record.targetName.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Target: ${record.targetName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
