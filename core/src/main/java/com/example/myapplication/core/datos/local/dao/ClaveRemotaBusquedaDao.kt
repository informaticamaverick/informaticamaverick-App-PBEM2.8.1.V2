package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.ClaveRemotaBusquedaEntity

/**
 * --- DAO DE CLAVES REMOTAS ---
 */
@Dao
interface ClaveRemotaBusquedaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarClave(clave: ClaveRemotaBusquedaEntity)

    @Query("SELECT * FROM claves_remotas_busqueda WHERE idConsulta = :idConsulta")
    suspend fun obtenerClave(idConsulta: String): ClaveRemotaBusquedaEntity?

    @Query("DELETE FROM claves_remotas_busqueda WHERE idConsulta = :idConsulta")
    suspend fun eliminarClave(idConsulta: String)

    @Query("DELETE FROM claves_remotas_busqueda")
    suspend fun limpiarTodo()
}

































