package com.example.myapplication.core.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.core.data.local.AppDatabase
import com.example.myapplication.core.data.local.dao.*
import com.example.myapplication.core.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * --- MÓDULO HILT: CORE (PROVEEDOR MAESTRO) ---
 * Provee las dependencias de Base de Datos, DAOs y Repositorios compartidos.
 * Al estar aquí, cualquier app que use :core tendrá acceso automático a estos servicios.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    // --- 1. PERSISTENCIA COMPARTIDA (ROOM) ---

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "maverick_shared.db"
        ).fallbackToDestructiveMigration().build()
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
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    @Singleton
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    @Singleton
    fun provideCalendarDao(db: AppDatabase): CalendarDao = db.calendarDao()


    // --- 2. REPOSITORIOS COMPARTIDOS ---

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context,
        userDao: UserDao,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository = UserRepository(context, userDao, auth, firestore)

    @Provides
    @Singleton
    fun provideChatRepository(
        chatDao: ChatDao,
        firestore: FirebaseFirestore,
        database: FirebaseDatabase,
        auth: FirebaseAuth,
        budgetRepository: BudgetRepository,
        appointmentRepository: AppointmentRepository,
        @ApplicationContext context: Context
    ): ChatRepository = ChatRepository(chatDao, firestore, database, auth, budgetRepository, appointmentRepository, context)

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetDao: BudgetDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): BudgetRepository = BudgetRepository(budgetDao, firestore, storage)

    @Provides
    @Singleton
    fun provideProviderRepository(
        providerDao: ProviderDao,
        firestore: FirebaseFirestore
    ): ProviderRepository = ProviderRepository(providerDao, firestore)

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        firestore: FirebaseFirestore
    ): CategoryRepository = CategoryRepository(categoryDao, firestore)

    @Provides
    @Singleton
    fun provideAppointmentRepository(
        firestore: FirebaseFirestore,
        calendarDao: CalendarDao
    ): AppointmentRepository = AppointmentRepository(firestore, calendarDao)

    @Provides
    @Singleton
    fun provideCalendarRepository(
        calendarDao: CalendarDao,
        categoryDao: CategoryDao,
        firestore: FirebaseFirestore
    ): CalendarRepository = CalendarRepository(calendarDao, categoryDao, firestore)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth
    ): AuthRepository = AuthRepository(auth)
}
