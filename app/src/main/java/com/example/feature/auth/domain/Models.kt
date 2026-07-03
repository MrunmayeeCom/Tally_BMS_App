package com.example.feature.auth.domain

data class UserDomain(
    val email: String,
    val role: String,
    val userName: String
)

data class CompanyDomain(
    val companyGuid: String,
    val name: String
)
