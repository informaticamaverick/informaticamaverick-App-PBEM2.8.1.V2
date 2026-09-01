package com.example.myapplication.prestador.viewmodel.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.datos.repositorios.PrestadorCalendarioRepositorio
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * --- VIEWMODEL DE CALENDARIO (ELITE v2026.FINAL) ---
 * Gestiona la visualización unificada de la agenda del prestador.
 */
@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val calendarioRepository: PrestadorCalendarioRepositorio
) : ViewModel() {

    private val idPrestador = auth.currentUser?.uid ?: ""

    /**
     * Flujo reactivo de todos los eventos del prestador (o sucursal activa).
     */
    val todosLosEventos: StateFlow<List<com.example.myapplication.core.dominio.modelos.EventoDominio>> = if (idPrestador.isBlank()) {
        MutableStateFlow(emptyList())
    } else {
        calendarioRepository.obtenerTodosLosEventos(idPrestador)
            .map { list -> list.map { com.example.myapplication.core.dominio.mapeadores.EventoMappers.aUiModel(it) } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}














































