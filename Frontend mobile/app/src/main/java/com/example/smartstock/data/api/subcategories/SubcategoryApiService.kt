package com.example.smartstock.data.api

import com.example.smartstock.data.model.SubcategoryDto
import retrofit2.http.GET

interface SubcategoryApiService {
    @GET("api/subcategorias")
    suspend fun getSubcategories(): List<SubcategoryDto>
}
