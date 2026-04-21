package com.example.myapplication.prestador.data.model

data class ServicioFirebase(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val colorHex: String = "",
    val imageUrl: String = "",
    val superCategory: String = "",
    val superCategoryIcon: String = "",
    val updatedAt: Long = 0L
)