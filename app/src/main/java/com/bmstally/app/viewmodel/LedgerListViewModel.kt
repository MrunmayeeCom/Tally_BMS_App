package com.bmstally.app.viewmodel

import androidx.lifecycle.ViewModel
import com.bmstally.app.data.repository.BmsRepository
import com.bmstally.app.model.Ledger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class LedgerListState(
    val ledgers: List<Ledger> = emptyList(),
    val searchQuery: String = "",
    val filterType: String = "all",
    val selectedGroup: String = "All Groups",
    val currentPage: Int = 1,
    val pageSize: Int = 10
) {
    val groups: List<String>
        get() = listOf("All Groups") + ledgers.map { it.parent_group ?: "" }.filter { it.isNotEmpty() }.distinct().sorted()

    val enrichedLedgers: List<Ledger>
        get() = ledgers

    val filteredLedgers: List<Ledger>
        get() = enrichedLedgers.filter { ledger ->
            val matchesSearch = ledger.name.contains(searchQuery, ignoreCase = true)
            val matchesType = filterType == "all" || ledger.nature == filterType
            val matchesGroup = selectedGroup == "All Groups" || ledger.parent_group == selectedGroup
            matchesSearch && matchesType && matchesGroup
        }

    val totalRecords: Int get() = filteredLedgers.size
    val totalPages: Int get() = maxOf(1, (totalRecords + pageSize - 1) / pageSize)

    val paginatedLedgers: List<Ledger>
        get() {
            val start = (currentPage - 1) * pageSize
            return filteredLedgers.drop(start).take(pageSize)
        }
}

@HiltViewModel
class LedgerListViewModel @Inject constructor(
    private val repository: BmsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LedgerListState())
    val state: StateFlow<LedgerListState> = _state.asStateFlow()

    fun loadLedgers() {
        _state.value = _state.value.copy(ledgers = repository.getLedgers())
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query, currentPage = 1)
    }

    fun setFilterType(type: String) {
        _state.value = _state.value.copy(filterType = type, currentPage = 1)
    }

    fun setSelectedGroup(group: String) {
        _state.value = _state.value.copy(selectedGroup = group, currentPage = 1)
    }

    fun setPage(page: Int) {
        _state.value = _state.value.copy(currentPage = page.coerceIn(1, _state.value.totalPages))
    }

    fun setPageSize(size: Int) {
        _state.value = _state.value.copy(pageSize = size, currentPage = 1)
    }
}
