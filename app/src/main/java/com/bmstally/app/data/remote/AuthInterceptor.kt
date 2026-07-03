package com.bmstally.app.data.remote

import com.bmstally.app.data.auth.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenManager.accessToken

        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("X-Tenant-Id", tokenManager.tenantId ?: "")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            Timber.w("Received 401 - token may be expired")
            // Token refresh handled by BmsApiService or Authenticator
        }

        return response
    }
}
