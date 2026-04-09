package com.example.smartstock.data.model

data class AuthLoginRequestDto(
    val email: String,
    val password: String,
)

data class AuthLoginResponseDto(
    val token: String,
    val usuarioId: Int,
    val rolId: Int,
    val rolNombre: String,
    val nombre: String,
    val email: String,
)
