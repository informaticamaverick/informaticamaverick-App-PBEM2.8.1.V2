package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la entidad del Usuario.
 *
 * Esta interfaz define los métodos que Room utilizará para interactuar con la tabla 'user_profile'.
 * Todas las operaciones de lectura y escritura en la base de datos local pasarán por aquí.
 *
 * Las funciones 'suspend' indican que deben ser llamadas desde una corrutina para no bloquear
 * el hilo principal.
 * El uso de 'Flow' permite a la UI (a través del ViewModel y Repositorio) suscribirse
 * a los cambios de los datos. Cuando el usuario se actualice en la base de datos,
 * la UI lo reflejará automáticamente.
 */
@Dao
interface UserDao {

    /**
     * Inserta o actualiza un usuario en la base de datos.
     *
     * @param user La entidad del usuario a guardar.
     * OnConflictStrategy.REPLACE asegura que si se inserta un usuario con un ID que ya existe,
     * el antiguo registro será reemplazado por el nuevo. Esto es ideal para manejar actualizaciones.
     */
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    /**
     * Obtiene el perfil del usuario actual.
     *
     * @return Un Flow que emite la UserEntity del usuario o null si no hay ninguno guardado.
     * El Flow notificará a sus observadores cada vez que los datos del usuario cambien.
     * Se usa 'LIMIT 1' para asegurar que solo se devuelva un registro, ya que esta tabla
     * está diseñada para contener solo al usuario logueado.
     */
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    /**
     * Obtiene el perfil del usuario actual una sola vez (suspend).
     * Útil para verificaciones puntuales en el repositorio.
     */
    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserOnce(): UserEntity?

    /**
     * Borra todos los datos de la tabla de usuarios.
     *
     * Esta función es útil para operaciones como 'cerrar sesión', donde se debe limpiar
     * toda la información local del usuario.
     */
    @Query("DELETE FROM user_profile")
    suspend fun deleteUser()
}