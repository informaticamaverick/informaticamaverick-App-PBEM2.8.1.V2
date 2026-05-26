package com.example.myapplication.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.myapplication.data.local.CategoryDao
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.SembradoServiciosInicia
import com.example.myapplication.data.model.CategoryFirestoreDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CATEGORÍAS (HÍBRIDO) ---
 * Gestiona la fuente de verdad única en Room, con sincronización bajo demanda desde Firestore.
 * 
 * Plan de Acción:
 * 1. [NUEVO] Se prioriza el sembrado local si la base de datos está vacía.
 * 2. Se eliminaron todas las referencias a colores en el modelo de datos.
 * 3. Se integra la variable 'description' (detalle) en la persistencia.
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    private val TAG = "CategoryRepository"
    
    // --- SECCIÓN: PERSISTENCIA DE VERSIÓN LOCAL ---
    private val prefs: SharedPreferences = context.getSharedPreferences("category_prefs", Context.MODE_PRIVATE)
    private val KEY_VERSION = "local_categories_version"

    // La UI observará este Flow. La fuente de verdad ÚNICA es Room.
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    /**
     * Inserta o actualiza una categoría individual en Room.
     */
    suspend fun insertOrUpdate(category: CategoryEntity) {
        categoryDao.insertOrUpdate(category)
    }

    suspend fun getCategoryByName(name: String): CategoryEntity? {
        return categoryDao.getCategoryByName(name)
    }

    // ==================================================================================
    // --- 🔄 SECCIÓN: SINCRONIZACIÓN HÍBRIDA (FIRESTORE -> ROOM) ---
    // ==================================================================================
    /**
     * Sincroniza las categorías desde la colección "Servicios" en Firestore hacia Room.
     * Basado en la bandera global de versión en 'config/metadata'.
     */
    suspend fun syncWithFirebase() {
        try {
            Log.d(TAG, "Iniciando proceso de sincronización de categorías...")

            // -------------------------------------------------------
            // 1. [PLAN DE ACCIÓN] ACTUALIZAR PRIMERO LA BASE DE DATOS LOCAL
            // Si Room está vacío, sembramos los datos locales ANTES de intentar conectar con Firebase.
            // -------------------------------------------------------
            val currentCount = categoryDao.getCount()
            Log.d(TAG, "Estado de Room: $currentCount categorías encontradas.")
            
            if (currentCount == 0) {
                Log.i(TAG, "Base de datos vacía. Ejecutando sembrado local prioritario...")
                seedLocalDatabase()
                Log.i(TAG, "Sembrado local completado exitosamente.")
            }

            // 2. Obtener versión remota (Documento liviano para ahorrar cuota de Firebase)
            Log.d(TAG, "Consultando metadatos en Firestore (config/metadata)...")
            val metadata = firestore.collection("config").document("metadata").get().await()
            val remoteVersion = metadata.getLong("categoriesVersion") ?: 0L
            val localVersion = prefs.getLong(KEY_VERSION, 0L)
            
            Log.d(TAG, "Versiones de categorías - Local: $localVersion, Remota: $remoteVersion")

            // 3. Lógica de Actualización: Solo si la bandera remota es mayor
            if (remoteVersion > localVersion) {
                Log.i(TAG, "Nueva versión detectada ($remoteVersion > $localVersion). Descargando colección 'Servicios'...")
                
                // 4. Descarga masiva de la colección "Servicios"
                val snapshot = firestore.collection("Servicios").get().await()
                Log.d(TAG, "Documentos recibidos de Firestore: ${snapshot.size()}")
                
                // 5. Transformación Estricta: Solo name, icon, description, superCategory
                val entities = snapshot.documents.mapNotNull { doc ->
                    try {
                        val dto = doc.toObject(CategoryFirestoreDto::class.java)
                        dto?.let {
                            CategoryEntity(
                                name = it.name,
                                icon = it.icon,
                                superCategory = it.superCategory,
                                superCategoryIcon = it.superCategoryIcon,
                                description = it.description,
                                // Datos secundarios o que no vienen de Firebase
                                providerIds = emptyList(),
                                isNew = false,
                                isNewPrestador = false,
                                isAd = false,
                                isFavorite = false
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapeando documento ${doc.id}: ${e.message}")
                        null
                    }
                }

                // 6. Transacción atómica en Room: Limpiar y recargar datos frescos
                if (entities.isNotEmpty()) {
                    Log.i(TAG, "Actualizando Room con ${entities.size} categorías frescas.")
                    categoryDao.deleteAll()
                    categoryDao.insertAll(entities)
                    
                    // 7. Actualizar versión local para evitar re-sincronizaciones
                    prefs.edit().putLong(KEY_VERSION, remoteVersion).apply()
                    Log.i(TAG, "Sincronización finalizada y versión local actualizada a $remoteVersion.")
                } else {
                    Log.w(TAG, "La colección de Firestore retornó 0 entidades válidas.")
                }
            } else {
                Log.d(TAG, "La base de datos local ya está actualizada (Versión $localVersion).")
            }
        } catch (e: Exception) {
            Log.e(TAG, "ERROR CRÍTICO EN SINCRONIZACIÓN: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * [NUEVO] Siembra los datos iniciales desde el archivo local SembradoServiciosInicia.
     * Esto garantiza que el usuario vea categorías inmediatamente después de una instalación limpia.
     */
    private suspend fun seedLocalDatabase() {
        val entities = SembradoServiciosInicia.categories.map { item ->
            CategoryEntity(
                name = item.name,
                icon = item.icon,
                superCategory = item.superCategory,
                superCategoryIcon = item.superCategoryIcon,
                description = item.description,
                providerIds = item.providerIds,
                isNew = item.isNew,
                isNewPrestador = item.isNewPrestador,
                isAd = item.isAd,
                // [NUEVO] Se respeta el estado de favorito definido en el sembrado local
                isFavorite = item.isFavorite
            )
        }
        if (entities.isNotEmpty()) {
            categoryDao.insertAll(entities)
        }
    }
}
