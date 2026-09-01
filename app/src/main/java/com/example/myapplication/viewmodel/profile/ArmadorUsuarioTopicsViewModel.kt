/*
package com.example.myapplication.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.datos.repositorios.SincUsuarioTopicksRepositorio
import com.example.myapplication.core.datos.local.dao.SuscripcionTopicDao
import com.example.myapplication.core.datos.local.entidades.SuscripcionTopicEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- ARMADOR DE TÓPICOS PARA EL USUARIO (US) ---
 * [PROPÓSITO]: Orquestar la suscripción a canales de noticias, zonas y concursos para el Cliente.
 * [LEY #9]: Estándar Maverick. Especialista en la Higiene de Red del Cliente.
 */
@HiltViewModel
class ArmadorUsuarioTopicsViewModel @Inject constructor(
    private val sincTopicsRepo: SincUsuarioTopicksRepositorio,
    private val suscripcionDao: SuscripcionTopicDao
) : ViewModel() {

    /**
     * Tópicos activos a los que el cliente está suscrito actualmente.
     */
    val misSuscripciones: StateFlow<List<SuscripcionTopicEntity>> = suscripcionDao.obtenerSuscripcionesActivas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 🔥 [ELITE]: Sincroniza las suscripciones de zona basadas en las direcciones del cliente.
     */
    fun sincronizarZonasDeInteres(codigosPostales: List<String>) {
        viewModelScope.launch {
            sincTopicsRepo.sincronizarTopicosInteres(codigosPostales)
        }
    }

    /**
     * 🔥 [ELITE]: Suscribe al cliente a concursos de rubros específicos en una zona.
     */
    fun suscribirRubrosEnZona(cp: String, rubros: List<String>) {
        viewModelScope.launch {
            sincTopicsRepo.suscribirAConcursos(cp, rubros)
        }
    }
}
*/
































