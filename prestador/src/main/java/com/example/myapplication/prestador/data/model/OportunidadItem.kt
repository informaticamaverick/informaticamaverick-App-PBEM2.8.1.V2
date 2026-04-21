package com.example.myapplication.prestador.data.model

data class OportunidadItem(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val creadoEn: Long = 0L,
    val urgente: Boolean = false,
    val clienteId: String = "",
    val clienteNombre: String = "",
    val estado: String = "pendiente",
    val distanciaKm: Double = 0.0,
    val categoria: String = ""
)