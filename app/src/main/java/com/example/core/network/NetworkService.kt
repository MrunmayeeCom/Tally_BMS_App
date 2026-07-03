package com.example.core.network

import android.content.Context
import com.example.core.session.SessionManager
import com.example.feature.outstanding.data.OutstandingApiService
import com.example.feature.reminder.data.ReminderApiService
import com.example.feature.followup.data.FollowUpApiService
import com.example.feature.salesteam.data.SalesTeamApiService
import com.example.feature.reports.data.ReportsApiService
import com.example.feature.user.data.remote.UserApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class NetworkService(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val baseUrl: String = getBaseUrlFromConfig()
) {

    companion object {
        private fun getBaseUrlFromConfig(): String {
            return try {
                val clazz = Class.forName("com.example.BuildConfig")
                val field = clazz.getField("API_BASE_URL")
                val value = field.get(null) as? String
                if (!value.isNullOrBlank()) value else "https://api.tallybms.com"
            } catch (e: Exception) {
                // If not found in build config, default to sandbox API gateway
                "https://api.tallybms.com"
            }
        }
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(sessionManager))
            .authenticator(TokenAuthenticator(sessionManager))
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    val outstandingApiService: OutstandingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OutstandingApiService::class.java)
    }

    val reminderApiService: ReminderApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ReminderApiService::class.java)
    }

    val followUpApiService: FollowUpApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FollowUpApiService::class.java)
    }

    val salesTeamApiService: SalesTeamApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SalesTeamApiService::class.java)
    }

    val reportsApiService: ReportsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ReportsApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UserApiService::class.java)
    }

    val inventoryApiService: com.example.feature.inventory.data.InventoryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(com.example.feature.inventory.data.InventoryApiService::class.java)
    }

    val territoryApiService: com.example.feature.territory.data.TerritoryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(com.example.feature.territory.data.TerritoryApiService::class.java)
    }

    val quotationApiService: com.example.feature.quotation.data.api.QuotationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(com.example.feature.quotation.data.api.QuotationApiService::class.java)
    }

    val approvalApiService: com.example.feature.approval.data.api.ApprovalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(com.example.feature.approval.data.api.ApprovalApiService::class.java)
    }

    val securityApiService: com.example.feature.security.data.api.SecurityApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(com.example.feature.security.data.api.SecurityApiService::class.java)
    }
}
