package com.example.feature.user.presentation

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.security.domain.models.PermissionAction
import com.example.feature.security.domain.models.Role
import com.example.feature.security.domain.models.SecurityModule
import com.example.feature.user.domain.models.User
import com.example.feature.user.domain.models.UserActivity
import com.example.feature.user.domain.models.UserStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit,
    onNavigateToInvite: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDirectCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshAll()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "User Directory",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Direct Create Team Member
                FloatingActionButton(
                    onClick = {
                        viewModel.clearForm()
                        showDirectCreateDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.testTag("direct_create_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Member Directly")
                }

                // Invite Team Member
                FloatingActionButton(
                    onClick = onNavigateToInvite,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("invite_fab")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Invite Teammate")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search and Status Filters Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search Header
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search by name, email, employee code...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Filter Status Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statuses = listOf(
                            null to "All",
                            UserStatus.ACTIVE to "Active",
                            UserStatus.INACTIVE to "Inactive",
                            UserStatus.LOCKED to "Locked"
                        )

                        statuses.forEach { (statusKey, label) ->
                            val isSelected = state.statusFilter == statusKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateStatusFilter(statusKey) },
                                label = { Text(label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("filter_status_${label.lowercase()}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Directory List
            val filteredUsers = state.users.filter { user ->
                val matchesSearch = user.name.contains(state.searchQuery, ignoreCase = true) ||
                        user.email.contains(state.searchQuery, ignoreCase = true) ||
                        user.employeeCode.contains(state.searchQuery, ignoreCase = true)
                val matchesStatus = state.statusFilter == null || user.status == state.statusFilter
                matchesSearch && matchesStatus
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No Teammates Registered",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Add coworkers directly using the FAB to configure their access levels.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredUsers, key = { it.id }) { user ->
                        UserCardItem(
                            user = user,
                            roles = state.roles,
                            onClick = { onNavigateToDetail(user.id) }
                        )
                    }
                }
            }
        }
    }

    // Direct Create Dialog
    if (showDirectCreateDialog) {
        AlertDialog(
            onDismissRequest = { showDirectCreateDialog = false },
            title = { Text("Create Core User") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = state.formName,
                            onValueChange = { viewModel.updateFormName(it) },
                            label = { Text("Teammate Full Name*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_name_field"),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = state.formEmail,
                            onValueChange = { viewModel.updateFormEmail(it) },
                            label = { Text("Login Email Address*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_email_field"),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = state.formPhone,
                            onValueChange = { viewModel.updateFormPhone(it) },
                            label = { Text("SMS Telephone No.") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = state.formEmployeeCode,
                            onValueChange = { viewModel.updateFormEmployeeCode(it) },
                            label = { Text("Internal Employee ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = state.formCompany,
                            onValueChange = { viewModel.updateFormCompany(it) },
                            label = { Text("Target Company Domain") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = state.formTerritory,
                            onValueChange = { viewModel.updateFormTerritory(it) },
                            label = { Text("Territory / Beat Alignment") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        Text(
                            text = "Assign Initial Role Access Levels",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(state.roles) { role ->
                        val isAssigned = state.formRoles.contains(role.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleFormRole(role.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isAssigned,
                                onCheckedChange = { viewModel.toggleFormRole(role.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(role.name, fontWeight = FontWeight.Medium)
                                Text(role.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createUser {
                            showDirectCreateDialog = false
                        }
                    },
                    enabled = state.formName.isNotBlank() && state.formEmail.isNotBlank(),
                    modifier = Modifier.testTag("form_confirm_btn")
                ) {
                    Text("Provision Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserCardItem(
    user: User,
    roles: List<Role>,
    onClick: () -> Unit
) {
    val initials = user.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
    val statusColor = when (user.status) {
        UserStatus.ACTIVE -> Color(0xFF4CAF50)
        UserStatus.INACTIVE -> Color(0xFF9E9E9E)
        UserStatus.LOCKED -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("user_card_${user.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded Avatar Frame
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 16.sp
                )
                // Small state indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(statusColor, shape = CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${user.employeeCode} • ${user.email}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                // Render Assigned Role pills
                if (user.assignedRoles.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        user.assignedRoles.take(3).forEach { roleId ->
                            val roleName = roles.firstOrNull { it.id == roleId }?.name ?: roleId
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    roleName,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Arrow Indicator
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteUserScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Co-Worker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Send access invitations to your remote workforce. They will receive credentials instantly bypass.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.formEmail,
                            onValueChange = { viewModel.updateFormEmail(it) },
                            label = { Text("Email Address*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invite_email_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.formPhone,
                            onValueChange = { viewModel.updateFormPhone(it) },
                            label = { Text("Mobile Phone") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invite_phone_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.formName,
                            onValueChange = { viewModel.updateFormName(it) },
                            label = { Text("Full Name (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Text("Pre-Assign Policy Roles", style = MaterialTheme.typography.titleMedium)
            }

            items(state.roles) { role ->
                val isAssigned = state.formRoles.contains(role.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleFormRole(role.id) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAssigned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAssigned,
                            onCheckedChange = { viewModel.toggleFormRole(role.id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(role.name, fontWeight = FontWeight.SemiBold)
                            Text(role.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.inviteUser("EMAIL", {}) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invite_via_email_btn"),
                        enabled = state.formEmail.contains("@")
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Email Invite")
                    }

                    FilledTonalButton(
                        onClick = { viewModel.inviteUser("SMS", {}) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invite_via_sms_btn"),
                        enabled = state.formPhone.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SMS Invite")
                    }
                }
            }

            // Results showcase
            if (state.invitationLink != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Invitation Link Ready",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SelectionContainerOrText(
                                label = "Direct URL",
                                value = state.invitationLink ?: "",
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(state.invitationLink ?: ""))
                                    Toast.makeText(context, "Copied link directly!", Toast.LENGTH_SHORT).show()
                                }
                            )

                            SelectionContainerOrText(
                                label = "Temporary PIN / Password",
                                value = state.tempPassword ?: "",
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(state.tempPassword ?: ""))
                                    Toast.makeText(context, "Copied temp password!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionContainerOrText(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.Refresh, contentDescription = "Copy text")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailEditRoleScreen(
    userId: String,
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableIntStateOf(0) } // 0: Profile & Controls, 1: Assignments & clearances, 2: Login Logs

    LaunchedEffect(userId) {
        viewModel.refreshAll()
    }

    // Set selected user based on ID
    val resolvedUser = state.users.firstOrNull { it.id == userId }
    LaunchedEffect(resolvedUser) {
        if (resolvedUser != null) {
            viewModel.selectUser(resolvedUser)
        }
    }

    val user = state.selectedUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.name ?: "Teammate Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
        }
    ) { paddingValues ->
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Secondary Tab Headers
                TabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Profile & Actions") }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Policy Assignment") }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Security Events") }
                    )
                }

                // Active Pane Switcher
                when (activeTab) {
                    0 -> ProfileAndControlsTab(user, viewModel, onBack)
                    1 -> RoleAssignmentAndMatrixTab(user, state.roles, state.resolvedPermissions, viewModel)
                    2 -> UserSecurityLogsTab(userId, state.activities)
                }
            }
        }
    }
}

@Composable
fun ProfileAndControlsTab(
    user: User,
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High level banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor = when (user.status) {
                        UserStatus.ACTIVE -> Color(0xFF4CAF50)
                        UserStatus.INACTIVE -> Color(0xFF9E9E9E)
                        UserStatus.LOCKED -> Color(0xFFF44336)
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(statusColor, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = user.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }
            }
        }

        // Details Matrix
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Identity Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider()

                    ProfileFieldRow(label = "Internal Employee Code", value = user.employeeCode)
                    ProfileFieldRow(label = "Primary Telephone", value = user.phone.ifBlank { "Unregistered" })
                    ProfileFieldRow(label = "Assigned Franchise Domain", value = user.assignedCompany)
                    ProfileFieldRow(label = "Operational Beat / Territory", value = user.assignedTerritory)
                    ProfileFieldRow(label = "Last Connection Recorded", value = user.lastLogin ?: "Never Connected")
                }
            }
        }

        // Action controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Administrative Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider()

                    // Toggle active status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.activateUser(user.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("activate_user_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            enabled = user.status != UserStatus.ACTIVE
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activate")
                        }

                        Button(
                            onClick = { viewModel.deactivateUser(user.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("deactivate_user_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                            enabled = user.status != UserStatus.INACTIVE
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Suspend")
                        }
                    }

                    Button(
                        onClick = { viewModel.lockUser(user.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lock_user_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        enabled = user.status != UserStatus.LOCKED
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Authorization Credentials")
                    }

                    FilledTonalButton(
                        onClick = { viewModel.resetPassword(user.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_password_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Password & Issue PIN")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.deleteUser(user.id) {
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_user_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Erase Profile Reference")
                    }
                }
            }
        }

        if (state.infoMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = state.infoMessage ?: "",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.outline, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun RoleAssignmentAndMatrixTab(
    user: User,
    allRoles: List<Role>,
    resolvedPermissions: Map<SecurityModule, Set<PermissionAction>>,
    viewModel: UserViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Multi-Role Security Settings",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Check roles below. This user inherits the combined access authorizations of every checked role.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Checklist of Roles
        items(allRoles) { role ->
            val isChecked = user.assignedRoles.contains(role.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val current = user.assignedRoles.toMutableList()
                        if (current.contains(role.id)) {
                            current.remove(role.id)
                        } else {
                            current.add(role.id)
                        }
                        viewModel.updateUserRoles(user.id, current)
                    },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = {
                            val current = user.assignedRoles.toMutableList()
                            if (current.contains(role.id)) {
                                current.remove(role.id)
                            } else {
                                current.add(role.id)
                            }
                            viewModel.updateUserRoles(user.id, current)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(role.name, fontWeight = FontWeight.SemiBold)
                        Text(role.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // Effective Permissions Matrix Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Effective Permissions Preview (Union Matrix)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Realtime preview analyzing the union code compiled from the checked roles above:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Grid/List mapping of resolved permissions!
        items(resolvedPermissions.entries.toList()) { (module, actions) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.4f)) {
                        Text(
                            text = module.name.replace("_", " "),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Render inline actions chips
                    Row(
                        modifier = Modifier.weight(0.6f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (actions.isEmpty()) {
                            Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                Text("NO ACCESS", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, modifier = Modifier.padding(4.dp))
                            }
                        } else {
                            // Flow of individual badges
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                actions.chunked(3).forEach { chunk ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        chunk.forEach { action ->
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = action.name,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSecurityLogsTab(
    userId: String,
    activities: List<UserActivity>
) {
    if (activities.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                Text("No Security Events Registered", fontWeight = FontWeight.SemiBold)
                Text(
                    "Any login attempts, status flips, or credential changes will record here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activities) { act ->
                val cardBg = when (act.status) {
                    "SUCCESS" -> Color(0xFFE8F5E9)
                    "FAILURE" -> Color(0xFFFFEBEE)
                    else -> Color(0xFFECEFF1)
                }
                val textColor = when (act.status) {
                    "SUCCESS" -> Color(0xFF283593)
                    "FAILURE" -> Color(0xFFC62828)
                    else -> Color(0xFF37474F)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                act.eventName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Box(
                                modifier = Modifier
                                    .background(cardBg, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    act.status,
                                    color = textColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = act.timestamp,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Text(
                            text = act.deviceInfo,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "IP: ${act.ipAddress} • Loc: ${act.location ?: "N/A"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
