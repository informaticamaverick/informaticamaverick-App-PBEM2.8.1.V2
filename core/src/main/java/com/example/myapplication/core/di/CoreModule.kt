package com.example.myapplication.core.di

import android.content.Context
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.remoto.api.WeatherApiService
import com.example.myapplication.core.datos.remoto.api.GeorefApiService
import com.example.myapplication.core.datos.repositorios.*
import com.example.myapplication.core.dominio.repository.TopicoRepositorio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideClimaRepositorio(
        apiService: WeatherApiService
    ): ClimaRepositorio = ClimaRepositorio(apiService)

    @Provides
    @Singleton
    fun provideTopicoRepositorio(impl: FirebaseTopicRepositorio): TopicoRepositorio = impl

    @Provides
    @Singleton
    fun provideGeorefRepository(api: GeorefApiService): GeorefRepositorio = 
        GeorefRepositorio(api)

    @Provides
    @Singleton
    fun provideNotificacionRepository(dao: NotificacionDao): NotificacionRepositorio =
        NotificacionRepositorio(dao)
}


































