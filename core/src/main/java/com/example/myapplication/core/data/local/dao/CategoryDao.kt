package com.example.myapplication.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.core.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE CATEGORÍAS (COMPARTIDO) ---
 * Gestiona el catálogo de servicios. Ambas apps comparten las mismas categorías
 * para asegurar que el Prestador se registre en rubros que el Cliente pueda buscar.
 */
@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories_table ORDER BY superCategory ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories_table")
    suspend fun getCount(): Int

    @Query("DELETE FROM categories_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM categories_table WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchCategories(query: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories_table WHERE superCategory = :superCategory ORDER BY name ASC")
    fun getCategoriesBySuperCategory(superCategory: String): Flow<List<CategoryEntity>>

    /**
     * Obtiene metadatos enriquecidos uniendo categorías con su configuración visual.
     */
    @Query("""
        SELECT 
            sc.name as title, 
            sc.icon as icon, 
            sc.color as color,
            COUNT(c.name) as totalItems,
            MAX(CASE WHEN c.isFavorite = 1 THEN 1 ELSE 0 END) as hasFavoriteCategories
        FROM super_categories_table sc
        LEFT JOIN categories_table c ON sc.name = c.superCategory
        GROUP BY sc.name
        ORDER BY sc.name ASC
    """)
    fun getSuperCategoryMetadata(): Flow<List<SuperCategoryLight>>
}

/**
 * Representación enriquecida de una Supercategoría.
 */
data class SuperCategoryLight(
    val title: String,
    val icon: String,
    val color: Long,
    val totalItems: Int,
    val hasFavoriteCategories: Int
)
