package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.AppMetadataEntity

/**
 * --- DAO DE METADATOS (SSOT) ---
 */
@Dao
interface AppMetadataDao {
    @Query("SELECT * FROM app_metadata WHERE clave = :clave LIMIT 1")
    fun obtenerMetadataFlujo(clave: String): kotlinx.coroutines.flow.Flow<AppMetadataEntity?>

    @Query("SELECT valor FROM app_metadata WHERE clave = :clave LIMIT 1")
    suspend fun obtenerValor(clave: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarMetadata(metadata: AppMetadataEntity)
}

































