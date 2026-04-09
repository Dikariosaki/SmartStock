package com.example.smartstock.data.api

import com.example.smartstock.data.model.MovementDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MovementApiService {
    @GET("api/movimientos")
    suspend fun getMovements(): List<MovementDto>

    @GET("api/movimientos/inventario/{id}")
    suspend fun getMovementsByInventory(@Path("id") inventoryId: Int): List<MovementDto>
}
