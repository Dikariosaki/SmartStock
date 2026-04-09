package com.example.smartstock.data.api

import com.example.smartstock.data.model.RoleDto
import retrofit2.http.GET

interface RoleApiService {
    @GET("api/roles")
    suspend fun getRoles(): List<RoleDto>
}
