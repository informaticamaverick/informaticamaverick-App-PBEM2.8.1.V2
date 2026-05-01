package com.example.myapplication.core.di

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.BudgetDao
import com.example.myapplication.data.local.CalendarDao
import com.example.myapplication.data.local.CategoryDao
import com.example.myapplication.data.local.ChatDao
import com.example.myapplication.data.local.FastCategoryDao
import com.example.myapplication.data.local.ProviderDao
import com.example.myapplication.data.local.UserDao
import com.example.myapplication.data.repository.CategoryRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.FastRepository
import com.example.myapplication.data.repository.ProviderRepository
import com.example.myapplication.presentation.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
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
 * ==========================================================
 * SECCIÓN 1: MÓDULO DE INYECCIÓN DE DEPENDENCIAS (AppModule)
 * ==========================================================
 * Este archivo gestiona la creación y provisión de dependencias
 * de la base de datos local (Room) y los repositorios de la app.
 * 
 * Estrategia: Zero Cost (Sin Firebase Storage, uso de RTDB para Base64).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ----------------------------------------------------------
    // SECCIÓN 2: PROVEEDORES DE BASE DE DATOS Y DAOs
    // ----------------------------------------------------------

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return AppDatabase.getDatabase(context, dbScope)
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideProviderDao(db: AppDatabase): ProviderDao = db.providerDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    @Singleton
    fun provideCalendarDao(database: AppDatabase): CalendarDao {
        return database.calendarDao()
    }

    @Provides
    @Singleton
    fun provideFastCategoryDao(db: AppDatabase): FastCategoryDao = db.fastCategoryDao()

    // ----------------------------------------------------------
    // SECCIÓN 3: PROVEEDORES DE REPOSITORIOS Y UTILIDADES
    // ----------------------------------------------------------

    @Provides
    @Singleton
    fun provideFastRepository(
        dao: ProviderDao,
        fastCategoryDao: FastCategoryDao,
        firestore: FirebaseFirestore
    ): FastRepository {
        return FastRepository(dao, fastCategoryDao, firestore)
    }

    @Provides
    @Singleton
    fun provideProviderRepository(dao: ProviderDao, firestore: FirebaseFirestore): ProviderRepository {
        return ProviderRepository(dao, firestore)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        dao: CategoryDao,
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): CategoryRepository {
        return CategoryRepository(dao, firestore, context)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    /**
     * Provee la instancia de ChatRepository.
     * [ACTUALIZADO] Se eliminó FirebaseStorage para cumplir con la estrategia "Zero Cost".
     * Se sincronizó con los parámetros requeridos por el constructor del repositorio.
     */
    @Provides
    @Singleton
    fun provideChatRepository(
        chatDao: ChatDao,
        budgetDao: BudgetDao,
        calendarDao: CalendarDao,
        firestore: FirebaseFirestore,
        database: FirebaseDatabase,
        auth: FirebaseAuth,
        @ApplicationContext context: Context,
        notificationHelper: NotificationHelper
    ): ChatRepository {
        return ChatRepository(
            chatDao = chatDao,
            budgetDao = budgetDao,
            calendarDao = calendarDao,
            firestore = firestore,
            database = database,
            auth = auth,
            context = context,
            notificationHelper = notificationHelper
        )
    }
}
