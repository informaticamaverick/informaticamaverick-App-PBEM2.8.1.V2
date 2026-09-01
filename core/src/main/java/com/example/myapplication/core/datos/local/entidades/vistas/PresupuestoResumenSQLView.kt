package com.example.myapplication.core.datos.local.entidades.vistas

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity

/**
 * --- VISTA SOBERANA DE PRESUPUESTOS (ELITE v2026) ---
 * [PROPÓSITO]: Resolver la identidad del prestador y rubros en tiempo real (SSOT).
 */
@DatabaseView("""
    SELECT 
        p.*,
        COALESCE(pr.nombreVisible, s.nombre, u.nombreVisible, p.nombrePrestador) as nombreSoberano,
        COALESCE(pr.miniaturaBase64, pr.urlFotoPerfil, e.miniaturaBase64, e.urlFoto, u.miniaturaBase64, u.urlFotoPerfil, p.urlFotoPrestador) as fotoSoberana,
        COALESCE(pr.estaVerificado, e.estaVerificada, 0) as estaVerificadoSoberano,
        COALESCE(pr.idCategorias, e.idCategorias, '[]') as idsCategoriasSoberanas
    FROM presupuestos_finales p
    LEFT JOIN prestadores pr ON p.idPrestador = pr.id
    LEFT JOIN sucursales s ON p.idPrestador = s.id
    LEFT JOIN empresas e ON s.idEmpresaPadre = e.id
    LEFT JOIN identidades_usuario u ON p.idPrestador = u.id
""")
data class PresupuestoResumenSQLView(
    @Embedded val presupuesto: PresupuestoFinalEntity,
    val nombreSoberano: String,
    val fotoSoberana: String?,
    val estaVerificadoSoberano: Boolean,
    val idsCategoriasSoberanas: List<String>
)
