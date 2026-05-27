package com.example.myapplication.core.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.core.data.local.dao.ProviderDao
import com.example.myapplication.core.database.LocalDatabase
import com.example.myapplication.core.database.TokenManager
import com.example.myapplication.core.data.local.dao.FastCategoryDao
import com.example.myapplication.data.local.dao.ShortcutDao
import com.example.myapplication.data.repository.FastRepository
import com.example.myapplication.data.repository.ShortcutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * --- MÓDULO DE INYECCIÓN DE DEPENDENCIAS (APP CLIENTE) ---
 * Provee únicamente las dependencias exclusivas de la App del Cliente.
 * Las dependencias compartidas se proveen desde :core.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- 1. PERSISTENCIA LOCAL (NO COMPARTIDA) ---

    @Provides
    @Singleton
    fun provideLocalDatabase(@ApplicationContext context: Context): LocalDatabase {
        return Room.databaseBuilder(
            context,
            LocalDatabase::class.java,
            "maverick_client_local.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideShortcutDao(db: LocalDatabase): ShortcutDao = db.shortcutDao()

    @Provides
    @Singleton
    fun provideFastCategoryDao(db: LocalDatabase): FastCategoryDao = db.fastCategoryDao()


    // --- 2. REPOSITORIOS ESPECÍFICOS ---

    @Provides
    @Singleton
    fun provideShortcutRepository(
        shortcutDao: ShortcutDao
    ): ShortcutRepository = ShortcutRepository(shortcutDao)

    @Provides
    @Singleton
    fun provideFastRepository(
        providerDao: ProviderDao,
        fastCategoryDao: FastCategoryDao
    ): FastRepository = FastRepository(providerDao, fastCategoryDao)


    // --- 3. UTILIDADES ---

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }
}
