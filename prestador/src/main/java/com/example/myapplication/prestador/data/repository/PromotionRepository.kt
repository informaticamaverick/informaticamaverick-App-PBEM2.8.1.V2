package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.PromotionDao
import com.example.myapplication.prestador.data.local.entity.PromotionEntity
import com.example.myapplication.prestador.data.model.PromotionType
import com.example.myapplication.prestador.data.model.ProviderPromotion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromotionRepository @Inject constructor(
    private val promotionDao: PromotionDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val coleccion = firestore.collection("promociones")
    /**
     * GUARDAR nueva promoción
     */
    suspend fun savePromotion(promotion: PromotionEntity) {
        promotionDao.insertPromotion(promotion)
    }
    
    /**
     * GUARDAR múltiples promociones
     */
    suspend fun savePromotions(promotions: List<PromotionEntity>) {
        promotionDao.insertPromotions(promotions)
    }
    
    /**
     * ACTUALIZAR promoción existente
     */
    suspend fun updatePromotion(promotion: PromotionEntity) {
        promotionDao.updatePromotion(promotion)
    }
    
    /**
     * ELIMINAR promoción
     */
    suspend fun deletePromotion(promotionId: String) {
        promotionDao.deletePromotionById(promotionId)
        try { coleccion.document(promotionId).delete().await() } catch (_: Exception) { }
    }
    
    /**
     * OBTENER promoción por ID
     */
    fun getPromotionById(promotionId: String): Flow<PromotionEntity?> {
        return promotionDao.getPromotionById(promotionId)
    }
    
    /**
     * OBTENER todas las promociones del prestador
     */
    fun getPromotionsByProvider(providerId: String): Flow<List<PromotionEntity>> {
        return promotionDao.getPromotionsByProvider(providerId)
    }
    
    /**
     * OBTENER solo promociones activas
     */
    fun getActivePromotions(providerId: String): Flow<List<PromotionEntity>> {
        return promotionDao.getActivePromotions(providerId)
    }
    
    /**
     * OBTENER promociones por tipo (STORY o PROMOTION)
     */
    fun getPromotionsByType(providerId: String, type: PromotionType): Flow<List<PromotionEntity>> {
        return promotionDao.getPromotionsByType(providerId, type.name)
    }
    
    /**
     * OBTENER promociones expiradas
     */
    fun getExpiredPromotions(providerId: String): Flow<List<PromotionEntity>> {
        val currentTime = System.currentTimeMillis()
        return promotionDao.getExpiredPromotions(providerId, currentTime)
    }
    
    /**
     * ACTUALIZAR el estado de una promoción
     */
    suspend fun updatePromotionStatus(promotionId: String, status: String) {
        promotionDao.updatePromotionStatus(promotionId, status)
    }
    
    /**
     * INCREMENTAR likes
     */
    suspend fun incrementLikes(promotionId: String) {
        promotionDao.incrementLikes(promotionId)
    }
    
    /**
     * INCREMENTAR vistas
     */
    suspend fun incrementViews(promotionId: String) {
        promotionDao.incrementViews(promotionId)
    }
    
    /**
     * CONTAR promociones activas
     */
    suspend fun countActivePromotions(providerId: String): Int {
        return promotionDao.countActivePromotions(providerId)
    }
    
    /**
     * LIMPIAR promociones expiradas
     */
    suspend fun cleanExpiredPromotions() {
        val currentTime = System.currentTimeMillis()
        promotionDao.deleteExpiredPromotions(currentTime)
    }
    
    /**
     * CREAR y GUARDAR promoción desde ProviderPromotion
     * Convierte el modelo de UI a Entity y lo guarda
     */
    suspend fun createPromotionFromModel(promotion: ProviderPromotion): String {
        val promotionId = UUID.randomUUID().toString()
        val entity = PromotionEntity(
            id = promotionId,
            providerId = promotion.providerId,
            providerName = promotion.providerName,
            providerImageUrl = promotion.providerImageUrl,
            type = promotion.type.name,
            title = promotion.title,
            description = promotion.description,
            imageUrls = listToJson(promotion.imageUrls),
            discount = promotion.discount,
            categories = listToJson(promotion.categories),
            createdAt = promotion.createdAt,
            expiresAt = promotion.expiresAt,
            status = promotion.status.name,
            likes = promotion.likes,
            views = promotion.views,
            rating = promotion.rating
        )
        promotionDao.insertPromotion(entity)
        // Sincronizar con Firestore para que el cliente pueda verlo
        try {
            coleccion.document(promotionId).set(
                mapOf(
                    "id" to promotionId,
                    "providerId" to promotion.providerId,
                    "providerName" to promotion.providerName,
                    "providerImageUrl" to (promotion.providerImageUrl ?: ""),
                    "type" to promotion.type.name,
                    "title" to promotion.title,
                    "description" to promotion.description,
                    "imageUrls" to promotion.imageUrls,
                    "discount" to promotion.discount,
                    "categories" to promotion.categories,
                    "createdAt" to promotion.createdAt,
                    "expiresAt" to promotion.expiresAt,
                    "status" to promotion.status.name,
                    "likes" to promotion.likes,
                    "views" to promotion.views,
                    "rating" to promotion.rating
                )
            ).await()
        } catch (_: Exception) { }
        return promotionId
    }
    
    /**
     * OBTENER promociones como ProviderPromotion (modelo de UI)
     * Convierte de Entity a modelo con Flow
     */
    fun getPromotionsAsModel(providerId: String): Flow<List<ProviderPromotion>> {
        return getPromotionsByProvider(providerId).map { entities ->
            entities.map { entity -> entityToModel(entity) }
        }
    }
    
    /**
     * OBTENER promoción por ID como modelo de UI
     */
    fun getPromotionByIdAsModel(promotionId: String): kotlinx.coroutines.flow.Flow<ProviderPromotion?> {
        return getPromotionById(promotionId).map { entity ->
            entity?.let { entityToModel(it) }
        }
    }

    /**
     * ACTUALIZAR promoción existente desde ProviderPromotion
     */
    suspend fun updatePromotionFromModel(promotion: ProviderPromotion) {
        val entity = PromotionEntity(
            id = promotion.id,
            providerId = promotion.providerId,
            providerName = promotion.providerName,
            providerImageUrl = promotion.providerImageUrl,
            type = promotion.type.name,
            title = promotion.title,
            description = promotion.description,
            imageUrls = listToJson(promotion.imageUrls),
            discount = promotion.discount,
            categories = listToJson(promotion.categories),
            createdAt = promotion.createdAt,
            expiresAt = promotion.expiresAt,
            status = promotion.status.name,
            likes = promotion.likes,
            views = promotion.views,
            rating = promotion.rating
        )
        promotionDao.updatePromotion(entity)
        try {
            coleccion.document(promotion.id).set(
                mapOf(
                    "id" to promotion.id,
                    "providerId" to promotion.providerId,
                    "providerName" to promotion.providerName,
                    "providerImageUrl" to (promotion.providerImageUrl ?: ""),
                    "type" to promotion.type.name,
                    "title" to promotion.title,
                    "description" to promotion.description,
                    "imageUrls" to promotion.imageUrls,
                    "discount" to promotion.discount,
                    "categories" to promotion.categories,
                    "createdAt" to promotion.createdAt,
                    "expiresAt" to promotion.expiresAt,
                    "status" to promotion.status.name,
                    "likes" to promotion.likes,
                    "views" to promotion.views,
                    "rating" to promotion.rating
                )
            ).await()
        } catch (_: Exception) { }
    }

    // ========== MÉTODOS AUXILIARES DE CONVERSIÓN ==========
    
    /**
     * Convierte List<String> a JSON String
     * ["url1", "url2"] → "[\"url1\",\"url2\"]"
     */
    private fun listToJson(list: List<String>): String {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }
    
    /**
     * Convierte JSON String a List<String>
     * "[\"url1\",\"url2\"]" → ["url1", "url2"]
     */
    private fun jsonToList(json: String): List<String> {
        if (json.isEmpty()) return emptyList()
        val list = mutableListOf<String>()
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }
    
    /**
     * Convierte PromotionEntity a ProviderPromotion
     */
    private fun entityToModel(entity: PromotionEntity): ProviderPromotion {
        return ProviderPromotion(
            id = entity.id,
            providerId = entity.providerId,
            providerName = entity.providerName,
            providerImageUrl = entity.providerImageUrl,
            type = PromotionType.valueOf(entity.type),
            title = entity.title,
            description = entity.description,
            imageUrls = jsonToList(entity.imageUrls),
            discount = entity.discount,
            categories = jsonToList(entity.categories),
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt,
            status = com.example.myapplication.prestador.data.model.PromotionStatus.valueOf(entity.status),
            likes = entity.likes,
            views = entity.views,
            rating = entity.rating
        )
    }
}
