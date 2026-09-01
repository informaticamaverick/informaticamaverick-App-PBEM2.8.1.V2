package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.datos.local.TokenManager
import com.example.myapplication.datos.repositorios.UsuarioConfiguracionRepositorio
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * --- MÓDULO DE INYECCIÓN DE DEPENDENCIAS (APP CLIENTE) ---
 * Provee únicamente las dependencias exclusivas de la App del Cliente.
 * [ELITE v2026]: Toda la persistencia ha sido migrada al Core.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // --- 1. UTILIDADES Y CONFIGURACIÓN ---

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideUserSettingsRepository(@ApplicationContext context: Context): UsuarioConfiguracionRepositorio {
        return UsuarioConfiguracionRepositorio(context)
    }
}

