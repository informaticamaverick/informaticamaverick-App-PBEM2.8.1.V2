package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.data.local.entity.FastCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FastCategoryDao {
    @Query("SELECT * FROM fast_category_usage ORDER BY lastUsedTimestamp DESC, usageCount DESC LIMIT :limit")
    fun getMostUsed(limit: Int): Flow<List<FastCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FastCategoryEntity)

    @Query("UPDATE fast_category_usage SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp WHERE name = :name")
    suspend fun incrementUsage(name: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT EXISTS(SELECT 1 FROM fast_category_usage WHERE name = :name)")
    suspend fun exists(name: String): Boolean

    @Transaction
    suspend fun registerUsage(category: CategoryEntity) {
        if (exists(category.name)) {
            incrementUsage(category.name)
        } else {
            upsert(
                FastCategoryEntity(
                    name = category.name,
                    icon = category.icon,
                    superCategory = category.superCategory
                )
            )
        }
    }
}
