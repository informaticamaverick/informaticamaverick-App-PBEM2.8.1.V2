package com.example.myapplication.core.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.core.data.local.dao.UserDao
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.data.remote.UserDataMapper
import com.example.myapplication.core.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE USUARIO (COMPARTIDO) ---
 * [ELITE v5.1]: Implementa Carga On-Demand Jerárquica (Subcolecciones).
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

    /**
     * Obtiene un usuario de forma reactiva por ID (Sección COMÚN).
     */
    fun getUserById(uid: String): Flow<User?> {
        return userDao.getUserByIdFlow(uid).map { it?.toDomain() }
    }

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers().map { entities -> 
        entities.map { it.toDomain() } 
    }

    fun searchUsers(query: String): Flow<List<User>> = userDao.searchUsers("%$query%").map { entities ->
        entities.map { it.toDomain() }
    }


    // ======================================================================================
    // === SECCIÓN 2: LÓGICA COMPARTIDA (SINCRONIZACIÓN Y PERSISTENCIA) ===
    // ======================================================================================

    /**
     * Sincroniza el perfil y desglosa las empresas en subcolecciones (Ley #3.2 Deep Loading).
     */
    suspend fun syncUserWithFirebase(user: User, remotePhotoUrl: String? = null) {
        val uid = auth.currentUser?.uid ?: user.uid
        if (uid.isBlank()) {
            Log.e(TAG, "❌ [SYNC_USER] UID vacío, abortando sincronización.")
            return
        }

        Log.d(TAG, "📡 [SYNC_USER] Iniciando para: ${user.displayName} (UID: $uid) | Direcciones: ${user.personalAddresses.size}")

        try {
            val now = System.currentTimeMillis()
            val userToSync = user.copy(updatedAt = now)

            // 1. Persistencia local inmediata (Costo Zero)
            userDao.insertOrUpdateUser(UserEntity.fromDomain(userToSync))
            Log.d(TAG, "💾 [SYNC_USER] Guardado en Room (Local) OK.")

            // 2. Perfil Básico (Shallow Document)
            val isLocal = userToSync.photoUrl?.startsWith("/") == true || userToSync.photoUrl?.startsWith("content://") == true
            val finalPhotoUrl = remotePhotoUrl ?: if (isLocal) null else userToSync.photoUrl
            
            val perfilMap = mutableMapOf<String, Any?>(
                "name" to userToSync.name,
                "lastName" to userToSync.lastName,
                "email" to userToSync.email,
                "phoneNumber" to userToSync.phoneNumber,
                "displayName" to userToSync.displayName,
                "bio" to userToSync.bio,
                "photoUrl" to finalPhotoUrl,
                "profileThumbnail" to userToSync.profileThumbnail
            ).filterValues { it != null && (it !is String || it.isNotBlank()) }

            val userData = mutableMapOf<String, Any?>(
                "uid" to uid,
                "email" to userToSync.email,
                "displayName" to userToSync.displayName,
                "perfil" to perfilMap,
                "isVerified" to userToSync.isVerified,
                "hasCompanyProfile" to userToSync.hasCompanyProfile,
                "personalAddresses" to userToSync.personalAddresses,
                "isOnline" to userToSync.isOnline,
                "createdAt" to userToSync.createdAt,
                "updatedAt" to now
            ).filterValues { it != null }

            Log.d(TAG, "📡 [SYNC_USER] Enviando a Firestore. Direcciones: ${userToSync.personalAddresses.size}")
            firestore.collection("usuarios").document(uid).set(userData, SetOptions.merge()).await()
            Log.d(TAG, "🌐 [SYNC_USER] Documento principal en Firestore OK.")

            // 3. Sincronización Profunda de Empresas (Subcolecciones)
            userToSync.companies.forEach { company ->
                val compRef = firestore.collection("usuarios").document(uid)
                    .collection("companies").document(company.id)
                
                // 🔥 [ELITE] Remoteize: Convertir rutas locales a Base64 para el transporte a Firebase
                val companyRemoteData = remoteizeCompany(company)
                compRef.set(companyRemoteData, SetOptions.merge()).await()

                company.branches.forEach { branch ->
                    val branchRemoteData = remoteizeBranch(branch)
                    compRef.collection("branches").document(branch.id).set(branchRemoteData, SetOptions.merge()).await()
                }
            }
            Log.d(TAG, "🏢 [SYNC_USER] Empresas y sucursales sincronizadas.")
            
            // 4. Actualizar el documento principal con datos Shallow para respuesta inmediata
            val shallowCompanies = userToSync.companies.map { company ->
                val remotePhoto = if (company.photoUrl?.startsWith("/") == true) {
                    fileToBase64(company.photoUrl)
                } else company.photoUrl

                mapOf(
                    "id" to company.id,
                    "name" to company.name,
                    "photoUrl" to remotePhoto,
                    "thumbnailBase64" to company.thumbnailBase64,
                    "branches" to company.branches.take(2).map { b -> 
                        mapOf(
                            "id" to b.id,
                            "name" to b.name,
                            "address" to b.address
                        )
                    }
                )
            }
            firestore.collection("usuarios").document(uid).update("companies", shallowCompanies).await()
            
            Log.d(TAG, "✅ [SYNC_USER] Proceso completo finalizado exitosamente.")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Fallo en sincronización remota: ${e.message}")
            throw e
        }
    }

    private fun remoteizeCompany(company: CompanyClient): Map<String, Any?> {
        val remotePhoto = if (company.photoUrl?.startsWith("/") == true) fileToBase64(company.photoUrl) else company.photoUrl
        return mapOf(
            "id" to company.id,
            "name" to company.name,
            "razonSocial" to company.razonSocial,
            "cuit" to company.cuit,
            "email" to company.email,
            "phoneNumber" to company.phoneNumber,
            "photoUrl" to remotePhoto,
            "thumbnailBase64" to company.thumbnailBase64
        )
    }

    private fun remoteizeBranch(branch: BranchClient): Map<String, Any?> {
        val repsRemote = branch.representatives.map { rep ->
            val remotePhoto = if (rep.photoUrl?.startsWith("/") == true) fileToBase64(rep.photoUrl) else rep.photoUrl
            mapOf(
                "id" to rep.id,
                "nombre" to rep.nombre,
                "apellido" to rep.apellido,
                "cargo" to rep.cargo,
                "photoUrl" to remotePhoto,
                "thumbnailBase64" to rep.thumbnailBase64
            )
        }
        return mapOf(
            "id" to branch.id,
            "name" to branch.name,
            "description" to branch.description,
            "workingHours" to branch.workingHours,
            "isMainBranch" to branch.isMainBranch,
            "address" to branch.address,
            "representatives" to repsRemote
        )
    }

    private fun fileToBase64(path: String): String? {
        return try {
            val file = java.io.File(path)
            if (file.exists()) {
                val bytes = file.readBytes()
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo archivo a Base64: ${e.message}")
            null
        }
    }

    /**
     * Busca y sincroniza un usuario específico (cliente) desde Firebase.
     * Útil para prestadores que necesitan ver el perfil del cliente.
     */
    suspend fun fetchAndSyncUserDetail(uid: String) {
        if (uid.isBlank()) return
        try {
            val snapshot = firestore.collection("usuarios").document(uid).get().await()
            if (snapshot.exists()) {
                val userEntity = UserDataMapper.fromFirestore(snapshot)
                userEntity?.let {
                    userDao.insertOrUpdateUser(it)
                    Log.d(TAG, "⚡ Sincronizado perfil de cliente: $uid")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando detalle del usuario: ${e.message}")
        }
    }

    /**
     * Carga Shallow (Datos básicos) desde Firestore.
     */
    suspend fun refreshUserFromRemote() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val snapshot = firestore.collection("usuarios").document(uid).get().await()
            val userEntity = UserDataMapper.fromFirestore(snapshot)
            
            userEntity?.let {
                userDao.insertOrUpdateUser(it)
                Log.d(TAG, "⚡ Shallow Load completado para usuario.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refrescando usuario: ${e.message}")
        }
    }

    /**
     * [DEEP LOAD]: Carga la jerarquía completa de empresas del usuario.
     */
    suspend fun loadFullUserCompanies() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val companiesSnapshot = firestore.collection("usuarios").document(uid)
                .collection("companies").get().await()

            val fullCompanies = companiesSnapshot.documents.map { compDoc ->
                val branchesSnapshot = compDoc.reference.collection("branches").get().await()
                val branches = branchesSnapshot.documents.map { branchDoc ->
                    branchDoc.toObject(BranchClient::class.java)!!
                }
                compDoc.toObject(CompanyClient::class.java)!!.copy(branches = branches)
            }

            val currentUser = userDao.getUserOnce()
            currentUser?.let {
                val updatedUser = it.copy(companies = fullCompanies)
                userDao.insertOrUpdateUser(updatedUser)
                Log.d(TAG, "✅ Deep Load de empresas completado (${fullCompanies.size} empresas).")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en Deep Load de empresas: ${e.message}")
        }
    }

    fun setUserOnline(isOnline: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("usuarios").document(uid).update("isOnline", isOnline)
    }

    suspend fun initializeNewUserFromGoogle(userBase: User, googleProfile: Map<String, Any?>?) {
        try {
            val firstName = googleProfile?.get("given_name") as? String ?: userBase.name
            val lastName = googleProfile?.get("family_name") as? String ?: userBase.lastName
            
            val enrichedUser = userBase.copy(
                name = firstName,
                lastName = lastName,
                isProfileComplete = false
            )

            userDao.insertOrUpdateUser(UserEntity.fromDomain(enrichedUser))
            syncUserWithFirebase(enrichedUser)
            
            Log.d(TAG, "✅ Nuevo usuario de Google inicializado: ${userBase.uid}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inicializando usuario de Google: ${e.message}")
        }
    }
    suspend fun clearLocalUser() {
        userDao.deleteUser()
        Log.d(TAG, "🧹 Datos locales de usuario eliminados.")
    }

    suspend fun deleteAccount() {
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection("usuarios").document(uid).delete().await()
            clearLocalUser()
            Log.d(TAG, "💀 Cuenta eliminada permanentemente para: $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando cuenta: ${e.message}")
            throw e
        }
    }
}
