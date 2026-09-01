package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.PromocionComentarioEntity
import com.example.myapplication.core.datos.local.entidades.PromocionEntity
import com.example.myapplication.core.datos.local.entidades.PromocionLikeEntity
import com.example.myapplication.core.datos.local.entidades.vistas.PromocionDetalle
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA GESTIÓN DE PROMOCIONES Y ENGAGEMENT ---
 */
@Dao
interface PromocionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPromocion(promocion: PromocionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarListaPromociones(lista: List<PromocionEntity>)

    @Query("DELETE FROM promociones WHERE id = :idPromocion")
    suspend fun eliminarPromocion(idPromocion: String)

    @Query("SELECT * FROM promociones WHERE id = :id")
    suspend fun obtenerPorId(id: String): PromocionEntity?

    @Query("SELECT * FROM promociones WHERE fechaExpiracion > :ahora ORDER BY fechaCreacion DESC")
    fun obtenerActivas(ahora: Long): Flow<List<PromocionEntity>>

    @Query("SELECT * FROM promociones WHERE fechaExpiracion > :ahora AND codigoPostal = :cp ORDER BY fechaCreacion DESC")
    fun obtenerActivasPorZona(ahora: Long, cp: String): Flow<List<PromocionEntity>>

    @Query("SELECT * FROM promociones WHERE fechaExpiracion > :ahora AND tipo = 'STORY' ORDER BY fechaCreacion DESC")
    fun obtenerHistoriasActivas(ahora: Long): Flow<List<PromocionEntity>>

    @Query("SELECT * FROM promociones WHERE fechaExpiracion > :ahora AND tipo = 'STORY' AND codigoPostal = :cp ORDER BY fechaCreacion DESC")
    fun obtenerHistoriasActivasPorZona(ahora: Long, cp: String): Flow<List<PromocionEntity>>

    @Query("SELECT * FROM promociones WHERE fechaExpiracion > :ahora AND filtrosBusquedaJson LIKE '%' || :idConsulta || '%' ORDER BY fechaCreacion DESC")
    fun obtenerActivasPaginadas(ahora: Long, idConsulta: String): androidx.paging.PagingSource<Int, PromocionEntity>

    @Query("SELECT * FROM promociones WHERE idPrestador = :idPrestador ORDER BY fechaCreacion DESC")
    fun obtenerPorPrestador(idPrestador: String): Flow<List<PromocionEntity>>

    @Query("DELETE FROM promociones WHERE fechaExpiracion < :ahora")
    suspend fun eliminarExpiradas(ahora: Long)

    @Query("UPDATE promociones SET conteoLikes = conteoLikes + :delta WHERE id = :idPromocion")
    suspend fun actualizarEstadoLike(idPromocion: String, delta: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReaccion(like: PromocionLikeEntity)

    @Query("SELECT leGusta FROM reacciones_promo WHERE idPromocion = :idPromocion LIMIT 1")
    suspend fun esPromocionFavorita(idPromocion: String): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarComentarios(lista: List<PromocionComentarioEntity>)

    @Query("SELECT * FROM comentarios_promo WHERE idPromocion = :idPromocion ORDER BY marcaTiempo DESC")
    fun obtenerComentariosPorPromo(idPromocion: String): Flow<List<PromocionComentarioEntity>>

    @Query("DELETE FROM comentarios_promo WHERE idPromocion = :idPromocion")
    suspend fun eliminarComentariosDePromocion(idPromocion: String)

    // --- MÉTODOS DE VISTA (OPCIONALES) ---

    @Query("""
        SELECT 
            p.*, 
            IFNULL(l.leGusta, 0) as reaccionada
        FROM promociones p
        LEFT JOIN reacciones_promo l ON p.id = l.idPromocion
        WHERE p.idPrestador = :idPrestador
        ORDER BY p.fechaCreacion DESC
    """)
    fun obtenerDetallesPorPrestador(idPrestador: String): Flow<List<PromocionDetalle>>
}
