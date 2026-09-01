package com.example.myapplication.core.datos.local.semillas

import android.content.Context
import android.util.Log
import com.example.myapplication.core.datos.local.dao.AppMetadataDao
import com.example.myapplication.core.datos.local.dao.CategoriaDao
import com.example.myapplication.core.datos.local.dao.SuperCategoriaDao
import com.example.myapplication.core.datos.local.entidades.AppMetadataEntity
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.SuperCategoriaEntity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.example.myapplication.core.utilidades.normalizeFull
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- CATEGORY SEEDER (POLÍTICA ZERO COSTO - v2026.ELITE) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@Keep
@Singleton
class CategoriaSeeder @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val categoriaDao: CategoriaDao,
    private val superCategoriaDao: SuperCategoriaDao,
    private val metadataDao: AppMetadataDao
) {
    private val TAG = "CategoriaSeeder"
    private val FILE_NAME = "seed_data.json"
    private val KEY_CATALOG_VERSION = "CATALOG_SEED_VERSION"
    private val CURRENT_SEED_VERSION = 5 // Bumb tras limpieza de campos CategoriaEntity

    suspend fun seedIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        try {
            val versionActualStr = metadataDao.obtenerValor(KEY_CATALOG_VERSION)
            val versionActual = versionActualStr?.toIntOrNull() ?: 0
            
            val isDbEmpty = categoriaDao.obtenerConteo() == 0L || superCategoriaDao.obtenerConteo() == 0L

            if (versionActual < CURRENT_SEED_VERSION || isDbEmpty) {
                Log.d(TAG, "🌱 [ELITE_SEED] Iniciando sembrado atómico (v$CURRENT_SEED_VERSION)...")
                
                val jsonString = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
                val seedData = Gson().fromJson(jsonString, SeedData::class.java)

                // 1. Supercategorías
                val superEntities = seedData.superCategories.map {
                    SuperCategoriaEntity(
                        id = it.id,
                        nombre = it.name,
                        icono = it.icon.ifBlank { "📁" },
                        color = it.color,
                        descripcion = it.description
                    )
                }
                superCategoriaDao.insertarLista(superEntities)

                // 2. Categorías
                val superMap = superEntities.associateBy { it.nombre }

                val catEntities = seedData.categories.map {
                    val sCat = superMap[it.superCategory]
                    CategoriaEntity(
                        id = it.id,
                        nombre = it.name,
                        icono = it.icon.ifBlank { "📄" },
                        idSuperCategoria = sCat?.id ?: "OTROS",
                        descripcion = it.description,
                        esNueva = false
                    )
                }
                categoriaDao.insertarLista(catEntities)

                metadataDao.guardarMetadata(AppMetadataEntity(KEY_CATALOG_VERSION, CURRENT_SEED_VERSION.toString()))

                Log.d(TAG, "✅ [ELITE_SEED] Sembrado completado.")
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ [ELITE_SEED] Error: ${e.message}")
        }
        return@withContext false
    }

    @Keep
    private data class SeedData(
        @SerializedName("superCategories") val superCategories: List<SuperCategoryJson> = emptyList(),
        @SerializedName("categories") val categories: List<CategoryJson> = emptyList()
    )

    @Keep
    private data class SuperCategoryJson(
        @SerializedName("id") val id: String = "",
        @SerializedName("name") val name: String = "",
        @SerializedName("icon") val icon: String = "",
        @SerializedName("color") val color: Long = 0xFFCCCCCC,
        @SerializedName("description") val description: String = ""
    )

    @Keep
    private data class CategoryJson(
        @SerializedName("id") val id: String = "",
        @SerializedName("name") val name: String = "",
        @SerializedName("icon") val icon: String = "",
        @SerializedName("superCategory") val superCategory: String = "",
        @SerializedName("description") val description: String = ""
    )
}


































