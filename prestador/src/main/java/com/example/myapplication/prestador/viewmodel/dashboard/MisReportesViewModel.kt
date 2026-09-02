package com.example.myapplication.prestador.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.SoporteRepositorio
import com.example.myapplication.core.datos.repositorios.TicketSoporte
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * --- MIS REPORTES (v2026) ---
 * Le muestra al prestador sus propios tickets de soporte, con la respuesta
 * y el estado que va actualizando el admin en vivo (mismo `soporte` que
 * escribe [[com.example.myapplication.prestador.viewmodel.dashboard.ReportarProblemaViewModel]]).
 */
@HiltViewModel
class MisReportesViewModel @Inject constructor(
    private val soporteRepositorio: SoporteRepositorio
) : ViewModel() {

    val tickets: StateFlow<List<TicketSoporte>> =
        (FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            soporteRepositorio.observarMisTickets(uid)
        } ?: flowOf(emptyList()))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** El caller debe verificar antes que el ticket no esté "resuelto" (caso cerrado). */
    suspend fun responder(ticketId: String, texto: String): Result<Unit> {
        val usuario = FirebaseAuth.getInstance().currentUser
            ?: return Result.failure(Exception("Sin sesión"))
        return soporteRepositorio.agregarMensaje(ticketId, texto, usuario.email)
    }
}
