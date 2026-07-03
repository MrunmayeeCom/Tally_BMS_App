package com.example.feature.crm.domain

interface ICrmRepository {
    suspend fun getCustomers(
        query: String?,
        filter: String?,
        sort: String?,
        page: Int,
        pageSize: Int
    ): List<CrmCustomer>

    suspend fun getCustomerDetail(customerId: String): CrmCustomerDetail

    suspend fun getCustomerTimeline(customerId: String): List<CrmTimelineEvent>
}
