package com.example.feature.reminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.session.SessionManager
import com.example.feature.crm.domain.ICrmRepository
import com.example.feature.crm.domain.CrmCustomer
import com.example.feature.outstanding.domain.IOutstandingRepository
import com.example.feature.outstanding.domain.OutstandingItem
import com.example.feature.reminder.domain.IReminderRepository
import com.example.feature.reminder.domain.ReminderTemplate
import com.example.feature.reminder.domain.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ManualReminderViewModel(
    private val reminderRepo: IReminderRepository,
    private val outstandingRepo: IOutstandingRepository,
    private val crmRepo: ICrmRepository,
    private val sessionManager: SessionManager,
    val initialPartyId: String,
    val initialBillId: String?
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CrmCustomer>>(emptyList())
    val customers: StateFlow<List<CrmCustomer>> = _customers.asStateFlow()

    private val _outstandingInvoices = MutableStateFlow<List<OutstandingItem>>(emptyList())
    val outstandingInvoices: StateFlow<List<OutstandingItem>> = _outstandingInvoices.asStateFlow()

    private val _templates = MutableStateFlow<List<ReminderTemplate>>(emptyList())
    val templates: StateFlow<List<ReminderTemplate>> = _templates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userRole = MutableStateFlow<String?>("Super Admin")
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _assigneeName = MutableStateFlow<String>("Current User")
    val assigneeName: StateFlow<String> = _assigneeName.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userRole.value = sessionManager.userRole.firstOrNull() ?: "Super Admin"
                _assigneeName.value = sessionManager.userName.firstOrNull() ?: "Administrator"

                // Load templates
                _templates.value = reminderRepo.getReminderTemplates()

                // Load customers from CRM
                _customers.value = crmRepo.getCustomers(null, null, null, 1, 100)

                // Load all outstanding receivable balances
                _outstandingInvoices.value = outstandingRepo.getOutstandingItems(null, "Receivable", null, null, 1, 100)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to synchronize CRM and Outstanding invoices: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearNotifications() {
        _successMessage.value = null
        _errorMessage.value = null
    }

    fun submitReminder(
        partyId: String,
        partyName: String,
        billId: String?,
        billNo: String?,
        type: String,
        message: String,
        scheduleDate: String,
        scheduleTime: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _successMessage.value = null
            _errorMessage.value = null
            try {
                if (partyId.isBlank() || partyName.isBlank()) {
                    _errorMessage.value = "Please select a customer first."
                    return@launch
                }
                if (message.isBlank()) {
                    _errorMessage.value = "Reminder text copy cannot be empty."
                    return@launch
                }

                val fullDateTime = "$scheduleDate $scheduleTime"
                val reminder = reminderRepo.createManualReminder(
                    partyId = partyId,
                    partyName = partyName,
                    billId = billId,
                    billNo = billNo,
                    type = type,
                    message = message,
                    scheduleDateTime = fullDateTime,
                    assigneeName = _assigneeName.value
                )
                _successMessage.value = "Ad-hoc ${reminder.type} Reminder Scheduled successfully for ${reminder.partyName}!"
            } catch (e: Exception) {
                _errorMessage.value = "Error scheduling reminder: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
