package com.example.feature.inventory.data

import com.example.feature.inventory.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface InventoryApiService {

    @GET("api/v1/inventory/items")
    suspend fun getStockItems(
        @Query("companyId") companyId: String
    ): Response<List<StockItemDto>>

    @GET("api/v1/inventory/godowns")
    suspend fun getGodowns(
        @Query("companyId") companyId: String
    ): Response<List<GodownDto>>

    @GET("api/v1/inventory/levels")
    suspend fun getStockLevels(
        @Query("companyId") companyId: String
    ): Response<List<StockLevelDto>>

    @GET("api/v1/inventory/transactions")
    suspend fun getTransactions(
        @Query("companyId") companyId: String
    ): Response<List<StockTransactionDto>>

    @POST("api/v1/inventory/transfer")
    suspend fun transferStock(
        @Body request: StockTransferRequest
    ): Response<StockTransactionDto>

    @POST("api/v1/inventory/adjust")
    suspend fun adjustStock(
        @Body request: AdjustmentRequest
    ): Response<StockTransactionDto>
}
