package com.example.feature.territory.domain

data class Territory(
    val id: String,
    val name: String,
    val description: String,
    val companyId: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusKm: Double,
    val salesRepId: String?,
    val salesRepName: String?,
    val boundsJson: String = "" // Optional coordinates array for spatial boundaries representation
)

data class Beat(
    val id: String,
    val territoryId: String,
    val name: String,
    val description: String,
    val companyId: String,
    val salesRepId: String?,
    val salesRepName: String?,
    val customerIds: List<String> = emptyList() // Map to/from comma-separated String in local DB
)

data class TerritoryPerformance(
    val territoryId: String,
    val territoryName: String,
    val totalVisits: Int,
    val totalCollections: Double,
    val totalOutstanding: Double,
    val recoverySuccessRate: Double // e.g. 85.0 (percentage)
)

sealed class TerritoryUiState {
    object Loading : TerritoryUiState()
    data class Success(
        val territories: List<Territory> = emptyList(),
        val beats: List<Beat> = emptyList(),
        val performances: List<TerritoryPerformance> = emptyList()
    ) : TerritoryUiState()
    data class Error(val message: String) : TerritoryUiState()
}
