package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.core.datos.repositorios.PresupuestoRepositorio
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- ALERTAS SOBERANAS REPOSITORIO (Atómico - v2026.ELITE) ---
 * [PROPÓSITO]: Unificar el conteo de actividad del sistema para optimizar RAM y batería.
 * [LEY #2]: Costo Zero. Evita duplicar consultas a la base de datos local.
 * [RELACIÓN]: Provee datos sincronizados a la Barra de Navegación y al Portavoz de Be.
 */
@Singleton
class AlertasSoberanasRepositorio @Inject constructor(
    private val chatRepo: ChatMotorSincRepositorio,
    private val presupuestoRepositorio: PresupuestoRepositorio,
    private val eventoRepo: EventoRepositorio
) {

    data class AlertasGlobales(
        val tieneChat: Boolean = false,
        val tienePresupuesto: Boolean = false,
        val tieneCalendario: Boolean = false,
        val totalNoLeidosChat: Int = 0
    )

    /**
     * Observa el ecosistema completo de alertas para una identidad específica.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarAlertasParaIdentidad(uid: String): Flow<AlertasGlobales> {
        return combine(
            chatRepo.obtenerConteoNoLeidosGlobal(uid),
            presupuestoRepositorio.obtenerPresupuestosPorIdentidad(uid),
            flowOf(false) // Placeholder para calendario
        ) { noLeidos: Int, presupuestos: List<com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity>, calendario: Boolean ->
            AlertasGlobales(
                tieneChat = noLeidos > 0,
                tienePresupuesto = presupuestos.any { it.estado == EstadoPresupuesto.PENDIENTE },
                tieneCalendario = calendario,
                totalNoLeidosChat = noLeidos
            )
        }.distinctUntilChanged()
    }
}




