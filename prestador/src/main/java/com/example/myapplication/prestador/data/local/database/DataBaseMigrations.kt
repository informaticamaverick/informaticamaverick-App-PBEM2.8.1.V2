package com.example.myapplication.prestador.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MIGRACIONES DE LA BASE DE DATOS
 * Cada migración actualiza la BD de una versión a otra sin perder datos
 */
object DatabaseMigrations {

    /**
     * Migración de versión 6 a 7
     * Cambios:
     * - Agregar Foreign Keys a appointments, presupuestos y promotions
     * - Agregar índices para mejorar rendimiento
     */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Como Room no puede alterar tablas con FK fácilmente,
            // necesitamos recrear las tablas afectadas
            
            // 1. RECREAR TABLA APPOINTMENTS con Foreign Keys
            database.execSQL("""
                CREATE TABLE appointments_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    clientId TEXT NOT NULL,
                    clientName TEXT NOT NULL,
                    providerId TEXT NOT NULL,
                    service TEXT NOT NULL,
                    date TEXT NOT NULL,
                    time TEXT NOT NULL,
                    status TEXT NOT NULL,
                    notes TEXT NOT NULL DEFAULT '',
                    proposedBy TEXT NOT NULL DEFAULT 'provider',
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(clientId) REFERENCES clientes(id) ON DELETE CASCADE,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Crear índices para appointments
            database.execSQL("CREATE INDEX index_appointments_clientId ON appointments_new(clientId)")
            database.execSQL("CREATE INDEX index_appointments_providerId ON appointments_new(providerId)")
            database.execSQL("CREATE INDEX index_appointments_providerId_date_time ON appointments_new(providerId, date, time)")
            
            // Copiar datos antiguos
            database.execSQL("""
                INSERT INTO appointments_new 
                SELECT id, clientId, clientName, providerId, service, date, time, status, 
                       notes, proposedBy, createdAt, updatedAt
                FROM appointments
            """.trimIndent())
            
            // Eliminar tabla antigua y renombrar
            database.execSQL("DROP TABLE appointments")
            database.execSQL("ALTER TABLE appointments_new RENAME TO appointments")
            
            
            // 2. RECREAR TABLA PRESUPUESTOS con Foreign Keys
            database.execSQL("""
                CREATE TABLE presupuestos_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    numeroPresupuesto TEXT NOT NULL,
                    clienteId TEXT NOT NULL,
                    prestadorId TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    validezDias INTEGER NOT NULL,
                    subtotal REAL NOT NULL,
                    impuestos REAL NOT NULL,
                    total REAL NOT NULL,
                    estado TEXT NOT NULL,
                    notas TEXT NOT NULL DEFAULT '',
                    itemsJson TEXT NOT NULL DEFAULT '',
                    serviciosJson TEXT NOT NULL DEFAULT '',
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(clienteId) REFERENCES clientes(id) ON DELETE CASCADE,
                    FOREIGN KEY(prestadorId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Crear índices para presupuestos
            database.execSQL("CREATE INDEX index_presupuestos_clienteId ON presupuestos_new(clienteId)")
            database.execSQL("CREATE INDEX index_presupuestos_prestadorId ON presupuestos_new(prestadorId)")
            database.execSQL("CREATE INDEX index_presupuestos_clienteId_estado ON presupuestos_new(clienteId, estado)")
            
            // Copiar datos antiguos
            database.execSQL("""
                INSERT INTO presupuestos_new 
                SELECT id, numeroPresupuesto, clienteId, prestadorId, fecha, validezDias,
                       subtotal, impuestos, total, estado, notas, itemsJson, serviciosJson,
                       createdAt, updatedAt
                FROM presupuestos
            """.trimIndent())
            
            // Eliminar tabla antigua y renombrar
            database.execSQL("DROP TABLE presupuestos")
            database.execSQL("ALTER TABLE presupuestos_new RENAME TO presupuestos")
            
            
            // 3. RECREAR TABLA PROMOTIONS con Foreign Keys
            database.execSQL("""
                CREATE TABLE promotions_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    providerId TEXT NOT NULL,
                    providerName TEXT NOT NULL,
                    providerImageUrl TEXT,
                    type TEXT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    imageUrls TEXT NOT NULL,
                    discount INTEGER,
                    categories TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    expiresAt INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    likes INTEGER NOT NULL DEFAULT 0,
                    views INTEGER NOT NULL DEFAULT 0,
                    rating REAL NOT NULL DEFAULT 0.0,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Crear índices para promotions
            database.execSQL("CREATE INDEX index_promotions_providerId ON promotions_new(providerId)")
            database.execSQL("CREATE INDEX index_promotions_providerId_createdAt ON promotions_new(providerId, createdAt)")
            
            // Copiar datos antiguos
            database.execSQL("""
                INSERT INTO promotions_new 
                SELECT id, providerId, providerName, providerImageUrl, type, title, 
                       description, imageUrls, discount, categories, createdAt, expiresAt,
                       status, likes, views, rating
                FROM promotions
            """.trimIndent())
            
            // Eliminar tabla antigua y renombrar
            database.execSQL("DROP TABLE promotions")
            database.execSQL("ALTER TABLE promotions_new RENAME TO promotions")
        }
    }
    
    /**
     * Migración de versión 7 a 8
     * Cambios:
     * - Remover Foreign Keys de presupuestos (temporalmente)
     * - Esto permite crear presupuestos sin necesidad de tener providerId existente
     */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Recrear tabla presupuestos SIN Foreign Keys
            database.execSQL("""
                CREATE TABLE presupuestos_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    numeroPresupuesto TEXT NOT NULL,
                    clienteId TEXT NOT NULL,
                    prestadorId TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    validezDias INTEGER NOT NULL,
                    subtotal REAL NOT NULL,
                    impuestos REAL NOT NULL,
                    total REAL NOT NULL,
                    estado TEXT NOT NULL,
                    notas TEXT NOT NULL DEFAULT '',
                    itemsJson TEXT NOT NULL DEFAULT '',
                    serviciosJson TEXT NOT NULL DEFAULT '',
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            
            // Crear solo índices (sin FK)
            database.execSQL("CREATE INDEX index_presupuestos_clienteId ON presupuestos_new(clienteId)")
            database.execSQL("CREATE INDEX index_presupuestos_prestadorId ON presupuestos_new(prestadorId)")
            database.execSQL("CREATE INDEX index_presupuestos_clienteId_estado ON presupuestos_new(clienteId, estado)")
            
            // Copiar datos antiguos
            database.execSQL("""
                INSERT INTO presupuestos_new 
                SELECT id, numeroPresupuesto, clienteId, prestadorId, fecha, validezDias,
                       subtotal, impuestos, total, estado, notas, itemsJson, serviciosJson,
                       createdAt, updatedAt
                FROM presupuestos
            """.trimIndent())
            
            // Eliminar tabla antigua y renombrar
            database.execSQL("DROP TABLE presupuestos")
            database.execSQL("ALTER TABLE presupuestos_new RENAME TO presupuestos")
        }
    }
    
    /**
     * Migración de versión 9 a 10
     * Cambios:
     * - Agregar campos profesionales y de configuración a providers
     */
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columnas nuevas a la tabla providers
            database.execSQL("ALTER TABLE providers ADD COLUMN dniCuit TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN profesion TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN matricula TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN provincia TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN codigoPostal TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN pais TEXT NOT NULL DEFAULT 'Argentina'")
            database.execSQL("ALTER TABLE providers ADD COLUMN atencionUrgencias INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN vaDomicilio INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN turnosEnLocal INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN tieneEmpresa INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN nombreEmpresa TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN cuitEmpresa TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN direccionEmpresa TEXT DEFAULT NULL")
        }
    }
    
    /**
     * Migración de versión 10 a 11
     * Cambios:
     * - Agregar campo tieneMatricula a providers
     */
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columna tieneMatricula
            database.execSQL("ALTER TABLE providers ADD COLUMN tieneMatricula INTEGER NOT NULL DEFAULT 0")
        }
    }
    
    /**
     * Migración de versión 11 a 12
     * Cambios:
     * - Agregar campo trabajaConOtros a providers
     * - Crear tabla empleados para gestionar equipo de trabajo
     */
    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columna trabajaConOtros a providers
            database.execSQL("ALTER TABLE providers ADD COLUMN trabajaConOtros INTEGER NOT NULL DEFAULT 0")
            
            // Crear tabla empleados con el orden exacto de columnas
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS empleados (
                    id TEXT PRIMARY KEY NOT NULL,
                    prestadorId TEXT NOT NULL,
                    nombre TEXT NOT NULL,
                    apellido TEXT NOT NULL,
                    dni TEXT NOT NULL,
                    activo INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            
            // Crear índice para búsquedas por prestadorId
            database.execSQL("CREATE INDEX IF NOT EXISTS index_empleados_prestadorId ON empleados(prestadorId)")
        }
    }
    
    /**
     * Migración de versión 12 a 13
     * Cambios:
     * - Agregar campos para dirección del local (direccionLocal, provinciaLocal, codigoPostalLocal)
     */
    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columnas para dirección del local
            database.execSQL("ALTER TABLE providers ADD COLUMN direccionLocal TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN provinciaLocal TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN codigoPostalLocal TEXT DEFAULT NULL")
        }
    }
    
    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columna serviceType para tipos de servicio
            database.execSQL("ALTER TABLE providers ADD COLUMN serviceType TEXT NOT NULL DEFAULT 'TECHNICAL'")
        }
    }
    
    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Crear tabla de horarios de disponibilidad para profesionales
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS availability_schedules (
                    id TEXT PRIMARY KEY NOT NULL,
                    providerId TEXT NOT NULL,
                    dayOfWeek INTEGER NOT NULL,
                    startTime TEXT NOT NULL,
                    endTime TEXT NOT NULL,
                    appointmentDuration INTEGER NOT NULL,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """)
            
            // Crear índice para providerId
            database.execSQL("CREATE INDEX IF NOT EXISTS index_availability_schedules_providerId ON availability_schedules(providerId)")
        }
    }
    
    /**
     * Migración 15 → 16: Agregar tabla rental_spaces para alquiler de espacios
     */
    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Crear tabla rental_spaces
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS rental_spaces (
                    id TEXT PRIMARY KEY NOT NULL,
                    providerId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    pricePerHour REAL NOT NULL,
                    blockDuration INTEGER NOT NULL,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Crear índice para providerId
            database.execSQL("CREATE INDEX IF NOT EXISTS index_rental_spaces_providerId ON rental_spaces(providerId)")
        }
    }
    
    /**
     * Migración 16 → 17: Agregar campos a appointments para multi-tipo de servicio
     */
    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Recrear tabla appointments con nuevos campos
            // Es más seguro recrear que usar ALTER TABLE para campos nullable
            database.execSQL("""
                CREATE TABLE appointments_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    clientId TEXT NOT NULL,
                    clientName TEXT NOT NULL,
                    providerId TEXT NOT NULL,
                    service TEXT NOT NULL,
                    date TEXT NOT NULL,
                    time TEXT NOT NULL,
                    duration INTEGER NOT NULL DEFAULT 60,
                    status TEXT NOT NULL,
                    notes TEXT NOT NULL DEFAULT '',
                    proposedBy TEXT NOT NULL DEFAULT 'provider',
                    serviceType TEXT NOT NULL DEFAULT 'TECHNICAL',
                    rentalSpaceId TEXT DEFAULT NULL,
                    scheduleId TEXT DEFAULT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(clientId) REFERENCES clientes(id) ON DELETE CASCADE,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE,
                    FOREIGN KEY(rentalSpaceId) REFERENCES rental_spaces(id) ON DELETE SET NULL,
                    FOREIGN KEY(scheduleId) REFERENCES availability_schedules(id) ON DELETE SET NULL
                )
            """.trimIndent())
            
            // Crear índices
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_clientId ON appointments_new(clientId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_providerId ON appointments_new(providerId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_providerId_date_time ON appointments_new(providerId, date, time)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_rentalSpaceId ON appointments_new(rentalSpaceId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_scheduleId ON appointments_new(scheduleId)")
            
            // Copiar datos existentes (sin los campos nuevos que tienen defaults)
            database.execSQL("""
                INSERT INTO appointments_new (
                    id, clientId, clientName, providerId, service, date, time,
                    status, notes, proposedBy, createdAt, updatedAt
                )
                SELECT 
                    id, clientId, clientName, providerId, service, date, time,
                    status, notes, proposedBy, createdAt, updatedAt
                FROM appointments
            """.trimIndent())
            
            // Eliminar tabla vieja y renombrar
            database.execSQL("DROP TABLE appointments")
            database.execSQL("ALTER TABLE appointments_new RENAME TO appointments")
        }
    }
    
    /**
     * Migración 17 → 18: Arreglar esquema de sucursales (prestadorId -> businessId)
     */
    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Verificar si la tabla sucursales tiene la columna incorrecta
            // Room puede haber generado un esquema incorrecto en versiones anteriores
            
            // Eliminar índice viejo si existe
            try {
                database.execSQL("DROP INDEX IF EXISTS index_sucursales_prestadorId")
            } catch (e: Exception) {
                // Ignorar si no existe
            }
            
            // Crear índice correcto si no existe
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sucursales_businessId ON sucursales(businessId)")
        }
    }
    
    /**
     * Migración 20 → 21: Agregar campos específicos por tipo de servicio
     */
    val MIGRATION_20_21 = object : Migration(20, 21) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columnas nuevas con valores por defecto
            database.execSQL(
                "ALTER TABLE appointments ADD COLUMN urgencyLevel TEXT DEFAULT NULL"
            )
            database.execSQL(
                "ALTER TABLE appointments ADD COLUMN peopleCount INTEGER DEFAULT NULL"
            )
        }
    }

    /**
     * Lista de todas las migraciones disponibles
     *
     */
    val MIGRATION_21_22 = object : Migration(21, 22) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE appointments ADD COLUMN assignedEmployeeIds TEXT DEFAULT NULL"
            )

        }
    }


    val MIGRATION_22_23 = object : Migration(22, 23) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE presupuestos ADD COLUMN appointmentId TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE presupuestos ADD COLUMN firestoreId TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE presupuestos ADD COLUMN syncedAt INTEGER DEFAULT NULL")
        }
    }

    /**
     * Lista de todas las migraciones disponibles
     */
    val MIGRATION_23_24 = object : Migration(23, 24) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `plantillas_presupuesto` (
                    `id` TEXT NOT NULL,
                    `nombre` TEXT NOT NULL,
                    `itemsJson` TEXT NOT NULL DEFAULT '',
                    `createdAT` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )"""
            )
        }
    }

    val MIGRATION_24_25 = object : Migration(24, 25) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE plantillas_presupuesto ADD COLUMN serviciosJson TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_25_26 = object : Migration(25, 26) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE presupuestos ADD COLUMN honorariosJson TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE presupuestos ADD COLUMN gastosJson TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE presupuestos ADD COLUMN impuestosJson TEXT NOT NULL DEFAULT ''")
        }
    }

    val MIGRATION_26_27 = object : Migration(26, 27) {
        override fun migrate(database: SupportSQLiteDatabase) {
            //1. Nuevos campos en providers
            database.execSQL("ALTER TABLE providers ADD COLUMN apellido TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN titulo TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN latitud REAL DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN longitud REAL DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN suscripto INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN verificado INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN favorito INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN envios INTEGER NOT NULL DEFAULT 0")

            //2. Nuevos campos en business
            database.execSQL("ALTER TABLE business ADD COLUMN direccionId TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE business ADD COLUMN referenteId TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE business ADD COLUMN categorias TEXT NOT NULL DEFAULT '[]'")
            database.execSQL("ALTER TABLE business ADD COLUMN imagenesProductos TEXT NOT NULL DEFAULT '[]'")
            database.execSQL("ALTER TABLE business ADD COLUMN atencion24hs INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE business ADD COLUMN localComercial INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE business ADD COLUMN visitaDomicilio INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE business ADD COLUMN envios INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE business ADD COLUMN turnos INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE business ADD COLUMN verificado INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE business ADD COLUMN rating REAL NOT NULL DEFAULT 0")

            //3. Recrear tabla sucursales
            // Se removieron: direccion, codigoPostal, latitude, longitude
            // Se agregaron: direccionId, referenteId
            // SQLite no permite DROP COLUMN, hay que recrear la tabla
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS sucursales_new (
                    id TEXT NOT NULL,
                    businessId TEXT NOT NULL,
                    nombre TEXT NOT NULL,
                    telefono TEXT,
                    email TEXT,
                    horario TEXT,
                    direccionId TEXT,
                    referenteId TEXT,
                    isActive INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(id),
                    FOREIGN KEY(businessId) REFERENCES business(id) ON DELETE CASCADE
                )
            """.trimIndent())

            // Copiar datos conservando solo las columnas que siguen existiendo
            database.execSQL("""
                INSERT INTO sucursales_new
                    (id, businessId, nombre, telefono, email, horario, isActive, createdAt, updatedAt)
                SELECT id, businessId, nombre, telefono, email, horario, isActive, createdAt, updatedAt
                FROM sucursales
            """.trimIndent())

            database.execSQL("DROP INDEX IF EXISTS index_sucursales_businessId")
            database.execSQL("DROP TABLE sucursales")
            database.execSQL("ALTER TABLE sucursales_new RENAME TO sucursales")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sucursales_businessId ON sucursales(businessId)")

            //4. Crear tabla direcciones
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS direcciones (
                    id TEXT NOT NULL,
                    referenciaId TEXT NOT NULL,
                    referenciaTipo TEXT NOT NULL,
                    pais TEXT NOT NULL DEFAULT 'Argentina',
                    provincia TEXT,
                    localidad TEXT,
                    codigoPostal TEXT,
                    calle TEXT,
                    numero TEXT,
                    latitud REAL,
                    longitud REAL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
            """.trimIndent())

            //5. Crear tabla referentes
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS referentes (
                    id TEXT NOT NULL,
                    providerId TEXT NOT NULL,
                    nombre TEXT NOT NULL,
                    apellido TEXT,
                    cargo TEXT,
                    imageUrl TEXT,
                    empresaId TEXT,
                    sucursalId TEXT,
                    activo INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
            """.trimIndent())
        }
    }

    val MIGRATION_27_28 = object : Migration(27, 28) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE providers ADD COLUMN horarioLocal TEXT")
            database.execSQL("ALTER TABLE business ADD COLUMN horario TEXT")
        }
    }


    val MIGRATION_28_29 = object : Migration(28, 29) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE providers ADD COLUMN atiendeVirtual INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_29_30 = object : Migration(29,30) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS notificaciones (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    tipo TEXT NOT NULL,
                    titulo TEXT NOT NULL,
                    mensaje TEXT NOT NULL,
                    fechaMs INTEGER NOT NULL,
                    leida INTEGER NOT NULL,
                    accionRoute TEXT
                    )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_30_31 = object : Migration(30, 31) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE presupuestos ADD COLUMN tituloTrabajo TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_31_32 = object  : Migration(31, 32) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE messages ADD COLUMN budgetDataJson TEXT DEFAULT NULL"
            )
        }
    }

    val MIGRATION_32_33 = object : Migration(32,33) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE presupuestos ADD COLUMN categorias TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_33_34 = object : Migration(33, 34) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE providers ADD COLUMN galleryImages TEXT NOT NULL DEFAULT '[]'"
            )
        }
    }

    val MIGRATION_37_38 = object : Migration(37, 38) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Room no permite eliminar la restricción UNIQUE directamente.
            // Debemos recrear la tabla 'conversations' sin la restricción UNIQUE en userId.
            
            // 1. Crear la tabla temporal con el esquema correcto
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `conversations_new` (
                    `conversationId` TEXT NOT NULL, 
                    `userId` TEXT NOT NULL, 
                    `userName` TEXT NOT NULL, 
                    `userAvatarUrl` TEXT, 
                    `serviceType` TEXT, 
                    `jobDescription` TEXT, 
                    `lastMessage` TEXT, 
                    `lastMessageTimestamp` INTEGER NOT NULL, 
                    `lastMessageType` TEXT NOT NULL, 
                    `unreadCount` INTEGER NOT NULL, 
                    `totalMessages` INTEGER NOT NULL, 
                    `isOnline` INTEGER NOT NULL, 
                    `isPinned` INTEGER NOT NULL, 
                    `isMuted` INTEGER NOT NULL, 
                    `isArchived` INTEGER NOT NULL, 
                    `isBlocked` INTEGER NOT NULL, 
                    `notificationsEnabled` INTEGER NOT NULL, 
                    `isVisible` INTEGER NOT NULL, 
                    `isLocked` INTEGER NOT NULL, 
                    `lockPassword` TEXT, 
                    `createdAt` INTEGER NOT NULL, 
                    `updatedAt` INTEGER NOT NULL, 
                    `isSynced` INTEGER NOT NULL, 
                    PRIMARY KEY(`conversationId`)
                )
            """.trimIndent())

            // 2. Copiar los datos de la tabla vieja a la nueva
            database.execSQL("""
                INSERT INTO conversations_new (
                    conversationId, userId, userName, userAvatarUrl, serviceType, 
                    jobDescription, lastMessage, lastMessageTimestamp, lastMessageType, 
                    unreadCount, totalMessages, isOnline, isPinned, isMuted, 
                    isArchived, isBlocked, notificationsEnabled, isVisible, 
                    isLocked, lockPassword, createdAt, updatedAt, isSynced
                )
                SELECT 
                    conversationId, userId, userName, userAvatarUrl, serviceType, 
                    jobDescription, lastMessage, lastMessageTimestamp, lastMessageType, 
                    unreadCount, totalMessages, isOnline, isPinned, isMuted, 
                    isArchived, isBlocked, notificationsEnabled, isVisible, 
                    isLocked, lockPassword, createdAt, updatedAt, isSynced
                FROM conversations
            """.trimIndent())

            // 3. Eliminar la tabla vieja
            database.execSQL("DROP TABLE conversations")

            // 4. Renombrar la tabla nueva
            database.execSQL("ALTER TABLE conversations_new RENAME TO conversations")

            // 5. Re-crear los índices (ahora sin el UNIQUE en userId)
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_userId` ON `conversations` (`userId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_lastMessageTimestamp` ON `conversations` (`lastMessageTimestamp`)")
        }
    }

    val MIGRATION_38_39 = object : Migration(38, 39) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE 'providers' ADD COLUMN 'priorizarEmpresa' INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    val MIGRATION_39_40 = object : Migration(39, 40) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'messages' ADD COLUMN'calendarStartDate' TEXT")
                        database.execSQL("ALTER TABLE 'messages' ADD COLUMN'calendarEndDate' TEXT")
                        database.execSQL("ALTER TABLE 'messages' ADD COLUMN'availabilityJson' TEXT")
                        database.execSQL("ALTER TABLE 'messages' ADD COLUMN'bookedSlotsJson' TEXT")
                        database.execSQL("ALTER TABLE 'messages' ADD COLUMN'calendarInviteMessageId' TEXT")
        }
    }

    val MIGRATION_40_41 = object : Migration(40, 41) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Para instalaciones frescas en v40, la columna trabajaConOtros no existe
            // porque el campo era un alias computado (no columna Room). La agregamos si falta.
            // Para usuarios que actualizaron desde v11, ya existe (MIGRATION_11_12 la creó).
            val cursor = database.query("PRAGMA table_info(providers)")
            var hasColumn = false
            while (cursor.moveToNext()) {
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex >= 0 && cursor.getString(nameIndex) == "trabajaConOtros") {
                    hasColumn = true
                    break
                }
            }
            cursor.close()
            if (!hasColumn) {
                database.execSQL("ALTER TABLE providers ADD COLUMN trabajaConOtros INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    val MIGRATION_41_42 = object : Migration(41, 42) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Recrear tabla sin CASCADE para evitar borrado en INSERT OR REPLACE del provider
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS availability_schedules_new (
                 id TEXT NOT NULL PRIMARY KEY,
                 providerId TEXT NOT NULL,
                 dayOfWeek INTEGER NOT NULL,
                 startTime TEXT NOT NULL,
                 endTime TEXT NOT NULL,
                 appointmentDuration INTEGER NOT NULL,
                 isActive INTEGER NOT NULL DEFAULT 1,
                 createdAt INTEGER NOT NULL,
                 updatedAt INTEGER NOT NULL,
                 FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE 
NO ACTION
             )
         """)
            db.execSQL("INSERT INTO availability_schedules_new SELECT * FROM availability_schedules")
                    db.execSQL("DROP TABLE availability_schedules")
                    db.execSQL("ALTER TABLE availability_schedules_new RENAME TO availability_schedules")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_availability_schedules_providerId ON availability_schedules(providerId)")
        }
    }



    val MIGRATION_42_43 = object : Migration(42, 43) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS booked_appointments (
                    id TEXT NOT NULL PRIMARY KEY,
                    clientId TEXT NOT NULL,
                    clientName TEXT NOT NULL,
                    date TEXT NOT NULL,
                    time TEXT NOT NULL,
                    notes TEXT NOT NULL DEFAULT '',
                    status TEXT NOT NULL DEFAULT 'CONFIRMED',
                    chatId TEXT NOT NULL DEFAULT '',
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
        }
    }

    val MIGRATION_43_44 = object : Migration(43, 44) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE availability_schedules ADD COLUMN worksByAppointment INTEGER NOT NULL DEFAULT 1"
            )
        }
    }

    val MIGRATION_44_45 = object : Migration(44, 45) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE booked_appointments ADD COLUMN service TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_45_46 = object : Migration(45, 46) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE messages ADD COLUMN receiptService TEXT")
            db.execSQL("ALTER TABLE messages ADD COLUMN receiptProviderName TEXT")
            db.execSQL("ALTER TABLE messages ADD COLUMN receiptProfession TEXT")
            db.execSQL("ALTER TABLE messages ADD COLUMN receiptAddress TEXT")
            db.execSQL("ALTER TABLE messages ADD COLUMN receiptCode TEXT")
            db.execSQL("ALTER TABLE messages ADD COLUMN receiptIsTechnician INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_46_47 = object : Migration(46, 47) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE messages ADD COLUMN receiptPrioritizeCompany INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_47_48 = object : Migration(47, 48) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE availability_schedules ADD COLUMN scheduleType TEXT NOT NULL DEFAULT 'TECHNICAL_VISIT'")
            // providerCompanyName agregado por Maxi sin migración
            db.execSQL("ALTER TABLE presupuestos ADD COLUMN providerCompanyName TEXT DEFAULT NULL")
            // Campos nuevos en messages agregados por Maxi sin migración
            db.execSQL("ALTER TABLE messages ADD COLUMN appointmentType TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE messages ADD COLUMN providerAddress TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE messages ADD COLUMN companyId TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE messages ADD COLUMN categoryId TEXT DEFAULT NULL")
            // Campos nuevos en conversations agregados por Maxi sin migración
            db.execSQL("ALTER TABLE conversations ADD COLUMN companyId TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE conversations ADD COLUMN categoryId TEXT DEFAULT NULL")
        }
    }

    val MIGRATION_48_49 = object : Migration(48, 49) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE messages ADD COLUMN budgetRequestDescription TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE messages ADD COLUMN budgetRequestClientAddress TEXT DEFAULT NULL")
        }
    }

    val MIGRATION_49_50 = object : Migration(49, 50) {
        override fun migrate(db: SupportSQLiteDatabase) {
            //Nuevo campos en Presupuestos
            db.execSQL("ALTER TABLE presupuestos ADD COLUMN honorariosJson TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE presupuestos ADD COLUMN gastosJson TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE presupuestos ADD COLUMN impuestosJson TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE presupuestos ADD COLUMN appointmentId TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE presupuestos ADD COLUMN firestoreId TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE presupuestos ADD COLUMN syncedAt INTEGER DEFAULT NULL")
        }
    }

    val MIGRATION_50_51 =object : Migration(50, 51) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Nota legal en presupuestos
            db.execSQL("ALTER TABLE presupuestos ADD COLUMN notaLegal TEXT NOT NULL DEFAULT ''")
        }
    }

    val MIGRATION_51_52 = object : Migration(51, 52) {
        override fun migrate(db: SupportSQLiteDatabase) {
            //Campos de respuesta estilo WhatsApp en messages
            db.execSQL("ALTER TABLE messages ADD COLUMN replyToId TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE messages ADD COLUMN replyToContent TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE messages ADD COLUMN replyToSenderName TEXT DEFAULT NULL,")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_13_14,
        MIGRATION_14_15,
        MIGRATION_15_16,
        MIGRATION_16_17,
        MIGRATION_17_18,
        MIGRATION_20_21,
        MIGRATION_21_22,
        MIGRATION_22_23,
        MIGRATION_23_24,
        MIGRATION_24_25,
        MIGRATION_25_26,
        MIGRATION_26_27,
        MIGRATION_27_28,
        MIGRATION_28_29,
        MIGRATION_29_30,
        MIGRATION_30_31,
        MIGRATION_31_32,
        MIGRATION_32_33,
        MIGRATION_33_34,
        MIGRATION_37_38,
        MIGRATION_38_39,
        MIGRATION_39_40,
        MIGRATION_40_41,
        MIGRATION_41_42,
        MIGRATION_42_43,
        MIGRATION_43_44,
        MIGRATION_44_45,
        MIGRATION_45_46,
        MIGRATION_46_47,
        MIGRATION_47_48,
        MIGRATION_48_49,
        MIGRATION_49_50,
        MIGRATION_50_51,
        MIGRATION_51_52
    )
}

