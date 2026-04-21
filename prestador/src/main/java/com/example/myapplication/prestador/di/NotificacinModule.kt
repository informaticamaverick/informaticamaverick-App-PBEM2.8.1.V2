package com.example.myapplication.prestador.di

import android.content.Context
import com.example.myapplication.prestador.data.local.dao.NotificacionDao
import com.example.myapplication.prestador.data.repository.NotificacionRepository
import com.example.myapplication.prestador.data.repository.RoomNotificacionRepository
import com.example.myapplication.prestador.utils.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificacionModule {

    @Provides
    @Singleton
    fun provideNotificacionRepository(
        notificacionDao: NotificacionDao
    ): NotificacionRepository {
        return RoomNotificacionRepository(notificacionDao)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper {
        return NotificationHelper(context)
    }
}