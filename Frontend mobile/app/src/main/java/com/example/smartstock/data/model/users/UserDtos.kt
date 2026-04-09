package com.example.smartstock.data.model

data class UserDto(
    val usuarioId: Int?,
    val rolId: Int,
    val rolNombre: String?,
    val nombre: String,
    val cedula: Int,
    val email: String,
    val estado: Boolean,
    val telefono: String?
)

data class CreateUserRequestDto(
    val rolId: Int,
    val nombre: String,
    val cedula: Int,
    val email: String,
    val password: String? = null,
    val telefono: String? = null,
    val estado: Boolean = true
)

data class UserStatusRequestDto(
    val estado: Boolean
)

data class RoleDto(
    val rolId: Int,
    val nombre: String,
)
