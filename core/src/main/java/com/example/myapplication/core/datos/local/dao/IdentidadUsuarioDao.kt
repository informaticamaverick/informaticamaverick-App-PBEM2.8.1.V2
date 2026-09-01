package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.IdentidadUsuarioEntity
import com.example.myapplication.core.datos.local.relaciones.UsuarioConDireccionesRelacionesBD
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA IDENTIDAD DE USUARIO ---
 */
@Dao
interface IdentidadUsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: IdentidadUsuarioEntity)

    @Query("SELECT * FROM identidades_usuario WHERE id = :uid")
    fun obtenerPorId(uid: String): Flow<IdentidadUsuarioEntity?>

    @Query("SELECT * FROM identidades_usuario WHERE id = :uid")
    suspend fun obtenerPorIdSync(uid: String): IdentidadUsuarioEntity?

    @Query("DELETE FROM identidades_usuario WHERE id = :uid")
    suspend fun eliminarPorId(uid: String)

    @Transaction
    @Query("SELECT * FROM identidades_usuario WHERE id = :uid")
    fun obtenerUsuarioConDirecciones(uid: String): Flow<UsuarioConDireccionesRelacionesBD?>
}
