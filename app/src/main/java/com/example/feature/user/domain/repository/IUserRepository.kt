package com.example.feature.user.domain.repository

import com.example.core.common.Resource
import com.example.feature.user.domain.models.User
import com.example.feature.user.domain.models.UserActivity
import com.example.feature.user.domain.models.UserStatus
import com.example.feature.user.data.model.InviteUserResponse
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getUsers(companyId: String, forceRefresh: Boolean): Flow<Resource<List<User>>>
    fun getUserById(userId: String): Flow<Resource<User?>>
    fun createUser(user: User): Flow<Resource<User>>
    fun inviteUser(email: String, phone: String, assignedRoles: List<String>, invitationType: String): Flow<Resource<InviteUserResponse>>
    fun updateUserStatus(userId: String, status: UserStatus): Flow<Resource<Boolean>>
    fun resetUserPassword(userId: String): Flow<Resource<String>>
    fun deleteUser(userId: String): Flow<Resource<Boolean>>
    fun getUserActivities(userId: String, forceRefresh: Boolean): Flow<Resource<List<UserActivity>>>
    suspend fun saveLocalActivity(activity: UserActivity): Boolean
}
