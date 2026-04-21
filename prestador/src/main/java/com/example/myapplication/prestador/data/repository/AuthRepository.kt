package com.example.myapplication.prestador.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val application: android.app.Application
) {
    // Obtener usuario actual
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Login con email y contraseña
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login con google
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cerrar sesión (Firebase + limpiar credenciales Google para forzar re-selección de cuenta)
    fun signOut() {
        auth.signOut()
        com.google.android.gms.auth.api.signin.GoogleSignIn
            .getClient(
                application,
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                ).build()
            ).signOut()
    }

    // Enviar email de recuperacion de contraseña
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si existe perfil del prestador en firestore

    suspend fun checkUserProfileExists(uid: String): Boolean {
        return try {
            android.util.Log.d("PrestadorAuthRepo", "Verificando perfil para uid: $uid")

            val doc = firestore.collection("providers").document(uid).get().await()

            android.util.Log.d("PrestadorAuthRepo", "Documento existe: ${doc.exists()}")

            if (!doc.exists()) {
                android.util.Log.d("PrestadorAuthRepos", "El documento no existe en providers")
                return false
            }

            // El documento existe en la colección "providers" — es un prestador
            android.util.Log.d("PrestadorAuthRepo", "Perfil prestador encontrado en providers")
            true
        } catch (e: Exception) {
            android.util.Log.e("PrestadorAuthRepo", "Error verificando perfil: ${e.message}")
            false
        }
    }
}
