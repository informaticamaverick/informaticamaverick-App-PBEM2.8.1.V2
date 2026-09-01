package com.example.myapplication.di

import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.dao.ResultadoBusquedaPrestadorDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * --- MÓDULO DE BASE DE DATOS (APP CLIENTE) ---
 * [ELITE v2026]: Provee los DAOs exclusivos usando la base de datos unificada del Core.
 * [LEY #9]: Estándar.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideResultadoBusquedaPrestadorDao(db: AppDatabase): ResultadoBusquedaPrestadorDao {
        return db.resultadoBusquedaPrestadorDao()
    }
}
