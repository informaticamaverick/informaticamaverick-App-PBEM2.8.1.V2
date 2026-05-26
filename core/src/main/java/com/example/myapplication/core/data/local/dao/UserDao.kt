package com.example.myapplication.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.core.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE USUARIO (COMPARTIDO) ---
 * Gestiona el perfil del dueño de la aplicación en el almacenamiento local.
 */
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    /**
     * Obtiene el flujo de datos del usuario actual (Observable).
     */
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    /**
     * Obtiene el perfil del usuario de forma inmediata (No observable).
     */
    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserOnce(): UserEntity?

    /**
     * Limpia la información del usuario (Cierre de sesión).
     */
    @Query("DELETE FROM user_profile")
    suspend fun deleteUser()
}
