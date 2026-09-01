package com.example.myapplication.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.ubicacionDataStore: DataStore<Preferences> by preferencesDataStore(name = "mav_ubicacion_cache")

/**
 * --- MÓDULO DE UBICACIÓN (ELITE 2026) ---
 * [PROPÓSITO]: Proveer la persistencia ligera para coordenadas GPS.
 */
@Module
@InstallIn(SingletonComponent::class)
object UbicacionModule {

    @Provides
    @Singleton
    fun provideUbicacionDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.ubicacionDataStore
    }
}
