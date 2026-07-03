package com.example.core.di

import android.content.Context
import com.example.core.database.TallyBmsDatabase
import com.example.core.network.ApiService
import com.example.core.network.NetworkService
import com.example.core.session.SessionManager
import com.example.feature.accounting.data.AccountingRepository
import com.example.feature.auth.domain.IAuthRepository
import com.example.feature.dashboard.domain.IDashboardRepository
import com.example.feature.crm.domain.ICrmRepository
import com.example.feature.outstanding.domain.IOutstandingRepository
import com.example.feature.reminder.domain.IReminderRepository
import com.example.feature.followup.domain.IFollowUpRepository
import com.example.feature.salesteam.domain.ISalesTeamRepository
import com.example.feature.accounting.domain.IOrderRepository

interface AppContainer {
    val context: Context
    val sessionManager: SessionManager
    val networkService: NetworkService
    val apiService: ApiService
    val database: TallyBmsDatabase
    val authRepository: IAuthRepository
    val accountingRepository: AccountingRepository
    val orderRepository: IOrderRepository
    val dashboardRepository: IDashboardRepository
    val crmRepository: ICrmRepository
    val outstandingRepository: IOutstandingRepository
    val reminderRepository: IReminderRepository
    val followUpRepository: IFollowUpRepository
    val salesTeamRepository: ISalesTeamRepository
    val reportsRepository: com.example.feature.reports.domain.IReportsRepository
    val inventoryRepository: com.example.feature.inventory.domain.InventoryRepository
    val territoryRepository: com.example.feature.territory.domain.ITerritoryRepository
    val syncRepository: com.example.feature.sync.domain.repository.ISyncRepository
    val quotationRepository: com.example.feature.quotation.domain.IQuotationRepository
    val approvalRepository: com.example.feature.approval.domain.IApprovalRepository
    val securityRepository: com.example.feature.security.domain.ISecurityRepository
    val userRepository: com.example.feature.user.domain.repository.IUserRepository
}
