package com.example.myapplication.uishared.ui.components.chat

import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio

/**
 * --- ITEM DE PAGINACIÓN DE CHAT (V2026.7) ---
 */
sealed class ItemPaginacionChat {
    data class Mensaje(
        val entidad: MensajeEntity,
        val presupuesto: PresupuestoResumenDominio? = null
    ) : ItemPaginacionChat()
    
    data class SeparadorFecha(val fecha: String) : ItemPaginacionChat()
}
