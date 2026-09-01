package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.EmpresaEntity
import com.example.myapplication.core.datos.local.relaciones.EmpresaCompletaRelacionesBD
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE EMPRESAS (SSOT) ---
 */
@Dao
interface EmpresaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEmpresa(empresa: EmpresaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(empresas: List<EmpresaEntity>)

    @Query("SELECT * FROM empresas WHERE id = :id")
    fun obtenerPorId(id: String): Flow<EmpresaEntity?>

    @Query("SELECT * FROM empresas WHERE id = :id")
    suspend fun obtenerPorIdSync(id: String): EmpresaEntity?

    @Query("SELECT * FROM empresas WHERE idPropietario = :uid")
    fun obtenerPorPropietario(uid: String): Flow<List<EmpresaEntity>>

    @Query("SELECT * FROM empresas WHERE idPropietario = :uid")
    suspend fun obtenerPorPropietarioSync(uid: String): List<EmpresaEntity>

    @Query("DELETE FROM empresas WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("DELETE FROM empresas WHERE idPropietario = :uid")
    suspend fun eliminarPorPropietario(uid: String)

    @Transaction
    @Query("SELECT * FROM empresas WHERE idPropietario = :uid")
    fun obtenerEmpresasCompletas(uid: String): Flow<List<EmpresaCompletaRelacionesBD>>

    @Transaction
    @Query("SELECT * FROM empresas WHERE id = :id")
    fun obtenerEmpresaCompleta(id: String): Flow<EmpresaCompletaRelacionesBD?>
}
