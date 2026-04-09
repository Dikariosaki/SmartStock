package com.example.smartstock.data.repository

import com.example.smartstock.data.api.UserApiService
import com.example.smartstock.data.model.UserDto
import com.example.smartstock.data.api.RoleApiService
import com.example.smartstock.data.model.toDomain
import com.example.smartstock.data.model.toDto
import com.example.smartstock.domain.model.CreateUserRequest
import com.example.smartstock.domain.model.RoleOption
import com.example.smartstock.domain.model.User
import com.example.smartstock.domain.repository.UserRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class UserRepositoryImpl
    @Inject
    constructor(
        private val userApiService: UserApiService,
        private val roleApiService: RoleApiService
    ) : UserRepository {
        override suspend fun getUsers(): List<User> = parseUsersResponse(userApiService.getUsers()).map { it.toDomain() }

        override suspend fun getUserById(id: Int): User = userApiService.getUserById(id).toDomain()

        override suspend fun createUser(request: CreateUserRequest): User =
            userApiService.createUser(request.toDto()).toDomain()

        override suspend fun updateUser(
            id: Int,
            request: CreateUserRequest
        ): User = userApiService.updateUser(id, request.toDto()).toDomain()

        override suspend fun deleteUser(id: Int) {
            userApiService.deleteUser(id)
        }

        override suspend fun activateUser(id: Int) {
            userApiService.activateUser(id)
        }

        override suspend fun deactivateUser(id: Int) {
            userApiService.deactivateUser(id)
        }

        override suspend fun getRoles(): List<RoleOption> = roleApiService.getRoles().map { it.toDomain() }

        private fun parseUsersResponse(response: JsonElement): List<UserDto> {
            val gson = Gson()

            if (response.isJsonArray) {
                val listType = object : TypeToken<List<UserDto>>() {}.type
                return gson.fromJson(response, listType)
            }

            if (response.isJsonObject) {
                val dataElement = response.asJsonObject.get("data")
                if (dataElement != null && dataElement.isJsonArray) {
                    val listType = object : TypeToken<List<UserDto>>() {}.type
                    return gson.fromJson(dataElement, listType)
                }
            }

            throw JsonParseException("Formato de respuesta de usuarios no soportado")
        }
    }
