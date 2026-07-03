package com.example.feature.followup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.session.SessionManager
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.crm.domain.ICrmRepository
import com.example.feature.followup.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 1. Follow-Up Dashboard ViewModel
class FollowUpDashboardViewModel(
    private val repository: IFollowUpRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FollowUpDashboardStats>>(UiState.Loading)
    val uiState: StateFlow<UiState<FollowUpDashboardStats>> = _uiState.asStateFlow()

    private val _urgentFollowUps = MutableStateFlow<List<FollowUp>>(emptyList())
    val urgentFollowUps: StateFlow<List<FollowUp>> = _urgentFollowUps.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val stats = repository.getDashboardStats()
                // Fetch today/overdue list for quick visual actions on dashboard
                val urgent = repository.getFollowUps(status = "In Progress")
                    .take(5)
                _urgentFollowUps.value = urgent
                _uiState.value = UiState.Success(stats)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load follow-up dashboard summaries.")
            }
        }
    }
}

// 2. Follow-Up List ViewModel
class FollowUpListViewModel(
    private val repository: IFollowUpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<FollowUp>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<FollowUp>>> = _uiState.asStateFlow()

    // Filters and sorting states
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("All")        // "All", "Call", "Visit", "Email", "WhatsApp"
    val filterPriority = MutableStateFlow("All")    // "All", "High", "Medium", "Low"
    val filterStatus = MutableStateFlow("All")      // "All", "Open", "In Progress", "Waiting Response", "Promised Payment", "Completed", "Cancelled"
    val filterAssignedUser = MutableStateFlow("All")// "All" or names of users
    val sortBy = MutableStateFlow("Due Date (Earliest)") // "Due Date (Earliest)", "Due Date (Latest)", "Priority (High to Low)", "Customer (A-Z)"

    init {
        loadFollowUps()
    }

    fun loadFollowUps() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val items = repository.getFollowUps(
                    query = searchQuery.value,
                    type = if (filterType.value == "All") null else filterType.value,
                    priority = if (filterPriority.value == "All") null else filterPriority.value,
                    status = if (filterStatus.value == "All") null else filterStatus.value,
                    assignedUser = if (filterAssignedUser.value == "All") null else filterAssignedUser.value,
                    sort = sortBy.value
                )
                _uiState.value = UiState.Success(items)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred loading follow-up registries.")
            }
        }
    }

    fun updateFilters(
        query: String = searchQuery.value,
        type: String = filterType.value,
        priority: String = filterPriority.value,
        status: String = filterStatus.value,
        user: String = filterAssignedUser.value,
        sort: String = sortBy.value
    ) {
        searchQuery.value = query
        filterType.value = type
        filterPriority.value = priority
        filterStatus.value = status
        filterAssignedUser.value = user
        sortBy.value = sort
        loadFollowUps()
    }
}

// 3. Create Follow-Up ViewModel (CRM integrated)
class CreateFollowUpViewModel(
    private val repository: IFollowUpRepository,
    private val crmRepository: ICrmRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CrmCustomer>>(emptyList())
    val customers: StateFlow<List<CrmCustomer>> = _customers.asStateFlow()

    private val _creationState = MutableStateFlow<FormState>(FormState.Idle)
    val creationState: StateFlow<FormState> = _creationState.asStateFlow()

    // Pre-allocated mock user list for role assignments
    val availableUsers = listOf(
        Pair("user_rep01", "Rajesh Kumar"),
        Pair("user_rep02", "Anita Desai"),
        Pair("user_rep03", "Amit Patel"),
        Pair("user_admin", "Super Admin")
    )

    init {
        loadCrmCustomers()
    }

    private fun loadCrmCustomers() {
        viewModelScope.launch {
            try {
                val list = crmRepository.getCustomers(null, "All", "Name (A-Z)", 1, 100)
                _customers.value = list
            } catch (e: Exception) {
                // Return fallback mock lists for picker safety
                _customers.value = listOf(
                    CrmCustomer("cust_01", "Acme Distributors Pvt Ltd", 284300.0, 159300.0, "Risky", "DL", "Rajesh Kumar"),
                    CrmCustomer("cust_02", "Starlight Retail Enterprises", 42000.0, 42000.0, "Active", "MH", "Anita Desai"),
                    CrmCustomer("cust_03", "Vertex Corporate Solutions", 750000.0, 430000.0, "Cr Overdue", "DL", "Rajesh Kumar")
                )
            }
        }
    }

    fun submitFollowUp(
        customerId: String,
        customerName: String,
        type: String,
        notes: String,
        priority: String,
        dueDate: String,
        assignedUserIndex: Int
    ) {
        if (customerId.isBlank() || notes.isBlank() || dueDate.isBlank()) {
            _creationState.value = FormState.Error("Please ensure Customer, Notes, and Due Date fields are completely entered.")
            return
        }

        viewModelScope.launch {
            _creationState.value = FormState.Submitting
            try {
                val (assignedId, assignedName) = availableUsers[assignedUserIndex]
                
                repository.createFollowUp(
                    customerId = customerId,
                    customerName = customerName,
                    type = type,
                    notes = notes,
                    priority = priority,
                    dueDate = dueDate,
                    assignedUserId = assignedId,
                    assignedUserName = assignedName
                )
                _creationState.value = FormState.Success
            } catch (e: Exception) {
                _creationState.value = FormState.Error(e.message ?: "Failed to compile and register follow-up task.")
            }
        }
    }

    fun resetState() {
        _creationState.value = FormState.Idle
    }

    sealed interface FormState {
        object Idle : FormState
        object Submitting : FormState
        object Success : FormState
        data class Error(val message: String) : FormState
    }
}

