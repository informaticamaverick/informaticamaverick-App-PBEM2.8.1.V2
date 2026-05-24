package com.example.myapplication.prestador.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.prestador.data.local.dao.*
import com.example.myapplication.prestador.data.local.database.DatabaseMigrations
import com.example.myapplication.prestador.data.local.database.PrestadorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.myapplication.prestador.data.local.dao.NotificacionDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providePrestadorDatabase(
        @ApplicationContext context: Context
    ): PrestadorDatabase {
        return Room.databaseBuilder(
            context,
            PrestadorDatabase::class.java,
            "prestador_database_V21"
        )
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProviderDao(database: PrestadorDatabase): ProviderDao {
        return database.providerDao()
    }

    @Provides
    @Singleton
    fun providePromotionDao(database: PrestadorDatabase): PromotionDao {
        return database.promotionDao()
    }

    @Provides
    @Singleton
    fun providePresupuestoDao(database: PrestadorDatabase): PresupuestoDao {
        return database.presupuestoDao()
    }

    @Provides
    @Singleton
    fun provideClienteDao(database: PrestadorDatabase): ClienteDao {
        return database.clienteDao()
    }


    @Provides
    @Singleton
    fun provideMessageDao(database: PrestadorDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideConversationDao(database: PrestadorDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    @Singleton
    fun provideEmpleadoDao(database: PrestadorDatabase): EmpleadoDao {
        return database.empleadoDao()
    }

    @Provides
    @Singleton
    fun provideAvailabilityScheduleDao(database: PrestadorDatabase): AvailabilityScheduleDao {
        return database.availabilityScheduleDao()
    }

    @Provides
    @Singleton
    fun provideRentalSpaceDao(database: PrestadorDatabase): RentalSpaceDao {
        return database.rentalSpaceDao()
    }

    @Provides
    @Singleton
    fun providePlantillaPresupuestoDao(database: PrestadorDatabase): PlantillaPresupuestoDao {
        return database.plantillaPresupuestoDao()
    }



    @Provides
    @Singleton
    fun provideReferenteDao(database: PrestadorDatabase): ReferenteDao {
        return database.referenteDao()
    }

    @Provides
    @Singleton
    fun provideNotificacionDao(database:
    PrestadorDatabase): NotificacionDao {
        return database.noticacionDao()
    }

    @Provides
    @Singleton
    fun provideBookedAppointmentDao(database: PrestadorDatabase): BookedAppointmentDao {
        return database.bookedAppointmentDao()
    }

    @Provides
    @Singleton
    fun provideBlockedDateDao(database: PrestadorDatabase): BlockedDateDao {
        return database.blockedDateDao()
    }

}
