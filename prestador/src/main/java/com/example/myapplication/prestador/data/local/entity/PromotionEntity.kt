package com.example.myapplication.prestador.data.local.entity

import android.icu.text.CaseMap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad de base de datos: Promotion (Promoción / Historia)
 *
 * Esta tabla guarda todas las promociones e historias que el prestador crea.
 * Cada Fila = Una promción publicada
 * Importante: Room NO soporta listas directamente, por eso:
 * -imageUrls se guarda como String JSON: ["url1", "url2"
 * CATEGORIES SE GUARDA COMO String JSON:
 * Despues los convertivos  de vuelta a List<String> cuando los leemos
 *
 * Los enums tambien se guardan como String
 */

@Entity(
    tableName = "promotions",
    foreignKeys = [
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["providerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["providerId"]),
        Index(value = ["providerId", "createdAt"])
    ]
)
data class PromotionEntity(
    @PrimaryKey
    val id: String,
    val providerId: String,
    val providerName: String,
    val providerImageUrl: String? = null,
    val type: String,  //STORY o PROMOTIOON
    val title: String,
    val description: String,
    val imageUrls: String, //JSON string de lista
    val discount: Int? = null,
    val categories: String, //JSON string de lista
    val createdAt: Long,
    val expiresAt: Long,
    val status: String, // "SCTIVE", "EXPIRED"
    val likes: Int = 0,
    val views: Int = 0,
    val rating: Float = 0f
)

