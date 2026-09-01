package com.example.myapplication.core.di

import com.example.myapplication.core.datos.remoto.api.GeorefApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * --- MÓDULO HILT: GEOREF (SSOT 2026) ---
 * [ELITE]: Centraliza la provisión del servicio oficial de datos geográficos.
 */
@Module
@InstallIn(SingletonComponent::class)
object GeorefModule {

    @Provides
    @Singleton
    fun provideGeorefApiService(): GeorefApiService {
        return Retrofit.Builder()
            .baseUrl("https://apis.datos.gob.ar/georef/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeorefApiService::class.java)
    }
}

































