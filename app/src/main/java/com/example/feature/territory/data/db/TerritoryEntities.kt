package com.example.feature.territory.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "territory_items")
data class LocalTerritory(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val companyId: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusKm: Double,
    val salesRepId: String?,
    val salesRepName: String?,
    val boundsJson: String = ""
)

@Entity(tableName = "beat_items")
data class LocalBeat(
    @PrimaryKey val id: String,
    val territoryId: String,
    val name: String,
    val description: String,
    val companyId: String,
    val salesRepId: String?,
    val salesRepName: String?,
    val customerIdsCsv: String = "" // Comma-separated values of customer IDs
)
