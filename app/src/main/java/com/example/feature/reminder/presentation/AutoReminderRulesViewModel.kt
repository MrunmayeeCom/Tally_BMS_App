package com.example.feature.reminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.reminder.domain.AutoReminderRule
import com.example.feature.reminder.domain.IReminderRepository
import com.example.feature.reminder.domain.ReminderTemplate
import com.example.feature.reminder.domain.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AutoReminderRulesViewModel(
    private val repository: IReminderRepository
) : ViewModel() {

    private val _rulesState = MutableStateFlow<UiState<List<AutoReminderRule>>>(UiState.Loading)
    val rulesState: StateFlow<UiState<List<AutoReminderRule>>> = _rulesState.asStateFlow()

    private val _templates = MutableStateFlow<List<ReminderTemplate>>(emptyList())
    val templates: StateFlow<List<ReminderTemplate>> = _templates.asStateFlow()

    init {
        loadRulesAndTemplates()
    }

    fun loadRulesAndTemplates() {
        viewModelScope.launch {
            _rulesState.value = UiState.Loading
            try {
                val templatesList = repository.getReminderTemplates()
                _templates.value = templatesList

                val rulesList = repository.getAutoReminderRules()
                _rulesState.value = UiState.Success(rulesList)
            } catch (e: Exception) {
                _rulesState.value = UiState.Error(e.message ?: "Failed to read auto rules.")
            }
        }
    }

    fun toggleRule(id: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                val success = repository.toggleAutoReminderRule(id, isActive)
                if (success) {
                    // Update UI state without full reload
                    val currentState = _rulesState.value
                    if (currentState is UiState.Success) {
                        val updatedList = currentState.data.map {
                            if (it.id == id) it.copy(isActive = isActive) else it
                        }
                        _rulesState.value = UiState.Success(updatedList)
                    }
                }
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    fun createRule(
        name: String,
        triggerType: String,
        triggerValue: String,
        commType: String,
        templateId: String
    ) {
        viewModelScope.launch {
            _rulesState.value = UiState.Loading
            try {
                val newRule = AutoReminderRule(
                    id = "",
                    name = name,
                    triggerType = triggerType,
                    triggerValue = triggerValue,
                    communicationType = commType,
                    templateId = templateId,
                    isActive = true
                )
                repository.createAutoReminderRule(newRule)
                loadRulesAndTemplates()
            } catch (e: Exception) {
                _rulesState.value = UiState.Error(e.message ?: "Failed to create rule.")
            }
        }
    }
}
