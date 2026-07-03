package com.example.feature.auth.domain

import com.example.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    val isLoggedIn: Flow<Boolean>
    val userRole: Flow<String?>
    val userName: Flow<String?>
    val activeCompanyId: Flow<String?>
    val activeTenantId: Flow<String?>

    fun login(email: String, password: String): Flow<Resource<Unit>>
    fun forgotPassword(email: String): Flow<Resource<String>>
    fun resetPassword(email: String, otpCode: String, newPass: String): Flow<Resource<String>>
    fun getCompanies(): Flow<Resource<List<CompanyDomain>>>
    fun getActiveCompany(): Flow<Resource<CompanyDomain?>>
    suspend fun setActiveCompany(companyGuid: String)
    suspend fun switchCompany(companyId: String)
    suspend fun logout()
}
