package com.example.myapplication.core.data.local.seed

import android.content.Context
import android.util.Log
import com.example.myapplication.core.data.local.dao.CategoryDao
import com.example.myapplication.core.data.local.dao.SuperCategoryDao
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.SuperCategoryEntity
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- CATEGORY SEEDER (POLÍTICA ZERO COSTO) ---
 * Clase encargada de poblar la base de datos local desde un archivo JSON.
 * Garantiza que la app tenga contenido inicial sin usar Firebase.
 */
@Singleton
class CategorySeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryDao: CategoryDao,
    private val superCategoryDao: SuperCategoryDao
) {
    private val TAG = "CategorySeeder"
    private val FILE_NAME = "seed_data.json"

    /**
     * Realiza el sembrado si las tablas están vacías.
     * Retorna TRUE si se realizó el sembrado, FALSE si ya había datos.
     */
    suspend fun seedIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        try {
            val categoryCount = categoryDao.getCount()
            val superCategoryCount = superCategoryDao.getCount()

            if (categoryCount == 0 || superCategoryCount == 0) {
                Log.d(TAG, "🌱 Iniciando sembrado local de categorías...")
                
                val jsonString = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
                val seedData = Gson().fromJson(jsonString, SeedData::class.java)

                // 1. Sembrar Supercategorías
                val superEntities = seedData.superCategories.map {
                    SuperCategoryEntity(
                        name = it.name,
                        icon = it.icon,
                        color = it.color,
                        description = it.description
                    )
                }
                superCategoryDao.insertAll(superEntities)

                // 2. Sembrar Categorías
                // Buscamos el icono de la supercategoría dinámicamente para asegurar integridad
                val superIconMap = superEntities.associate { it.name to it.icon }

                val catEntities = seedData.categories.map {
                    CategoryEntity(
                        name = it.name,
                        icon = it.icon,
                        superCategory = it.superCategory,
                        superCategoryIcon = superIconMap[it.superCategory] ?: "📂",
                        description = it.description,
                        isFavorite = it.isFavorite ?: false
                    )
                }
                categoryDao.insertAll(catEntities)

                Log.d(TAG, "✅ Sembrado completado: ${superEntities.size} grupos y ${catEntities.size} rubros.")
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error durante el sembrado: ${e.message}")
        }
        return@withContext false
    }

    // Modelos internos para el parseo del JSON
    private data class SeedData(
        val superCategories: List<SuperCategoryJson>,
        val categories: List<CategoryJson>
    )

    private data class SuperCategoryJson(
        val name: String,
        val icon: String,
        val color: Long,
        val description: String
    )

    private data class CategoryJson(
        val name: String,
        val icon: String,
        val superCategory: String,
        val description: String,
        val isFavorite: Boolean? = false
    )
}
