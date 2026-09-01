package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.CategoriaRapidaEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA GESTIÓN DE CATEGORÍAS FAST (v2026.ELITE) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@Dao
interface CategoriaRapidaDao {
    @Query("SELECT * FROM uso_categorias_fast ORDER BY marcaTiempoUltimoUso DESC, conteoUso DESC LIMIT :limite")
    fun obtenerMasUsados(limite: Int): Flow<List<CategoriaRapidaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(entidad: CategoriaRapidaEntity)

    @Query("UPDATE uso_categorias_fast SET conteoUso = conteoUso + 1, marcaTiempoUltimoUso = :marcaTiempo WHERE id = :id")
    suspend fun incrementarUso(id: String, marcaTiempo: Long = System.currentTimeMillis())

    @Query("SELECT EXISTS(SELECT 1 FROM uso_categorias_fast WHERE id = :id)")
    suspend fun existe(id: String): Boolean

    @Transaction
    suspend fun registrarUso(categoria: CategoriaEntity) {
        if (existe(categoria.id)) {
            incrementarUso(categoria.id)
        } else {
            insertarOActualizar(
                CategoriaRapidaEntity(
                    id = categoria.id,
                    idSuperCategoria = categoria.idSuperCategoria
                )
            )
        }
    }
}

































