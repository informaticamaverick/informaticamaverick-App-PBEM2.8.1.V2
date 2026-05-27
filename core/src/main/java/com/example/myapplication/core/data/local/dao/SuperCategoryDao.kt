package com.example.myapplication.core.data.local.dao

import androidx.room.*
import com.example.myapplication.core.data.local.entity.SuperCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE SUPERCATEGORÍAS ---
 * Gestiona el catálogo de grupos de servicios.
 */
@Dao
interface SuperCategoryDao {

    @Query("SELECT * FROM super_categories_table ORDER BY name ASC")
    fun getAllSuperCategories(): Flow<List<SuperCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(superCategories: List<SuperCategoryEntity>)

    @Query("SELECT COUNT(*) FROM super_categories_table")
    suspend fun getCount(): Int

    @Query("DELETE FROM super_categories_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM super_categories_table WHERE name = :name LIMIT 1")
    suspend fun getSuperCategoryByName(name: String): SuperCategoryEntity?
}
