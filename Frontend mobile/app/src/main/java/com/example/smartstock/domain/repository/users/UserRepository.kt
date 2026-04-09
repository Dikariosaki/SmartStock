package com.example.smartstock.domain.repository

import com.example.smartstock.domain.model.CreateUserRequest
import com.example.smartstock.domain.model.RoleOption
import com.example.smartstock.domain.model.User

interface UserRepository {
    suspend fun getUsers(): List<User>

    suspend fun getUserById(id: Int): User

    suspend fun createUser(request: CreateUserRequest): User

    suspend fun updateUser(
        id: Int,
        request: CreateUserRequest
    ): User

    suspend fun deleteUser(id: Int)

    suspend fun activateUser(id: Int)

    suspend fun deactivateUser(id: Int)

    suspend fun getRoles(): List<RoleOption>
}
