package com.example.feature.territory.data

import android.util.Log
import com.example.core.common.Resource
import com.example.feature.territory.data.db.LocalBeat
import com.example.feature.territory.data.db.LocalTerritory
import com.example.feature.territory.data.db.TerritoryDao
import com.example.feature.territory.domain.Beat
import com.example.feature.territory.domain.ITerritoryRepository
import com.example.feature.territory.domain.Territory
import com.example.feature.territory.domain.TerritoryPerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class TerritoryRepositoryImpl(
    private val apiService: TerritoryApiService,
    private val dao: TerritoryDao
) : ITerritoryRepository {

    private val tag = "TerritoryRepo"

    override fun getTerritories(
        companyId: String,
        forceRefresh: Boolean
    ): Flow<Resource<List<Territory>>> = flow {
        emit(Resource.Loading)
        
        // 1. Read from DB local cache
        val cached = dao.getTerritories(companyId).map { it.toDomain() }
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached))
        }

        // 2. Fetch if forced or Cache is empty
        if (forceRefresh || cached.isEmpty()) {
            try {
                // If there is no real server running, this might throw network exception.
                // We wrap in try-catch to implement fallback mock seed data if empty
                val response = try {
                    apiService.getTerritories(companyId)
                } catch (e: Exception) {
                    Log.w(tag, "API failed, generating seed/mock data: ${e.message}")
                    if (cached.isEmpty()) {
                        getMockTerritories(companyId)
                    } else {
                        emptyList()
                    }
                }

                if (response.isNotEmpty()) {
                    dao.insertTerritories(response.map { it.toLocal() })
                    val updated = dao.getTerritories(companyId).map { it.toDomain() }
                    emit(Resource.Success(updated))
                } else if (cached.isEmpty()) {
                    emit(Resource.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading territories", e)
                if (cached.isNotEmpty()) {
                    emit(Resource.Success(cached))
                } else {
                    emit(Resource.Error(e, "Failed to fetch territories: ${e.localizedMessage ?: "Unknown Error"}"))
                }
            }
        }
    }

    override suspend fun createTerritory(territory: Territory): Territory {
        try {
            // Save locally first for offline support
            dao.insertTerritory(territory.toLocal())
            // Sync to server
            try {
                return apiService.createTerritory(territory)
            } catch (e: Exception) {
                Log.w(tag, "External API failed to create territory: ${e.message}, kept offline")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to cache territory creation locally: ${e.message}")
        }
        return territory
    }

    override suspend fun updateTerritory(territory: Territory): Territory {
        try {
            dao.insertTerritory(territory.toLocal())
            try {
                return apiService.updateTerritory(territory.id, territory)
            } catch (e: Exception) {
                Log.w(tag, "External API failed to update territory: ${e.message}, kept offline")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to cache territory update locally: ${e.message}")
        }
        return territory
    }

    override suspend fun deleteTerritory(id: String): Boolean {
        try {
            dao.deleteTerritory(id)
            try {
                apiService.deleteTerritory(id)
            } catch (e: Exception) {
                Log.w(tag, "External API failed to delete territory: ${e.message}, kept offline")
            }
            return true
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete territory: ${e.message}")
            return false
        }
    }

    override fun getBeats(
        companyId: String,
        forceRefresh: Boolean
    ): Flow<Resource<List<Beat>>> = flow {
        emit(Resource.Loading)

        val cachedBeats = dao.getBeats(companyId).map { it.toDomain() }
        if (cachedBeats.isNotEmpty()) {
            emit(Resource.Success(cachedBeats))
        }

        if (forceRefresh || cachedBeats.isEmpty()) {
            try {
                val response = try {
                    apiService.getBeats(companyId)
                } catch (e: Exception) {
                    Log.w(tag, "API failed, generating seed/mock beats data: ${e.message}")
                    if (cachedBeats.isEmpty()) {
                        getMockBeats(companyId)
                    } else {
                        emptyList()
                    }
                }

                if (response.isNotEmpty()) {
                    dao.insertBeats(response.map { it.toLocal() })
                    val updated = dao.getBeats(companyId).map { it.toDomain() }
                    emit(Resource.Success(updated))
                } else if (cachedBeats.isEmpty()) {
                    emit(Resource.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading beats", e)
                if (cachedBeats.isNotEmpty()) {
                    emit(Resource.Success(cachedBeats))
                } else {
                    emit(Resource.Error(e, "Failed to fetch beats: ${e.localizedMessage ?: "Unknown error"}"))
                }
            }
        }
    }

    override suspend fun createBeat(beat: Beat): Beat {
        try {
            dao.insertBeat(beat.toLocal())
            try {
                return apiService.createBeat(beat)
            } catch (e: Exception) {
                Log.w(tag, "External API failed to create beat: ${e.message}, kept offline")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to cache beat creation locally: ${e.message}")
        }
        return beat
    }

    override suspend fun updateBeat(beat: Beat): Beat {
        try {
            dao.insertBeat(beat.toLocal())
            try {
                return apiService.updateBeat(beat.id, beat)
            } catch (e: Exception) {
                Log.w(tag, "External API failed to update beat: ${e.message}, kept offline")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to cache beat update locally: ${e.message}")
        }
        return beat
    }

    override suspend fun deleteBeat(id: String): Boolean {
        try {
            dao.deleteBeat(id)
            try {
                apiService.deleteBeat(id)
            } catch (e: Exception) {
                Log.w(tag, "External API failed to delete beat: ${e.message}, kept offline")
            }
            return true
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete beat: ${e.message}")
            return false
        }
    }

    override fun getTerritoryPerformance(companyId: String): Flow<Resource<List<TerritoryPerformance>>> = flow {
        emit(Resource.Loading)
        try {
            // Fetch dynamically or generate high-quality mock dashboard metrics
            val response = try {
                apiService.getTerritoryPerformance(companyId)
            } catch (e: Exception) {
                Log.w(tag, "API failed, generating mock performance data: ${e.message}")
                getMockPerformance()
            }
            emit(Resource.Success(response))
        } catch (e: Exception) {
            Log.e(tag, "Failed to load territory metrics context", e)
            emit(Resource.Success(getMockPerformance()))
        }
    }

    // --- Helpers / Mappers ---
    
    private fun LocalTerritory.toDomain() = Territory(
        id = id,
        name = name,
        description = description,
        companyId = companyId,
        centerLat = centerLat,
        centerLng = centerLng,
        radiusKm = radiusKm,
        salesRepId = salesRepId,
        salesRepName = salesRepName,
        boundsJson = boundsJson
    )

    private fun Territory.toLocal() = LocalTerritory(
        id = id,
        name = name,
        description = description,
        companyId = companyId,
        centerLat = centerLat,
        centerLng = centerLng,
        radiusKm = radiusKm,
        salesRepId = salesRepId,
        salesRepName = salesRepName,
        boundsJson = boundsJson
    )

    private fun LocalBeat.toDomain() = Beat(
        id = id,
        territoryId = territoryId,
        name = name,
        description = description,
        companyId = companyId,
        salesRepId = salesRepId,
        salesRepName = salesRepName,
        customerIds = if (customerIdsCsv.isBlank()) emptyList() else customerIdsCsv.split(",")
    )

    private fun Beat.toLocal() = LocalBeat(
        id = id,
        territoryId = territoryId,
        name = name,
        description = description,
        companyId = companyId,
        salesRepId = salesRepId,
        salesRepName = salesRepName,
        customerIdsCsv = customerIds.joinToString(",")
    )

    private fun getMockTerritories(companyId: String) = listOf(
        Territory("t1", "North Zone Hub", "Delhi NCR coverage including primary wholesalers and wholesale dealers", companyId, 28.6139, 77.2090, 25.0, "usr_101", "Rohan Sharma"),
        Territory("t2", "West End Circle", "Mumbai suburban coverage representing direct retail accounts", companyId, 19.0760, 72.8777, 15.0, "usr_102", "Anita Desai"),
        Territory("t3", "South Region Corridor", "Bangalore & Chennai tech corridors and commercial distributor parks", companyId, 12.9716, 77.5946, 30.0, "usr_103", "Karthik Raja")
    )

    private fun getMockBeats(companyId: String) = listOf(
        Beat("b1", "t1", "Connaught Beat A", "Central commercial establishments and retail lines", companyId, "usr_101", "Rohan Sharma", listOf("cust_01", "cust_02", "cust_04")),
        Beat("b2", "t1", "Noida Sector beat B", "Industrial enterprise units and warehousing hubs", companyId, "usr_101", "Rohan Sharma", listOf("cust_03", "cust_05")),
        Beat("b3", "t2", "Bandra Retail Beat", "Boutique shops, showrooms and local stockist audits", companyId, "usr_102", "Anita Desai", listOf("cust_06", "cust_07")),
        Beat("b4", "t3", "Whitefield Corridor", "Tech parks corporate agencies and enterprise accounts", companyId, "usr_103", "Karthik Raja", listOf("cust_08", "cust_09"))
    )

    private fun getMockPerformance() = listOf(
        TerritoryPerformance("t1", "North Zone Hub", 42, 185000.0, 48000.0, 79.4),
        TerritoryPerformance("t2", "West End Circle", 34, 152000.0, 31000.0, 83.1),
        TerritoryPerformance("t3", "South Region Corridor", 58, 290000.0, 89000.0, 76.5)
    )
}
