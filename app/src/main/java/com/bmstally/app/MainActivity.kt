package com.bmstally.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bmstally.app.ui.navigation.Routes
import com.bmstally.app.ui.screens.*
import com.bmstally.app.ui.theme.BmsTallyTheme
import com.bmstally.app.viewmodel.LedgerListViewModel
import com.bmstally.app.viewmodel.LedgerDetailViewModel
import com.bmstally.app.viewmodel.AppViewModel
import com.bmstally.app.viewmodel.ItemsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BmsTallyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BmsTallyNavHost()
                }
            }
        }
    }
}

@Composable
fun BmsTallyNavHost() {
    val navController = rememberNavController()
    val appViewModel: AppViewModel = hiltViewModel()
    val appState by appViewModel.state.collectAsState()
    val tenantId = appState.tenantId
    val itemsViewModel: ItemsViewModel = hiltViewModel()

    NavHost(navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            val loginState by appViewModel.loginState.collectAsState()

            LaunchedEffect(loginState) {
                if (loginState is com.bmstally.app.viewmodel.LoginUiState.Success) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                loginState = loginState,
                onLogin = { tenantCode, username, password ->
                    appViewModel.login(tenantCode, username, password)
                },
                onResetState = { appViewModel.resetLoginState() }
            )
        }

        composable(Routes.HOME) {
            LaunchedEffect(appState.tenantId) {
                if (appState.tenantId.isNotEmpty()) {
                    itemsViewModel.setTenantId(appState.tenantId)
                    itemsViewModel.loadItems()
                }
            }
            HomeScreen(
                viewModel = appViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    appViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onCreateEntry = { navController.navigate(Routes.ENTRY_SCREEN) },
                onReminderSheet = { navController.navigate(Routes.MANUAL_REMINDER) }
            )
        }

        composable(Routes.ENTRY_SCREEN) {
            EntryScreen(
                entryTypes = appViewModel.entryTypes,
                onCreateTransaction = { type -> navController.navigate(Routes.createTransaction(type)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CHECK_IN_REPORT) {
            CheckInReportScreen(
                tenantId = tenantId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FOLLOW_UPS) {
            FollowUpsScreen(
                tenantId = tenantId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MANAGE_USERS) {
            ManageUsersScreen(
                tenantId = tenantId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REMINDER_SCHEDULER) {
            ReminderSchedulerScreen(
                tenantId = tenantId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MANUAL_REMINDER) {
            ManualReminderScreen(
                tenantId = tenantId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ITEMS) {
            ItemsScreen(
                viewModel = itemsViewModel,
                onBack = { navController.popBackStack() },
                onCreateItem = { navController.navigate(Routes.CREATE_ITEM) }
            )
        }

        composable(Routes.CREATE_ITEM) {
            CreateItemScreen(
                tenantId = tenantId,
                viewModel = itemsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.TRANSACTION_MODULE,
            arguments = listOf(navArgument("title") { type = NavType.StringType })
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            TransactionModuleScreen(
                tenantId = tenantId,
                title = title,
                onBack = { navController.popBackStack() },
                onCreateNew = { navController.navigate(Routes.createTransaction(title)) }
            )
        }

        composable(Routes.WALLET) {
            WalletScreen(
                tenantId = tenantId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.CREATE_TRANSACTION,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            CreateTransactionScreen(
                tenantId = tenantId,
                type = type,
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }

        composable(
            Routes.REPORT_DETAIL,
            arguments = listOf(navArgument("title") { type = NavType.StringType })
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            ReportDetailScreen(title)
        }

        composable(Routes.ORDER_LIST) {
            OrderListScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VOUCHER_LIST) {
            VoucherListScreen(
                onBack = { navController.popBackStack() },
                onCreateVoucher = { navController.navigate(Routes.VOUCHER_CREATE) }
            )
        }

        composable(Routes.VOUCHER_CREATE) {
            VoucherCreateScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.LEDGER_LIST) {
            LedgerListScreen(
                onBack = { navController.popBackStack() },
                onViewLedger = { ledgerGuid -> navController.navigate(Routes.ledgerDetail(ledgerGuid)) }
            )
        }

        composable(
            Routes.LEDGER_DETAIL,
            arguments = listOf(navArgument("ledgerGuid") { type = NavType.StringType })
        ) { backStackEntry ->
            val ledgerGuid = backStackEntry.arguments?.getString("ledgerGuid") ?: ""
            LedgerDetailScreen(
                ledgerGuid = ledgerGuid,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.COMPANIES) { PlaceholderScreen("Companies", onBack = { navController.popBackStack() }) }
        composable(Routes.USERS) { PlaceholderScreen("Users", onBack = { navController.popBackStack() }) }
        composable(Routes.SETTINGS) { PlaceholderScreen("Settings", onBack = { navController.popBackStack() }) }
        composable(Routes.SUBSCRIPTION) { PlaceholderScreen("Purchase Subscription", onBack = { navController.popBackStack() }) }
        composable(Routes.HELP) { PlaceholderScreen("Help", onBack = { navController.popBackStack() }) }
        composable(Routes.ABOUT) { PlaceholderScreen("About", onBack = { navController.popBackStack() }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(title: String, onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("$title Page", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
