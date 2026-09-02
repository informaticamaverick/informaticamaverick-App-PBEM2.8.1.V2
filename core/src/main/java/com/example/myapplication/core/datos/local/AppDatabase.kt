package com.example.myapplication.core.datos.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.vistas.*

/**
 * --- BASE DE DATOS MAESTRA UNIFICADA (ROOM - v2026.ELITE) ---
 * [LEY #9]: Organización Atómica por Sectores Funcionales.
 */
@Database(
    entities = [
        // --- SECTOR A: IDENTIDAD (Núcleo) ---
        CuentaEntity::class,
        IdentidadUsuarioEntity::class,
        IdentidadPrestadorEntity::class,
        PrestadorFtsEntity::class,
        EmpresaEntity::class,
        SucursalEntity::class,
        SucursalFtsEntity::class,
        DireccionEntity::class,

        // --- SECTOR B: OPERATIVO (Motor de Negocio) ---
        RecursoEntity::class,
        RecursoFtsEntity::class,
        EquipoTrabajoEntity::class,
        EquipoTrabajoFtsEntity::class,
        HorarioEntity::class,
        ExcepcionHorariaEntity::class,

        // --- SECTOR C: COMERCIAL (Transacciones) ---
        ConcursoPublicoEntity::class,
        ConcursoPublicoFtsEntity::class,
        PresupuestoFinalEntity::class,
        ProductoFinalEntity::class,
        FinanzaFinalEntity::class,

        // --- SECTOR D: SOCIAL Y COMUNICACIÓN (Engagement) ---
        MensajeEntity::class,
        ConversacionEntity::class,
        ConversacionFtsEntity::class,
        EventoEntity::class,
        NotificacionEntity::class,
        PromocionEntity::class,
        PromocionComentarioEntity::class,
        PromocionLikeEntity::class,
        ReviewEntity::class,
        ShortcutEntity::class,

        // --- SECTOR E: INFRAESTRUCTURA (Soporte Técnico) ---
        CategoriaEntity::class,
        CategoriaFtsEntity::class,
        SuperCategoriaEntity::class,
        CategoriaRapidaEntity::class,
        TelemetryEntity::class,
        AppMetadataEntity::class,
        ClaveRemotaBusquedaEntity::class,
        SuscripcionTopicEntity::class,

        // --- SECTOR F: BÚSQUEDA SOBERANA (Exclusivo App Azul - Migrado a Core para estabilidad) ---
        RelacionBusquedaEntity::class
    ],
    views = [
        ConcursoPublicoResumenSQLView::class,
        CategoriaResumenSQLView::class,
        ConversacionResumenSQLView::class,
        PresupuestoResumenSQLView::class,
        ResultadoBusquedaPrestadorSQLView::class,
        InventarioSucursalSQLView::class
    ],
    version = 85, // 🔥 [ELITE v2026]: Agregado matriculaFotoUrl a IdentidadPrestadorEntity
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // --- DAOs SECTOR IDENTIDAD ---
    abstract fun CuentaDao(): CuentaDao
    abstract fun usuarioDao(): IdentidadUsuarioDao
    abstract fun prestadorDao(): IdentidadPrestadorDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun sucursalDao(): SucursalDao
    abstract fun direccionDao(): DireccionDao

    // --- DAOs SECTOR OPERATIVO ---
    abstract fun recursoDao(): RecursoDao
    abstract fun equipoTrabajoDao(): EquipoTrabajoDao
    abstract fun horarioDao(): HorarioDao
    abstract fun excepcionHorariaDao(): ExcepcionHorariaDao
    abstract fun inventarioDao(): InventarioDao

    // --- DAOs SECTOR COMERCIAL ---
    abstract fun presupuestoFinalDao(): PresupuestoFinalDao
    abstract fun concursoPublicoDao(): ConcursoPublicoDao

    // --- DAOs SECTOR SOCIAL ---
    abstract fun ChatDao(): ChatDao
    abstract fun eventoDao(): EventoDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun promotionDao(): PromocionDao
    abstract fun reviewDao(): ReviewDao
    abstract fun shortcutDao(): ShortcutDao

    // --- DAOs SECTOR INFRAESTRUCTURA ---
    abstract fun metadataDao(): AppMetadataDao
    abstract fun claveRemotaBusquedaDao(): ClaveRemotaBusquedaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun categoriaRapidaDao(): CategoriaRapidaDao
    abstract fun superCategoriaDao(): SuperCategoriaDao
    abstract fun telemetryDao(): TelemetryDao
    abstract fun suscripcionTopicDao(): SuscripcionTopicDao

    // --- DAOs SECTOR BÚSQUEDA (Exclusivo App Azul) ---
    abstract fun resultadoBusquedaPrestadorDao(): ResultadoBusquedaPrestadorDao
}

