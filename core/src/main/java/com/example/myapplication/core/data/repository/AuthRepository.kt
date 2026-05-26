package com.example.myapplication.core.data.repository

import android.util.Log
import com.example.myapplication.core.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE AUTENTICACIÓN (COMPARTIDO) ---
 * Centraliza la lógica de inicio de sesión, registro y recuperación de contraseñas.
 * Utiliza Firebase Auth como proveedor principal. Al estar en :core, permite
 * que tanto la App del Cliente como la del Prestador usen el mismo flujo de seguridad.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    /**
     * Inicia sesión utilizando un Token de ID de Google.
     * @param idToken Token obtenido desde el cliente de Google Sign-In.
     * @return Result con el objeto User y un mapa opcional de datos adicionales del perfil.
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

            val additionalInfo = authResult.additionalUserInfo?.profile
            Result.success(Pair(user, additionalInfo))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en signInWithGoogle: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Envía un correo electrónico para restablecer la contraseña.
     * @param email Correo electrónico de la cuenta a recuperar.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.setLanguageCode("es") // Aseguramos el idioma español
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el usuario que tiene la sesión activa actualmente.
     * @return Objeto User o null si no hay sesión iniciada.
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
     * Cierra la sesión activa en el dispositivo.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Elimina el usuario de Firebase Auth.
     */
    suspend fun deleteAuthUser(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inicia sesión con correo y contraseña tradicionales.
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<User> {
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
