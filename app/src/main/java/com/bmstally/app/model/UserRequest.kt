package com.bmstally.app.model

data class UserRequest(
    val id: Int,
    val userName: String,
    val userEmail: String,
    val message: String,
    val createdAt: String,
    val isRead: Boolean = false
)
