package com.example.core.di

import android.content.Context
import com.example.core.database.TallyBmsDatabase
import com.example.core.network.ApiService
import com.example.core.network.NetworkService
import com.example.core.session.SessionManager
import com.example.feature.accounting.data.AccountingRepository
import com.example.feature.auth.data.AuthRepositoryImpl
import com.example.feature.auth.domain.IAuthRepository
import com.example.feature.dashboard.data.DashboardRepositoryImpl
import com.example.feature.dashboard.domain.IDashboardRepository
import com.example.feature.crm.data.CrmRepositoryImpl
import com.example.feature.crm.domain.ICrmRepository
import com.example.feature.outstanding.data.OutstandingRepositoryImpl
import com.example.feature.outstanding.domain.IOutstandingRepository
import com.example.feature.reminder.data.ReminderRepositoryImpl
import com.example.feature.reminder.domain.IReminderRepository
import com.example.feature.followup.data.FollowUpRepositoryImpl
import com.example.feature.followup.domain.IFollowUpRepository
import com.example.feature.salesteam.data.SalesTeamRepositoryImpl
import com.example.feature.salesteam.domain.ISalesTeamRepository
import com.example.feature.accounting.domain.IOrderRepository
import com.example.feature.accounting.data.OrderRepositoryImpl

class AppContainerImpl(override val context: Context) : AppContainer {

    override val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    override val networkService: NetworkService by lazy {
        NetworkService(context, sessionManager)
    }

    override val apiService: ApiService by lazy {
        networkService.apiService
    }

    override val database: TallyBmsDatabase by lazy {
        TallyBmsDatabase.getDatabase(context)
    }

    override val authRepository: IAuthRepository by lazy {
        AuthRepositoryImpl(apiService, sessionManager)
    }

    override val accountingRepository: AccountingRepository by lazy {
        AccountingRepository(
            context = context,
            apiService = apiService,
            ledgerDao = database.ledgerDao(),
            voucherDao = database.voucherDao(),
            billDao = database.billDao(),
            sessionManager = sessionManager
        )
    }

    override val orderRepository: IOrderRepository by lazy {
        OrderRepositoryImpl(apiService, database.orderDao())
    }

    override val dashboardRepository: IDashboardRepository by lazy {
        DashboardRepositoryImpl(apiService)
    }

    override val crmRepository: ICrmRepository by lazy {
        CrmRepositoryImpl(apiService, database.crmDao())
    }

    override val outstandingRepository: IOutstandingRepository by lazy {
        OutstandingRepositoryImpl(networkService.outstandingApiService)
    }

    override val reminderRepository: IReminderRepository by lazy {
        ReminderRepositoryImpl(networkService.reminderApiService)
    }

    override val followUpRepository: IFollowUpRepository by lazy {
        FollowUpRepositoryImpl(
            apiService = networkService.followUpApiService,
            crmRepository = crmRepository,
            outstandingRepository = outstandingRepository,
            reminderRepository = reminderRepository,
            sessionManager = sessionManager
        )
    }

    override val salesTeamRepository: ISalesTeamRepository by lazy {
        SalesTeamRepositoryImpl(networkService.salesTeamApiService)
    }

    override val reportsRepository: com.example.feature.reports.domain.IReportsRepository by lazy {
        com.example.feature.reports.data.ReportsRepositoryImpl(networkService.reportsApiService)
    }

    override val inventoryRepository: com.example.feature.inventory.domain.InventoryRepository by lazy {
        com.example.feature.inventory.data.InventoryRepositoryImpl(
            context = context,
            stockItemDao = database.stockItemDao(),
            godownDao = database.godownDao(),
            stockLevelDao = database.stockLevelDao(),
            stockTransactionDao = database.stockTransactionDao(),
            apiService = networkService.inventoryApiService
        )
    }

    override val territoryRepository: com.example.feature.territory.domain.ITerritoryRepository by lazy {
        com.example.feature.territory.data.TerritoryRepositoryImpl(
            apiService = networkService.territoryApiService,
            dao = database.territoryDao()
        )
    }

    override val syncRepository: com.example.feature.sync.domain.repository.ISyncRepository by lazy {
        com.example.feature.sync.data.repository.SyncRepositoryImpl(
            context = context,
            tallyDatabase = database,
            syncDao = com.example.feature.sync.data.db.SyncDatabase.getDatabase(context).syncDao()
        )
    }

    override val quotationRepository: com.example.feature.quotation.domain.IQuotationRepository by lazy {
        com.example.feature.quotation.data.repository.QuotationRepositoryImpl(
            apiService = networkService.quotationApiService,
            quotationDao = database.quotationDao(),
            orderDao = database.orderDao()
        )
    }

    override val approvalRepository: com.example.feature.approval.domain.IApprovalRepository by lazy {
        com.example.feature.approval.data.repository.ApprovalRepositoryImpl(
            apiService = networkService.approvalApiService,
            approvalDao = database.approvalDao()
        )
    }

    override val securityRepository: com.example.feature.security.domain.ISecurityRepository by lazy {
        com.example.feature.security.data.repository.SecurityRepositoryImpl(
            apiService = networkService.securityApiService,
            securityDao = database.securityDao()
        )
    }

    override val userRepository: com.example.feature.user.domain.repository.IUserRepository by lazy {
        com.example.feature.user.data.repository.UserRepositoryImpl(
            apiService = networkService.userApiService,
            userDao = database.userDao()
        )
    }
}
