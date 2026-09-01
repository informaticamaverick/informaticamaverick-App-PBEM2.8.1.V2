package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.ReviewEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE RESEÑAS (Ley #2 - Costo Zero) ---
 */
@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReseña(reseña: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarListaReseñas(reseñas: List<ReviewEntity>)

    @Query("SELECT * FROM reviews WHERE targetId = :targetId ORDER BY timestamp DESC")
    fun obtenerReseñasPorObjetivo(targetId: String): Flow<List<ReviewEntity>>

    @Query("SELECT AVG(rating) FROM reviews WHERE targetId = :targetId")
    fun obtenerCalificacionPromedio(targetId: String): Flow<Float?>

    @Query("SELECT COUNT(*) FROM reviews WHERE targetId = :targetId")
    fun obtenerTotalReseñas(targetId: String): Flow<Int>

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun eliminarReseña(id: String)
}

































