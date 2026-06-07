package com.example.myapplication.presentation.global

/**
 * --- ENUM DE CONTEXTO DEL HUD (ELITE VERSION) ---
 * Define el comportamiento global de la UI basado en el contexto activo.
 * @param isSearchable Permite activar el modo búsqueda de Be.
 * @param requiresBottomBar Define si la barra de navegación principal debe ser visible.
 * @param showBeAssistant Define si el asistente Be debe flotar en este contexto.
 * @param allowsCustomTools Permite que los Obreros inyecten herramientas propias.
 */
enum class HUDContext(
    val isSearchable: Boolean = true,
    val requiresBottomBar: Boolean = true,
    val showBeAssistant: Boolean = true,
    val allowsCustomTools: Boolean = true
) {
    HOME(isSearchable = true, requiresBottomBar = true, showBeAssistant = true, allowsCustomTools = true),
    BUDGETS(isSearchable = true, requiresBottomBar = true, showBeAssistant = true, allowsCustomTools = true),
    
    // Contexto para la CREACIÓN de licitaciones (Oculta todo para dar foco)
    BUDGETS_TENDERS(isSearchable = false, requiresBottomBar = false, showBeAssistant = false, allowsCustomTools = true),

    BUDGETS_DIRECT(isSearchable = true, requiresBottomBar = true, showBeAssistant = true, allowsCustomTools = true),
    
    // El contexto CHAT por defecto es la LISTA de chats (requiere barra y Be)
    CHAT(isSearchable = false, requiresBottomBar = true, showBeAssistant = true, allowsCustomTools = true),
    
    // Nuevo contexto para la CONVERSACIÓN activa (oculta para dar espacio)
    CHAT_CONVERSATION(isSearchable = false, requiresBottomBar = false, showBeAssistant = false, allowsCustomTools = true),
    
    CALENDAR(isSearchable = false, requiresBottomBar = true, showBeAssistant = true, allowsCustomTools = true),
    PROMO(isSearchable = false, requiresBottomBar = true, showBeAssistant = true, allowsCustomTools = false),
    TENDER_DETAILS(isSearchable = false, requiresBottomBar = true, showBeAssistant = true, allowsCustomTools = true),
    PROFILE(isSearchable = false, requiresBottomBar = false, showBeAssistant = false, allowsCustomTools = true),
    PROFILE_PRESTADOR(isSearchable = false, requiresBottomBar = false, showBeAssistant = false, allowsCustomTools = true),
    SEARCH_RESULTS(isSearchable = true, requiresBottomBar = false, showBeAssistant = true, allowsCustomTools = true),
    FAST(isSearchable = true, requiresBottomBar = false, showBeAssistant = true, allowsCustomTools = false),
    UNKNOWN(isSearchable = false, requiresBottomBar = false, showBeAssistant = false, allowsCustomTools = false)
}

/**
 * --- ENUM PARA EL ESTADO DE NAVEGACIÓN INICIAL ---
 */
enum class InitialNavTarget {
    CHECKING, LOGIN, MAIN_SCREEN, PROFILE_EDIT
}









