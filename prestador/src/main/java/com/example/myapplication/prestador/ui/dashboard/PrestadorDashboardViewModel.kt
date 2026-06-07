package com.example.myapplication.prestador.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * --- PRESTADOR DASHBOARD VIEWMODEL ---
 * Maneja el estado global del dashboard y la sesión del usuario.
 */
@HiltViewModel
class PrestadorDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * Cierra la sesión del usuario de forma segura.
     * Sigue la Ley #2: Limpia estados en la nube antes de desconectar.
     */
    fun signOut() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                try {
                    // Sincronización proactiva del estado offline (Ley #5)
                    FirebaseDatabase.getInstance()
                        .reference.child("users").child(uid).child("online")
                        .setValue(false).await()
                } catch (e: Exception) { 
                    // Falla silenciosa si no hay internet al cerrar sesión
                }
            }
            authRepository.signOut()
        }
    }
}
