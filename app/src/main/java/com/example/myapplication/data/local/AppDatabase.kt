package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.model.fake.PrestadorSampleDataFalso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * --- BASE DE DATOS MAESTRA (AppDatabase) ---
 * Centraliza la persistencia de Usuarios, Prestadores, Categorías,
 * Licitaciones, Presupuestos y Mensajes de Chat.
 */
@Database(
    entities = [
        UserEntity::class,      // Tabla de Usuarios
        ProviderEntity::class,  // Tabla de Prestadores
        CategoryEntity::class,  // Tabla de Categorías
        TenderEntity::class,    // Tabla de Licitaciones (Pedidos del cliente)
        BudgetEntity::class,    // Tabla de Presupuestos (Ofertas de prestadores)
        MessageEntity::class,    // Tabla de Mensajes (Historial de Chat)
        CalendarEventEntity::class, // Tabla de Eventos del Calendario (NUEVO)
        FastCategoryEntity::class // Tabla de Categorías de Acceso Rápido (NUEVO)
    ],
    version = 30, // 🔥 [INCREMENTADO] Se agregaron campos a CalendarEventEntity
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // --- DAOs: Accesos a las tablas ---
    abstract fun userDao(): UserDao
    abstract fun providerDao(): ProviderDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun chatDao(): ChatDao
    abstract fun calendarDao(): CalendarDao
    abstract fun fastCategoryDao(): FastCategoryDao // NUEVO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia Singleton de la base de datos.
         */
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration() // Limpia la BD al cambiar versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * --- CALLBACK DE INICIALIZACIÓN ---
     * Se activa únicamente la primera vez que se crea la base de datos física.
     */
    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    // Obtenemos los DAOs necesarios
                    val categoryDao = database.categoryDao()
                    val providerDao = database.providerDao()
                    val budgetDao = database.budgetDao()
                    // val calendarDao = database.calendarDao() // Instanciamos el nuevo DAO


                    // ===================================================
                    // 1. CARGA DE CATEGORÍAS (Sembrado Inicial)
                    // ===================================================
                    val categoryEntities = SembradoServiciosInicia.categories.map { item ->
                        CategoryEntity(
                            name = item.name,
                            icon = item.icon,
                            superCategory = item.superCategory,
                            superCategoryIcon = item.superCategoryIcon,
                            description = item.description, // [NUEVO] Mapeo del detalle descriptivo
                            providerIds = item.providerIds,
                            isNew = item.isNew,
                            isNewPrestador = item.isNewPrestador,
                            isAd = item.isAd
                        )
                    }
                    categoryDao.insertAll(categoryEntities)

                    // ===================================================
                    // 2. GENERACIÓN DE DATOS SEMILLA (SIMULACIÓN TOTAL)
                    // Llamamos a generateAll() que ahora lee internamente de las categorías.
                    // ===================================================
                    val seedData = PrestadorSampleDataFalso.generateAll(categoryEntities)

                    // 3. CARGA DE PRESTADORES
                    providerDao.insertAll(seedData.providers)

                    // 4. CARGA DE LICITACIONES (RE-HABILITADO)
                    seedData.tenders.forEach { tender ->
                        budgetDao.insertTender(tender)
                    }

                    // 5. CARGA DE AGENDA / EVENTOS DE CALENDARIO (DESACTIVADO PARA USAR DATOS REALES)
                    // calendarDao.insertAllEvents(seedData.calendarEvents)

                    // 6. CARGA DE PRESUPUESTOS (DESACTIVADO POR USUARIO)
                    // seedData.budgets.forEach { budget ->
                    //    budgetDao.insertBudget(budget)
                    // }

                    // 7. CARGA DE MENSAJES DE CHAT (DESACTIVADO POR USUARIO)
                    // chatDao.insertAllMessages(seedData.messages)
                }
            }
        }
    }
}
