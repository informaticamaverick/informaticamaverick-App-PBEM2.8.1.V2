package com.example.myapplication.presentation.client

/**
 * --- ENUM DE CONTEXTO DEL HUD ---
 * Define en qué sección de la app se encuentra el usuario para adaptar Be.
 */
enum class HUDContext {
    HOME, BUDGETS, BUDGETS_TENDERS, BUDGETS_DIRECT, CHAT, CALENDAR, PROMO, TENDER_DETAILS, PROFILE, SEARCH_RESULTS, FAST, UNKNOWN
}

/**
 * --- ENUM PARA EL ESTADO DE NAVEGACIÓN INICIAL ---
 */
enum class InitialNavTarget {
    CHECKING, LOGIN, MAIN_SCREEN, PROFILE_EDIT
}
