package com.example.myapplication.core.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.core.data.local.dao.UserDao
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.data.remote.UserDataMapper
import com.example.myapplication.core.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE USUARIO (COMPARTIDO) ---
 * Gestiona el perfil del usuario autenticado en todo el ecosistema Maverick. 
 * Implementa la estrategia "Offline-First" y garantiza la Identidad Unificada (SSOT).
 */
@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val TAG = "UserRepository"

    // ======================================================================================
    // === SECCIÓN 1: ESTADO Y OBSERVABLES (Single Source of Truth) ===
    // ======================================================================================

    /**
     * Observable del perfil de usuario actual. Proviene directamente de Room.
     */
    val userProfile: Flow<UserEntity?> = userDao.getUser()

    /**
     * Obtiene el perfil de usuario de forma inmediata (One-shot).
     */
    suspend fun getUserOnce(): UserEntity? = userDao.getUserOnce()


    // ======================================================================================
    // === SECCIÓN 2: LÓGICA COMPARTIDA (SINCRONIZACIÓN Y PERSISTENCIA) ===
    // ======================================================================================

    /**
     * Sincroniza el objeto User con Firestore.
     * Guarda localmente en Room antes de intentar la subida a la nube.
     * Estructura los datos jerárquicamente para compatibilidad absoluta entre apps.
     */
    suspend fun syncUserWithFirebase(user: User) {
        val uid = auth.currentUser?.uid ?: user.uid
        if (uid.isBlank()) return

        try {
            // 1. Persistencia local inmediata (Costo Zero)
            userDao.insertOrUpdateUser(UserEntity.fromDomain(user))

            // 2. Preparación de mapa compatible con SSOT
            val perfilMap = mapOf(
                "name" to user.name,
                "nombre" to user.name, 
                "lastName" to user.lastName,
                "apellido" to user.lastName,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "telefono" to user.phoneNumber,
                "displayName" to user.displayName,
                "bio" to user.bio,
                "photoUrl" to user.photoUrl,
                "imageUrl" to user.photoUrl,
                "bannerImageUrl" to user.bannerImageUrl,
                "rating" to user.rating
            )

            val userData = mutableMapOf<String, Any?>(
                "uid" to uid,
                "email" to user.email,
                "displayName" to user.displayName,
                "perfil" to perfilMap,
                "isVerified" to user.isVerified,
                "rating" to user.rating,
                "hasCompanyProfile" to user.hasCompanyProfile,
                "personalAddresses" to user.personalAddresses,
                "companies" to user.companies,
                "isOnline" to user.isOnline,
                "createdAt" to user.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            // 3. Sincronización con la nube
            firestore.collection("usuarios").document(uid).set(userData, SetOptions.merge()).await()
            
            Log.d(TAG, "✅ Perfil sincronizado con la nube (Estructura Elite).")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Fallo en sincronización remota: ${e.message}")
            throw e
        }
    }

    /**
     * Descarga el perfil desde Firestore y lo mapea quirúrgicamente a Room.
     */
    suspend fun refreshUserFromRemote() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val snapshot = firestore.collection("usuarios").document(uid).get().await()
            val userEntity = UserDataMapper.fromFirestore(snapshot)
            
            userEntity?.let {
                userDao.insertOrUpdateUser(it)
                Log.d(TAG, "⚡ Room actualizado desde Firestore via Mapper.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refrescando usuario: ${e.message}")
        }
    }

    /**
     * Actualiza el estado de disponibilidad del usuario.
     */
    fun setUserOnline(isOnline: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("usuarios").document(uid).update("isOnline", isOnline)
    }


    // ======================================================================================
    // === SECCIÓN 3: FUNCIONES ESPECÍFICAS - APP CLIENTE (ON-BOARDING) ===
    // ======================================================================================

    /**
     * Inicializa un usuario nuevo extrayendo datos enriquecidos de Google.
     * Solo se ejecuta si el usuario no tiene una cuenta previa en Firestore.
     */
    suspend fun initializeNewUserFromGoogle(user: User, googleProfile: Map<String, Any?>?) {
        val uid = user.uid
        if (uid.isBlank()) return

        try {
            val doc = firestore.collection("usuarios").document(uid).get().await()
            if (!doc.exists()) {
                Log.d(TAG, "🆕 Usuario nuevo detectado. Autocompletando perfil desde Google.")
                
                val givenName = googleProfile?.get("given_name") as? String ?: ""
                val familyName = googleProfile?.get("family_name") as? String ?: ""
                val photoUrl = googleProfile?.get("picture") as? String ?: user.photoUrl

                val enrichedUser = user.copy(
                    name = givenName.ifBlank { user.name },
                    lastName = familyName.ifBlank { user.lastName },
                    photoUrl = photoUrl,
                    isProfileComplete = false 
                )
                
                syncUserWithFirebase(user = enrichedUser)
            } else {
                Log.d(TAG, "👋 Usuario existente. Omitiendo autocompletado.")
                refreshUserFromRemote()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en inicialización Google: ${e.message}")
            syncUserWithFirebase(user)
        }
    }


    // ======================================================================================
    // === SECCIÓN 4: SEGURIDAD Y LIMPIEZA DE DATOS (ZONA DE RIESGO) ===
    // ======================================================================================

    /**
     * Limpia la base de datos local (Cierre de sesión).
     */
    suspend fun clearLocalUser() {
        userDao.deleteUser()
        Log.d(TAG, "🧹 Datos locales de usuario eliminados.")
    }

    /**
     * Elimina la cuenta del usuario de forma permanente en Firestore y Room.
     */
    suspend fun deleteAccount() {
        val uid = auth.currentUser?.uid ?: return
        try {
            // 1. Borrado en la nube
            firestore.collection("usuarios").document(uid).delete().await()
            // 2. Borrado local
            clearLocalUser()
            Log.d(TAG, "💀 Cuenta eliminada permanentemente para: $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando cuenta: ${e.message}")
            throw e
        }
    }
}
