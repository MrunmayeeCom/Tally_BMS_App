package com.example.feature.salesteam.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.session.SessionManager
import com.example.feature.crm.domain.ICrmRepository
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.salesteam.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// === UI States ===

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

// === 1. Sales Team Dashboard ViewModel ===

class SalesTeamDashboardViewModel(
    private val repository: ISalesTeamRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SalesTeamDashboardStats>>(UiState.Loading)
    val uiState: StateFlow<UiState<SalesTeamDashboardStats>> = _uiState.asStateFlow()

    private val _activities = MutableStateFlow<List<SalesTeamActivityEvent>>(emptyList())
    val activities: StateFlow<List<SalesTeamActivityEvent>> = _activities.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val companyId = sessionManager.companyId.firstOrNull() ?: "comp_01"
                val stats = repository.getDashboardStats(companyId)
                _uiState.value = UiState.Success(stats)

                val acts = repository.getActivityFeed(companyId)
                _activities.value = acts.take(10) // show top 10 on dashboard
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to fetch Executive coordinates: ${e.message}")
            }
        }
    }
}

// === 2. Manage Users ViewModel ===

class ManageUsersViewModel(
    private val repository: ISalesTeamRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<SalesTeamMember>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SalesTeamMember>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTerritory = MutableStateFlow("All")
    val selectedTerritory: StateFlow<String> = _selectedTerritory.asStateFlow()

    private val _selectedRole = MutableStateFlow("All")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    init {
        loadMembers()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadMembers()
    }

    fun setTerritory(territory: String) {
        _selectedTerritory.value = territory
        loadMembers()
    }

    fun setRole(role: String) {
        _selectedRole.value = role
        loadMembers()
    }

    fun loadMembers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val companyId = sessionManager.companyId.firstOrNull() ?: "comp_01"
                val members = repository.getTeamMembers(
                    companyId = companyId,
                    query = _searchQuery.value,
                    territory = _selectedTerritory.value,
                    role = _selectedRole.value
                )
                _uiState.value = UiState.Success(members)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to query member registrar")
            }
        }
    }

    fun updateMember(userId: String, status: String, territory: String, role: String) {
        viewModelScope.launch {
            repository.updateUserStatusAndTerritory(userId, status, territory, role)
            loadMembers()
        }
    }
}

// === 3. User Detail ViewModel ===

class UserDetailViewModel(
    private val repository: ISalesTeamRepository,
    private val userId: String
) : ViewModel() {

    private val _memberState = MutableStateFlow<UiState<SalesTeamMember>>(UiState.Loading)
    val memberState: StateFlow<UiState<SalesTeamMember>> = _memberState.asStateFlow()

    private val _performanceState = MutableStateFlow<UiState<SalesRepPerformanceMetrics>>(UiState.Loading)
    val performanceState: StateFlow<UiState<SalesRepPerformanceMetrics>> = _performanceState.asStateFlow()

    private val _checkInHistory = MutableStateFlow<List<CheckInRecord>>(emptyList())
    val checkInHistory: StateFlow<List<CheckInRecord>> = _checkInHistory.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _memberState.value = UiState.Loading
            _performanceState.value = UiState.Loading
            try {
                val member = repository.getMemberDetail(userId)
                if (member != null) {
                    _memberState.value = UiState.Success(member)
                } else {
                    _memberState.value = UiState.Error("Member record not found")
                }

                val perf = repository.getMemberPerformance(userId, "Monthly")
                _performanceState.value = UiState.Success(perf)

                val history = repository.getCheckInHistory(userId, "comp_01")
                _checkInHistory.value = history
            } catch (e: Exception) {
                _memberState.value = UiState.Error(e.message ?: "Network Error")
                _performanceState.value = UiState.Error(e.message ?: "Network Error")
            }
        }
    }
}

// === 4. Check-In / Check-Out ViewModel ===

