package com.example.myapplication.ui.componentes.sistema.contexto

import com.example.myapplication.core.dominio.filtros.*
import com.example.myapplication.ui.componentes.be.modelos.BeDictionary

/**
 * --- ARMADOR DE FILTROS (v2026.ELITE) ---
 * [PROPÓSITO]: Traducir data classes de filtros estructurados a burbujas visuales.
 * [LEY #12]: Be es solo un portavoz. El armador le da la voz visual.
 */
object ArmadorFiltrosV3 {

    /**
     * Convierte los filtros de Chat en una lista de burbujas para la UI.
     */
    fun armarBurbujasChat(filtros: FiltrosChat): List<ModeloBurbujaFiltro> {
        val burbujas = mutableListOf<ModeloBurbujaFiltro>()

        if (filtros.soloNoLeidos) {
            burbujas.add(crearBurbujaDesdeDictionary("filter_unread", "No leídos"))
        }

        if (filtros.soloVerificados) {
            burbujas.add(crearBurbujaDesdeDictionary("filter_verified", "Verificados"))
        }

        filtros.idsCategorias.forEach { id ->
            burbujas.add(ModeloBurbujaFiltro(
                id = "cat_$id",
                etiqueta = id.uppercase(),
                emoji = "📋"
            ))
        }

        return burbujas
    }

    /**
     * Convierte los filtros de Concurso Público en burbujas.
     */
    fun armarBurbujasConcursoPublico(
        filtros: FiltrosConcursoPublico,
        todasLasCategorias: List<com.example.myapplication.core.datos.local.entidades.CategoriaEntity> = emptyList()
    ): List<ModeloBurbujaFiltro> {
        val burbujas = mutableListOf<ModeloBurbujaFiltro>()

        if (filtros.soloActivos) {
            burbujas.add(crearBurbujaDesdeDictionary("filter_concurso_activo", "Abiertos"))
        }

        if (filtros.soloCerrados) {
            burbujas.add(crearBurbujaDesdeDictionary("filter_concurso_cerrado", "Cerrados"))
        }

        if (filtros.soloAdjudicados) {
            burbujas.add(crearBurbujaDesdeDictionary("filter_concurso_adjudicado", "Adjudicados"))
        }

        if (filtros.soloNoLeidos) {
            burbujas.add(crearBurbujaDesdeDictionary("filter_concurso_no_leidos", "No leídos"))
        }

        val mapaCat = todasLasCategorias.associateBy { it.id }
        filtros.idsCategorias.forEach { catId ->
            val cat = mapaCat[catId]
            burbujas.add(
                ModeloBurbujaFiltro(
                    id = "cat_$catId",
                    etiqueta = cat?.nombre ?: catId,
                    emoji = cat?.icono ?: "📋",
                    color = com.example.myapplication.uishared.estilos.SharedPalette.ElectricCyan
                )
            )
        }

        return burbujas
    }

    private fun crearBurbujaDesdeDictionary(id: String, fallbackLabel: String): ModeloBurbujaFiltro {
        val visuales = BeDictionary.Filters[id]
        return ModeloBurbujaFiltro(
            id = id,
            etiqueta = visuales?.label ?: fallbackLabel,
            emoji = visuales?.emoji ?: "🔹",
            color = visuales?.color ?: com.example.myapplication.uishared.estilos.SharedPalette.ElectricCyan
        )
    }
}
