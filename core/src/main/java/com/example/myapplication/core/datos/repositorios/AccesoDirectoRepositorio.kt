package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.ShortcutDao
import com.example.myapplication.core.datos.local.entidades.ShortcutEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE ACCESOS DIRECTOS (Atómico) ---
 * [LEY #9]: Estándar Mav en Español.
 */
@Singleton
class AccesoDirectoRepositorio @Inject constructor(
    private val shortcutDao: ShortcutDao
) {
    fun obtenerShortcutsPorContexto(contexto: String): Flow<List<ShortcutEntity>> =
        shortcutDao.obtenerShortcutsPorContexto(contexto)

    suspend fun agregarShortcut(
        contexto: String, 
        idDestino: String, 
        tipo: String,
        etiqueta: String? = null,
        icono: String? = null
    ) {
        val id = "${contexto}_${idDestino}"
        shortcutDao.insertarShortcut(
            ShortcutEntity(
                id = id, 
                idDestino = idDestino, 
                contexto = contexto, 
                tipo = tipo,
                etiqueta = etiqueta,
                icono = icono
            )
        )
    }

    suspend fun eliminarShortcut(contexto: String, idDestino: String) {
        shortcutDao.eliminarShortcut(contexto, idDestino)
    }

    suspend fun limpiarShortcutsPorTipo(contexto: String, tipo: String) {
        // Implementar en DAO si se requiere limpieza masiva
    }
}



































