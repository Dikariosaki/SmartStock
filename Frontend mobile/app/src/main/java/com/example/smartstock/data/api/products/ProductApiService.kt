package com.example.smartstock.data.api

import com.example.smartstock.data.model.ProductCreateRequestDto
import com.example.smartstock.data.model.ProductDto
import com.example.smartstock.data.model.ProductUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProductApiService {
    @GET("api/productos")
    suspend fun getProducts(): List<ProductDto>

    @GET("api/productos/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductDto

    @POST("api/productos")
    suspend fun createProduct(@Body request: ProductCreateRequestDto): ProductDto

    @PUT("api/productos/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body request: ProductUpdateRequestDto
    ): ProductDto

    @PUT("api/productos/{id}/activate")
    suspend fun activateProduct(@Path("id") id: Int)

    @PUT("api/productos/{id}/deactivate")
    suspend fun deactivateProduct(@Path("id") id: Int)
}
