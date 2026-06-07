package com.example.myapplication.prestador.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.core.data.local.dao.ChatDao
import com.example.myapplication.core.data.local.dao.ProviderDao
import com.example.myapplication.core.data.local.dao.UserDao
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.prestador.data.local.Converters
import com.example.myapplication.prestador.data.local.dao.*
import com.example.myapplication.prestador.data.local.entity.*

/**
 * --- BASE DE DATOS DEL MÓDULO PRESTADOR (BOVEDA ÚNICA) ---
 * [ACTUALIZADO]: Se han consolidado las entidades núcleo (SSOT) y las locales del prestador.
 * Sigue la Ley #2: El almacenamiento local es la fuente de verdad inmediata.
 */
@Database(
    entities = [
        UserEntity::class,           // Núcleo Core (Identidad Humana)
        ProviderEntity::class,       // Núcleo Core (Perfil Profesional)
        MessageEntity::class,        // Núcleo Core (Comunicación)
        PresupuestoEntity::class,    // Local Prestador
        PromotionEntity::class,      // Local Prestador
        ConversationEntity::class,   // Local Prestador (Metadatos Chat)
        AvailabilityScheduleEntity::class, // Local Prestador
        RentalSpaceEntity::class,    // Local Prestador
        PlantillaPresupuestoEntity::class, // Local Prestador
        ReferenteEntity::class,      // Local Prestador
        NotificacionEntity::class,   // Local Prestador
        BookedAppointmentEntity::class, // Local Prestador
        BlockedDateEntity::class,    // Local Prestador
   ],
    version = 55, // Incrementada por consolidación de esquema
    exportSchema = true
)
@androidx.room.TypeConverters(Converters::class)
abstract class PrestadorDatabase : RoomDatabase() {
    
    // --- DAOs del Núcleo Core (Soberanía de Datos) ---
    abstract fun userDao(): UserDao
    abstract fun providerDao(): ProviderDao
    abstract fun chatDao(): ChatDao

    // --- DAOs Locales del Prestador ---
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun promotionDao(): PromotionDao
    abstract fun conversationDao(): ConversationDao
    abstract fun availabilityScheduleDao(): AvailabilityScheduleDao
    abstract fun rentalSpaceDao(): RentalSpaceDao
    abstract fun plantillaPresupuestoDao(): PlantillaPresupuestoDao
    abstract fun referenteDao(): ReferenteDao
    abstract fun noticacionDao(): NotificacionDao
    abstract fun bookedAppointmentDao(): BookedAppointmentDao
    abstract fun blockedDateDao(): BlockedDateDao
}
