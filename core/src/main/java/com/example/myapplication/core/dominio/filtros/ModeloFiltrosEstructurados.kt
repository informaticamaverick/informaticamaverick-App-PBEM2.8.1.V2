package com.example.myapplication.core.dominio.filtros

/**
 * --- MODELO DE FILTROS ESTRUCTURADOS (v2026.ELITE) ---
 * [PROPÓSITO]: Definir los contratos de filtrado que viajan desde la UI hasta el SQL.
 * [LEY #14]: Filtrado en la Fuente. El filtrado ocurre en SQL, no en RAM.
 */

interface FiltrosSoberanos {
    val consulta: String
    val orden: String
    fun estaActivo(): Boolean
}

/**
 * Filtros específicos para la bandeja de Chats.
 */
data class FiltrosChat(
    override val consulta: String = "",
    override val orden: String = "reciente",
    val soloNoLeidos: Boolean = false,
    val soloVerificados: Boolean = false,
    val idsCategorias: Set<String> = emptySet()
) : FiltrosSoberanos {
    override fun estaActivo() = consulta.isNotEmpty() || soloNoLeidos || soloVerificados || idsCategorias.isNotEmpty()
}

/**
 * Filtros para la Agenda de Calendario.
 */
data class FiltrosCalendario(
    override val consulta: String = "",
    override val orden: String = "fecha",
    val mostrarVisitas: Boolean = true,
    val mostrarTurnos: Boolean = true,
    val mostrarEnvios: Boolean = true,
    val idCategorias: Set<String> = emptySet()
) : FiltrosSoberanos {
    override fun estaActivo() = consulta.isNotEmpty() || !mostrarVisitas || !mostrarTurnos || !mostrarEnvios || idCategorias.isNotEmpty()
}

/**
 * Filtros para el Panel de Presupuestos y Concursos.
 */
data class FiltrosConcursoPublico(
    override val consulta: String = "",
    override val orden: String = "sort_date",
    val soloActivos: Boolean = false,
    val soloCerrados: Boolean = false,
    val soloAdjudicados: Boolean = false,
    val soloNoLeidos: Boolean = false,
    val idsCategorias: Set<String> = emptySet()
) : FiltrosSoberanos {
    override fun estaActivo() = consulta.isNotEmpty() || soloActivos || soloCerrados || soloAdjudicados || soloNoLeidos || idsCategorias.isNotEmpty()
}

/**
 * Filtros para el Feed de Promociones e Historias.
 */
data class FiltrosPromocion(
    override val consulta: String = "",
    override val orden: String = "populares",
    val soloDescuentos: Boolean = false,
    val soloEventos: Boolean = false,
    val idSuperCategoria: String? = null
) : FiltrosSoberanos {
    override fun estaActivo() = consulta.isNotEmpty() || soloDescuentos || soloEventos || idSuperCategoria != null
}
