package com.example.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.settings.domain.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel : ViewModel() {

    private val _companyProfile = MutableStateFlow(
        CompanyProfile(
            tenantName = "Relic Steel & Tubes Corp",
            gstin = "27AAAAA1111A1Z1",
            phone = "+91 98765 43210",
            email = "finance@relicsteel.com",
            registeredAddress = "Office 402, Trade Tower, Bandra Kurla Complex, Mumbai, MH - 400051",
            companyLogoUrl = "https://relicsteel.com/assets/logo.png"
        )
    )
    val companyProfile: StateFlow<CompanyProfile> = _companyProfile.asStateFlow()

    private val _syncSettings = MutableStateFlow(
        SyncSettings(
            desktopAgentUrl = "http://192.168.1.144:9010",
            syncTokenKey = "tly_tok_8f0a3e89bc5f",
            autoSyncCycleMinutes = 15,
            localOfflineStorageLimitMb = 512,
            heartbeatStatus = "Connected",
            lastHeartbeatTime = getCurrentTime()
        )
    )
    val syncSettings: StateFlow<SyncSettings> = _syncSettings.asStateFlow()

    private val _appSettings = MutableStateFlow(
        AppSettings(
            selectedTheme = "Slate",
            biometricAccessActive = true,
            pushNotificationsActive = true,
            developerToolsEnabled = false,
            languageCode = "en_IN"
        )
    )
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _licenseInfo = MutableStateFlow(
        LicenseInfo(
            activePlanName = "Enterprise Multi-Firm Plus",
            maxSeatsCount = 25,
            consumedSeatsCount = 18,
            serialLicenseNumber = "TL-9944-8833-2195",
            licenseExpiresAt = getExpiryDate(),
            billingInterval = "Annual Renewal",
            supportsOfflineSync = true
        )
    )
    val licenseInfo: StateFlow<LicenseInfo> = _licenseInfo.asStateFlow()

    private val _tallyCompanies = MutableStateFlow(
        listOf(
            TallyCompanyConnection(
                companyId = "comp_01",
                tallyCompanyName = "Relic Steel (Mumbai Branch)",
                isActiveConnection = true,
                tallyCompanyGstin = "27AAAAA1111A1Z1",
                lastSyncedAt = getCurrentTime(-45)
            ),
            TallyCompanyConnection(
                companyId = "comp_02",
                tallyCompanyName = "Relic Tubes (Gujarat Alloys division)",
                isActiveConnection = false,
                tallyCompanyGstin = "24BBBBB2222B2Z2",
                lastSyncedAt = getCurrentTime(-720)
            ),
            TallyCompanyConnection(
                companyId = "comp_03",
                tallyCompanyName = "Apex Warehousing & Logistics",
                isActiveConnection = false,
                tallyCompanyGstin = "27CCCCCC3333C3Z3",
                lastSyncedAt = getCurrentTime(-1440)
            )
        )
    )
    val tallyCompanies: StateFlow<List<TallyCompanyConnection>> = _tallyCompanies.asStateFlow()

    private val _isTestingHeartbeat = MutableStateFlow(false)
    val isTestingHeartbeat: StateFlow<Boolean> = _isTestingHeartbeat.asStateFlow()

    private val _saveSuccessFeedback = MutableStateFlow(false)
    val saveSuccessFeedback: StateFlow<Boolean> = _saveSuccessFeedback.asStateFlow()

    private fun getCurrentTime(offsetSeconds: Int = 0): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.SECOND, offsetSeconds)
        return sdf.format(cal.time)
    }

    private fun getExpiryDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, 1)
        return sdf.format(cal.time)
    }

    fun updateCompanyProfile(profile: CompanyProfile) {
        _companyProfile.value = profile
        triggerFeedback()
    }

    fun updateSyncSettings(sync: SyncSettings) {
        _syncSettings.value = sync
        triggerFeedback()
    }

    fun updateAppSettings(app: AppSettings) {
        _appSettings.value = app
        triggerFeedback()
    }

    fun testHeartbeat() {
        if (_isTestingHeartbeat.value) return
        viewModelScope.launch {
            _isTestingHeartbeat.value = true
            _syncSettings.value = _syncSettings.value.copy(heartbeatStatus = "Testing...")
            delay(1200)
            val random = (1..100).random()
            val finalStatus = when {
                random > 15 -> "Connected"
                random > 5 -> "Degraded"
                else -> "Disconnected"
            }
            _syncSettings.value = _syncSettings.value.copy(
                heartbeatStatus = finalStatus,
                lastHeartbeatTime = getCurrentTime()
            )
            _isTestingHeartbeat.value = false
        }
    }

    fun rotateSyncToken() {
        viewModelScope.launch {
            val randomToken = "tly_tok_" + UUID.randomUUID().toString().substring(0, 12).replace("-", "")
            _syncSettings.value = _syncSettings.value.copy(syncTokenKey = randomToken)
            delay(400)
            triggerFeedback()
        }
    }

    fun addTallyCompany(company: TallyCompanyConnection) {
        val current = _tallyCompanies.value.toMutableList()
        current.add(company)
        _tallyCompanies.value = current
        triggerFeedback()
    }

    fun switchActiveTallyCompany(companyId: String) {
        val current = _tallyCompanies.value.map {
            it.copy(isActiveConnection = it.companyId == companyId)
        }
        _tallyCompanies.value = current
        triggerFeedback()
    }

    fun renewOrUpgradeLicense() {
        viewModelScope.launch {
            val current = _licenseInfo.value
            _licenseInfo.value = current.copy(
                maxSeatsCount = current.maxSeatsCount + 5,
                activePlanName = "Enterprise Unlimited Seats Elite"
            )
            triggerFeedback()
        }
    }

    private fun triggerFeedback() {
        viewModelScope.launch {
            _saveSuccessFeedback.value = true
            delay(1500)
            _saveSuccessFeedback.value = false
        }
    }
}
