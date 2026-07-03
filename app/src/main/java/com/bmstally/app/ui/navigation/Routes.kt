package com.bmstally.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val REMINDER_SCHEDULER = "reminder_scheduler"
    const val MANUAL_REMINDER = "manual_reminder"
    const val ENTRY_SCREEN = "entry_screen"
    const val TRANSACTION_MODULE = "transaction_module/{title}"
    const val CREATE_TRANSACTION = "create_transaction/{type}"
    const val CHECK_IN_REPORT = "check_in_report"
    const val FOLLOW_UPS = "follow_ups"
    const val MANAGE_USERS = "manage_users"
    const val ITEMS = "items"
    const val CREATE_ITEM = "create_item"
    const val WALLET = "wallet"
    const val REPORT_DETAIL = "report_detail/{title}"

    const val LEDGER_LIST = "ledger_list"
    const val LEDGER_DETAIL = "ledger_detail/{ledgerGuid}"
    const val VOUCHER_LIST = "voucher_list"
    const val VOUCHER_CREATE = "voucher_create"
    const val ORDER_LIST = "order_list"
    const val MONTHLY_SUMMARY = "monthly_summary"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val REQUEST_MESSAGE = "request_message"

    // Drawer pages
    const val COMPANIES = "companies"
    const val USERS = "users"
    const val SETTINGS = "settings"
    const val SUBSCRIPTION = "subscription"
    const val HELP = "help"
    const val ABOUT = "about"

    fun transactionModule(title: String) = "transaction_module/$title"
    fun createTransaction(type: String) = "create_transaction/$type"
    fun reportDetail(title: String) = "report_detail/$title"
    fun ledgerDetail(ledgerGuid: String) = "ledger_detail/$ledgerGuid"
}