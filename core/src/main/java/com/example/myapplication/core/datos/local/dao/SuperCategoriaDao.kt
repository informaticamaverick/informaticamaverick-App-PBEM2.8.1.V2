package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.SuperCategoriaEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE SUPERCATEGORÍAS (v2026.ELITE) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@Dao
interface SuperCategoriaDao {

    @Query("SELECT * FROM super_categorias ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<SuperCategoriaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(superCategorias: List<SuperCategoriaEntity>)

    @Query("SELECT COUNT(*) FROM super_categorias")
    suspend fun obtenerConteo(): Long

    @Query("DELETE FROM super_categorias")
    suspend fun eliminarTodas()

    @Query("SELECT * FROM super_categorias WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): SuperCategoriaEntity?

    @Query("SELECT * FROM super_categorias WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): SuperCategoriaEntity?
}

































