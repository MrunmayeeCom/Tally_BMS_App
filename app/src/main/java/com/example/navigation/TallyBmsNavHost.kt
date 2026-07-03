package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.feature.auth.presentation.LoginScreen
import com.example.feature.auth.presentation.ForgotPasswordScreen
import com.example.feature.auth.presentation.TenantCompanySwitcherScreen
import com.example.feature.dashboard.presentation.HomeDashboardScreen
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.TallyBMSApp
import com.example.core.common.ViewModelFactory
import com.example.feature.crm.presentation.CustomerListScreen
import com.example.feature.crm.presentation.CustomerListViewModel
import com.example.feature.crm.presentation.CustomerDetailScreen
import com.example.feature.crm.presentation.CustomerDetailViewModel
import com.example.feature.crm.presentation.CustomerTimelineScreen
import com.example.feature.crm.presentation.CustomerTimelineViewModel
import com.example.feature.outstanding.presentation.OutstandingDashboardScreen
import com.example.feature.outstanding.presentation.OutstandingDashboardViewModel
import com.example.feature.outstanding.presentation.OutstandingListScreen
import com.example.feature.outstanding.presentation.OutstandingListViewModel
import com.example.feature.outstanding.presentation.OutstandingDetailScreen
import com.example.feature.outstanding.presentation.OutstandingDetailViewModel
import com.example.feature.outstanding.presentation.AgingAnalysisScreen
import com.example.feature.outstanding.presentation.AgingAnalysisViewModel
import com.example.feature.outstanding.presentation.RecoveryTrackingScreen
import com.example.feature.outstanding.presentation.RecoveryTrackingViewModel
import com.example.feature.reminder.presentation.*
import com.example.feature.followup.presentation.*
import com.example.feature.salesteam.presentation.*
import com.example.feature.reports.presentation.*
import com.example.feature.accounting.presentation.*
import com.example.feature.inventory.presentation.*
import com.example.feature.territory.presentation.TerritoryViewModel
import com.example.feature.territory.presentation.TerritoryBeatManagementScreen
import com.example.feature.sync.presentation.SyncDashboardScreen
import com.example.feature.sync.presentation.ConflictResolutionLogScreen
import com.example.feature.sync.presentation.SyncViewModel
import com.example.feature.settings.presentation.*
import com.example.feature.quotation.presentation.*
import com.example.feature.approval.presentation.*
import com.example.feature.security.presentation.SecurityViewModel
import com.example.feature.security.presentation.SecurityPermissionsParentScreen

