package com.example.myapplication.prestador.data.model

import java.util.UUID

/**
 * Modelo de datos para las promociones/publicaciones del prestador.
 * Compatible con el modelo Promotion del cliente.
 */
data class ProviderPromotion(
    val id: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val providerImageUrl: String? = null,
    
    // Tipo de publicación
    val type: PromotionType = PromotionType.PROMOTION,
    
    // Contenido
    val title: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(), // Hasta 3 imágenes
    
    // Descuento y categorías
    val discount: Int? = null, // Porcentaje (0-100)
    val categories: List<String> = emptyList(), // Servicios que ofrece
    
    // Fechas y duración
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis(),
    
    // Estado
    val status: PromotionStatus = PromotionStatus.ACTIVE,
    
    // Métricas
    val likes: Int = 0,
    val views: Int = 0,
    var isLiked: Boolean = false,
    
    // Rating heredado del prestador
    val rating: Float = 0f
) {
    companion object {
        /**
         * Crea una nueva promoción con ID único generado
         */
        fun create(
            providerId: String,
            providerName: String,
            providerImageUrl: String? = null,
            type: PromotionType = PromotionType.PROMOTION,
            title: String = "",
            description: String = "",
            imageUrls: List<String> = emptyList(),
            discount: Int? = null,
            categories: List<String> = emptyList(),
            rating: Float = 0f
        ): ProviderPromotion {
            return ProviderPromotion(
                id = UUID.randomUUID().toString(),
                providerId = providerId,
                providerName = providerName,
                providerImageUrl = providerImageUrl,
                type = type,
                title = title,
                description = description,
                imageUrls = imageUrls,
                discount = discount,
                categories = categories,
                expiresAt = calculateExpirationDate(type),
                rating = rating
            )
        }
        
        /**
         * Calcula la fecha de expiración según el tipo de promoción
         */
        fun calculateExpirationDate(type: PromotionType): Long {
            val now = System.currentTimeMillis()
            val expirationMillis = when (type) {
                PromotionType.STORY -> 24 * 60 * 60 * 1000L // 24 horas
                PromotionType.PROMOTION -> 7 * 24 * 60 * 60 * 1000L // 7 días
            }
            return now + expirationMillis
        }
    }
    
    /**
     * Verifica si la promoción está expirada
     */
    fun isExpired(): Boolean {
        return System.currentTimeMillis() >= expiresAt
    }
    
    /**
     * Obtiene las horas restantes antes de expirar
     */
    fun hoursUntilExpiration(): Long {
        val millisRemaining = expiresAt - System.currentTimeMillis()
        return if (millisRemaining > 0) millisRemaining / (1000 * 3600) else 0
    }
    
    /**
     * Convierte a formato compatible con el cliente
     */
    fun toClientFormat(): Map<String, Any?> {
        return mapOf(
            "id" to id.hashCode(),
            "imageUrls" to imageUrls,
            "providerImageUrl" to providerImageUrl,
            "providerName" to providerName,
            "description" to description,
            "providerId" to providerId,
            "categories" to categories,
            "rating" to rating,
            "likes" to likes,
            "isLiked" to isLiked,
            "discount" to discount
        )
    }
}

/**
 * Tipo de promoción
 */
enum class PromotionType(val duration: String) {
    STORY("24 horas"),       // Historias estilo Instagram
    PROMOTION("7 días")      // Promociones con más duración
}

/**
 * Estado de la promoción
 */
enum class PromotionStatus {
    DRAFT,      // Borrador (no publicada)
    ACTIVE,     // Activa y visible
    EXPIRED,    // Expirada automáticamente
    ARCHIVED    // Archivada manualmente por el prestador
}

/**
 * Validador de promociones
 */
object PromotionValidator {
    fun validate(promotion: ProviderPromotion): PromotionValidationResult {
        val errors = mutableListOf<String>()
        
        if (promotion.title.isBlank()) {
            errors.add("El título es obligatorio")
        }
        
        if (promotion.description.isBlank()) {
            errors.add("La descripción es obligatoria")
        }
        
        if (promotion.imageUrls.isEmpty()) {
            errors.add("Debe agregar al menos una imagen")
        }
        
        if (promotion.imageUrls.size > 3) {
            errors.add("Máximo 3 imágenes permitidas")
        }
        
        if (promotion.categories.isEmpty()) {
            errors.add("Debe seleccionar al menos una categoría")
        }
        
        promotion.discount?.let {
            if (it !in 1..100) {
                errors.add("El descuento debe estar entre 1% y 100%")
            }
        }
        
        return if (errors.isEmpty()) {
            PromotionValidationResult.Valid
        } else {
            PromotionValidationResult.Invalid(errors)
        }
    }
}

/**
 * Resultado de validación
 */
sealed class PromotionValidationResult {
    object Valid : PromotionValidationResult()
    data class Invalid(val errors: List<String>) : PromotionValidationResult()
}
