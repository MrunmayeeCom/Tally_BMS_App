package com.example.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import com.example.core.common.BaseViewModel
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.feature.auth.domain.IAuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn

class LoginViewModel(
    private val authRepository: IAuthRepository
) : BaseViewModel<Unit>() {

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun performLogin(email: String, password: String) {
        launchSafe {
            authRepository.login(email, password).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        updateState(UiState.Loading)
                    }
                    is Resource.Success -> {
                        updateState(UiState.Success(Unit))
                    }
                    is Resource.Error -> {
                        updateState(UiState.Error(resource.message ?: "Authentication failed."))
                    }
                }
            }
        }
    }

    fun logout() {
        launchSafe {
            authRepository.logout()
            updateState(UiState.Idle)
        }
    }
}

