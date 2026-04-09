package com.example.smartstock.data.model

data class TaskDto(
    val tareaId: Int?,
    val titulo: String,
    val descripcion: String?,
    val asignadoA: Int?,
    val asignadoNombre: String?,
    val fechaCreacion: String,
    val fechaFin: String?,
    val estado: Boolean
)

data class TaskCreateRequestDto(
    val titulo: String,
    val descripcion: String?,
    val asignadoA: Int?
)

data class TaskUpdateRequestDto(
    val titulo: String,
    val descripcion: String?,
    val asignadoA: Int?,
    val estado: Boolean
)
