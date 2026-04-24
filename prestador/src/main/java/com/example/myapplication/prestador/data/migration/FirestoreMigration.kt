package com.example.myapplication.prestador.data.migration

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreMigration {

    private const val PREFS_NAME = "firestore_migrations"
    private const val KEY_V1_DONE = "migration_v1_done"
    private const val KEY_V2_DONE = "migration_v2_done"
    private const val KEY_V3_DONE = "migration_v3_done"
    private const val KEY_V4_DONE = "migration_v4_done"
    private const val TAG = "FirestoreMigration"

    suspend fun runIfNeeded(context: Context, firestore: FirebaseFirestore, uid: String) {
        runV1IfNeeded(context, firestore, uid)
        runV2IfNeeded(context, firestore, uid)
        runV3IfNeeded(context, firestore, uid)
        runV4IfNeeded(context, firestore, uid)
    }

    // V1: elimina "roles", mueve "modalidad" a raíz, borra root-level imageBase64
    private suspend fun runV1IfNeeded(context: Context, firestore: FirebaseFirestore, uid: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_V1_DONE, false)) return

        try {
            val docRef = firestore.collection("providers").document(uid)
            val doc = docRef.get().await()

            if (!doc.exists()) {
                prefs.edit().putBoolean(KEY_V1_DONE, true).apply()
                return
            }

            val updates = mutableMapOf<String, Any>()
            val deletes = mutableMapOf<String, Any>()

            if (doc.contains("roles")) deletes["roles"] = FieldValue.delete()

            @Suppress("UNCHECKED_CAST")
            val modalidad = doc.get("modalidad") as? Map<String, Any>
            if (modalidad != null) {
                modalidad["vaDomicilio"]?.let       { updates["vaDomicilio"] = it }
                modalidad["atencionUrgencias"]?.let { updates["atencionUrgencias"] = it }
                modalidad["turnosEnLocal"]?.let     { updates["turnosEnLocal"] = it }
                modalidad["envios"]?.let            { updates["envios"] = it }
                deletes["modalidad"] = FieldValue.delete()
            }

            val rootImageUrl = doc.getString("imageUrl") ?: ""
            val rootImageBase64 = doc.getString("imageBase64") ?: ""
            if (rootImageUrl.isNotEmpty() && rootImageBase64.isNotEmpty()) {
                deletes["imageBase64"] = FieldValue.delete()
            }

            val allChanges = updates + deletes
            if (allChanges.isNotEmpty()) {
                docRef.update(allChanges).await()
                Log.d(TAG, "Migración v1 aplicada: ${allChanges.keys}")
            }

            prefs.edit().putBoolean(KEY_V1_DONE, true).apply()
            Log.d(TAG, "Migración v1 completada")
        } catch (e: Exception) {
            Log.e(TAG, "Error en migración v1", e)
        }
    }

    // V2: limpia campos obsoletos (favorito, hasPhysicalStore, hasStoreAppointments, perfil.imageBase64)
    //     y agrega campos nuevos con defaults (doesService, doesProduct, emails, campos raíz)
    private suspend fun runV2IfNeeded(context: Context, firestore: FirebaseFirestore, uid: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_V2_DONE, false)) return

        try {
            val docRef = firestore.collection("providers").document(uid)
            val doc = docRef.get().await()

            if (!doc.exists()) {
                prefs.edit().putBoolean(KEY_V2_DONE, true).apply()
                return
            }

            val updates = mutableMapOf<String, Any>()
            val deletes = mutableMapOf<String, Any>()

            // 1. Eliminar campos obsoletos
            listOf("favorito", "hasPhysicalStore", "hasStoreAppointments").forEach { field ->
                if (doc.contains(field)) deletes[field] = FieldValue.delete()
            }

            // 2. Eliminar perfil.imageBase64 (base64 ocupa demasiado espacio)
            @Suppress("UNCHECKED_CAST")
            val perfil = doc.get("perfil") as? Map<String, Any>
            val perfilImageBase64 = perfil?.get("imageBase64") as? String ?: ""
            if (perfilImageBase64.isNotEmpty()) {
                deletes["perfil.imageBase64"] = FieldValue.delete()
            }

            // 3. Agregar campos nuevos si no existen
            if (!doc.contains("doesService")) updates["doesService"] = false
            if (!doc.contains("doesProduct")) updates["doesProduct"] = false
            if (!doc.contains("emails")) {
                val email = perfil?.get("email") as? String ?: ""
                updates["emails"] = if (email.isNotBlank()) listOf(email) else emptyList<String>()
            }
            if (!doc.contains("verificado")) updates["verificado"] = false
            if (!doc.contains("suscripto"))  updates["suscripto"] = false

            // 4. Promover campos a raíz para búsqueda si solo están en submapas
            @Suppress("UNCHECKED_CAST")
            val localMap = doc.get("local") as? Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val ubicacion = doc.get("ubicacion") as? Map<String, Any>

            if (doc.getBoolean("turnosEnLocal") == null) {
                updates["turnosEnLocal"] = localMap?.get("turnosEnLocal") as? Boolean ?: false
            }
            if ((doc.getString("codigoPostal") ?: "").isBlank()) {
                val cp = (ubicacion?.get("codigoPostal") as? String)
                    ?: (localMap?.get("codigoPostalLocal") as? String) ?: ""
                if (cp.isNotBlank()) updates["codigoPostal"] = cp
            }
            if ((doc.getString("provincia") ?: "").isBlank()) {
                val prov = (ubicacion?.get("provincia") as? String)
                    ?: (localMap?.get("provinciaLocal") as? String) ?: ""
                if (prov.isNotBlank()) updates["provincia"] = prov
            }

            // 5. Promover latitud/longitud desde colección "direcciones" si no están en raíz
            if (doc.getDouble("latitud") == null || doc.getDouble("longitud") == null) {
                try {
                    val dirSnap = firestore.collection("direcciones")
                        .whereEqualTo("referenciaId", uid)
                        .whereEqualTo("referenciaTipo", "PRESTADOR")
                        .get().await()
                    val dirDoc = dirSnap.documents.firstOrNull()
                    val lat = dirDoc?.getDouble("latitud")
                    val lng = dirDoc?.getDouble("longitud")
                    if (lat != null) updates["latitud"] = lat
                    if (lng != null) updates["longitud"] = lng
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo leer direcciones para lat/lng: ${e.message}")
                }
            }

            val allChanges = updates + deletes
            if (allChanges.isNotEmpty()) {
                docRef.update(allChanges).await()
                Log.d(TAG, "Migración v2 aplicada: ${allChanges.keys}")
            }

            prefs.edit().putBoolean(KEY_V2_DONE, true).apply()
            Log.d(TAG, "Migración v2 completada")
        } catch (e: Exception) {
            Log.e(TAG, "Error en migración v2", e)
        }
    }

    // V3: borra perfil.imageBase64 si fue re-creado después de v2 (por código viejo)
    private suspend fun runV3IfNeeded(context: Context, firestore: FirebaseFirestore, uid: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_V3_DONE, false)) return

        try {
            val docRef = firestore.collection("providers").document(uid)
            val doc = docRef.get().await()
            if (!doc.exists()) return

            @Suppress("UNCHECKED_CAST")
            val perfil = doc.get("perfil") as? Map<String, Any>
            val hasImageBase64 = perfil?.containsKey("imageBase64") == true

            if (hasImageBase64) {
                docRef.update("perfil.imageBase64", FieldValue.delete()).await()
                Log.d(TAG, "Migración v3: eliminado perfil.imageBase64")
            }

            prefs.edit().putBoolean(KEY_V3_DONE, true).apply()
            Log.d(TAG, "Migración v3 completada")
        } catch (e: Exception) {
            Log.e(TAG, "Error en migración v3", e)
        }
    }

    // V4: elimina campos planos con punto en el nombre (ej: "empresa.cuitEmpresa" como campo raíz)
    //     que fueron creados por un bug donde se usaba set(merge) en lugar de update()
    private suspend fun runV4IfNeeded(context: Context, firestore: FirebaseFirestore, uid: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_V4_DONE, false)) return

        try {
            val docRef = firestore.collection("providers").document(uid)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                prefs.edit().putBoolean(KEY_V4_DONE, true).apply()
                return
            }

            val flatDotFields = listOf(
                "empresa.cuitEmpresa", "empresa.nombreEmpresa", "empresa.razonSocial",
                "empresa.direccionEmpresa", "empresa.tieneEmpresa", "empresa.trabajaConOtros",
                "empresa.codigoPostalNegocio",
                "perfil.nombre", "perfil.apellido", "perfil.email", "perfil.telefono",
                "perfil.dniCuit", "perfil.matricula", "perfil.profesion", "perfil.description",
                "perfil.tieneMatricula",
                "ubicacion.direccion", "ubicacion.codigoPostal", "ubicacion.provincia",
                "ubicacion.pais", "ubicacion.localidad",
                "local.direccionLocal", "local.provinciaLocal", "local.codigoPostalLocal",
                "local.horarioLocal", "local.turnosEnLocal"
            )

            // Un campo plano con punto en el nombre existe cuando doc.getData() tiene esa clave EXACTA
            val data = doc.data ?: emptyMap()
            val toDelete = mutableMapOf<String, Any>()
            for (field in flatDotFields) {
                if (data.containsKey(field)) {
                    toDelete[field] = FieldValue.delete()
                    Log.d(TAG, "Migración v4: eliminando campo plano '$field'")
                }
            }

            if (toDelete.isNotEmpty()) {
                // Usar set(merge) para borrar campos literales con punto (update() los interpretaría como rutas)
                docRef.set(toDelete, com.google.firebase.firestore.SetOptions.merge()).await()
                Log.d(TAG, "Migración v4: eliminados ${toDelete.size} campos planos")
            }

            prefs.edit().putBoolean(KEY_V4_DONE, true).apply()
            Log.d(TAG, "Migración v4 completada")
        } catch (e: Exception) {
            Log.e(TAG, "Error en migración v4", e)
        }
    }
}