@Composable
fun TallyBmsNavHost(
    navController: NavHostController,
    startDestination: String = NavigationGraph.AUTH_ROOT,
    userRole: String? = "company_admin",
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ==========================================
        // 1. AUTHENTICATION & ONBOARDING SUB-GRAPH
        // ==========================================
        navigation(
            startDestination = Screen.Login.route,
            route = NavigationGraph.AUTH_ROOT
        ) {
            composable(route = Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.TenantCompanySwitcher.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            composable(route = Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.TenantCompanySwitcher.route) {
                TenantCompanySwitcherScreen(
                    onCompanySelected = {
                        navController.navigate(NavigationGraph.MAIN_ROOT) {
                            popUpTo(NavigationGraph.AUTH_ROOT) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ==========================================
        // 2. MAIN COCKPIT GRAPH (HOME & CORE FEATURES)
        // ==========================================
        navigation(
            startDestination = Screen.HomeDashboard.route,
            route = NavigationGraph.MAIN_ROOT
        ) {
            composable(route = Screen.HomeDashboard.route) {
                HomeDashboardScreen(
                    onNavigateToLedgers = { navController.navigate(Screen.LedgerList.route) },
                    onNavigateToVouchers = { navController.navigate(Screen.VoucherList.route) },
                    onNavigateToBills = { navController.navigate(Screen.BillList.route) },
                    onNavigateToOutstanding = { navController.navigate(Screen.OutstandingList.route) },
                    onNavigateToFollowUps = { navController.navigate(Screen.FollowUpList.route) },
                    onNavigateToSync = { navController.navigate(Screen.SyncStatus.route) },
                    onNavigateToApprovals = { navController.navigate(Screen.PendingApprovals.route) },
                    onNavigateToOrders = { navController.navigate(Screen.OrderBook.route) },
                    onNavigateToInventory = { navController.navigate(Screen.StockItemList.route) }
                )
            }
        }

        // ==========================================
        // 3. ACCOUNTING MODULE SUB-GRAPH
        // ==========================================
        // ==========================================
        // 3. ACCOUNTING MODULE SUB-GRAPH
        // ==========================================
        navigation(
            startDestination = Screen.LedgerList.route,
            route = NavigationGraph.ACCOUNTING_ROOT
        ) {
            composable(route = Screen.LedgerList.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: LedgerViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        LedgerViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                LedgerListScreen(
                    viewModel = vm,
                    onNavigateToDetail = { ledgerId ->
                        navController.navigate(Screen.LedgerDetail.createRoute(ledgerId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.LedgerDetail.route,
                arguments = Screen.LedgerDetail.arguments
            ) { backStackEntry ->
                val ledgerId = backStackEntry.arguments?.getString("ledgerId") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: LedgerViewModel = viewModel(
                    key = "ledger_detail_$ledgerId",
                    factory = ViewModelFactory(context.container) { container ->
                        LedgerViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                LedgerDetailScreen(
                    ledgerId = ledgerId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CreateEditLedger.route,
                arguments = Screen.CreateEditLedger.arguments
            ) {
                // Redirect/Reuse creation dialog on LedgerList Screen
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: LedgerViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        LedgerViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                LedgerListScreen(
                    viewModel = vm,
                    onNavigateToDetail = { ledgerId ->
                        navController.navigate(Screen.LedgerDetail.createRoute(ledgerId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.VoucherList.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: VoucherViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        VoucherViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                VoucherListScreen(
                    viewModel = vm,
                    onNavigateToDetail = { voucherId ->
                        navController.navigate(Screen.VoucherDetail.createRoute(voucherId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CreateEditVoucher.route,
                arguments = Screen.CreateEditVoucher.arguments
            ) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: VoucherViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        VoucherViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                VoucherListScreen(
                    viewModel = vm,
                    onNavigateToDetail = { voucherId ->
                        navController.navigate(Screen.VoucherDetail.createRoute(voucherId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.VoucherDetail.route,
                arguments = Screen.VoucherDetail.arguments
            ) { backStackEntry ->
                val voucherId = backStackEntry.arguments?.getString("voucherId") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: VoucherViewModel = viewModel(
                    key = "voucher_detail_$voucherId",
                    factory = ViewModelFactory(context.container) { container ->
                        VoucherViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                VoucherDetailScreen(
                    voucherId = voucherId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.BillList.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: BillViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        BillViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                BillListScreen(
                    viewModel = vm,
                    onNavigateToDetail = { billId ->
                        navController.navigate(Screen.BillDetail.createRoute(billId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.BillDetail.route,
                arguments = Screen.BillDetail.arguments
            ) { backStackEntry ->
                val billId = backStackEntry.arguments?.getString("billId") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: BillViewModel = viewModel(
                    key = "bill_detail_$billId",
                    factory = ViewModelFactory(context.container) { container ->
                        BillViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                BillDetailScreen(
                    billId = billId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.TransactionExplorer.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: TransactionExplorerViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        TransactionExplorerViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                TransactionExplorerScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.FinancialSummary.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: FinancialSummaryViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        FinancialSummaryViewModel(container.accountingRepository, container.sessionManager)
                    }
                )
                FinancialSummaryScreen(
                    viewModel = vm,
                    onNavigateToLedger = { ledgerId ->
                        navController.navigate(Screen.LedgerDetail.createRoute(ledgerId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.OrderBook.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val orderVm: OrderViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        OrderViewModel(container.orderRepository)
                    }
                )
                val companyIdState = context.container.sessionManager.companyId.collectAsState(initial = "comp_01")
                OrderBookScreen(
                    orderViewModel = orderVm,
                    companyId = companyIdState.value ?: "comp_01",
                    onNavigateToCreate = {
                        navController.navigate(Screen.CreateEditOrder.createRoute())
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CreateEditOrder.route,
                arguments = Screen.CreateEditOrder.arguments
            ) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val orderVm: OrderViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        OrderViewModel(container.orderRepository)
                    }
                )
                val companyIdState = context.container.sessionManager.companyId.collectAsState(initial = "comp_01")
                CreateEditOrderScreen(
                    orderViewModel = orderVm,
                    crmRepository = context.container.crmRepository,
                    companyId = companyIdState.value ?: "comp_01",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.SyncStatus.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val syncVm: SyncViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        SyncViewModel(container.syncRepository)
                    }
                )
                SyncDashboardScreen(
                    viewModel = syncVm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToConflicts = { navController.navigate(Screen.ConflictResolution.route) }
                )
            }

            composable(route = Screen.ConflictResolution.route) {
                ConflictResolutionLogScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // ==========================================
        // 4. INVENTORY MODULE SUB-GRAPH
        // ==========================================
        navigation(
            startDestination = Screen.StockItemList.route,
            route = NavigationGraph.INVENTORY_ROOT
        ) {
            composable(route = Screen.StockItemList.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.INVENTORY_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: InventoryViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) { container ->
                        InventoryViewModel(container.inventoryRepository, container.sessionManager)
                    }
                )
                ItemMasterScreen(
                    viewModel = vm,
                    companyId = "company_tally_bms_sandbox",
                    onNavigateToDetail = { itemId ->
                        navController.navigate(Screen.StockItemDetail.createRoute(itemId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.StockItemDetail.route,
                arguments = Screen.StockItemDetail.arguments
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.INVENTORY_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: InventoryViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) { container ->
                        InventoryViewModel(container.inventoryRepository, container.sessionManager)
                    }
                )
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                ItemDetailScreen(
                    viewModel = vm,
                    itemId = itemId,
                    companyId = "company_tally_bms_sandbox",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.StockGroups.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.INVENTORY_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: InventoryViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) { container ->
                        InventoryViewModel(container.inventoryRepository, container.sessionManager)
                    }
                )
                StockSummaryScreen(
                    viewModel = vm,
                    companyId = "company_tally_bms_sandbox",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.GodownLocationView.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.INVENTORY_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: InventoryViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) { container ->
                        InventoryViewModel(container.inventoryRepository, container.sessionManager)
                    }
                )
                WarehouseScreen(
                    viewModel = vm,
                    companyId = "company_tally_bms_sandbox",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.LowStockAlerts.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.INVENTORY_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: InventoryViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) { container ->
                        InventoryViewModel(container.inventoryRepository, container.sessionManager)
                    }
                )
                InventoryReportsScreen(
                    viewModel = vm,
                    companyId = "company_tally_bms_sandbox",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.StockTransfer.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.INVENTORY_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: InventoryViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) { container ->
                        InventoryViewModel(container.inventoryRepository, container.sessionManager)
                    }
                )
                StockTransferScreen(
                    viewModel = vm,
                    companyId = "company_tally_bms_sandbox",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.StockTransactions.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.INVENTORY_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: InventoryViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) { container ->
                        InventoryViewModel(container.inventoryRepository, container.sessionManager)
                    }
                )
                InventoryTransactionsScreen(
                    viewModel = vm,
                    companyId = "company_tally_bms_sandbox",
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // ==========================================
        // 5. OUTSTANDING & RECOVERY SUB-GRAPH
        // ==========================================
        navigation(
            startDestination = Screen.OutstandingDashboard.route,
            route = NavigationGraph.OUTSTANDING_ROOT
        ) {
            composable(route = Screen.OutstandingDashboard.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: OutstandingDashboardViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        OutstandingDashboardViewModel(container.outstandingRepository, container.sessionManager)
                    }
                )
                OutstandingDashboardScreen(
                    viewModel = vm,
                    onNavigateToList = {
                        navController.navigate(Screen.OutstandingList.route)
                    },
                    onNavigateToAging = {
                        navController.navigate(Screen.AgingReport.route)
                    },
                    onNavigateToPipeline = {
                        navController.navigate(Screen.RecoveryPipeline.route)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.OutstandingList.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: OutstandingListViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        OutstandingListViewModel(container.outstandingRepository, container.sessionManager)
                    }
                )
                OutstandingListScreen(
                    viewModel = vm,
                    onNavigateToDetail = { partyId ->
                        navController.navigate(Screen.PartyOutstandingDetail.createRoute(partyId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.PartyOutstandingDetail.route,
                arguments = Screen.PartyOutstandingDetail.arguments
            ) { backStackEntry ->
                val partyId = backStackEntry.arguments?.getString("partyId") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: OutstandingDetailViewModel = viewModel(
                    key = "outstanding_detail_$partyId",
                    factory = ViewModelFactory(context.container) { container ->
                        OutstandingDetailViewModel(container.outstandingRepository, partyId)
                    }
                )
                OutstandingDetailScreen(
                    viewModel = vm,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.AgingReport.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: AgingAnalysisViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        AgingAnalysisViewModel(container.outstandingRepository)
                    }
                )
                AgingAnalysisScreen(
                    viewModel = vm,
                    onNavigateToDetail = { partyId ->
                        navController.navigate(Screen.PartyOutstandingDetail.createRoute(partyId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.RecoveryPipeline.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: RecoveryTrackingViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        RecoveryTrackingViewModel(container.outstandingRepository)
                    }
                )
                RecoveryTrackingScreen(
                    viewModel = vm,
                    onNavigateToDetail = { partyId ->
                        navController.navigate(Screen.PartyOutstandingDetail.createRoute(partyId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.ReminderTemplates.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReminderTemplatesViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReminderTemplatesViewModel(container.reminderRepository)
                    }
                )
                ReminderTemplatesScreen(
                    viewModel = vm,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.AutoReminderRules.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: AutoReminderRulesViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        AutoReminderRulesViewModel(container.reminderRepository)
                    }
                )
                AutoReminderRulesScreen(
                    viewModel = vm,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.ManualReminderComposer.route,
                arguments = Screen.ManualReminderComposer.arguments
            ) { backStackEntry ->
                val partyId = backStackEntry.arguments?.getString("partyId") ?: ""
                val billId = backStackEntry.arguments?.getString("billId")
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ManualReminderViewModel = viewModel(
                    key = "manual_composer_${partyId}_${billId}",
                    factory = ViewModelFactory(context.container) { container ->
                        ManualReminderViewModel(
                            reminderRepo = container.reminderRepository,
                            outstandingRepo = container.outstandingRepository,
                            crmRepo = container.crmRepository,
                            sessionManager = container.sessionManager,
                            initialPartyId = partyId,
                            initialBillId = billId
                        )
                    }
                )
                ManualReminderScreen(
                    viewModel = vm,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.ReminderHistoryLog.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReminderHistoryViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReminderHistoryViewModel(container.reminderRepository, container.sessionManager)
                    }
                )
                ReminderHistoryScreen(
                    viewModel = vm,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.ReminderDashboard.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReminderDashboardViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReminderDashboardViewModel(container.reminderRepository, container.sessionManager)
                    }
                )
                ReminderDashboardScreen(
                    viewModel = vm,
                    onNavigateToManual = {
                        navController.navigate(Screen.ManualReminderComposer.createRoute("", ""))
                    },
                    onNavigateToRules = {
                        navController.navigate(Screen.AutoReminderRules.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.ReminderHistoryLog.route)
                    },
                    onNavigateToTemplates = {
                        navController.navigate(Screen.ReminderTemplates.route)
                    },
                    onNavigateToScheduler = {
                        navController.navigate(Screen.ReminderScheduler.route)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.ReminderScheduler.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReminderSchedulerViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReminderSchedulerViewModel(container.reminderRepository)
                    }
                )
                ReminderSchedulerScreen(
                    viewModel = vm,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==========================================
        // 6. SALES TEAM MODULE SUB-GRAPH
        // ==========================================
        navigation(
            startDestination = Screen.TeamOverview.route,
            route = NavigationGraph.SALES_TEAM_ROOT
        ) {
            composable(route = Screen.TeamOverview.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: SalesTeamDashboardViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        SalesTeamDashboardViewModel(container.salesTeamRepository, container.sessionManager)
                    }
                )
                SalesTeamDashboardScreen(
                    viewModel = vm,
                    onNavigateToManageUsers = { navController.navigate("sales/users") },
                    onNavigateToUserDetail = { userId -> navController.navigate(Screen.RepProfileTargets.createRoute(userId)) },
                    onNavigateToCheckIn = { navController.navigate(Screen.CheckInCheckOut.route) },
                    onNavigateToVisits = { navController.navigate("sales/visits") },
                    onNavigateToPerformance = { navController.navigate("sales/performance") },
                    onNavigateToActivityFeed = { navController.navigate(Screen.CheckInHistory.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.RepProfileTargets.route,
                arguments = Screen.RepProfileTargets.arguments
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: UserDetailViewModel = viewModel(
                    key = userId,
                    factory = ViewModelFactory(context.container) { container ->
                        UserDetailViewModel(container.salesTeamRepository, userId)
                    }
                )
                UserDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.CheckInCheckOut.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: CheckInCheckOutViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        CheckInCheckOutViewModel(container.salesTeamRepository, container.crmRepository, container.sessionManager)
                    }
                )
                CheckInCheckOutScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.CheckInHistory.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ActivityFeedViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ActivityFeedViewModel(container.salesTeamRepository, container.sessionManager)
                    }
                )
                ActivityFeedScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = "sales/users") {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ManageUsersViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ManageUsersViewModel(container.salesTeamRepository, container.sessionManager)
                    }
                )
                ManageUsersScreen(
                    viewModel = vm,
                    onNavigateToUserDetail = { userId -> navController.navigate(Screen.RepProfileTargets.createRoute(userId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = "sales/visits") {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: CustomerVisitViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        CustomerVisitViewModel(container.salesTeamRepository, container.crmRepository, container.sessionManager)
                    }
                )
                CustomerVisitScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = "sales/performance") {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: PerformanceDashboardViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        PerformanceDashboardViewModel(container.salesTeamRepository, container.sessionManager)
                    }
                )
                PerformanceDashboardScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.FollowUpList.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: FollowUpDashboardViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        FollowUpDashboardViewModel(container.followUpRepository, container.sessionManager)
                    }
                )
                FollowUpDashboardScreen(
                    viewModel = vm,
                    onNavigateToList = { navController.navigate("sales/followups/list") },
                    onNavigateToCreate = { navController.navigate(Screen.CreateEditFollowUp.createRoute()) },
                    onNavigateToCalendar = { navController.navigate("sales/followups/calendar") },
                    onNavigateToDetail = { id -> navController.navigate(Screen.FollowUpDetailOutcome.createRoute(id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = "sales/followups/list") {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: FollowUpListViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        FollowUpListViewModel(container.followUpRepository)
                    }
                )
                FollowUpListScreen(
                    viewModel = vm,
                    onNavigateToCreate = { navController.navigate(Screen.CreateEditFollowUp.createRoute()) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.FollowUpDetailOutcome.createRoute(id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = "sales/followups/calendar") {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: FollowUpCalendarViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        FollowUpCalendarViewModel(container.followUpRepository)
                    }
                )
                FollowUpCalendarScreen(
                    viewModel = vm,
                    onNavigateToDetail = { id -> navController.navigate(Screen.FollowUpDetailOutcome.createRoute(id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CreateEditFollowUp.route,
                arguments = Screen.CreateEditFollowUp.arguments
            ) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: CreateFollowUpViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        CreateFollowUpViewModel(
                            repository = container.followUpRepository,
                            crmRepository = container.crmRepository,
                            sessionManager = container.sessionManager
                        )
                    }
                )
                FollowUpCreateScreen(
                    viewModel = vm,
                    onSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.FollowUpDetailOutcome.route,
                arguments = Screen.FollowUpDetailOutcome.arguments
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: FollowUpDetailViewModel = viewModel(
                    key = "followup_detail_$id",
                    factory = ViewModelFactory(context.container) { container ->
                        FollowUpDetailViewModel(
                            repository = container.followUpRepository,
                            sessionManager = container.sessionManager,
                            followUpId = id
                        )
                    }
                )
                FollowUpDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.TerritoryBeatManagement.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: TerritoryViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        TerritoryViewModel(
                            repository = container.territoryRepository,
                            sessionManager = container.sessionManager
                        )
                    }
                )
                val compState = context.container.sessionManager.companyId.collectAsState(initial = "comp_01")
                TerritoryBeatManagementScreen(
                    viewModel = vm,
                    crmRepository = context.container.crmRepository,
                    salesTeamRepository = context.container.salesTeamRepository,
                    currentCompanyId = compState.value ?: "comp_01",
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // ==========================================
        // 7. CRM MODULE SUB-GRAPH (CUSTOMER 360)
        // ==========================================
        navigation(
            startDestination = Screen.CustomerListSearch.route,
            route = NavigationGraph.CRM_ROOT
        ) {
            composable(route = Screen.CustomerListSearch.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: CustomerListViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        CustomerListViewModel(container.crmRepository, container.sessionManager)
                    }
                )
                CustomerListScreen(
                    viewModel = vm,
                    onNavigateToDetail = { customerId ->
                        navController.navigate(Screen.CustomerProfile360.createRoute(customerId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.CustomerProfile360.route,
                arguments = Screen.CustomerProfile360.arguments,
                deepLinks = Screen.CustomerProfile360.deepLinks
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: CustomerDetailViewModel = viewModel(
                    key = "detail_$customerId",
                    factory = ViewModelFactory(context.container) { container ->
                        CustomerDetailViewModel(container.crmRepository, customerId)
                    }
                )
                CustomerDetailScreen(
                    viewModel = vm,
                    onNavigateToTimeline = {
                        navController.navigate(Screen.CustomerTimeline.createRoute(customerId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.CustomerTimeline.route,
                arguments = Screen.CustomerTimeline.arguments
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: CustomerTimelineViewModel = viewModel(
                    key = "timeline_$customerId",
                    factory = ViewModelFactory(context.container) { container ->
                        CustomerTimelineViewModel(container.crmRepository, customerId)
                    }
                )
                CustomerTimelineScreen(
                    viewModel = vm,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==========================================
        // 8. REPORTS MODULE SUB-GRAPH
        // ==========================================
        navigation(
            startDestination = Screen.MonthlySummaryReport.route,
            route = NavigationGraph.REPORTS_ROOT
        ) {
            composable(route = Screen.MonthlySummaryReport.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReportsViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReportsViewModel(container.reportsRepository, container.sessionManager)
                    }
                )
                ReportsHubScreen(
                    viewModel = vm,
                    initialScreen = "dashboard",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOutstanding = { navController.navigate("reports/outstanding") },
                    onNavigateToReminders = { navController.navigate("reports/reminder-analytics") },
                    onNavigateToFollowUps = { navController.navigate(Screen.CheckInComplianceReport.route) },
                    onNavigateToPerformance = { navController.navigate(Screen.SalesPerformanceReport.route) },
                    onNavigateToBuilder = { navController.navigate(Screen.CustomReportBuilder.route) },
                    onNavigateToExports = { navController.navigate(Screen.ExportCenter.route) }
                )
            }

            composable(route = "reports/outstanding") {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReportsViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReportsViewModel(container.reportsRepository, container.sessionManager)
                    }
                )
                ReportsHubScreen(
                    viewModel = vm,
                    initialScreen = "outstanding",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBuilder = { navController.navigate(Screen.CustomReportBuilder.route) }
                )
            }

            composable(route = "reports/reminder-analytics") {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReportsViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReportsViewModel(container.reportsRepository, container.sessionManager)
                    }
                )
                ReportsHubScreen(
                    viewModel = vm,
                    initialScreen = "reminders",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.SalesPerformanceReport.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReportsViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReportsViewModel(container.reportsRepository, container.sessionManager)
                    }
                )
                ReportsHubScreen(
                    viewModel = vm,
                    initialScreen = "performance",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.CheckInComplianceReport.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReportsViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReportsViewModel(container.reportsRepository, container.sessionManager)
                    }
                )
                ReportsHubScreen(
                    viewModel = vm,
                    initialScreen = "followups",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.CustomReportBuilder.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReportsViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReportsViewModel(container.reportsRepository, container.sessionManager)
                    }
                )
                ReportsHubScreen(
                    viewModel = vm,
                    initialScreen = "builder",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ReportViewer.route,
                arguments = Screen.ReportViewer.arguments
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: "dashboard"
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReportsViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReportsViewModel(container.reportsRepository, container.sessionManager)
                    }
                )
                ReportsHubScreen(
                    viewModel = vm,
                    initialScreen = reportId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.ExportCenter.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ReportsViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ReportsViewModel(container.reportsRepository, container.sessionManager)
                    }
                )
                ReportsHubScreen(
                    viewModel = vm,
                    initialScreen = "exports",
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // ==========================================
        // 9. APPROVALS MODULE SUB-GRAPH
        // ==========================================
        navigation(
            startDestination = Screen.PendingApprovals.route,
            route = NavigationGraph.APPROVALS_ROOT
        ) {
            composable(route = Screen.PendingApprovals.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ApprovalViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ApprovalViewModel(
                            repository = container.approvalRepository,
                            sessionManager = container.sessionManager
                        )
                    }
                )
                ApprovalDashboardScreen(
                    viewModel = vm,
                    onNavigateToDetail = { approvalId ->
                        navController.navigate(Screen.ApprovalDetailAction.createRoute(approvalId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ApprovalDetailAction.route,
                arguments = Screen.ApprovalDetailAction.arguments
            ) { backStackEntry ->
                val approvalId = backStackEntry.arguments?.getString("approvalId") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: ApprovalViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        ApprovalViewModel(
                            repository = container.approvalRepository,
                            sessionManager = container.sessionManager
                        )
                    }
                )
                ApprovalDetailScreen(
                    approvalId = approvalId,
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // ==========================================
        // 10. NOTIFICATIONS MODULE SUB-GRAPH
        // ==========================================
        navigation(
            startDestination = Screen.NotificationInbox.route,
            route = NavigationGraph.NOTIFICATIONS_ROOT
        ) {
            composable(route = Screen.NotificationInbox.route) {
                ScreenPlaceholder(
                    title = "49. Notification Inbox Screen",
                    purpose = "Active terminal feed for sync logs, approvals, and reminders.",
                    fields = listOf("Inbox Alerts chronologic column card view", "Selection Filter Category menu options bar"),
                    actions = listOf("Flag alert as read", "Redirect navigation instantly to Alert source origin detail location", "Flush inbox state clean"),
                    apis = listOf("GET /api/v1/notifications", "PUT /api/v1/notifications/{id}/read"),
                    dbCollections = listOf("notifications"),
                    permissions = "All authenticated roles (Inbox results are pre-filtered based on credentials)"
                )
            }

            composable(route = Screen.NotificationPreferences.route) {
                ScreenPlaceholder(
                    title = "50. Notification Preferences Screen",
                    purpose = "Turn system push signals or messaging channels on/off.",
                    fields = listOf("Transactional Category alerts switcher row checklist", "Dispatch channel toggler row switches"),
                    actions = listOf("Commit altered alert properties to profile data"),
                    apis = listOf("PUT /api/v1/notifications/preferences"),
                    dbCollections = listOf("users"),
                    permissions = "All authenticated roles"
                )
            }
        }

        // ==========================================
        // 10.5. QUOTATIONS MODULE SUB-GRAPH
        // ==========================================
        composable(route = Screen.QuotationDashboard.route) {
            val context = LocalContext.current.applicationContext as TallyBMSApp
            val vm: QuotationViewModel = viewModel(
                factory = ViewModelFactory(context.container) { container ->
                    QuotationViewModel(
                        quotationRepository = container.quotationRepository,
                        crmRepository = container.crmRepository,
                        inventoryRepository = container.inventoryRepository,
                        sessionManager = container.sessionManager
                    )
                }
            )
            QuotationDashboardScreen(
                viewModel = vm,
                onNavigateToList = { navController.navigate(Screen.QuotationList.route) },
                onNavigateToCreate = { id -> navController.navigate(Screen.CreateEditQuotation.createRoute(id)) },
                onNavigateToAnalytics = { navController.navigate(Screen.QuotationAnalyticsView.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.QuotationList.route) {
            val context = LocalContext.current.applicationContext as TallyBMSApp
            val vm: QuotationViewModel = viewModel(
                factory = ViewModelFactory(context.container) { container ->
                    QuotationViewModel(
                        quotationRepository = container.quotationRepository,
                        crmRepository = container.crmRepository,
                        inventoryRepository = container.inventoryRepository,
                        sessionManager = container.sessionManager
                    )
                }
            )
            QuotationListScreen(
                viewModel = vm,
                onNavigateToCreate = { id -> navController.navigate(Screen.CreateEditQuotation.createRoute(id)) },
                onNavigateToDetail = { id -> navController.navigate(Screen.QuotationDetail.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateEditQuotation.route,
            arguments = Screen.CreateEditQuotation.arguments
        ) { backStackEntry ->
            val quotationId = backStackEntry.arguments?.getString("quotationId")
            val context = LocalContext.current.applicationContext as TallyBMSApp
            val vm: QuotationViewModel = viewModel(
                factory = ViewModelFactory(context.container) { container ->
                    QuotationViewModel(
                        quotationRepository = container.quotationRepository,
                        crmRepository = container.crmRepository,
                        inventoryRepository = container.inventoryRepository,
                        sessionManager = container.sessionManager
                    )
                }
            )
            CreateEditQuotationScreen(
                viewModel = vm,
                quotationId = quotationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.QuotationDetail.route,
            arguments = Screen.QuotationDetail.arguments
        ) { backStackEntry ->
            val quotationId = backStackEntry.arguments?.getString("quotationId") ?: ""
            val context = LocalContext.current.applicationContext as TallyBMSApp
            val vm: QuotationViewModel = viewModel(
                factory = ViewModelFactory(context.container) { container ->
                    QuotationViewModel(
                        quotationRepository = container.quotationRepository,
                        crmRepository = container.crmRepository,
                        inventoryRepository = container.inventoryRepository,
                        sessionManager = container.sessionManager
                    )
                }
            )
            QuotationDetailScreen(
                viewModel = vm,
                quotationId = quotationId,
                onNavigateToEdit = { id -> navController.navigate(Screen.CreateEditQuotation.createRoute(id)) },
                onNavigateToApproval = { id -> navController.navigate(Screen.QuotationApproval.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.QuotationApproval.route,
            arguments = Screen.QuotationApproval.arguments
        ) { backStackEntry ->
            val quotationId = backStackEntry.arguments?.getString("quotationId") ?: ""
            val context = LocalContext.current.applicationContext as TallyBMSApp
            val vm: QuotationViewModel = viewModel(
                factory = ViewModelFactory(context.container) { container ->
                    QuotationViewModel(
                        quotationRepository = container.quotationRepository,
                        crmRepository = container.crmRepository,
                        inventoryRepository = container.inventoryRepository,
                        sessionManager = container.sessionManager
                    )
                }
            )
            QuotationApprovalScreen(
                viewModel = vm,
                quotationId = quotationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.QuotationAnalyticsView.route) {
            val context = LocalContext.current.applicationContext as TallyBMSApp
            val vm: QuotationViewModel = viewModel(
                factory = ViewModelFactory(context.container) { container ->
                    QuotationViewModel(
                        quotationRepository = container.quotationRepository,
                        crmRepository = container.crmRepository,
                        inventoryRepository = container.inventoryRepository,
                        sessionManager = container.sessionManager
                    )
                }
            )
            QuotationAnalyticsScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // 11. SETTINGS MODULE SUB-GRAPH (ADMIN & CONFIG)
        // ==========================================
        navigation(
            startDestination = Screen.CompanyTenantProfile.route,
            route = NavigationGraph.SETTINGS_ROOT
        ) {
            composable(route = Screen.CompanyTenantProfile.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.SETTINGS_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: SettingsViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) {
                        SettingsViewModel()
                    }
                )
                CompanyTenantProfileScreen(
                    viewModel = vm,
                    onNavigateToSync = { navController.navigate(Screen.TallySyncAgentSettings.route) },
                    onNavigateToPreferences = { navController.navigate(Screen.AppPreferences.route) },
                    onNavigateToLicense = { navController.navigate(Screen.LicensingSubscription.route) },
                    onNavigateToCompanies = { navController.navigate(Screen.MultiCompanyManagement.route) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.MultiCompanyManagement.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.SETTINGS_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: SettingsViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) {
                        SettingsViewModel()
                    }
                )
                MultiCompanyManagementScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.UserList.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: com.example.feature.user.presentation.UserViewModel = viewModel(
                    factory = com.example.core.common.ViewModelFactory(context.container) { container ->
                        com.example.feature.user.presentation.UserViewModel(
                            userRepository = container.userRepository,
                            securityRepository = container.securityRepository,
                            sessionManager = container.sessionManager
                        )
                    }
                )
                com.example.feature.user.presentation.UserListScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onNavigateToInvite = { navController.navigate(Screen.InviteUser.route) },
                    onNavigateToDetail = { userId ->
                        navController.navigate(Screen.UserDetailEditRole.route.replace("{userId}", userId))
                    }
                )
            }

            composable(route = Screen.InviteUser.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: com.example.feature.user.presentation.UserViewModel = viewModel(
                    factory = com.example.core.common.ViewModelFactory(context.container) { container ->
                        com.example.feature.user.presentation.UserViewModel(
                            userRepository = container.userRepository,
                            securityRepository = container.securityRepository,
                            sessionManager = container.sessionManager
                        )
                    }
                )
                com.example.feature.user.presentation.InviteUserScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.UserDetailEditRole.route,
                arguments = Screen.UserDetailEditRole.arguments
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: com.example.feature.user.presentation.UserViewModel = viewModel(
                    factory = com.example.core.common.ViewModelFactory(context.container) { container ->
                        com.example.feature.user.presentation.UserViewModel(
                            userRepository = container.userRepository,
                            securityRepository = container.securityRepository,
                            sessionManager = container.sessionManager
                        )
                    }
                )
                com.example.feature.user.presentation.UserDetailEditRoleScreen(
                    userId = userId,
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.RolesPermissionsBuilder.route) {
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: SecurityViewModel = viewModel(
                    factory = ViewModelFactory(context.container) { container ->
                        SecurityViewModel(
                            repository = container.securityRepository,
                            sessionManager = container.sessionManager
                        )
                    }
                )
                SecurityPermissionsParentScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.LicensingSubscription.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.SETTINGS_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: SettingsViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) {
                        SettingsViewModel()
                    }
                )
                LicensingSubscriptionScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.TallySyncAgentSettings.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.SETTINGS_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: SettingsViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) {
                        SettingsViewModel()
                    }
                )
                TallySyncAgentSettingsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.AuditLogViewer.route) {
                ScreenPlaceholder(
                    title = "59. Audit Log Viewer Screen",
                    purpose = "Verify system operations, actions, and security compliance registers.",
                    fields = listOf("Organization activity history timeline rows card View", "Selection filtration query menus"),
                    actions = listOf("Request immutable audit log PDF ledger file summaries"),
                    apis = listOf("GET /api/v1/admin/audit-logs"),
                    dbCollections = listOf("audit_logs"),
                    permissions = "Super Admin, Company Admin"
                )
            }

            composable(route = Screen.AppPreferences.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavigationGraph.SETTINGS_ROOT)
                }
                val context = LocalContext.current.applicationContext as TallyBMSApp
                val vm: SettingsViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = ViewModelFactory(context.container) {
                        SettingsViewModel()
                    }
                )
                AppPreferencesScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
