package com.example.smartstock.data.api

import com.example.smartstock.data.model.CreateUserRequestDto
import com.example.smartstock.data.model.UserDto
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface UserApiService {
    @GET("api/usuarios")
    suspend fun getUsers(): JsonElement

    @GET("api/usuarios/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserDto

    @POST("api/usuarios")
    suspend fun createUser(@Body request: CreateUserRequestDto): UserDto

    @PUT("api/usuarios/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: CreateUserRequestDto
    ): UserDto

    @retrofit2.http.DELETE("api/usuarios/{id}")
    suspend fun deleteUser(@Path("id") id: Int)

    @PUT("api/usuarios/{id}/activate")
    suspend fun activateUser(@Path("id") id: Int)

    @PUT("api/usuarios/{id}/deactivate")
    suspend fun deactivateUser(@Path("id") id: Int)
}
