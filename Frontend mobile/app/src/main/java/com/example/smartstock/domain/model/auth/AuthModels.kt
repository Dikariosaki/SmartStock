package com.example.smartstock.domain.model

data class SessionUser(
    val token: String,
    val userId: Int,
    val roleId: Int,
    val roleName: String,
    val name: String,
    val email: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)
