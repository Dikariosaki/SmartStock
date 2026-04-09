package com.example.smartstock.data.api

import com.example.smartstock.data.model.InventoryDto
import com.example.smartstock.data.model.InventoryUpdateRequestDto
import com.example.smartstock.data.model.MovementStockRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface InventoryApiService {
    @GET("api/inventarios")
    suspend fun getInventories(): List<InventoryDto>

    @GET("api/inventarios/{id}")
    suspend fun getInventoryById(@Path("id") id: Int): InventoryDto

    @PUT("api/inventarios/{id}")
    suspend fun updateInventory(
        @Path("id") id: Int,
        @Body request: InventoryUpdateRequestDto
    ): InventoryDto

    @PUT("api/inventarios/{id}/activate")
    suspend fun activateInventory(@Path("id") id: Int)

    @PUT("api/inventarios/{id}/deactivate")
    suspend fun deactivateInventory(@Path("id") id: Int)

    @POST("api/inventarios/entrada")
    suspend fun registerEntry(@Body request: MovementStockRequestDto)

    @POST("api/inventarios/salida")
    suspend fun registerExit(@Body request: MovementStockRequestDto)

    @POST("api/inventarios/averia")
    suspend fun registerDamage(@Body request: MovementStockRequestDto)
}
