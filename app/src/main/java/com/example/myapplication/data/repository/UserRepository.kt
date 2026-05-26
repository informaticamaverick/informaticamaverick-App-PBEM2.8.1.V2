package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.data.local.UserDao
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.local.toEntity
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.BranchClient
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.RepresentativeClient
import com.example.myapplication.data.model.User
import com.example.myapplication.util.ImageUtils
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE USUARIO (UserRepository) ---
 * 
 * Este repositorio actúa como el mediador entre la base de datos local (Room)
 * y la base de datos remota (Firestore). Sincroniza el perfil del dueño de la app.
 */
@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    /**
     * Flujo reactivo del perfil de usuario desde Room.
     */
    val userProfile: Flow<UserEntity?> = userDao.getUser()

    /**
     * Comprime y sube una imagen a Storage si es una URI local.
     * Soporta: content://, file://, base64 raw (JPEG/PNG), y URLs https (pasa directo).
     */
    private suspend fun uploadAndGetUrl(uriString: String?, path: String): String? {
        if (uriString.isNullOrBlank()) return null

        // URLs de Storage o web: pasar directo
        if (uriString.startsWith("https://") || uriString.startsWith("http://")) {
            return uriString
        }

        // URIs locales (content:// o file://)
        if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
            return try {
                val uri = Uri.parse(uriString)
                val bytes = ImageUtils.compressImageToWebP(context, uri) ?: return uriString
                val ref = storage.reference.child(path)
                ref.putBytes(bytes).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                Log.d("UserRepository", "✓ Imagen subida: $path")
                downloadUrl
            } catch (e: Exception) {
                Log.e("UserRepository", "❌ Error subiendo imagen: ${e.message}")
                uriString
            }
        }

        // Base64 raw (strings largas que no son URIs ni URLs)
        if (uriString.length > 100 && !uriString.startsWith("data:")) {
            return try {
                val bytes = android.util.Base64.decode(uriString, android.util.Base64.NO_WRAP)
                val ref = storage.reference.child(path)
                ref.putBytes(bytes).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                Log.d("UserRepository", "✓ Base64 subida a Storage: $path")
                downloadUrl
            } catch (e: Exception) {
                Log.w("UserRepository", "⚠️ No se pudo subir base64 a Storage: ${e.message}")
                // Devolver como data URI para que Coil lo pueda mostrar localmente
                "data:image/jpeg;base64,$uriString"
            }
        }

        return uriString
    }

    /**
     * --- SECCIÓN: SINCRONIZACIÓN DE DATOS (Offline-First) ---
     * 
     * Sincronización PROFUNDA con Estructura Jerárquica.
     * LOGICA: 1. Guarda en Room -> 2. Sincroniza con Firebase -> 3. Actualiza Room con URLs remotas.
     */
    suspend fun syncUserWithFirebase(user: User) {
        val uid = auth.currentUser?.uid ?: user.uid
        if (uid.isBlank()) return

        try {
            // =========================================================================
            // PASO 1: ACTUALIZACIÓN LOCAL INMEDIATA (Garantiza funcionamiento Offline)
            // Marcamos como isSynced = false para indicar que hay cambios pendientes de nube.
            // =========================================================================
            val localCopy = user.copy(isSynced = false)
            userDao.insertOrUpdateUser(localCopy.toEntity())
            Log.d("UserRepository", "🏠 [LOCAL] Datos guardados en Room (Pendiente de Sync).")

            // =========================================================================
            // PASO 2: SINCRONIZACIÓN REMOTA (Firebase)
            // =========================================================================
            Log.d("UserRepository", "⏳ [REMOTO] Iniciando sincronización para: $uid")

            // A. Procesar Multimedia del Usuario (Subida de fotos)
            val userPhoto = uploadAndGetUrl(user.photoUrl, "users/$uid/profile.jpg")
            val userBanner = uploadAndGetUrl(user.bannerImageUrl, "users/$uid/banner.jpg")
            val userGallery = user.galleryImages.mapIndexed { index, uri ->
                uploadAndGetUrl(uri, "users/$uid/gallery/img_$index.jpg") ?: uri
            }

            // B. Documento Principal en Firestore
            val userDocRef = firestore.collection("usuarios").document(uid)
            val userMap = linkedMapOf<String, Any?>(
                "uid" to uid,
                "email" to user.email,
                "displayName" to user.displayName,
                "name" to user.name,
                "lastName" to user.lastName,
                "phoneNumber" to user.phoneNumber,
                "bio" to user.bio,
                "photoUrl" to userPhoto,
                "bannerImageUrl" to userBanner,
                "galleryImages" to userGallery,
                "personalAddresses" to user.personalAddresses,
                "additionalEmails" to user.additionalEmails,
                "additionalPhones" to user.additionalPhones,
                "hasCompanyProfile" to user.hasCompanyProfile,
                "isOnline" to user.isOnline,
                "isSubscribed" to user.isSubscribed,
                "isVerified" to user.isVerified,
                "notificationsEnabled" to user.notificationsEnabled,
                "isPublicProfile" to user.isPublicProfile,
                "isProfileComplete" to user.isProfileComplete,
                "rating" to user.rating,
                "favoriteProviderIds" to user.favoriteProviderIds,
                "latitude" to user.latitude,
                "longitude" to user.longitude,
                "createdAt" to user.createdAt
            )
            
            userDocRef.set(userMap).await() 

            // C. Subcolección: personalAddresses (Sincronización Directa)
            try {
                user.personalAddresses.forEach { address ->
                    userDocRef.collection("personalAddresses")
                        .document(address.id)
                        .set(address, SetOptions.merge())
                        .await()
                }
                Log.d("UserRepository", "📍 [REMOTO] Direcciones sincronizadas.")
            } catch (e: Exception) {
                Log.e("UserRepository", "⚠️ [REMOTO] Error en subcolección personalAddresses: ${e.message}")
            }

            // D. Subcolección: companies (Empresas y sus jerarquías)
            try {
                user.companies.forEach { company ->
                    val compPhoto = uploadAndGetUrl(company.photoUrl, "users/$uid/companies/${company.id}/logo.jpg")
                    val compBanner = uploadAndGetUrl(company.bannerImageUrl, "users/$uid/companies/${company.id}/banner.jpg")
                    
                    val companyDocRef = userDocRef.collection("companies").document(company.id)
                    val companyMap = mutableMapOf<String, Any?>(
                        "id" to company.id,
                        "name" to company.name,
                        "razonSocial" to company.razonSocial,
                        "cuit" to company.cuit,
                        "email" to company.email,
                        "phoneNumber" to company.phoneNumber,
                        "photoUrl" to compPhoto,
                        "bannerImageUrl" to compBanner
                    )
                    companyDocRef.set(companyMap, SetOptions.merge()).await()

                    // Sucursales
                    company.branches.forEach { branch ->
                        val branchGallery = branch.galleryImages.mapIndexed { index, uri ->
                            uploadAndGetUrl(uri, "users/$uid/companies/${company.id}/branches/${branch.id}/gallery/img_$index.jpg") ?: uri
                        }

                        val branchDocRef = companyDocRef.collection("branches").document(branch.id)
                        val branchMap = mutableMapOf<String, Any?>(
                            "id" to branch.id,
                            "name" to branch.name,
                            "isMainBranch" to branch.isMainBranch,
                            "address" to branch.address,
                            "galleryImages" to branchGallery
                        )
                        branchDocRef.set(branchMap, SetOptions.merge()).await()

                        // Representantes
                        branch.representatives.forEach { rep ->
                            val repPhoto = uploadAndGetUrl(rep.photoUrl, "users/$uid/companies/${company.id}/branches/${branch.id}/reps/${rep.id}.jpg")
                            val repMap = rep.copy(photoUrl = repPhoto)
                            branchDocRef.collection("representatives")
                                .document(rep.id)
                                .set(repMap, SetOptions.merge())
                                .await()
                        }
                    }
                }
                Log.d("UserRepository", "🏢 [REMOTO] Empresas y sucursales sincronizadas.")
            } catch (e: Exception) {
                Log.e("UserRepository", "⚠️ [REMOTO] Error en subcolección companies: ${e.message}")
            }

            // =========================================================================
            // PASO 3: ACTUALIZACIÓN LOCAL FINAL (Guardar URLs remotas y Marcar Sincronizado)
            // =========================================================================
            val finalUser = user.copy(
                photoUrl = userPhoto,
                bannerImageUrl = userBanner,
                galleryImages = userGallery,
                isSynced = true // SINCRONIZACIÓN COMPLETADA EXITOSAMENTE
            )
            userDao.insertOrUpdateUser(finalUser.toEntity())
            
            Log.d("UserRepository", "✅ [REMOTO] Sincronización exitosa. Estado: SYNCED")

        } catch (e: Exception) {
            Log.e("UserRepository", "❌ [FALLO] Error en sincronización: ${e.message}")
            // IMPORTANTE: Relanzamos la excepción para que el ViewModel notifique al usuario
            throw e
        }
    }

    /**
     * Reconstruye el objeto User desde la estructura jerárquica de Firestore.
     * Optimizada para evitar pérdida de datos en nuevos dispositivos y proteger datos locales ante errores de red/permisos.
     */
    suspend fun refreshUserFromRemote() {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid

        try {
            Log.d("UserRepository", "🔄 [SINC] Iniciando recuperación remota para: $uid")
            
            // 0. Obtener datos locales actuales como respaldo
            val localUser = userDao.getUserOnce()?.toDomain()

            // 1. Obtener Documento Principal
            val userDoc = firestore.collection("usuarios").document(uid).get().await()
            
            if (!userDoc.exists()) {
                Log.w("UserRepository", "⚠️ [SINC] El documento no existe en Firestore.")
                return
            }

            // Mapeo inicial (campos básicos)
            val userBase = userDoc.toObject(User::class.java) ?: User(uid = uid)
            Log.d("UserRepository", "✅ [SINC] Documento principal recuperado (${userBase.email})")

            // 2. Cargar Direcciones Personales (Subcolección)
            val addresses = try {
                val remoteAddresses = userDoc.reference.collection("personalAddresses")
                    .get().await().toObjects(AddressClient::class.java)
                
                Log.d("UserRepository", "📍 [SINC] Direcciones remotas: ${remoteAddresses.size}")
                remoteAddresses
            } catch (e: Exception) {
                Log.e("UserRepository", "❌ Error cargando personalAddresses: ${e.message}")
                if (e.message?.contains("PERMISSION_DENIED") == true) throw e
                localUser?.personalAddresses ?: emptyList()
            }

            // 3. Cargar Empresas (Subcolección)
            val companies = try {
                val companiesSnapshot = userDoc.reference.collection("companies").get().await()
                Log.d("UserRepository", "🏢 [SINC] Empresas encontradas en remoto: ${companiesSnapshot.size()}")
                
                companiesSnapshot.documents.mapNotNull { compDoc ->
                    val company = compDoc.toObject(CompanyClient::class.java) ?: return@mapNotNull null
                    
                    val branchesSnapshot = compDoc.reference.collection("branches").get().await()
                    val branches = branchesSnapshot.documents.mapNotNull { branchDoc ->
                        val branch = branchDoc.toObject(BranchClient::class.java) ?: return@mapNotNull null
                        
                        val reps = branchDoc.reference.collection("representatives")
                            .get().await().toObjects(RepresentativeClient::class.java)
                        
                        branch.copy(representatives = reps)
                    }
                    company.copy(branches = branches)
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "❌ Error crítico cargando empresas de Firestore: ${e.message}")
                if (e.message?.contains("PERMISSION_DENIED") == true) throw e
                localUser?.companies ?: emptyList()
            }

            // 6. Construir objeto final unificado (Marcamos como Sincronizado tras Refresh)
            val finalUser = userBase.copy(
                uid = uid,
                personalAddresses = if (addresses.isNotEmpty()) addresses else (localUser?.personalAddresses ?: userBase.personalAddresses),
                companies = if (companies.isNotEmpty()) companies else (localUser?.companies ?: emptyList()),
                isProfileComplete = userBase.isProfileComplete,
                isSynced = true // TRAS REFRESH EXITOSO, LOS DATOS ESTÁN SINCRONIZADOS
            )
            
            // 7. Persistir en Room
            userDao.insertOrUpdateUser(finalUser.toEntity())
            Log.d("UserRepository", "🎉 [SINC] Finalizado exitosamente. Estado: SYNCED")

        } catch (e: Exception) {
            Log.e("UserRepository", "🔥 [SINC] ERROR GENERAL: ${e.message}")
            throw e // IMPORTANTE: Relanzar para que el ViewModel detecte el error de permisos
        }
    }

    /**
     * Inicializa un nuevo usuario con datos extraídos de Google.
     * Ahora acepta un mapa con el perfil de Google para extraer nombre y apellido.
     */
    suspend fun initializeNewUserFromGoogle(userBase: User, googleProfile: Map<String, Any?>?) {
        // Extraer nombre y apellido de forma inteligente desde el perfil de Google
        val givenName = googleProfile?.get("given_name") as? String
        val familyName = googleProfile?.get("family_name") as? String
        
        val newUser = userBase.copy(
            name = givenName ?: userBase.displayName.split(" ").firstOrNull() ?: "",
            lastName = familyName ?: userBase.displayName.split(" ").drop(1).joinToString(" "),
            isOnline = true,
            createdAt = System.currentTimeMillis(),
            isProfileComplete = false
        )
        
        userDao.insertOrUpdateUser(newUser.toEntity())
        Log.d("UserRepository", "🏠 Perfil inicial enriquecido guardado en Room")
        
        try {
            syncUserWithFirebase(newUser)
        } catch (e: Exception) {
            Log.e("UserRepository", "⚠️ Error al sincronizar perfil inicial: ${e.message}")
        }
    }

    suspend fun clearLocalUser() {
        userDao.deleteUser()
        auth.signOut()
    }
}
