package com.example.myapplication.core.datos.local.entidades.vistas

import androidx.room.DatabaseView

/**
 * --- VISTA SQL DE INVENTARIO (v2026.SUPREME) ---
 * [PROPÓSITO]: Unificar Recursos y Equipo en una sola tabla lógica para la gestión elite.
 */
@DatabaseView(
    viewName = "v_inventario_sucursal",
    value = """
        SELECT 
            id, 
            idPropietario, 
            idSucursal, 
            nombre, 
            'RECURSO' as tipoActivo, 
            estaHabilitado, 
            tipoRecurso as categoria,
            nombre as subTitulo,
            descripcion as infoExtra,
            '' as matricula,
            '' as idRecursoVinculado
        FROM recursos
        UNION ALL
        SELECT 
            id, 
            idPropietario, 
            idSucursal, 
            (nombre || ' ' || apellido) as nombre, 
            'EQUIPO' as tipoActivo, 
            estaHabilitado, 
            cargo as categoria,
            cargo as subTitulo,
            detalle as infoExtra,
            '' as matricula,
            idRecursoVinculado
        FROM equipo_trabajo
    """
)
data class InventarioSucursalSQLView(
    val id: String,
    val idPropietario: String,
    val idSucursal: String?,
    val nombre: String,
    val tipoActivo: String, 
    val estaHabilitado: Boolean,
    val categoria: String,
    val subTitulo: String,
    val infoExtra: String,
    val matricula: String,
    val idRecursoVinculado: String?
)
