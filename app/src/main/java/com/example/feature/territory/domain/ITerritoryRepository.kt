package com.example.feature.territory.domain

import kotlinx.coroutines.flow.Flow
import com.example.core.common.Resource

interface ITerritoryRepository {
    fun getTerritories(companyId: String, forceRefresh: Boolean): Flow<Resource<List<Territory>>>
    suspend fun createTerritory(territory: Territory): Territory
    suspend fun updateTerritory(territory: Territory): Territory
    suspend fun deleteTerritory(id: String): Boolean

    fun getBeats(companyId: String, forceRefresh: Boolean): Flow<Resource<List<Beat>>>
    suspend fun createBeat(beat: Beat): Beat
    suspend fun updateBeat(beat: Beat): Beat
    suspend fun deleteBeat(id: String): Boolean

    fun getTerritoryPerformance(companyId: String): Flow<Resource<List<TerritoryPerformance>>>
}
