package com.example.myapplication.core.data.repository

import com.example.myapplication.core.data.local.dao.CategoryDao
import com.example.myapplication.core.data.local.dao.SuperCategoryLight
import com.example.myapplication.core.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CATEGORÍAS (COMPARTIDO) ---
 * [LEY #6: SOBERANÍA LOCAL]
 * Este repositorio gestiona el catálogo maestro de servicios de forma 100% OFFLINE.
 * No tiene conexión con Firebase para evitar costos y latencia.
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    // --- SECCIÓN 1: LECTURA REACTIVA (ROOM ONLY) ---

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    
    fun getSuperCategoryMetadata(): Flow<List<SuperCategoryLight>> = 
        categoryDao.getSuperCategoryMetadata()

    fun searchCategories(query: String): Flow<List<CategoryEntity>> =
        categoryDao.searchCategories(query)

    fun getCategoriesBySuperCategory(superCategory: String): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesBySuperCategory(superCategory)

    // --- SECCIÓN 2: GESTIÓN LOCAL ---

    suspend fun insertOrUpdate(category: CategoryEntity) {
        categoryDao.insertOrUpdate(category)
    }

    /**
     * [OBSOLETO]: Las categorías ya no se sincronizan con Firebase.
     * Se mantiene solo la firma vacía para compatibilidad si otros módulos la llaman.
     */
    suspend fun syncWithFirebase() {
        // No-op: Cumpliendo Ley #6
    }
}
