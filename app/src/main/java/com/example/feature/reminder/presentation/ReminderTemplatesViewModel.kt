package com.example.feature.reminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.reminder.domain.IReminderRepository
import com.example.feature.reminder.domain.ReminderTemplate
import com.example.feature.reminder.domain.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderTemplatesViewModel(
    private val repository: IReminderRepository
) : ViewModel() {

    private val _templatesState = MutableStateFlow<UiState<List<ReminderTemplate>>>(UiState.Loading)
    val templatesState: StateFlow<UiState<List<ReminderTemplate>>> = _templatesState.asStateFlow()

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            _templatesState.value = UiState.Loading
            try {
                val list = repository.getReminderTemplates()
                _templatesState.value = UiState.Success(list)
            } catch (e: Exception) {
                _templatesState.value = UiState.Error(e.message ?: "Failed to read reminder templates.")
            }
        }
    }

    fun addTemplate(name: String, type: String, subject: String?, body: String) {
        viewModelScope.launch {
            _templatesState.value = UiState.Loading
            try {
                val newTemplate = ReminderTemplate(
                    id = "",
                    name = name,
                    type = type,
                    subject = if (type == "Email") subject else null,
                    body = body
                )
                repository.createReminderTemplate(newTemplate)
                loadTemplates()
            } catch (e: Exception) {
                _templatesState.value = UiState.Error(e.message ?: "Failed to commit new template.")
            }
        }
    }

    fun editTemplate(id: String, name: String, type: String, subject: String?, body: String) {
        viewModelScope.launch {
            _templatesState.value = UiState.Loading
            try {
                val updatedTemplate = ReminderTemplate(
                    id = id,
                    name = name,
                    type = type,
                    subject = if (type == "Email") subject else null,
                    body = body
                )
                repository.updateReminderTemplate(updatedTemplate)
                loadTemplates()
            } catch (e: Exception) {
                _templatesState.value = UiState.Error(e.message ?: "Failed to update target template.")
            }
        }
    }
}
