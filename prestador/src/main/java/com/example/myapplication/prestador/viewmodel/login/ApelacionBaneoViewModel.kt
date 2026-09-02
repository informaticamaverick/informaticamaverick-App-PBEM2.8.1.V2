package com.example.myapplication.prestador.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.SoporteRepositorio
import com.example.myapplication.core.datos.repositorios.TicketSoporte
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- APELACIÓN DE BANEO (v2026) ---
 * Le permite al prestador, desde el propio diálogo de "Cuenta Suspendida",
 * mandar una apelación al mismo buzón de soporte que ya usa el resto de la
 * app — y ver el estado/respuesta si ya mandó una antes, sin salir del diálogo.
 * Requiere que la sesión de Firebase siga activa (no se cierra hasta que el
 * usuario cierra el diálogo), porque las reglas de Firestore exigen estar
 * autenticado para leer/crear en `soporte`.
 */
@HiltViewModel
class ApelacionBaneoViewModel @Inject constructor(
    private val soporteRepositorio: SoporteRepositorio
) : ViewModel() {

    companion object {
        const val CATEGORIA = "Cuenta suspendida"
        const val ASUNTO = "Apelación de cuenta suspendida"
    }

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val ticketApelacion: StateFlow<TicketSoporte?> =
        (FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            soporteRepositorio.observarMisTickets(uid)
        } ?: flowOf(emptyList()))
            .map { tickets -> tickets.firstOrNull { it.categoria == CATEGORIA } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun enviarApelacion(motivoBaneo: String?, comentario: String) {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            _error.value = "Tu sesión no es válida, volvé a intentar iniciar sesión"
            return
        }

        viewModelScope.launch {
            _enviando.value = true
            _error.value = null

            val mensaje = buildString {
                if (!motivoBaneo.isNullOrBlank()) {
                    append("Motivo del baneo: ")
                    append(motivoBaneo)
                    append(". ")
                }
                append(comentario.ifBlank { "Quiero apelar la suspensión de mi cuenta." })
            }

            soporteRepositorio.crearTicket(
                uid = usuario.uid,
                nombre = usuario.displayName ?: "Prestador",
                email = usuario.email ?: "",
                rol = "prestador",
                categoria = CATEGORIA,
                asunto = ASUNTO,
                mensaje = mensaje
            ).onSuccess {
                _enviando.value = false
            }.onFailure {
                _enviando.value = false
                _error.value = "No se pudo enviar la apelación, probá de nuevo"
            }
        }
    }

    /** El caller debe verificar antes que el ticket no esté "resuelto" (caso cerrado). */
    fun responder(ticketId: String, texto: String) {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            _error.value = "Tu sesión no es válida, volvé a intentar iniciar sesión"
            return
        }
        viewModelScope.launch {
            _enviando.value = true
            _error.value = null
            soporteRepositorio.agregarMensaje(ticketId, texto, usuario.email)
                .onSuccess { _enviando.value = false }
                .onFailure { _enviando.value = false; _error.value = "No se pudo enviar, probá de nuevo" }
        }
    }
}
