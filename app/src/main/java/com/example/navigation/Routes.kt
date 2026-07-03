package com.example.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

/**
 * Navigation destination structures for all 60 screens mapped in the Tally BMS Product Specification.
 */
object NavigationGraph {
    const val AUTH_ROOT = "auth_graph"
    const val MAIN_ROOT = "main_graph"
    const val ACCOUNTING_ROOT = "accounting_graph"
    const val INVENTORY_ROOT = "inventory_graph"
    const val OUTSTANDING_ROOT = "outstanding_graph"
    const val SALES_TEAM_ROOT = "sales_team_graph"
    const val CRM_ROOT = "crm_graph"
    const val REPORTS_ROOT = "reports_graph"
    const val APPROVALS_ROOT = "approvals_graph"
    const val NOTIFICATIONS_ROOT = "notifications_graph"
    const val SETTINGS_ROOT = "settings_graph"
}

/**
 * Screen Route specifications with deep-link mappings, expected routes, and argument converters.
 */
sealed class Screen(val route: String) {

    // === Auth & Onboarding ===
    object Login : Screen("auth/login")
    object ForgotPassword : Screen("auth/forgot")
    object TenantCompanySwitcher : Screen("auth/switcher")

    // === Home Dashboard ===
    object HomeDashboard : Screen("main/home")

    // === Accounting Module ===
    object LedgerList : Screen("accounting/ledgers")
    object LedgerDetail : Screen("accounting/ledgers/{ledgerId}") {
        fun createRoute(ledgerId: String) = "accounting/ledgers/$ledgerId"
        val arguments = listOf(navArgument("ledgerId") { type = NavType.StringType })
    }
    object CreateEditLedger : Screen("accounting/ledgers/create?ledgerId={ledgerId}") {
        fun createRoute(ledgerId: String? = null) = "accounting/ledgers/create" + if (ledgerId != null) "?ledgerId=$ledgerId" else ""
        val arguments = listOf(navArgument("ledgerId") { type = NavType.StringType; nullable = true; defaultValue = null })
    }
    object VoucherList : Screen("accounting/vouchers")
    object CreateEditVoucher : Screen("accounting/vouchers/create?voucherId={voucherId}") {
        fun createRoute(voucherId: String? = null) = "accounting/vouchers/create" + if (voucherId != null) "?voucherId=$voucherId" else ""
        val arguments = listOf(navArgument("voucherId") { type = NavType.StringType; nullable = true })
    }
    object VoucherDetail : Screen("accounting/vouchers/{voucherId}") {
        fun createRoute(voucherId: String) = "accounting/vouchers/$voucherId"
        val arguments = listOf(navArgument("voucherId") { type = NavType.StringType })
    }
    object BillList : Screen("accounting/bills")
    object BillDetail : Screen("accounting/bills/{billId}") {
        fun createRoute(billId: String) = "accounting/bills/$billId"
        val arguments = listOf(navArgument("billId") { type = NavType.StringType })
    }
    object OrderBook : Screen("accounting/orders")
    object CreateEditOrder : Screen("accounting/orders/create?orderId={orderId}") {
        fun createRoute(orderId: String? = null) = "accounting/orders/create" + if (orderId != null) "?orderId=$orderId" else ""
        val arguments = listOf(navArgument("orderId") { type = NavType.StringType; nullable = true })
    }
    object SyncStatus : Screen("accounting/sync")
    object ConflictResolution : Screen("accounting/sync/conflicts")
    object TransactionExplorer : Screen("accounting/transactions")
    object FinancialSummary : Screen("accounting/financials")

    // === Inventory Module ===
    object StockItemList : Screen("inventory/items")
    object StockItemDetail : Screen("inventory/items/{itemId}") {
        fun createRoute(itemId: String) = "inventory/items/$itemId"
        val arguments = listOf(navArgument("itemId") { type = NavType.StringType })
    }
    object StockGroups : Screen("inventory/groups")
    object GodownLocationView : Screen("inventory/godowns")
    object LowStockAlerts : Screen("inventory/low-stock")
    object StockTransfer : Screen("inventory/transfer")
    object StockTransactions : Screen("inventory/transactions")

    // === Outstanding & Recovery Module ===
    object OutstandingDashboard : Screen("outstanding/dashboard")
    object OutstandingList : Screen("outstanding/balances")
    object PartyOutstandingDetail : Screen("outstanding/party/{partyId}") {
        fun createRoute(partyId: String) = "outstanding/party/$partyId"
        val arguments = listOf(navArgument("partyId") { type = NavType.StringType })
    }
    object AgingReport : Screen("outstanding/aging")
    object RecoveryPipeline : Screen("outstanding/pipeline")
    object ReminderDashboard : Screen("outstanding/reminders/dashboard")
    object ReminderTemplates : Screen("outstanding/reminders/templates")
    object AutoReminderRules : Screen("outstanding/reminders/rules")
    object ReminderScheduler : Screen("outstanding/reminders/scheduler")
    object ManualReminderComposer : Screen("outstanding/reminders/compose?partyId={partyId}&billId={billId}") {
        fun createRoute(partyId: String, billId: String? = null) = 
            "outstanding/reminders/compose?partyId=$partyId" + if (billId != null) "&billId=$billId" else ""
        val arguments = listOf(
            navArgument("partyId") { type = NavType.StringType },
            navArgument("billId") { type = NavType.StringType; nullable = true }
        )
    }
    object ReminderHistoryLog : Screen("outstanding/reminders/history")

