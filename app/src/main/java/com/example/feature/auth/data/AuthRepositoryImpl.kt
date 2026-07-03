package com.example.feature.auth.data

import com.example.core.common.Resource
import com.example.core.network.*
import com.example.core.session.SessionManager
import com.example.feature.auth.domain.CompanyDomain
import com.example.feature.auth.domain.IAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : IAuthRepository {

    override val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn
    override val userRole: Flow<String?> = sessionManager.userRole
    override val userName: Flow<String?> = sessionManager.userName
    override val activeCompanyId: Flow<String?> = sessionManager.companyId
    override val activeTenantId: Flow<String?> = sessionManager.tenantId

    override fun login(email: String, password: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.login(LoginRequest(username = email, password = password, loginType = "ADMIN"))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveSession(
                    accessToken = body.token,
                    refreshToken = "",
                    tenantId = "",
                    companyId = "",
                    userRole = body.user?.role ?: "",
                    userName = body.user?.name ?: ""
                )
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(Exception("Login failed with code ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e, e.localizedMessage))
        }
    }

    override fun forgotPassword(email: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.message))
                } else {
                    emit(Resource.Error(Exception(body.message)))
                }
            } else {
                emit(Resource.Error(Exception("Forgot password request failed.")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e, e.localizedMessage))
        }
    }

    override fun resetPassword(email: String, otpCode: String, newPass: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.resetPassword(ResetPasswordRequest(email, otpCode, newPass))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Resource.Success(body.message))
                } else {
                    emit(Resource.Error(Exception(body.message)))
                }
            } else {
                emit(Resource.Error(Exception("Reset password request failed.")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e, e.localizedMessage))
        }
    }

    override fun getCompanies(): Flow<Resource<List<CompanyDomain>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getCompanies()
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                val domainCompanies = wrapper.data.map { dto ->
                    CompanyDomain(
                        companyGuid = dto.company_guid,
                        name = dto.name
                    )
                }
                emit(Resource.Success(domainCompanies))
            } else {
                emit(Resource.Error(Exception("Could not fetch companies: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e, e.localizedMessage))
        }
    }

    override fun getActiveCompany(): Flow<Resource<CompanyDomain?>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getActiveCompany()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val guid = body.company_guid
                if (guid != null) {
                    emit(Resource.Success(CompanyDomain(companyGuid = guid, name = "")))
                } else {
                    emit(Resource.Success(null))
                }
            } else {
                emit(Resource.Error(Exception("Could not fetch active company: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e, e.localizedMessage))
        }
    }

    override suspend fun setActiveCompany(companyGuid: String) {
        try {
            val response = apiService.setActiveCompany(SetActiveCompanyRequest(company_guid = companyGuid))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.switchCompany(body.company_guid ?: companyGuid)
            }
        } catch (e: Exception) {
            // silently fail
        }
    }

    override suspend fun switchCompany(companyId: String) {
        sessionManager.switchCompany(companyId)
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }
}
