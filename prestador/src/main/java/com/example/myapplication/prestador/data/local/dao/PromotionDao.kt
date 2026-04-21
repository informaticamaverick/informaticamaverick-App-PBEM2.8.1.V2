package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.PromotionEntity
import kotlinx.coroutines.flow.Flow

/** DAO para la tabla de promotions
 *
 * Define todas las operaciones para crear, leer, actualizar y eliminar promociones.
 * Incluye queries avanzados para filtrar por estado, tipo, fechas, etc.
 */

@Dao
interface PromotionDao {
    /**
     * INSERTAR una nueva promoción
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promotion: PromotionEntity)

    /**
     * INSERTAR múltiples promociones a la vez
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotions(promotions: List<PromotionEntity>)

    /**
     * ACTUALIZAR una promoción existente
     */

    @Update
    suspend fun updatePromotion(promotion: PromotionEntity)

    /**
     * ELIMINAR promoción por ID
     */
    @Query("DELETE FROM promotions WHERE id = :promotionId")
    suspend fun deletePromotionById(promotionId: String)

    /**
     * OBTENER promoción por ID (observando cambios)
     */

    @Query("SELECT * FROM promotions WHERE id = :promotionId")
    fun getPromotionById(promotionId: String): Flow<PromotionEntity?>

    /**
     * OBTENER todas las promociones del prestador actual
     * Ordenadas por fecha de creación (más recientes primero)
     */

    @Query("SELECT * FROM promotions WHERE providerId = :providerId ORDER BY createdAt DESC")
    fun getPromotionsByProvider(providerId: String): Flow<List<PromotionEntity>>

    /**
     * OBTENER solo promociones ACTIVAS
     * Filtra por status = ACTIVE
     */

    @Query("SELECT * FROM promotions WHERE providerId = :providerId AND status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActivePromotions(providerId: String): Flow<List<PromotionEntity>>

    /**
     * OBTENER promociones por tipo (STORY o PROMOTION)
     */
    @Query("SELECT * FROM promotions WHERE providerId = :providerId AND type = :type ORDER BY createdAt DESC")
    fun getPromotionsByType(providerId: String, type: String):
            Flow<List<PromotionEntity>>

    /**
     * OBTENER promociones EXPIRADAS
     * WHERE expiresAt < tiempo_actual :currentTime se pasa como parámetro
     */

    @Query("SELECT * FROM promotions WHERE providerId = :providerId AND expiresAt < :currentTime")
    fun getExpiredPromotions(providerId: String, currentTime: Long): Flow<List<PromotionEntity>>

    /**
     * ACTUALIZAR el estado de una promoción
     * Por ejemplo, cambiar de ACTIVE a EXPIRED
     */

    @Query("UPDATE promotions SET status = :status WHERE id = :promotionId")
    suspend fun updatePromotionStatus(promotionId: String, status: String)

    /**
     * INCREMENTAR los likes de una promoción
     * likes = likes + 1
     */
    @Query("UPDATE promotions SET likes = likes + 1 WHERE id = :promotionId")
    suspend fun incrementLikes(promotionId: String)

    /**
     * INCREMENTAR las vistas
     */
    @Query("UPDATE promotions SET views = views + 1 WHERE id = :promotionId")
    suspend fun incrementViews(promotionId: String)

    /**
     * CONTAR cuántas promociones activas tiene el prestador
     */
    @Query("SELECT COUNT(*) FROM promotions WHERE providerId = :providerId AND status = 'ACTIVE'")
    suspend fun countActivePromotions(providerId: String): Int

    /**
     * ELIMINAR todas las promociones expiradas
     * Útil para limpiar la BD periódicamente
     */

    @Query("DELETE FROM promotions WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredPromotions(currentTime: Long)

}