    // === Sales Team Module ===
    object TeamOverview : Screen("sales/overview")
    object RepProfileTargets : Screen("sales/rep/{userId}") {
        fun createRoute(userId: String) = "sales/rep/$userId"
        val arguments = listOf(navArgument("userId") { type = NavType.StringType })
    }
    object CheckInCheckOut : Screen("sales/checkin")
    object CheckInHistory : Screen("sales/checkin/history")
    object FollowUpList : Screen("sales/followups")
    object CreateEditFollowUp : Screen("sales/followups/create?id={id}") {
        fun createRoute(id: String? = null) = "sales/followups/create" + if (id != null) "?id=$id" else ""
        val arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
    }
    object FollowUpDetailOutcome : Screen("sales/followups/{id}") {
        fun createRoute(id: String) = "sales/followups/$id"
        val arguments = listOf(navArgument("id") { type = NavType.StringType })
    }
    object TerritoryBeatManagement : Screen("sales/beats")

    // === CRM (Customer 360) Module ===
    object CustomerListSearch : Screen("crm/customers")
    object CustomerProfile360 : Screen("crm/customers/{customerId}") {
        fun createRoute(customerId: String) = "crm/customers/$customerId"
        val arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        val deepLinks = listOf(
            navDeepLink { uriPattern = "https://tallybms.com/crm/customers/{customerId}" }
        )
    }
    object CustomerTimeline : Screen("crm/customers/{customerId}/timeline") {
        fun createRoute(customerId: String) = "crm/customers/$customerId/timeline"
        val arguments = listOf(navArgument("customerId") { type = NavType.StringType })
    }

    // === Reports Module ===
    object MonthlySummaryReport : Screen("reports/monthly")
    object SalesPerformanceReport : Screen("reports/performance")
    object CheckInComplianceReport : Screen("reports/compliance")
    object CustomReportBuilder : Screen("reports/builder")
    object ReportViewer : Screen("reports/viewer/{reportId}") {
        fun createRoute(reportId: String) = "reports/viewer/$reportId"
        val arguments = listOf(navArgument("reportId") { type = NavType.StringType })
    }
    object ExportCenter : Screen("reports/exports")

    // === Approvals Module ===
    object PendingApprovals : Screen("approvals/pending")
    object ApprovalDetailAction : Screen("approvals/{approvalId}") {
        fun createRoute(approvalId: String) = "approvals/$approvalId"
        val arguments = listOf(navArgument("approvalId") { type = NavType.StringType })
    }

    // === Notifications Module ===
    object NotificationInbox : Screen("notifications/inbox")
    object NotificationPreferences : Screen("notifications/prefs")

    // === Settings Module ===
    object CompanyTenantProfile : Screen("settings/profile")
    object MultiCompanyManagement : Screen("settings/companies")
    object UserList : Screen("settings/users")
    object InviteUser : Screen("settings/users/invite")
    object UserDetailEditRole : Screen("settings/users/{userId}") {
        fun createRoute(userId: String) = "settings/users/$userId"
        val arguments = listOf(navArgument("userId") { type = NavType.StringType })
    }
    object RolesPermissionsBuilder : Screen("settings/roles")
    object LicensingSubscription : Screen("settings/license")
    object TallySyncAgentSettings : Screen("settings/sync")
    object AuditLogViewer : Screen("settings/audit")
    object AppPreferences : Screen("settings/preferences")

    // === Quotation Module ===
    object QuotationDashboard : Screen("quots/dashboard")
    object QuotationList : Screen("quots/list")
    object CreateEditQuotation : Screen("quots/create?quotationId={quotationId}") {
        fun createRoute(quotationId: String? = null) = "quots/create" + if (quotationId != null) "?quotationId=$quotationId" else ""
        val arguments = listOf(navArgument("quotationId") { type = NavType.StringType; nullable = true })
    }
    object QuotationDetail : Screen("quots/detail/{quotationId}") {
        fun createRoute(quotationId: String) = "quots/detail/$quotationId"
        val arguments = listOf(navArgument("quotationId") { type = NavType.StringType })
    }
    object QuotationApproval : Screen("quots/approval/{quotationId}") {
        fun createRoute(quotationId: String) = "quots/approval/$quotationId"
        val arguments = listOf(navArgument("quotationId") { type = NavType.StringType })
    }
    object QuotationAnalyticsView : Screen("quots/analytics")
}
