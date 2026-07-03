package com.example.feature.settings.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.settings.domain.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyTenantProfileScreen(
    viewModel: SettingsViewModel,
    onNavigateToSync: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToLicense: () -> Unit,
    onNavigateToCompanies: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.companyProfile.collectAsState()
    val license by viewModel.licenseInfo.collectAsState()
    val sync by viewModel.syncSettings.collectAsState()
    val showFeedback by viewModel.saveSuccessFeedback.collectAsState()

    var editMode by remember { mutableStateOf(false) }
    var tenantName by remember(profile.tenantName) { mutableStateOf(profile.tenantName) }
    var gstin by remember(profile.gstin) { mutableStateOf(profile.gstin) }
    var phone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var email by remember(profile.email) { mutableStateOf(profile.email) }
    var address by remember(profile.registeredAddress) { mutableStateOf(profile.registeredAddress) }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Corporate Context Settings",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "Verify corporate tenant settings & parameters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (editMode) {
                        IconButton(
                            onClick = {
                                viewModel.updateCompanyProfile(
                                    profile.copy(
                                        tenantName = tenantName,
                                        gstin = gstin,
                                        phone = phone,
                                        email = email,
                                        registeredAddress = address
                                    )
                                )
                                editMode = false
                            },
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Save Profile", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(
                            onClick = { editMode = true },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Enterprise Identity Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Corporate Logo",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.tenantName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "GST Verified",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "GSTIN: ${profile.gstin}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Plan: ${license.activePlanName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // Inline Profile Editor & Details
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Tenant Profile Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (editMode) {
                                OutlinedTextField(
                                    value = tenantName,
                                    onValueChange = { tenantName = it },
                                    label = { Text("Corporate Name") },
                                    leadingIcon = { Icon(Icons.Default.Home, null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("profile_name_input"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = gstin,
                                    onValueChange = { gstin = it },
                                    label = { Text("Corporate GSTIN") },
                                    leadingIcon = { Icon(Icons.Default.List, null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("profile_gstin_input"),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Primary Phone") },
                                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("profile_phone_input"),
                                        singleLine = true
                                    )
                                }

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Billing Contact Email") },
                                    leadingIcon = { Icon(Icons.Default.Email, null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("profile_email_input"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = { Text("Registered Business Address") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("profile_address_input"),
                                    maxLines = 3
                                )
                            } else {
                                ProfileDetailField(
                                    icon = Icons.Default.Home,
                                    label = "Corporate Name",
                                    value = profile.tenantName
                                )
                                ProfileDetailField(
                                    icon = Icons.Default.List,
                                    label = "Corporate GSTIN",
                                    value = profile.gstin
                                )
                                ProfileDetailField(
                                    icon = Icons.Default.Phone,
                                    label = "Primary Phone",
                                    value = profile.phone
                                )
                                ProfileDetailField(
                                    icon = Icons.Default.Email,
                                    label = "Billing Contact Email",
                                    value = profile.email
                                )
                                ProfileDetailField(
                                    icon = Icons.Default.LocationOn,
                                    label = "Registered Address",
                                    value = profile.registeredAddress
                                )
                            }
                        }
                    }
                }

                // Interactive Navigation Hub Options
                item {
                    Text(
                        text = "System Configurations",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    SettingsNavigationCard(
                        title = "Tally Sync Agent Workspace",
                        subtitle = "Desktop Agent Port - Status: ${sync.heartbeatStatus}",
                        description = "Verify TCP credentials, rotate token keys, run heartbeat connection tests.",
                        icon = Icons.Default.Refresh,
                        badgeText = sync.heartbeatStatus,
                        badgeColor = when (sync.heartbeatStatus) {
                            "Connected" -> MaterialTheme.colorScheme.primary
                            "Testing..." -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        },
                        onClick = onNavigateToSync,
                        tag = "nav_to_sync_card"
                    )
                }

                item {
                    SettingsNavigationCard(
                        title = "App Custom Preferences",
                        subtitle = "Theme parameters & device behaviors",
                        description = "Toggle Slate Dark/Light backgrounds, manage system biometrics, and push alerts.",
                        icon = Icons.Default.Settings,
                        onClick = onNavigateToPreferences,
                        tag = "nav_to_preferences_card"
                    )
                }

                item {
                    SettingsNavigationCard(
                        title = "Licensing & User Seats",
                        subtitle = "Billing: ${license.consumedSeatsCount}/${license.maxSeatsCount} corporate seats",
                        description = "Manage active client seats, renew core licenses, inspect renewal timelines.",
                        icon = Icons.Default.Star,
                        onClick = onNavigateToLicense,
                        tag = "nav_to_license_card"
                    )
                }

                item {
                    SettingsNavigationCard(
                        title = "Multi-Company Mappings",
                        subtitle = "Central administration context",
                        description = "Register or deactivate Tally company linkages connected to this device workspace.",
                        icon = Icons.Default.Home,
                        onClick = onNavigateToCompanies,
                        tag = "nav_to_companies_card"
                    )
                }
            }

            // Success feedback toast overlay
            AnimatedVisibility(
                visible = showFeedback,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring()
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring()
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 6.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Settings successfully updated in cache database!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailField(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNavigationCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    badgeText: String? = null,
    badgeColor: Color = Color.Unspecified,
    onClick: () -> Unit,
    tag: String
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallySyncAgentSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val sync by viewModel.syncSettings.collectAsState()
    val isTesting by viewModel.isTestingHeartbeat.collectAsState()
    val showFeedback by viewModel.saveSuccessFeedback.collectAsState()

    var editMode by remember { mutableStateOf(false) }
    var agentUrl by remember(sync.desktopAgentUrl) { mutableStateOf(sync.desktopAgentUrl) }
    var syncCycle by remember(sync.autoSyncCycleMinutes) { mutableStateOf(sync.autoSyncCycleMinutes.toString()) }
    var limitMb by remember(sync.localOfflineStorageLimitMb) { mutableStateOf(sync.localOfflineStorageLimitMb.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tally Sync Agent Config", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (editMode) {
                        IconButton(
                            onClick = {
                                viewModel.updateSyncSettings(
                                    sync.copy(
                                        desktopAgentUrl = agentUrl,
                                        autoSyncCycleMinutes = syncCycle.toIntOrNull() ?: 15,
                                        localOfflineStorageLimitMb = limitMb.toIntOrNull() ?: 512
                                    )
                                )
                                editMode = false
                            },
                            modifier = Modifier.testTag("save_sync_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save Settings", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(
                            onClick = { editMode = true },
                            modifier = Modifier.testTag("edit_sync_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Connection Status Card
                item {
                    val statusColor = when (sync.heartbeatStatus) {
                        "Connected" -> MaterialTheme.colorScheme.primary
                        "Testing..." -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Desktop Link Status: ${sync.heartbeatStatus}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Verification check executed at: ${sync.lastHeartbeatTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.testHeartbeat() },
                                    enabled = !isTesting,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_heartbeat_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                                ) {
                                    if (isTesting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pinging Agent...")
                                    } else {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Ping Heartbeat")
                                    }
                                }
                            }
                        }
                    }
                }

                // URL & Secret Credentials
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Agent Handshake Settings",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (editMode) {
                                OutlinedTextField(
                                    value = agentUrl,
                                    onValueChange = { agentUrl = it },
                                    label = { Text("Tally Agent Endpoint URL") },
                                    leadingIcon = { Icon(Icons.Default.Build, null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("agent_url_input"),
                                    singleLine = true
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = syncCycle,
                                        onValueChange = { syncCycle = it },
                                        label = { Text("Sync Cycle (Mins)") },
                                        leadingIcon = { Icon(Icons.Default.Refresh, null) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("sync_cycle_input"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = limitMb,
                                        onValueChange = { limitMb = it },
                                        label = { Text("DB Limit (MB)") },
                                        leadingIcon = { Icon(Icons.Default.List, null) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("db_limit_input"),
                                        singleLine = true
                                    )
                                }
                            } else {
                                ProfileDetailField(
                                    icon = Icons.Default.Build,
                                    label = "Tally Agent Endpoint URL",
                                    value = sync.desktopAgentUrl
                                )
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProfileDetailField(
                                            icon = Icons.Default.Refresh,
                                            label = "Automatic Sync Interval",
                                            value = "${sync.autoSyncCycleMinutes} Minutes"
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProfileDetailField(
                                            icon = Icons.Default.List,
                                            label = "Offline Storage Space",
                                            value = "${sync.localOfflineStorageLimitMb} MB Cache"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Security handshake Token
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Authentication & Cryptographic Seeding",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "The cryptographic token secures raw network logs transfer between your central desktop Tally database listener and this Android context.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sync.syncTokenKey,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Button(
                                onClick = { viewModel.rotateSyncToken() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("rotate_token_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Re-generate/Rotate Handshake Token")
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showFeedback,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = "Sync Parameters saved successfully!",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPreferencesScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val showFeedback by viewModel.saveSuccessFeedback.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App System Preferences", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // UI Visual Theme Customizer
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Application Appearance Theme",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            val themesList = listOf("System", "Light", "Dark", "Slate")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                themesList.forEach { theme ->
                                    val isSelected = appSettings.selectedTheme == theme
                                    Button(
                                        onClick = {
                                            viewModel.updateAppSettings(appSettings.copy(selectedTheme = theme))
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("theme_selector_${theme.lowercase()}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(text = theme, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Security & Authentication
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Core Authentication Controls",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Biometric Security Lock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Unlock workspace with instant fingerprint register scan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = appSettings.biometricAccessActive,
                                    onCheckedChange = {
                                        viewModel.updateAppSettings(appSettings.copy(biometricAccessActive = it))
                                    },
                                    modifier = Modifier.testTag("biometric_switch")
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Dispatch Push Alerts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Receive push notices on new offline vouchers synchronizing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = appSettings.pushNotificationsActive,
                                    onCheckedChange = {
                                        viewModel.updateAppSettings(appSettings.copy(pushNotificationsActive = it))
                                    },
                                    modifier = Modifier.testTag("notification_switch")
                                )
                            }
                        }
                    }
                }

                // Regional Packs
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Regional Language Options",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            val regionalPacks = listOf(
                                Triple("en_IN", "English (India)", "Standard numeric lakhs separator"),
                                Triple("hi_IN", "हिन्दी (Hindi Locale)", "Standard Devanagari numerals")
                            )

                            regionalPacks.forEach { (code, name, explanation) ->
                                val isSelected = appSettings.languageCode == code
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                                        .clickable {
                                            viewModel.updateAppSettings(appSettings.copy(languageCode = code))
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = explanation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showFeedback,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = "Preferences successfully saved in local cache!",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensingSubscriptionScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val license by viewModel.licenseInfo.collectAsState()
    val showFeedback by viewModel.saveSuccessFeedback.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Licensing & Billing Seats", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Subscription Status
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = license.activePlanName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "License Key Code: ${license.serialLicenseNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Next scheduled annual billing cycle: ${license.licenseExpiresAt}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                            )
                        }
                    }
                }

                // Seats Progress Indicator
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Teammate Seat Allocations",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Consumed Seats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = license.consumedSeatsCount.toString(),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = " / ${license.maxSeatsCount} max",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }

                            val progress = license.consumedSeatsCount.toFloat() / license.maxSeatsCount.toFloat()
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Text(
                                text = "Each desktop agent connector allows synchronizing with multiple mobile seats representing on-field sales representatives and warehouse personnel.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Billing actions
                item {
                    Button(
                        onClick = { viewModel.renewOrUpgradeLicense() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("upgrade_license_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Corporate Seat Limit (+5 Seats)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            AnimatedVisibility(
                visible = showFeedback,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = "License details updated in billing system!",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// MultiCompany Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiCompanyManagementScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val companies by viewModel.tallyCompanies.collectAsState()
    val showFeedback by viewModel.saveSuccessFeedback.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newCompanyName by remember { mutableStateOf("") }
    var newCompanyGstin by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi-Company Contexts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_company_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Company Link")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "Mapped Tally ERP Companies",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(companies) { company ->
                    ElevatedCard(
                        onClick = { viewModel.switchActiveTallyCompany(company.companyId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("company_item_${company.companyId}"),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (company.isActiveConnection) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (company.isActiveConnection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = if (company.isActiveConnection) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = company.tallyCompanyName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "GSTIN: ${company.tallyCompanyGstin}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Last connection sync: ${company.lastSyncedAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            if (company.isActiveConnection) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Active Workspace",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Register Tally Company Link") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = newCompanyName,
                                onValueChange = { newCompanyName = it },
                                label = { Text("Tally ERP Company Name") },
                                modifier = Modifier.testTag("dialog_comp_name_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newCompanyGstin,
                                onValueChange = { newCompanyGstin = it },
                                label = { Text("Tally Company GSTIN") },
                                modifier = Modifier.testTag("dialog_comp_gstin_input"),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newCompanyName.isNotBlank() && newCompanyGstin.isNotBlank()) {
                                    viewModel.addTallyCompany(
                                        TallyCompanyConnection(
                                            companyId = "comp_${(companies.size + 1)}",
                                            tallyCompanyName = newCompanyName,
                                            isActiveConnection = false,
                                            tallyCompanyGstin = newCompanyGstin,
                                            lastSyncedAt = "Just registered"
                                        )
                                    )
                                    newCompanyName = ""
                                    newCompanyGstin = ""
                                    showAddDialog = false
                                }
                            },
                            modifier = Modifier.testTag("dialog_confirm_button")
                        ) {
                            Text("Map Connection")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = showFeedback,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Active Company Context switched!",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
