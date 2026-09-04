package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.datos.local.relaciones.PrestadorCompletoRelacionesBD
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA IDENTIDAD DE PRESTADOR (ELITE v2026) ---
 */
@Dao
interface IdentidadPrestadorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(prestador: IdentidadPrestadorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(prestadores: List<IdentidadPrestadorEntity>)

    @Query("SELECT * FROM prestadores WHERE id = :uid")
    fun obtenerPorId(uid: String): Flow<IdentidadPrestadorEntity?>

    @Query("SELECT * FROM prestadores WHERE id = :uid")
    suspend fun obtenerPorIdSync(uid: String): IdentidadPrestadorEntity?

    @Query("SELECT * FROM prestadores")
    fun obtenerTodos(): Flow<List<IdentidadPrestadorEntity>>

    @Query("DELETE FROM prestadores WHERE id = :uid")
    suspend fun eliminarPorId(uid: String)

    @Query("UPDATE prestadores SET estaVerificado = :verificado WHERE id = :uid")
    suspend fun actualizarVerificacion(uid: String, verificado: Boolean)

    @Query("UPDATE prestadores SET estaVerificado = :verificado WHERE id = :uid")
    suspend fun actualizarVerificacionSync(uid: String, verificado: Boolean)

    @Query("UPDATE prestadores SET estaEnLinea = :enLinea WHERE id = :uid")
    suspend fun actualizarEstaEnLinea(uid: String, enLinea: Boolean)

    @Transaction
    @Query("SELECT * FROM prestadores WHERE id = :uid")
    fun obtenerPrestadorCompleto(uid: String): Flow<PrestadorCompletoRelacionesBD?>

    @Query("SELECT * FROM prestadores WHERE id = :uid")
    suspend fun obtenerPrestadorPorId(uid: String): IdentidadPrestadorEntity?
}
