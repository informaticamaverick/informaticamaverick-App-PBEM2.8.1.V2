package com.example.myapplication.ui.componentes.be.modelos

/**
 * --- PROTOCOLO DE AUTORIDAD VISUAL (Z-INDEX) v2026.ELITE ---
 * [PROPÓSITO]: Centralizar las alturas de las capas de la aplicación para evitar colisiones.
 * [JERARQUÍA]: Basada en familias de 1000 para permitir capas intermedias.
 */
object BeZIndex {
    /** Capa base: Listas, fondos, mapas y contenido de pantalla. */
    const val MUNDO = 0f

    /** Capa de navegación: Barra inferior (BottomBar). */
    const val NAVEGACION = 1000f

    /** Capa emergente: Sheets (V3) y diálogos estándar. */
    const val SHEETS = 2000f

    /** Capa de soberanía local: Paneles de detalles que tapan la navegación. */
    const val PANELES = 3000f

    /** Capa de asistente base: El cuerpo de Be (FAB) y herramientas. */
    const val ASISTENTE_FAB = 4000f

    /** Capa de feedback: Burbujas de texto (Consejos) y Toasts (Logs). */
    const val ASISTENTE_BURBUJAS = 5000f

    /** Capa de autoridad máxima: Barra de búsqueda táctica (Escáner). */
    const val ASISTENTE_ESCÁNER = 8000f
}
