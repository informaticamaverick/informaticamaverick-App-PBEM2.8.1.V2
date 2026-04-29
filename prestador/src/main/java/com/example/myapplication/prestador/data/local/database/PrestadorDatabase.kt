package com.example.myapplication.prestador.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.prestador.data.local.dao.ClienteDao
import com.example.myapplication.prestador.data.local.dao.PresupuestoDao
import com.example.myapplication.prestador.data.local.dao.ProviderDao
import com.example.myapplication.prestador.data.local.dao.PromotionDao
import com.example.myapplication.prestador.data.local.dao.EmpleadoDao
import com.example.myapplication.prestador.data.local.dao.AvailabilityScheduleDao
import com.example.myapplication.prestador.data.local.dao.RentalSpaceDao
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.local.entity.PromotionEntity
import com.example.myapplication.prestador.data.local.entity.EmpleadoEntity
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import com.example.myapplication.prestador.data.local.entity.RentalSpaceEntity
import com.example.myapplication.prestador.data.local.dao.BusinessDao
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.example.myapplication.prestador.data.local.dao.MessageDao
import com.example.myapplication.prestador.data.local.dao.ConversationDao
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.prestador.data.local.dao.PlantillaPresupuestoDao
import com.example.myapplication.prestador.data.local.entity.PlantillaPresupuestoEntity
import com.example.myapplication.prestador.data.local.dao.DireccionDao
import com.example.myapplication.prestador.data.local.dao.ReferenteDao
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.example.myapplication.prestador.data.local.entity.ReferenteEntity
import com.example.myapplication.prestador.data.local.dao.BookedAppointmentDao
import com.example.myapplication.prestador.data.local.entity.BookedAppointmentEntity
import com.example.myapplication.prestador.data.local.dao.NotificacionDao
import com.example.myapplication.prestador.data.local.entity.NotificacionEntity
import androidx.room.migration.Migration


import androidx.room.TypeConverters
import com.example.myapplication.prestador.data.local.Converters

/**
 * BASE DE DATOS DEL MÓDULO PRESTADOR
 */
@Database(
    entities = [
        PresupuestoEntity::class,
        ClienteEntity::class,
        ProviderEntity::class,
        PromotionEntity::class,
        // BusinessEntity::class,      <-- OBSOLETO: Ahora integrado en ProviderEntity
        // SucursalEntity::class,      <-- OBSOLETO: Ahora integrado en ProviderEntity
        MessageEntity::class,
        ConversationEntity::class,
        EmpleadoEntity::class,
        AvailabilityScheduleEntity::class,
        RentalSpaceEntity::class,
        PlantillaPresupuestoEntity::class,
        // DireccionEntity::class,     <-- OBSOLETO: Ahora integrado en ProviderEntity
        ReferenteEntity::class,
        NotificacionEntity::class,
        BookedAppointmentEntity::class,
   ],
    version = 46,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PrestadorDatabase : RoomDatabase() {
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun providerDao(): ProviderDao
    abstract fun promotionDao(): PromotionDao
    // abstract fun businessDao(): BusinessDao   <-- OBSOLETO
    // abstract fun sucursalDao(): SucursalDao   <-- OBSOLETO
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun empleadoDao(): EmpleadoDao
    abstract fun availabilityScheduleDao(): AvailabilityScheduleDao
    abstract fun rentalSpaceDao(): RentalSpaceDao

    abstract fun plantillaPresupuestoDao(): PlantillaPresupuestoDao

    // abstract fun direccionDao(): DireccionDao <-- OBSOLETO
    abstract fun referenteDao(): ReferenteDao
    abstract fun noticacionDao(): NotificacionDao
    abstract fun bookedAppointmentDao(): BookedAppointmentDao
}

