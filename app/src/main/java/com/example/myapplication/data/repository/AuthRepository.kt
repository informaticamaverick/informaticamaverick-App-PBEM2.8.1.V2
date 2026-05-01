package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.AddressClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Inicia sesión con Google. 
     * Retorna un Result con el User y los datos adicionales del perfil de Google.
     */
    suspend fun signInWithGoogle(idToken: String): Result<Pair<User, Map<String, Any?>?>> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Usuario no encontrado"))

            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )

            // Extraemos info adicional del perfil de Google (name, last name, etc)
            val additionalInfo = authResult.additionalUserInfo?.profile

            Result.success(Pair(user, additionalInfo))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en signInWithGoogle: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ======================================================================================
     * --- SECCIÓN: VERIFICACIÓN DE PERFIL MAVERICK V5 ---
     * ======================================================================================
     * Verifica si el perfil existe y si tiene al menos una dirección con Código Postal.
     * Retorna un Pair(ExistePerfil, TieneCodigoPostal)
     */
    suspend fun checkProfileStatus(uid: String): Pair<Boolean, Boolean> {
        return try {
            val userDoc = withTimeoutOrNull(4000) {
                firestore.collection("usuarios").document(uid).get().await()
            } ?: firestore.collection("usuarios").document(uid).get(Source.CACHE).await()

            if (!userDoc.exists()) return Pair(false, false)

            // Obtenemos las direcciones personales del documento para validar el CP
            val personalAddressesRaw = userDoc.get("personalAddresses") as? List<*>
            var hasZipCode = false
            
            personalAddressesRaw?.forEach { item ->
                val addrMap = item as? Map<*, *>
                val cp = addrMap?.get("codigoPostal") as? String
                if (!cp.isNullOrBlank()) {
                    hasZipCode = true
                }
            }

            Pair(true, hasZipCode)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error verificando estatus de perfil: ${e.message}")
            Pair(false, false)
        }
    }

    /**
     * --- LÓGICA OFFLINE-FIRST PARA PERFIL ---
     * Verifica si el perfil existe, priorizando la velocidad y el acceso offline.
     */
    suspend fun checkUserProfileExists(uid: String): Boolean {
        return try {
            val userDoc = withTimeoutOrNull(4000) {
                firestore.collection("usuarios").document(uid).get().await()
            } ?: firestore.collection("usuarios").document(uid).get(Source.CACHE).await()

            if (!userDoc.exists()) return false

            // Ya no validamos por el campo "roles", solo verificamos que el documento exista
            // y si el perfil está marcado como completo.
            val isProfileComplete = userDoc.getBoolean("isProfileComplete") ?: false
            
            if (!isProfileComplete) {
                val phoneNumber = userDoc.getString("phoneNumber")
                val hasAddress = userDoc.get("personalAddresses") != null
                if (!phoneNumber.isNullOrEmpty() && hasAddress) {
                    return true
                }
            }
            isProfileComplete
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error verificando perfil: ${e.message}")
            true 
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.setLanguageCode("es")
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el usuario de Auth pero también intenta refrescar su estado desde Firestore
     * para verificar las direcciones reales.
     */
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString() ?: ""
        )
    }

    /**
     * NUEVO: Obtiene el estado completo del perfil desde Firestore.
     */
    suspend fun getFullUserProfile(uid: String): User? {
        return try {
            val userDoc = withTimeoutOrNull(4000) {
                firestore.collection("usuarios").document(uid).get().await()
            } ?: firestore.collection("usuarios").document(uid).get(Source.CACHE).await()
            
            if (!userDoc.exists()) return null
            
            // Convertimos el documento a nuestro modelo de dominio User
            // Aquí deberías tener una lógica de mapeo. Por ahora simulamos la carga de direcciones.
            val personalAddressesRaw = userDoc.get("personalAddresses") as? List<*>
            val addresses = mutableListOf<AddressClient>()
            personalAddressesRaw?.forEach { item ->
                val addrMap = item as? Map<*, *>
                addresses.add(AddressClient(
                    id = addrMap?.get("id") as? String ?: "",
                    calle = addrMap?.get("calle") as? String ?: "",
                    numero = addrMap?.get("numero") as? String ?: "",
                    localidad = addrMap?.get("localidad") as? String ?: "",
                    codigoPostal = addrMap?.get("codigoPostal") as? String ?: ""
                ))
            }
            
            User(
                uid = uid,
                email = userDoc.getString("email") ?: "",
                displayName = userDoc.getString("displayName") ?: "",
                personalAddresses = addresses,
                isProfileComplete = userDoc.getBoolean("isProfileComplete") ?: false
            )
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<User>{
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Usuario no encontrado"))
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
