package com.example.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tally_bms_session_prefs")

class SessionManager(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_TENANT_ID = stringPreferencesKey("active_tenant_id")
        private val KEY_COMPANY_ID = stringPreferencesKey("active_company_id")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
    }

    val accessToken: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_REFRESH_TOKEN] }
    val tenantId: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_TENANT_ID] }
    val companyId: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_COMPANY_ID] }
    val userRole: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_USER_ROLE] }
    val userName: Flow<String?> = dataStore.data.map { prefs -> prefs[KEY_USER_NAME] }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        tenantId: String,
        companyId: String,
        userRole: String,
        userName: String
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_TENANT_ID] = tenantId
            prefs[KEY_COMPANY_ID] = companyId
            prefs[KEY_USER_ROLE] = userRole
            prefs[KEY_USER_NAME] = userName
        }
    }

    suspend fun updateAccessToken(newAccessToken: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = newAccessToken
        }
    }

    suspend fun switchCompany(companyId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_COMPANY_ID] = companyId
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        !prefs[KEY_ACCESS_TOKEN].isNullOrEmpty()
    }
}
