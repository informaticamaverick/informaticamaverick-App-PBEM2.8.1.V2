package com.example.myapplication.core.data.repository

import android.util.Log
import com.example.myapplication.core.data.local.dao.CategoryDao
import com.example.myapplication.core.data.local.dao.SuperCategoryLight
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.remote.CategoryFirestoreDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CATEGORÍAS (COMPARTIDO) ---
 * Gestiona el catálogo maestro de servicios (ej: Plomería, Gas, etc).
 * Ambas apps usan este repositorio para obtener la lista oficial de rubros
 * sincronizada desde la colección "Servicios" en Firestore.
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val firestore: FirebaseFirestore
) {
    private val TAG = "CategoryRepository"

    // --- SECCIÓN 1: OBSERVABLES ---

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    
    fun getSuperCategoryMetadata(): Flow<List<SuperCategoryLight>> = 
        categoryDao.getSuperCategoryMetadata()

    fun getFilteredSuperCategoryMetadata(query: String): Flow<List<SuperCategoryLight>> =
        categoryDao.getSuperCategoryMetadata() // Por ahora retorno todo, Room no filtra grupos fácilmente con LIKE en el mismo query de agregación sin subqueries pesados

    fun searchCategories(query: String): Flow<List<CategoryEntity>> =
        categoryDao.searchCategories(query)

    fun getCategoriesBySuperCategory(superCategory: String): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesBySuperCategory(superCategory)

    suspend fun insertOrUpdate(category: CategoryEntity) {
        categoryDao.insertOrUpdate(category)
    }

    // --- SECCIÓN 2: SINCRONIZACIÓN ---

    /**
     * Descarga las categorías desde Firebase y actualiza la base de datos local.
     */
    suspend fun syncWithFirebase() {
        try {
            Log.d(TAG, "Sincronizando categorías desde remoto...")
            val snapshot = firestore.collection("Servicios").get().await()
            
            val entities = snapshot.documents.mapNotNull { doc ->
                val dto = doc.toObject(CategoryFirestoreDto::class.java)
                dto?.let {
                    CategoryEntity(
                        name = it.name,
                        icon = it.icon,
                        superCategory = it.superCategory,
                        superCategoryIcon = it.superCategoryIcon,
                        description = it.description
                    )
                }
            }

            if (entities.isNotEmpty()) {
                categoryDao.deleteAll()
                categoryDao.insertAll(entities)
                Log.d(TAG, "Sincronización exitosa: ${entities.size} rubros guardados.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización: ${e.message}")
        }
    }
}
