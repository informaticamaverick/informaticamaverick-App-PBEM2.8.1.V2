package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.vistas.InventarioSucursalSQLView
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo

/**
 * --- MAPPER DE INVENTARIO (v2026.ELITE) ---
 * [PROPÓSITO]: Transformar vistas de base de datos en modelos de dominio consumibles.
 */
object InventarioMappers {
    
    fun deVistaADominio(vista: InventarioSucursalSQLView): InventarioActivoDominio {
        val esRecurso = vista.tipoActivo == "RECURSO"
        return InventarioActivoDominio(
            id = vista.id,
            nombre = vista.nombre,
            tipo = if (esRecurso) TipoActivo.RECURSO else TipoActivo.EQUIPO,
            habilitado = vista.estaHabilitado,
            categoria = vista.categoria,
            subTitulo = vista.subTitulo,
            idSucursal = vista.idSucursal,
            equipamiento = if (esRecurso) vista.infoExtra else "",
            especialidad = if (!esRecurso) vista.categoria else "",
            matricula = vista.matricula,
            idRecursoVinculado = vista.idRecursoVinculado
        )
    }

    fun deListaVistaAListaDominio(lista: List<InventarioSucursalSQLView>): List<InventarioActivoDominio> {
        return lista.map { deVistaADominio(it) }
    }
}



