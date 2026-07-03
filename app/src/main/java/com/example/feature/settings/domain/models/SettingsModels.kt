package com.example.feature.settings.domain.models

import java.io.Serializable

data class CompanyProfile(
    val tenantName: String,
    val gstin: String,
    val phone: String,
    val email: String,
    val registeredAddress: String,
    val companyLogoUrl: String
) : Serializable

data class SyncSettings(
    val desktopAgentUrl: String,
    val syncTokenKey: String,
    val autoSyncCycleMinutes: Int,
    val localOfflineStorageLimitMb: Int,
    val heartbeatStatus: String, // "Connected", "Degraded", "Disconnected"
    val lastHeartbeatTime: String
) : Serializable

data class AppSettings(
    val selectedTheme: String, // "System", "Light", "Dark", "Slate"
    val biometricAccessActive: Boolean,
    val pushNotificationsActive: Boolean,
    val developerToolsEnabled: Boolean,
    val languageCode: String
) : Serializable

data class LicenseInfo(
    val activePlanName: String,
    val maxSeatsCount: Int,
    val consumedSeatsCount: Int,
    val serialLicenseNumber: String,
    val licenseExpiresAt: String,
    val billingInterval: String,
    val supportsOfflineSync: Boolean
) : Serializable

data class TallyCompanyConnection(
    val companyId: String,
    val tallyCompanyName: String,
    val isActiveConnection: Boolean,
    val tallyCompanyGstin: String,
    val lastSyncedAt: String
) : Serializable
