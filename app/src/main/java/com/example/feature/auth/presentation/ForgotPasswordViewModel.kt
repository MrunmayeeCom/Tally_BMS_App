package com.example.feature.auth.presentation

import com.example.core.common.BaseViewModel
import com.example.core.common.Resource
import com.example.core.common.UiState
import com.example.feature.auth.domain.IAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

class ForgotPasswordViewModel(
    private val authRepository: IAuthRepository
) : BaseViewModel<String>() {

    private val _isCodeSent = MutableStateFlow(false)
    val isCodeSent: StateFlow<Boolean> = _isCodeSent.asStateFlow()

    fun requestResetCode(email: String) {
        launchSafe {
            authRepository.forgotPassword(email).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        updateState(UiState.Loading)
                    }
                    is Resource.Success -> {
                        _isCodeSent.value = true
                        updateState(UiState.Success(resource.data ?: "Verification code sent to email."))
                    }
                    is Resource.Error -> {
                        updateState(UiState.Error(resource.message ?: "Failed to request reset code."))
                    }
                }
            }
        }
    }

    fun resetPassword(email: String, otpCode: String, newPass: String) {
        launchSafe {
            authRepository.resetPassword(email, otpCode, newPass).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        updateState(UiState.Loading)
                    }
                    is Resource.Success -> {
                        updateState(UiState.Success(resource.data ?: "Password split/reset successfully."))
                    }
                    is Resource.Error -> {
                        updateState(UiState.Error(resource.message ?: "Failed to reset password."))
                    }
                }
            }
        }
    }

    fun resetState() {
        _isCodeSent.value = false
        updateState(UiState.Idle)
    }
}
