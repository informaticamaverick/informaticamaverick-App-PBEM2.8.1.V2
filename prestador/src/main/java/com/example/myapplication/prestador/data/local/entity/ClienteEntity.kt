package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val email: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)