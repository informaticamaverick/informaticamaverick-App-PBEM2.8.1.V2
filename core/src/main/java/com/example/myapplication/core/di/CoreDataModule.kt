package com.example.myapplication.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.local.semillas.CategoriaSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

/**
 * --- MÓDULO DE DATOS CORE (SSOT 2026) ---
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreDataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        seederProvider: Provider<CategoriaSeeder>
    ): AppDatabase {
        val dbName = if (context.packageName.contains("prestador")) {
            "prestador.db"
        } else {
            "app.db"
        }

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            dbName
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        seederProvider.get().seedIfNeeded()
                    } catch (e: Exception) {
                        android.util.Log.e("AppDatabase", "❌ [ELITE_SEED_ERROR] Error en el sembrado automático: ${e.message}")
                    }
                }
            }
        })
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideAppMetadataDao(db: AppDatabase): AppMetadataDao = db.metadataDao()

    @Provides
    @Singleton
    fun provideClaveRemotaBusquedaDao(db: AppDatabase): ClaveRemotaBusquedaDao = db.claveRemotaBusquedaDao()

    @Provides
    @Singleton
    fun provideCuentaDao(db: AppDatabase): CuentaDao = db.CuentaDao()

    @Provides
    @Singleton
    fun providePrestadorDao(db: AppDatabase): IdentidadPrestadorDao = db.prestadorDao()

    @Provides
    @Singleton
    fun provideUsuarioDao(db: AppDatabase): IdentidadUsuarioDao = db.usuarioDao()

    @Provides
    @Singleton
    fun provideEmpresaDao(db: AppDatabase): EmpresaDao = db.empresaDao()

    @Provides
    @Singleton
    fun provideSucursalDao(db: AppDatabase): SucursalDao = db.sucursalDao()

    @Provides
    @Singleton
    fun provideChatDao(db: AppDatabase): ChatDao = db.ChatDao()

    @Provides
    @Singleton
    fun provideEventoDao(db: AppDatabase): EventoDao = db.eventoDao()

    @Provides
    @Singleton
    fun provideRecursoDao(db: AppDatabase): RecursoDao = db.recursoDao()

    @Provides
    @Singleton
    fun provideEquipoTrabajoDao(db: AppDatabase): EquipoTrabajoDao = db.equipoTrabajoDao()

    @Provides
    @Singleton
    fun provideSuperCategoriaDao(db: AppDatabase): SuperCategoriaDao = db.superCategoriaDao()

    @Provides
    @Singleton
    fun providePresupuestoFinalDao(db: AppDatabase): PresupuestoFinalDao = db.presupuestoFinalDao()

    @Provides
    @Singleton
    fun providePromocionDao(db: AppDatabase): PromocionDao = db.promotionDao()

    @Provides
    @Singleton
    fun provideTelemetryDao(db: AppDatabase): TelemetryDao = db.telemetryDao()

    @Provides
    @Singleton
    fun provideReviewDao(db: AppDatabase): ReviewDao = db.reviewDao()

    @Provides
    @Singleton
    fun provideHorarioDao(db: AppDatabase): HorarioDao = db.horarioDao()

    @Provides
    @Singleton
    fun provideExcepcionHorariaDao(db: AppDatabase): ExcepcionHorariaDao = db.excepcionHorariaDao()

    @Provides
    @Singleton
    fun provideInventarioDao(db: AppDatabase): InventarioDao = db.inventarioDao()

    @Provides
    @Singleton
    fun provideDireccionDao(db: AppDatabase): DireccionDao = db.direccionDao()

    @Provides
    @Singleton
    fun provideConcursoPublicoDao(db: AppDatabase): ConcursoPublicoDao = db.concursoPublicoDao()

    @Provides
    @Singleton
    fun provideShortcutDao(db: AppDatabase): ShortcutDao = db.shortcutDao()

    @Provides
    @Singleton
    fun provideCategoriaRapidaDao(db: AppDatabase): CategoriaRapidaDao = db.categoriaRapidaDao()

    @Provides
    @Singleton
    fun provideCategoriaDao(db: AppDatabase): CategoriaDao = db.categoriaDao()

    @Provides
    @Singleton
    fun provideNotificacionDao(db: AppDatabase): NotificacionDao = db.notificacionDao()

    @Provides
    @Singleton
    fun provideSuscripcionTopicDao(db: AppDatabase): SuscripcionTopicDao = db.suscripcionTopicDao()
}