class CheckInCheckOutViewModel(
    private val repository: ISalesTeamRepository,
    private val crmRepository: ICrmRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentCheckIn = MutableStateFlow<CheckInRecord?>(null)
    val currentCheckIn: StateFlow<CheckInRecord?> = _currentCheckIn.asStateFlow()

    private val _customers = MutableStateFlow<List<CrmCustomer>>(emptyList())
    val customers: StateFlow<List<CrmCustomer>> = _customers.asStateFlow()

    private val _submittingState = MutableStateFlow<Boolean>(false)
    val submittingState: StateFlow<Boolean> = _submittingState.asStateFlow()

    // GPS coordinate mock/state
    private val _latitude = MutableStateFlow(18.9224)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(72.8341)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _address = MutableStateFlow("Colaba Gateway area, Mumbai")
    val address: StateFlow<String> = _address.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        viewModelScope.launch {
            try {
                // Get logged in userId
                val currentRole = sessionManager.userRole.firstOrNull() ?: "Sales Executive"
                val userName = sessionManager.userName.firstOrNull() ?: "Rajesh Kumar"
                
                // For simulator clarity, use mock rep Rajesh Kumar's key
                val userId = "user_01" 
                
                val record = repository.getCurrentCheckIn(userId)
                _currentCheckIn.value = record

                val list = crmRepository.getCustomers(null, null, null, 1, 100)
                _customers.value = list
            } catch (e: Exception) {
                Log.e("CheckInCheckOutVM", "Error initializing states", e)
            }
        }
    }

    fun simulateNewLocation(lat: Double, lon: Double, addr: String) {
        _latitude.value = lat
        _longitude.value = lon
        _address.value = addr
    }

    fun checkIn(customerId: String, customerName: String, remarks: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _submittingState.value = true
            try {
                val userName = sessionManager.userName.firstOrNull() ?: "Rajesh Kumar"
                val userId = "user_01"
                
                repository.performCheckIn(
                    userId = userId,
                    userName = userName,
                    customerId = customerId,
                    customerName = customerName,
                    latitude = _latitude.value,
                    longitude = _longitude.value,
                    address = _address.value,
                    remarks = remarks
                )
                refreshState()
                onComplete()
            } catch (e: Exception) {
                Log.e("CheckInVM", "Check-in failure", e)
            } finally {
                _submittingState.value = false
            }
        }
    }

    fun checkOut(remarks: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val original = _currentCheckIn.value ?: return@launch
            _submittingState.value = true
            try {
                repository.performCheckOut(
                    checkInId = original.id,
                    latitude = _latitude.value,
                    longitude = _longitude.value,
                    address = _address.value,
                    remarks = remarks
                )
                refreshState()
                onComplete()
            } catch (e: Exception) {
                Log.e("CheckOutVM", "Check-out failure", e)
            } finally {
                _submittingState.value = false
            }
        }
    }
}

// === 5. Customer Visit ViewModel ===

class CustomerVisitViewModel(
    private val repository: ISalesTeamRepository,
    private val crmRepository: ICrmRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CrmCustomer>>(emptyList())
    val customers: StateFlow<List<CrmCustomer>> = _customers.asStateFlow()

    private val _visitHistory = MutableStateFlow<List<CustomerVisit>>(emptyList())
    val visitHistory: StateFlow<List<CustomerVisit>> = _visitHistory.asStateFlow()

    private val _submittingState = MutableStateFlow<Boolean>(false)
    val submittingState: StateFlow<Boolean> = _submittingState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val companyId = sessionManager.companyId.firstOrNull() ?: "comp_01"
                val custs = crmRepository.getCustomers(null, null, null, 1, 100)
                _customers.value = custs

                val history = repository.getVisitHistory(companyId)
                _visitHistory.value = history
            } catch (e: Exception) {
                Log.e("CustomerVisitVM", "Error fetching visit CRM profiles", e)
            }
        }
    }

    fun createVisit(
        customerId: String,
        customerName: String,
        notes: String,
        outcomeBadge: String,
        collectedAmount: Double,
        nextActionPlanned: String?,
        nextVisitDate: String?,
        lat: Double,
        lon: Double,
        address: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _submittingState.value = true
            try {
                val userName = sessionManager.userName.firstOrNull() ?: "Rajesh Kumar"
                val userId = "user_01"

                repository.createCustomerVisit(
                    customerId = customerId,
                    customerName = customerName,
                    userId = userId,
                    userName = userName,
                    notes = notes,
                    outcomeBadge = outcomeBadge,
                    collectedAmount = collectedAmount,
                    nextActionPlanned = nextActionPlanned,
                    nextVisitDate = nextVisitDate,
                    latitude = lat,
                    longitude = lon,
                    address = address
                )
                loadData()
                onSuccess()
            } catch (e: Exception) {
                Log.e("CustomerVisitVM", "Error logging visit outcome", e)
            } finally {
                _submittingState.value = false
            }
        }
    }
}

// === 6. Activity Feed ViewModel ===

class ActivityFeedViewModel(
    private val repository: ISalesTeamRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<SalesTeamActivityEvent>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SalesTeamActivityEvent>>> = _uiState.asStateFlow()

    private val _filterType = MutableStateFlow("All")
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    init {
        loadFeed()
    }

    fun setFilter(type: String) {
        _filterType.value = type
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val companyId = sessionManager.companyId.firstOrNull() ?: "comp_01"
                val list = repository.getActivityFeed(companyId = companyId, filterType = _filterType.value)
                _uiState.value = UiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to request activity stream")
            }
        }
    }
}

// === 7. Performance Dashboard ViewModel ===

class PerformanceDashboardViewModel(
    private val repository: ISalesTeamRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _timeframe = MutableStateFlow("Monthly")
    val timeframe: StateFlow<String> = _timeframe.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<SalesRepPerformanceMetrics>>(UiState.Loading)
    val uiState: StateFlow<UiState<SalesRepPerformanceMetrics>> = _uiState.asStateFlow()

    init {
        loadPerformance()
    }

    fun setTimeframe(tf: String) {
        _timeframe.value = tf
        loadPerformance()
    }

    fun loadPerformance() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val metrics = repository.getMemberPerformance("user_01", _timeframe.value)
                _uiState.value = UiState.Success(metrics)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error calculating targeted achievements: ${e.message}")
            }
        }
    }
}
