package com.example.navigation

import android.util.Log

/**
 * Access levels mapped from Section 6 of the Tally BMS Product Specification.
 */
enum class UserRole {
    SUPER_ADMIN,
    COMPANY_ADMIN,
    ACCOUNTANT,
    SALES_MANAGER,
    SALES_EXECUTIVE,
    VIEWER
}

/**
 * Enterprise Navigation Guard and RBAC enforcement layer.
 */
object NavGuards {

    /**
     * Inspects target route and user roles to enforce access before composition.
     */
    fun isAuthorized(role: String?, route: String): Boolean {
        if (role == null) return false
        val userRole = runCatching { UserRole.valueOf(role.uppercase()) }.getOrElse { UserRole.VIEWER }
        
        Log.d("TallyBMSAuth", "Authorizing route: $route for userRole: $userRole")

        return when {
            // Super Admin and Company Admin can go anywhere
            userRole == UserRole.SUPER_ADMIN || userRole == UserRole.COMPANY_ADMIN -> true

            // Accounting Route constraints
            route.startsWith("accounting/ledgers") -> {
                // Accountant and Sales Manager can view. Sales Exec cannot view arbitrary ledger statements.
                userRole == UserRole.ACCOUNTANT || userRole == UserRole.SALES_MANAGER || userRole == UserRole.VIEWER
            }
            route.contains("ledgers/create") -> {
                // Only Accountant can edit/create ledgers
                userRole == UserRole.ACCOUNTANT
            }
            route.startsWith("accounting/vouchers") -> true // All logged-in roles can access (Exec has limited type permissions)
            route.contains("sync") -> {
                // Only Accountant can access sync
                userRole == UserRole.ACCOUNTANT
            }

            // Sales Team & Check-In constraints
            route.startsWith("sales/overview") || route.startsWith("sales/beats") -> {
                // Execs do not see overall team overview
                userRole == UserRole.SALES_MANAGER
            }
            route.startsWith("sales/checkin") || route.startsWith("sales/followups") -> {
                // Execs, Managers, Admins can access check-ins and follow-ups
                userRole == UserRole.SALES_EXECUTIVE || userRole == UserRole.SALES_MANAGER
            }

            // Approvals constraints
            route.startsWith("approvals") -> {
                userRole == UserRole.ACCOUNTANT || userRole == UserRole.SALES_MANAGER
            }

            // Settings constraints
            route.startsWith("settings/profile") -> true
            route.contains("settings/users") || route.contains("settings/roles") || route.contains("settings/license") -> {
                // Admin settings
                false // Super Admin / Company Admin handled on top already
            }

            // Open access (Dashboard, preferences, notifications)
            else -> true
        }
    }

    /**
     * Determines whether the navigation tab for a module should be visible to a given role.
     */
    fun shouldShowTab(role: String?, tabRoot: String): Boolean {
        if (role == null) return false
        val userRole = runCatching { UserRole.valueOf(role.uppercase()) }.getOrElse { UserRole.VIEWER }

        return when (tabRoot) {
            NavigationGraph.ACCOUNTING_ROOT -> {
                userRole != UserRole.SALES_EXECUTIVE
            }
            NavigationGraph.SALES_TEAM_ROOT -> {
                userRole == UserRole.SALES_EXECUTIVE || userRole == UserRole.SALES_MANAGER || userRole == UserRole.SUPER_ADMIN || userRole == UserRole.COMPANY_ADMIN
            }
            NavigationGraph.APPROVALS_ROOT -> {
                userRole == UserRole.ACCOUNTANT || userRole == UserRole.SALES_MANAGER || userRole == UserRole.SUPER_ADMIN || userRole == UserRole.COMPANY_ADMIN
            }
            NavigationGraph.SETTINGS_ROOT -> true
            else -> true
        }
    }
}
