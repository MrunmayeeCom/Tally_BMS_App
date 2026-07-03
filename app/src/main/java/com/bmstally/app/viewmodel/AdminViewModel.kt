package com.bmstally.app.viewmodel

import androidx.lifecycle.ViewModel
import com.bmstally.app.data.MockDataService
import com.bmstally.app.data.repository.BmsRepository
import com.bmstally.app.model.DeleteHistoryRecord
import com.bmstally.app.model.UserRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: BmsRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<MockDataService.AppUser>>(emptyList())
    val users: StateFlow<List<MockDataService.AppUser>> = _users.asStateFlow()

    private val _deleteHistory = MutableStateFlow<List<DeleteHistoryRecord>>(emptyList())
    val deleteHistory: StateFlow<List<DeleteHistoryRecord>> = _deleteHistory.asStateFlow()

    private val _requests = MutableStateFlow<List<UserRequest>>(emptyList())
    val requests: StateFlow<List<UserRequest>> = _requests.asStateFlow()

    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun setTab(tab: Int) { _selectedTab.value = tab }

    fun loadUsers(tenantId: String) {
        _users.value = MockDataService.getUsers(tenantId)
    }

    fun setUserSearchQuery(q: String) { _userSearchQuery.value = q }

    val filteredUsers: List<MockDataService.AppUser>
        get() {
            val q = _userSearchQuery.value
            return if (q.isBlank()) _users.value
            else _users.value.filter { it.name.contains(q, ignoreCase = true) || it.email.contains(q, ignoreCase = true) }
        }

    fun deleteUser(userId: String) {
        _users.value = _users.value.filter { it.id != userId }
    }

    fun loadDeleteHistory() {
        _deleteHistory.value = repository.getDeleteHistory()
    }

    fun restoreRecord(id: Int) {
        repository.restoreDeleteHistory(id)
        _deleteHistory.value = _deleteHistory.value.filter { it.id != id }
    }

    fun loadRequests() {
        _requests.value = repository.getRequests()
    }
}
