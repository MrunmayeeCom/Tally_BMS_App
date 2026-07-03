package com.example.feature.territory.data

import com.example.feature.territory.domain.Territory
import com.example.feature.territory.domain.Beat
import com.example.feature.territory.domain.TerritoryPerformance
import retrofit2.http.*

interface TerritoryApiService {
    @GET("/api/v1/territory")
    suspend fun getTerritories(
        @Query("companyId") companyId: String
    ): List<Territory>

    @POST("/api/v1/territory")
    suspend fun createTerritory(
        @Body territory: Territory
    ): Territory

    @PUT("/api/v1/territory/{id}")
    suspend fun updateTerritory(
        @Path("id") id: String,
        @Body territory: Territory
    ): Territory

    @DELETE("/api/v1/territory/{id}")
    suspend fun deleteTerritory(
        @Path("id") id: String
    ): retrofit2.Response<Unit>

    @GET("/api/v1/beats")
    suspend fun getBeats(
        @Query("companyId") companyId: String
    ): List<Beat>

    @POST("/api/v1/beats")
    suspend fun createBeat(
        @Body beat: Beat
    ): Beat

    @PUT("/api/v1/beats/{id}")
    suspend fun updateBeat(
        @Path("id") id: String,
        @Body beat: Beat
    ): Beat

    @DELETE("/api/v1/beats/{id}")
    suspend fun deleteBeat(
        @Path("id") id: String
    ): retrofit2.Response<Unit>

    @GET("/api/v1/territory/performance")
    suspend fun getTerritoryPerformance(
        @Query("companyId") companyId: String
    ): List<TerritoryPerformance>
}
