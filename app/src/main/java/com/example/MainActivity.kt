package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.navigation.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TallyBmsShell()
            }
        }
    }
}

/**
 * High-fidelity enterprise navigation frame and cockpit dashboard shell for Tally BMS.
 * Built to let the CTO review all 60 modular screens in real-time on the device emulator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallyBmsShell() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTitle by remember { mutableStateOf("Tally BMS") }
    var userRole by remember { mutableStateOf("company_admin") }

    // Runtime Permission handling
    val permissionsToRequest = remember {
        val base = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            base + android.Manifest.permission.POST_NOTIFICATIONS
        } else {
            base
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (permission, isGranted) ->
            android.util.Log.d("TallyBmsShell", "Permission '$permission' state: $isGranted")
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissionsToRequest)
    }

    // Screen catalog helper to let users jump to ANY of the 60 specification screens directly in development!
    val screensList = listOf(
        Pair("1. Login Screen", Screen.Login.route),
        Pair("3. Forgot Password / Reset Link", Screen.ForgotPassword.route),
        Pair("4. Tenant & Company Switcher", Screen.TenantCompanySwitcher.route),
        Pair("5. Home Dashboard", Screen.HomeDashboard.route),
        Pair("6. Ledger List", Screen.LedgerList.route),
        Pair("7. Ledger Detail / Statement", Screen.LedgerDetail.createRoute("ledger_1029")),
        Pair("8. Create / Edit Ledger", Screen.CreateEditLedger.createRoute()),
        Pair("9. Voucher List", Screen.VoucherList.route),
        Pair("10. Create / Edit Voucher", Screen.CreateEditVoucher.createRoute()),
        Pair("11. Voucher Detail View", Screen.VoucherDetail.createRoute("vch_9841")),
        Pair("12. Bill List", Screen.BillList.route),
        Pair("13. Bill Detail View", Screen.BillDetail.createRoute("bill_8321")),
        Pair("14. Order Book (Quotes/Orders)", Screen.OrderBook.route),
        Pair("15. Create / Edit Order", Screen.CreateEditOrder.createRoute()),
        Pair("16. Tally Sync Status Controller", Screen.SyncStatus.route),
        Pair("17. Sync Conflict Resolution Row", Screen.ConflictResolution.route),
        Pair("18. Stock Item Catalog List", Screen.StockItemList.route),
        Pair("19. Stock Item Detail Information", Screen.StockItemDetail.createRoute("stock_8832")),
        Pair("20. Stock Categories / Groups", Screen.StockGroups.route),
        Pair("21. Godown Location Storage Split", Screen.GodownLocationView.route),
        Pair("22. Low Stock Warning Alerts", Screen.LowStockAlerts.route),
        Pair("23. Outstanding Aging Balances", Screen.OutstandingList.route),
        Pair("24. Debtors Outstanding Specifics", Screen.PartyOutstandingDetail.createRoute("party_9912")),
        Pair("25. Receivables Overdue report", Screen.AgingReport.route),
        Pair("26. Outstanding Recovery Pipeline", Screen.RecoveryPipeline.route),
        Pair("27. Billing Reminder SMS Copy", Screen.ReminderTemplates.route),
        Pair("28. Dunning Auto Scheduler Rules", Screen.AutoReminderRules.route),
        Pair("29. Ad-hoc Billing reminder Composer", Screen.ManualReminderComposer.createRoute("party_91", "bill_32")),
        Pair("30. Notifications Reminders Log", Screen.ReminderHistoryLog.route),
        Pair("31. Sales Team Performance Overview", Screen.TeamOverview.route),
        Pair("32. Sales Exec Targets Achievement", Screen.RepProfileTargets.createRoute("user_881")),
        Pair("33. Arrival Checkin On-site Log", Screen.CheckInCheckOut.route),
        Pair("34. Historic arrivals locations logs", Screen.CheckInHistory.route),
        Pair("35. Collection Visitation agendas", Screen.FollowUpList.route),
        Pair("36. Create Collection visitation task", Screen.CreateEditFollowUp.createRoute()),
        Pair("37. Complete collections visitation outcome", Screen.FollowUpDetailOutcome.createRoute("fl_901")),
        Pair("38. Rep Territory / Beat Scheduler", Screen.TerritoryBeatManagement.route),
        Pair("39. Customer CRM search Index", Screen.CustomerListSearch.route),
        Pair("40. Customer 360 Information Profile", Screen.CustomerProfile360.createRoute("cust_360")),
        Pair("41. Periodic Sales summary Report", Screen.MonthlySummaryReport.route),
        Pair("42. Team Target Achievements Report", Screen.SalesPerformanceReport.route),
        Pair("43. Visit compliance SLA Reports", Screen.CheckInComplianceReport.route),
        Pair("44. Custom SQL/noSQL Extract tool", Screen.CustomReportBuilder.route),
        Pair("45. Dynamic Report viewer matrix", Screen.ReportViewer.createRoute("rep_201")),
        Pair("46. Spreadsheet Export download logs", Screen.ExportCenter.route),
        Pair("47. Credit Limits Waiver Approval check", Screen.PendingApprovals.route),
        Pair("48. Approve/Reject exception waiver detail", Screen.ApprovalDetailAction.createRoute("app_73")),
        Pair("49. Sync warnings Notification Feed", Screen.NotificationInbox.route),
        Pair("50. Alerts Delivery preference set", Screen.NotificationPreferences.route),
        Pair("51. Corporate Profile organizational keys", Screen.CompanyTenantProfile.route),
        Pair("52. Multi-company Switch sync agents", Screen.MultiCompanyManagement.route),
        Pair("53. Employee listing authorization scopes", Screen.UserList.route),
        Pair("54. Dispatch coworker enrollment link", Screen.InviteUser.route),
        Pair("55. Modify worker assigned profiles", Screen.UserDetailEditRole.createRoute("user_19")),
        Pair("56. Custom RBAC modular security rules", Screen.RolesPermissionsBuilder.route),
        Pair("57. Subscriptions billing invoices seats", Screen.LicensingSubscription.route),
        Pair("58. Agent Heartbeats configurations setting", Screen.TallySyncAgentSettings.route),
        Pair("59. Audit Logs security compliance list", Screen.AuditLogViewer.route),
        Pair("60. Application Biometric preferences", Screen.AppPreferences.route)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Tally BMS Modules",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Choose any of the 60 screens mapped in the Blueprint specifications to review details:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Role switcher component helper for security guards testing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RBAC Profile:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("admin", "executive").forEach { role ->
                                FilterChip(
                                    selected = (role == "admin" && userRole == "company_admin") || (role == "executive" && userRole == "sales_executive"),
                                    onClick = {
                                        userRole = if (role == "admin") "company_admin" else "sales_executive"
                                    },
                                    label = { Text(role.capitalize()) },
                                    modifier = Modifier.testTag("rbac_toggle_$role")
                                )
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Lazy list of all 60 screens
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(screensList) { (title, route) ->
                            val isAllowed = NavGuards.isAuthorized(userRole, route)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isAllowed) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (isAllowed) {
                                            currentTitle = title
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                            scope.launch { drawerState.close() }
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAllowed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_open_button")
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu Navigation Open")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    // Five core enterprise cockpit options
                    val navItems = listOf(
                        Triple("Dashboard", Screen.HomeDashboard.route, Icons.Default.Home),
                        Triple("Accounting", Screen.LedgerList.route, Icons.Default.List),
                        Triple("Outstanding", Screen.OutstandingList.route, Icons.Default.ShoppingCart),
                        Triple("Sales Team", Screen.TeamOverview.route, Icons.Default.Person),
                        Triple("Quotation", Screen.QuotationDashboard.route, Icons.Default.Star)
                    )

                    navItems.forEach { (label, route, icon) ->
                        val isAllowed = NavGuards.isAuthorized(userRole, route)
                        NavigationBarItem(
                            selected = false,
                            onClick = {
                                if (isAllowed) {
                                    currentTitle = label
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
                            enabled = isAllowed,
                            modifier = Modifier.testTag("nav_tab_${label.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            TallyBmsNavHost(
                navController = navController,
                startDestination = NavigationGraph.AUTH_ROOT, // Start at Login -> Company Selection -> Dashboard
                userRole = userRole,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