// 4. Follow-Up Detail ViewModel (Status transition and Notes interaction)
class FollowUpDetailViewModel(
    private val repository: IFollowUpRepository,
    private val sessionManager: SessionManager,
    val followUpId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FollowUpDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<FollowUpDetail>> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val details = repository.getFollowUpDetail(followUpId)
                _uiState.value = UiState.Success(details)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to download complete customer interaction history.")
            }
        }
    }

    fun updateStatus(status: String, outcome: String?, promisedAmount: Double? = null, promisedDate: String? = null) {
        viewModelScope.launch {
            _actionState.value = ActionState.Processing
            try {
                repository.updateFollowUpStatus(
                    id = followUpId,
                    status = status,
                    outcome = outcome,
                    promisedAmount = promisedAmount,
                    promisedDate = promisedDate
                )
                _actionState.value = ActionState.Done
                loadDetail() // reload UI State
            } catch (e: Exception) {
                _actionState.value = ActionState.Failed(e.message ?: "Workflow progression command declined by multi-tenant gateway.")
            }
        }
    }

    fun addTimelineNote(notes: String) {
        if (notes.isBlank()) return
        viewModelScope.launch {
            _actionState.value = ActionState.Processing
            try {
                val operatorName = runBlocking { sessionManager.userName.firstOrNull() } ?: "Supervisor"
                repository.addFollowUpNote(followUpId, notes, operatorName)
                _actionState.value = ActionState.Done
                loadDetail() // Sync ui node list
            } catch (e: Exception) {
                _actionState.value = ActionState.Failed(e.message ?: "Interaction note posting declined.")
            }
        }
    }

    fun clearActionState() {
        _actionState.value = ActionState.Idle
    }

    fun getUserRole() = runBlocking { sessionManager.userRole.firstOrNull() } ?: "Sales Executive"

    sealed interface ActionState {
        object Idle : ActionState
        object Processing : ActionState
        object Done : ActionState
        data class Failed(val message: String) : ActionState
    }
}

// 5. Calendar View ViewModel
class FollowUpCalendarViewModel(
    private val repository: IFollowUpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Map<String, List<FollowUp>>>>(UiState.Loading)
    val uiState: StateFlow<UiState<Map<String, List<FollowUp>>>> = _uiState.asStateFlow()

    // Mode: "Daily", "Weekly", "Monthly"
    val calendarMode = MutableStateFlow("Weekly")

    init {
        loadCalendarObjects()
    }

    fun setMode(mode: String) {
        calendarMode.value = mode
        loadCalendarObjects()
    }

    fun loadCalendarObjects() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Fetch follow-ups for the pipeline range
                val list = repository.getFollowUps()
                
                // Group follow-ups by due date string
                val grouped = list.groupBy { it.dueDate }
                _uiState.value = UiState.Success(grouped)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to generate visual calendar timelines.")
            }
        }
    }
}
