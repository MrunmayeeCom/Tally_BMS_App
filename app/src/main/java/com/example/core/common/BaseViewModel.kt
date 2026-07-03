package com.example.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<T> : ViewModel() {

    protected val _uiState = MutableStateFlow<UiState<T>>(UiState.Idle)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleException(throwable)
    }

    protected fun launchSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    protected open fun handleException(throwable: Throwable) {
        throwable.printStackTrace()
        val userFriendlyMessage = when (throwable) {
            is java.net.UnknownHostException -> "No internet connection. Operating offline mode."
            is java.net.SocketTimeoutException -> "Network timed out. Please try again."
            is retrofit2.HttpException -> "Server returned error: ${throwable.message()}"
            else -> throwable.localizedMessage ?: "An unexpected error occurred."
        }
        _uiState.value = UiState.Error(userFriendlyMessage)
    }

    protected fun updateState(state: UiState<T>) {
        _uiState.value = state
    }
}
