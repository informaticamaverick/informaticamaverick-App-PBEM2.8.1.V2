package com.example.myapplication.prestador.datos.repositorios

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE AUTENTICACIÓN PRESTADOR (PRE) ---
 * [LEY #9]: Estándar Mav en Español.
 * [SOBERANÍA]: Punto de entrada exclusivo para la App del Prestador.
 */
@Singleton
class PrestadorAutenticacionRepositorio @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun observarUsuarioActual(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener {
            trySend(it.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun obtenerUsuarioActual(): FirebaseUser? = auth.currentUser

    suspend fun registrarseConEmailClave(email: String, clave: String): Result<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, clave).await()
        result.user?.let { Result.success(it) } ?: Result.failure(Exception("Usuario nulo tras registro"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun iniciarSesionConEmailClave(email: String, clave: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, clave).await()
        result.user?.let { Result.success(it) } ?: Result.failure(Exception("Usuario nulo tras login"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun iniciarSesionConGoogle(tokenIdentidad: String): Result<FirebaseUser> = try {
        val credencial = GoogleAuthProvider.getCredential(tokenIdentidad, null)
        val result = auth.signInWithCredential(credencial).await()
        result.user?.let { Result.success(it) } ?: Result.failure(Exception("Usuario nulo tras Google Auth"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun enviarCorreoRecuperacionClave(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun cerrarSesion() = auth.signOut()
}

