package com.example.smartstock.domain.model

data class User(
    val id: Int,
    val roleId: Int,
    val roleName: String?,
    val name: String,
    val cedula: Int,
    val email: String,
    val status: Boolean,
    val phone: String?
)

data class RoleOption(
    val id: Int,
    val name: String,
)

data class CreateUserRequest(
    val roleId: Int,
    val name: String,
    val cedula: Int,
    val email: String,
    val password: String? = null,
    val phone: String? = null,
    val status: Boolean = true
)
