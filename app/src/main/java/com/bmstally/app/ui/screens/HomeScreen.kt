package com.bmstally.app.ui.screens

import androidx.compose.foundation.background
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
import com.bmstally.app.ui.navigation.Routes
import com.bmstally.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onCreateEntry: () -> Unit,
    onReminderSheet: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showReminderSheet by remember { mutableStateOf(false) }

    if (showReminderSheet) {
        AlertDialog(
            onDismissRequest = { showReminderSheet = false },
            confirmButton = {},
            text = { ReminderBottomSheet(onContinue = { type ->
                showReminderSheet = false
                if (type == "manual") onNavigate(Routes.MANUAL_REMINDER)
                else onNavigate(Routes.REMINDER_SCHEDULER)
            }) }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFF3F51B5)).padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = Color.White,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            if (state.userName.isNotEmpty()) state.userName.first().uppercase() else "U",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color(0xFF3F51B5)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(state.userName.ifEmpty { "User" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    Text(state.userEmail, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                    Text(state.currentTenant?.name ?: "", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                }
                                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Expires on: 24 Jun 26", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }

                    SectionHeader("MY ACCOUNT")
                    NavDrawerItem(Icons.Default.AccountBalance, "Ledgers") { onNavigate(Routes.LEDGER_LIST); scope.launch { drawerState.close() } }
                    NavDrawerItem(Icons.Default.Receipt, "Vouchers") { onNavigate(Routes.VOUCHER_LIST); scope.launch { drawerState.close() } }
                    NavDrawerItem(Icons.Default.ShoppingCart, "Order Book") { onNavigate(Routes.ORDER_LIST); scope.launch { drawerState.close() } }
                    NavDrawerItem(Icons.Default.Business, "Companies") { onNavigate(Routes.COMPANIES); scope.launch { drawerState.close() } }
                    NavDrawerItem(Icons.Default.Group, "Users", showDot = true) { onNavigate(Routes.USERS); scope.launch { drawerState.close() } }
                    NavDrawerItem(Icons.Default.Settings, "Settings") { onNavigate(Routes.SETTINGS); scope.launch { drawerState.close() } }
                    NavDrawerItem(Icons.Default.People, "Refer a Friend") { scope.launch { drawerState.close() } }
                    NavDrawerItem(
                        icon = Icons.Default.AccountBalanceWallet, label = "Wallet",
                        trailing = {
                            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF4CAF50)) {
                                Text("300 coins", Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontSize = 11.sp)
                            }
                        }
                    ) { onNavigate(Routes.WALLET); scope.launch { drawerState.close() } }

                    HorizontalDivider()
                    SectionHeader("SUBSCRIPTION")
                    NavDrawerItem(Icons.Default.CardMembership, "Purchase Subscription") { onNavigate(Routes.SUBSCRIPTION); scope.launch { drawerState.close() } }

                    HorizontalDivider()
                    SectionHeader("SECURITY")
                    NavDrawerItem(Icons.Default.Lock, "Set Passcode") { scope.launch { drawerState.close() } }
                    NavDrawerItem(Icons.Default.Lock, "Forgot Password") { scope.launch { drawerState.close() } }

                    HorizontalDivider()
                    SectionHeader("SUPPORT")
                    NavDrawerItem(Icons.Default.Info, "Version 19.6.2") { scope.launch { drawerState.close() } }
                    NavDrawerItem(Icons.Default.HelpOutline, "Help") { onNavigate(Routes.HELP); scope.launch { drawerState.close() } }
                    NavDrawerItem(Icons.Default.Info, "About") { onNavigate(Routes.ABOUT); scope.launch { drawerState.close() } }

                    HorizontalDivider()
                    NavDrawerItem(Icons.Default.PowerSettingsNew, "Logout", tint = Color(0xFFEF5350)) {
                        onLogout()
                        scope.launch { drawerState.close() }
                    }
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = state.tabIndex == 0, onClick = { viewModel.setTab(0) },
                        icon = { Icon(Icons.Default.GridView, null) }, label = { Text("Dashboard") })
                    NavigationBarItem(selected = state.tabIndex == 1, onClick = { viewModel.setTab(1) },
                        icon = { Icon(Icons.Default.ReceiptLong, null) }, label = { Text("Outstanding") })
                    NavigationBarItem(selected = state.tabIndex == 2, onClick = { viewModel.setTab(2) },
                        icon = { Icon(Icons.Default.People, null) }, label = { Text("Sales Team") })
                    NavigationBarItem(selected = state.tabIndex == 3, onClick = { viewModel.setTab(3) },
                        icon = { Icon(Icons.Default.Assessment, null) }, label = { Text("Reports") })
                }
            },
            floatingActionButton = {
                when (state.tabIndex) {
                    0 -> FloatingActionButton(
                        onClick = onCreateEntry,
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    ) { Icon(Icons.Default.CalendarToday, null) }
                    1 -> FloatingActionButton(
                        onClick = { showReminderSheet = true },
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    ) { Icon(Icons.Default.NotificationsActive, null) }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
            when (state.tabIndex) {
                0 -> DashboardScreen(
                    tenant = state.currentTenant,
                    selectedCompany = state.selectedCompany,
                    companies = state.companies,
                    stats = viewModel.dashboardStats,
                    onSelectCompany = { viewModel.selectCompany(it) },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onItems = { onNavigate(Routes.ITEMS) },
                    onWallet = { onNavigate(Routes.WALLET) }
                )
                1 -> OutstandingScreen(
                    summary = viewModel.outstandingSummary,
                    ledgers = viewModel.outstandingLedgers,
                    groups = viewModel.outstandingGroups,
                    onMenu = { scope.launch { drawerState.open() } },
                    onReminderBanner = onReminderSheet
                )
                2 -> SalesTeamScreen(
                    entries = viewModel.salesEntries,
                    onNavigate = onNavigate
                )
                3 -> ReportsScreen(
                    onNavigate = onNavigate
                )
            }
        }
    }
}

}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
        fontWeight = FontWeight.W600,
        color = Color.Gray,
        fontSize = 13.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavDrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color(0xFF212121),
    showDot: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Box {
                Icon(icon, null, tint = tint)
                if (showDot) {
                    Box(
                        modifier = Modifier.size(8.dp).align(Alignment.TopEnd)
                    ) {
                        Box(
                            Modifier.size(8.dp).background(Color(0xFFFF9800), shape = MaterialTheme.shapes.extraLarge)
                        )
                    }
                }
            }
        },
        label = { Text(label, color = tint, fontSize = 14.sp) },
        badge = trailing,
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}