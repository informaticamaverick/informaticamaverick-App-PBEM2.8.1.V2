package com.example.myapplication.core.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.vistas.CategoriaResumenSQLView
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE CATEGORÍAS (COMPARTIDO - v2026.ELITE) ---
 */
@Dao
interface CategoriaDao {

    @Query("SELECT * FROM categorias ORDER BY idSuperCategoria ASC")
    fun obtenerTodas(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias")
    suspend fun obtenerTodasSync(): List<CategoriaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(categorias: List<CategoriaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(categoria: CategoriaEntity)

    @Query("SELECT COUNT(*) FROM categorias")
    suspend fun obtenerConteo(): Long

    @Query("DELETE FROM categorias")
    suspend fun eliminarTodas()

    @Query("""
        SELECT c.* FROM categorias c
        JOIN categorias_fts fts ON c.rowid = fts.rowid
        WHERE categorias_fts MATCH :consulta
        LIMIT 100
    """)
    fun buscarMatch(consulta: String): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): CategoriaEntity?

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    fun obtenerPorIdFlow(id: String): Flow<CategoriaEntity?>

    @Query("SELECT * FROM categorias WHERE idSuperCategoria = :idSuperCategoria ORDER BY nombre ASC")
    fun obtenerPorSuperCategoria(idSuperCategoria: String): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): CategoriaEntity?

    @Query("SELECT * FROM categorias WHERE nombre = :nombre LIMIT 1")
    fun obtenerPorNombreFlow(nombre: String): Flow<CategoriaEntity?>

    // --- MÉTODOS BASADOS EN VISTAS (ELITE v2026) ---

    @Query("SELECT * FROM CategoriaResumenSQLView ORDER BY superCategoriaNombre ASC, nombre ASC")
    fun obtenerResumenTodas(): Flow<List<CategoriaResumenSQLView>>

    @Query("SELECT * FROM CategoriaResumenSQLView WHERE idSuperCategoria = :idSuperCategoria ORDER BY nombre ASC")
    fun obtenerResumenPorSuperCategoria(idSuperCategoria: String): Flow<List<CategoriaResumenSQLView>>

    @Query("""
        SELECT cr.* FROM CategoriaResumenSQLView cr
        WHERE cr.rowid IN (
            SELECT rowid FROM categorias_fts WHERE categorias_fts MATCH :consulta
        )
        LIMIT 100
    """)
    fun buscarResumenMatch(consulta: String): Flow<List<CategoriaResumenSQLView>>

    @Query("""
        SELECT 
            sc.id as id,
            sc.nombre as titulo, 
            sc.icono as icono, 
            sc.color as color,
            COUNT(c.id) as totalItems
        FROM super_categorias sc
        LEFT JOIN categorias c ON sc.id = c.idSuperCategoria
        GROUP BY sc.id
        ORDER BY sc.nombre ASC
    """)
    fun obtenerMetadatosSuperCategorias(): Flow<List<SuperCategoriaShallow>>
}

data class SuperCategoriaShallow(
    val id: String,
    val titulo: String,
    val icono: String,
    val color: Long,
    val totalItems: Int
)
