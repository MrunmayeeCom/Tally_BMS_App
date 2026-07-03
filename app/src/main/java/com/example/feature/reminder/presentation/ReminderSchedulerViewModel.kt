package com.example.feature.reminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.reminder.domain.IReminderRepository
import com.example.feature.reminder.domain.ReminderSchedulerRule
import com.example.feature.reminder.domain.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderSchedulerViewModel(
    private val repository: IReminderRepository
) : ViewModel() {

    private val _schedulersState = MutableStateFlow<UiState<List<ReminderSchedulerRule>>>(UiState.Loading)
    val schedulersState: StateFlow<UiState<List<ReminderSchedulerRule>>> = _schedulersState.asStateFlow()

    init {
        loadSchedulers()
    }

    fun loadSchedulers() {
        viewModelScope.launch {
            _schedulersState.value = UiState.Loading
            try {
                val list = repository.getReminderSchedulerRules()
                _schedulersState.value = UiState.Success(list)
            } catch (e: Exception) {
                _schedulersState.value = UiState.Error(e.message ?: "Failed to read scheduler settings.")
            }
        }
    }

    fun toggleScheduler(id: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                val success = repository.toggleReminderSchedulerRule(id, isActive)
                if (success) {
                    val currentState = _schedulersState.value
                    if (currentState is UiState.Success) {
                        val updated = currentState.data.map {
                            if (it.id == id) it.copy(isActive = isActive) else it
                        }
                        _schedulersState.value = UiState.Success(updated)
                    }
                }
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    fun addScheduler(ruleName: String, frequency: String, timeOfDay: String, dayOfWeekOrMonth: String?) {
        viewModelScope.launch {
            _schedulersState.value = UiState.Loading
            try {
                val rule = ReminderSchedulerRule(
                    id = "",
                    ruleName = ruleName,
                    frequency = frequency,
                    timeOfDay = timeOfDay,
                    dayOfWeekOrMonth = dayOfWeekOrMonth,
                    isActive = true
                )
                repository.createReminderSchedulerRule(rule)
                loadSchedulers()
            } catch (e: Exception) {
                _schedulersState.value = UiState.Error(e.message ?: "Failed to create scheduler configuration.")
            }
        }
    }
}
