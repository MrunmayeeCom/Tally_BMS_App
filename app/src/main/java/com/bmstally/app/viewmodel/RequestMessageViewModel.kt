package com.bmstally.app.viewmodel

import androidx.lifecycle.ViewModel
import com.bmstally.app.data.repository.BmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RequestMessageViewModel @Inject constructor(
    private val repository: BmsRepository
) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    fun setMessage(m: String) { _message.value = m; _status.value = null }

    fun sendRequest() {
        val msg = _message.value.trim()
        if (msg.length < 10) {
            _status.value = "Please describe your request (min 10 characters)"
            return
        }
        _sending.value = true
        repository.sendRequest(msg)
        _message.value = ""
        _status.value = "Request sent successfully to admin"
        _sending.value = false
    }

    fun clearStatus() { _status.value = null }
}
