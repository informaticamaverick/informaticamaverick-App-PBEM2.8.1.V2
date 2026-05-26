package com.example.myapplication.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.core.data.local.dao.*
import com.example.myapplication.core.data.local.entity.*

/**
 * --- BASE DE DATOS COMPARTIDA (ROOM) ---
 * Contiene únicamente las entidades que se comparten entre Cliente y Prestador.
 */
@Database(
    entities = [
        UserEntity::class,
        ProviderEntity::class,
        CategoryEntity::class,
        TenderEntity::class,
        BudgetEntity::class,
        MessageEntity::class,
        CalendarEventEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun providerDao(): ProviderDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun chatDao(): ChatDao
    abstract fun calendarDao(): CalendarDao
}
