package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * --- CLAVE REMOTA DE BÚSQUEDA (Paging 3) ---
 * [PROPÓSITO]: Almacenar el cursor de Firestore para permitir la paginación 
 * infinita y el soporte offline por cada consulta (CP + Categoría).
 */
@Entity(tableName = "claves_remotas_busqueda")
data class ClaveRemotaBusquedaEntity(
    @PrimaryKey val idConsulta: String, // Generada por MotorDescubrimientoMav
    val ultimoDocumentoId: String?,
    val marcaTiempo: Long = System.currentTimeMillis()
)

































