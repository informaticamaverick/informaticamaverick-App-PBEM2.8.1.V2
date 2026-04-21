package com.example.myapplication.prestador.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.prestador.data.model.ServicioFirebase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton

class ServiciosRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("servicios_cache", Context.MODE_PRIVATE)

    suspend fun getServicios(): List<ServicioFirebase> {
        //1. Leer version remota desde config/metadata
        val remoteVersion = try {
            firestore.collection("config").document("metadata")
                .get().await()
                .getLong("categoriesVersion") ?: 0L
        } catch (e: Exception) {
            0L
        }
        //2. Leer versión local guardada
        val localVersion = prefs.getLong("categories_version", -1L)

        //3. Si la versión no cambió y tenemos caché, devolver caché
        if (remoteVersion != 0L && remoteVersion == localVersion) {
            val json = prefs.getString("categories_json", null)
            if (!json.isNullOrBlank()) {
                return parseCachedServicios(json)
            }
        }

        //4. version cambio o no hay cache -> descargar de firestore
        val snapshot = firestore.collection("Servicios").get().await()
        val servicios = snapshot.documents
            .map { doc ->
                ServicioFirebase(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    icon = doc.getString("icon") ?: "",
                    colorHex = doc.getString("colorHex") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    superCategory = doc.getString("supercategory") ?: "",
                    superCategoryIcon = doc.getString("superCategory") ?: "",
                    updatedAt = doc.getLong("updateAt") ?: 0L
                )
            }
            .filter { it.name.isNotBlank() }
            .sortedWith(compareBy({ it.superCategory}, { it.name }))


        //Guardar en cache local

        if (servicios.isNotEmpty()) {
            prefs.edit()
                .putLong("categories_version", remoteVersion)
                .putString("categories_json", serializerServicios(servicios))
                .apply()
        }

        return servicios
    }

    private fun serializerServicios(list: List<ServicioFirebase>): String {
        val array = JSONArray()
        list.forEach { s ->
            array.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("icon", s.icon)
                put("colorHex", s.colorHex)
                put("imageUrl", s.imageUrl)
                put("superCategory", s.superCategory)
                put("superCategoryIcon", s.superCategoryIcon)
                put("updatedAt", s.updatedAt)
            })
        }

        return array.toString()
    }

    private fun parseCachedServicios(json: String): List<ServicioFirebase> {
        val array = JSONArray(json)
        val list = mutableListOf<ServicioFirebase>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(ServicioFirebase(
                id = obj.optString("id"),
                name = obj.optString("name"),
                icon = obj.optString("icon"),
                colorHex = obj.optString("colorHex"),
                imageUrl = obj.optString("imageUrl"),
                superCategory = obj.optString("superCategory"),
                superCategoryIcon = obj.optString("superCategoryIcon"),
                updatedAt = obj.optLong("updatedAt")
            ))
        }

        return list
    }
}