package com.example.myapplication.di

import javax.inject.Qualifier

/**
 * --- QUALIFIER: APPLICATION SCOPE (ELITE 2026) ---
 * Identifica un CoroutineScope que vive durante todo el ciclo de vida de la aplicación.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
