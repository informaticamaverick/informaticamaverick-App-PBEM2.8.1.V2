package com.example.myapplication.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.core.data.local.dao.FastCategoryDao
import com.example.myapplication.data.local.dao.ShortcutDao
import com.example.myapplication.core.data.local.dao.FastCategoryEntity
import com.example.myapplication.data.local.entity.ShortcutEntity
import com.example.myapplication.core.data.local.Converters

/**
 * --- BASE DE DATOS LOCAL (APP CLIENTE) ---
 * Almacena datos que NO son compartidos con la App del Prestador, 
 * como los accesos directos personalizados y el historial de búsquedas rápidas.
 */
@Database(
    entities = [
        ShortcutEntity::class,
        FastCategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao
    abstract fun fastCategoryDao(): FastCategoryDao
}
