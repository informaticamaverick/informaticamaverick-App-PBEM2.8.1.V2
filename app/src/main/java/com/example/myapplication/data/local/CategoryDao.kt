package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    // Obtener todas las categorías ordenadas por Supercategoría.
    // Devuelve un Flow para que la UI se actualice automáticamente.
    @Query("SELECT * FROM categories_table ORDER BY superCategory ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    // Insertar o Actualizar. Esta función es CLAVE para Firebase.
    // Si el 'name' ya existe, reemplaza toda la fila con los nuevos datos.
    // 🔥 Aquí es donde conectarás los datos que vengan de Firebase.


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(category: CategoryEntity)

    // Para insertar la lista inicial de datos falsos de una sola vez.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    // [NUEVO] Para verificar si la base de datos está vacía antes de sincronizar
    @Query("SELECT COUNT(*) FROM categories_table")
    suspend fun getCount(): Int

    // [NUEVO] Limpiar tabla para sincronización total
    @Query("DELETE FROM categories_table")
    suspend fun deleteAll()

    // NOTA: No hay función @Delete. El usuario no puede borrar datos.
}