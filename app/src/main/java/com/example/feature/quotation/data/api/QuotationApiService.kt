package com.example.feature.quotation.data.api

import com.example.feature.quotation.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface QuotationApiService {

    @GET("api/v1/quotations")
    suspend fun getQuotations(
        @Query("companyId") companyId: String
    ): Response<List<QuotationDto>>

    @GET("api/v1/quotations/{id}")
    suspend fun getQuotationById(
        @Path("id") id: String
    ): Response<QuotationDto>

    @POST("api/v1/quotations")
    suspend fun createQuotation(
        @Body request: CreateQuotationRequest
    ): Response<QuotationDto>

    @PUT("api/v1/quotations/{id}")
    suspend fun updateQuotation(
        @Path("id") id: String,
        @Body request: CreateQuotationRequest
    ): Response<QuotationDto>

    @DELETE("api/v1/quotations/{id}")
    suspend fun deleteQuotation(
        @Path("id") id: String
    ): Response<Unit>

    @PUT("api/v1/quotations/{id}/status")
    suspend fun updateQuotationStatus(
        @Path("id") id: String,
        @Body request: UpdateQuotationStatusRequest
    ): Response<QuotationDto>

    @POST("api/v1/quotations/{id}/convert-to-order")
    suspend fun convertToOrder(
        @Path("id") id: String
    ): Response<ConvertOrderResponseDto>

    @GET("api/v1/quotations/analytics")
    suspend fun getAnalytics(
        @Query("companyId") companyId: String
    ): Response<QuotationAnalyticsDto>
}
