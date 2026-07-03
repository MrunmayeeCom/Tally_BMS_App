package com.example.feature.user.data.remote

import com.example.feature.user.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    @GET("api/v1/admin/users")
    suspend fun getUsers(
        @Query("companyId") companyId: String
    ): Response<List<UserDto>>

    @POST("api/v1/admin/users/create")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<UserDto>

    @POST("api/v1/admin/users/invite")
    suspend fun inviteUser(
        @Body request: InviteUserRequest
    ): Response<InviteUserResponse>

    @POST("api/v1/admin/users/{userId}/status")
    suspend fun updateUserStatus(
        @Path("userId") userId: String,
        @Query("status") status: String
    ): Response<StatusUpdateResponse>

    @POST("api/v1/admin/users/{userId}/reset-password")
    suspend fun resetUserPassword(
        @Path("userId") userId: String
    ): Response<StatusUpdateResponse>

    @DELETE("api/v1/admin/users/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: String
    ): Response<StatusUpdateResponse>

    @GET("api/v1/admin/users/{userId}/events")
    suspend fun getUserActivities(
        @Path("userId") userId: String
    ): Response<List<UserActivityDto>>
}
