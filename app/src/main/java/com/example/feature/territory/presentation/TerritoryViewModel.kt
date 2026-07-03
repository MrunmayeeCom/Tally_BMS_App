package com.example.feature.territory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.Resource
import com.example.core.session.SessionManager
import com.example.feature.territory.domain.Beat
import com.example.feature.territory.domain.ITerritoryRepository
import com.example.feature.territory.domain.Territory
import com.example.feature.territory.domain.TerritoryUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class TerritoryViewModel(
    private val repository: ITerritoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<TerritoryUiState>(TerritoryUiState.Loading)
    val uiState: StateFlow<TerritoryUiState> = _uiState.asStateFlow()

    private val _companyId = MutableStateFlow<String>("comp_01")

    init {
        // Collect tenant context and fetch appropriate isolated data
        viewModelScope.launch {
            sessionManager.companyId
                .filterNotNull()
                .collect { compId ->
                    _companyId.value = compId
                    loadData(compId, forceRefresh = false)
                }
        }
    }

    fun refresh() {
        val compId = _companyId.value
        loadData(compId, forceRefresh = true)
    }

    private fun loadData(companyId: String, forceRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = TerritoryUiState.Loading
            
            // Combine territories, beats, and performance metrics
            val territoriesFlow = repository.getTerritories(companyId, forceRefresh)
            val beatsFlow = repository.getBeats(companyId, forceRefresh)
            val performanceFlow = repository.getTerritoryPerformance(companyId)

            combine(territoriesFlow, beatsFlow, performanceFlow) { terrRes, beatRes, perfRes ->
                if (terrRes is Resource.Error) {
                    TerritoryUiState.Error(terrRes.message ?: "Failed to load territories")
                } else if (beatRes is Resource.Error) {
                    TerritoryUiState.Error(beatRes.message ?: "Failed to load beats")
                } else if (terrRes is Resource.Success && beatRes is Resource.Success && perfRes is Resource.Success) {
                    TerritoryUiState.Success(
                        territories = terrRes.data ?: emptyList(),
                        beats = beatRes.data ?: emptyList(),
                        performances = perfRes.data ?: emptyList()
                    )
                } else {
                    TerritoryUiState.Loading
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    // --- Territory CRUDS ---

    fun saveTerritory(
        id: String?,
        name: String,
        description: String,
        centerLat: Double,
        centerLng: Double,
        radiusKm: Double,
        salesRepId: String?,
        salesRepName: String?
    ) {
        viewModelScope.launch {
            val companyId = _companyId.value
            val isEdit = !id.isNullOrBlank()
            val territory = Territory(
                id = id ?: UUID.randomUUID().toString(),
                name = name,
                description = description,
                companyId = companyId,
                centerLat = centerLat,
                centerLng = centerLng,
                radiusKm = radiusKm,
                salesRepId = salesRepId,
                salesRepName = salesRepName
            )

            if (isEdit) {
                repository.updateTerritory(territory)
            } else {
                repository.createTerritory(territory)
            }
            loadData(companyId, forceRefresh = true)
        }
    }

    fun deleteTerritory(id: String) {
        viewModelScope.launch {
            repository.deleteTerritory(id)
            loadData(_companyId.value, forceRefresh = true)
        }
    }

    // --- Beat CRUDS ---

    fun saveBeat(
        id: String?,
        territoryId: String,
        name: String,
        description: String,
        salesRepId: String?,
        salesRepName: String?,
        customerIds: List<String>
    ) {
        viewModelScope.launch {
            val companyId = _companyId.value
            val isEdit = !id.isNullOrBlank()
            val beat = Beat(
                id = id ?: UUID.randomUUID().toString(),
                territoryId = territoryId,
                name = name,
                description = description,
                companyId = companyId,
                salesRepId = salesRepId,
                salesRepName = salesRepName,
                customerIds = customerIds
            )

            if (isEdit) {
                repository.updateBeat(beat)
            } else {
                repository.createBeat(beat)
            }
            loadData(companyId, forceRefresh = true)
        }
    }

    fun deleteBeat(id: String) {
        viewModelScope.launch {
            repository.deleteBeat(id)
            loadData(_companyId.value, forceRefresh = true)
        }
    }
}
