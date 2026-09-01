package com.example.myapplication.core.datos.local.entidades.vistas

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.example.myapplication.core.datos.local.entidades.ConversacionEntity

/**
 * --- VISTA SOBERANA DE CONVERSACIONES (ELITE v2026) ---
 * [PROPÓSITO]: Resolver la identidad visual y rubros en tiempo real desde la fuente (SSOT).
 * [LEY #2]: Costo Zero. Evita redundancia de datos en RAM.
 */
@DatabaseView("""
    SELECT 
        c.*,
        COALESCE(p.nombreVisible, s.nombre, u.nombreVisible, c.nombreRemoto) as nombreSoberano,
        COALESCE(p.urlFotoPerfil, e.urlFoto, u.urlFotoPerfil, c.fotoRemotaUrl) as fotoSoberana,
        COALESCE(p.miniaturaBase64, e.miniaturaBase64, u.miniaturaBase64, c.miniaturaRemotaBase64) as miniaturaSoberana,
        COALESCE(p.estaEnLinea, s.estaEnLinea, u.estaEnLinea, 0) as estaOnlineSoberano,
        COALESCE(p.estaVerificado, e.estaVerificada, 0) as estaVerificadoSoberano,
        COALESCE(p.idCategorias, e.idCategorias, '[]') as idsCategoriasSoberanas
    FROM conversaciones c
    LEFT JOIN prestadores p ON c.idIdentidadRemota = p.id
    LEFT JOIN sucursales s ON c.idIdentidadRemota = s.id
    LEFT JOIN empresas e ON s.idEmpresaPadre = e.id
    LEFT JOIN identidades_usuario u ON c.idIdentidadRemota = u.id
""")
data class ConversacionResumenSQLView(
    @Embedded val conversacion: ConversacionEntity,
    val nombreSoberano: String,
    val fotoSoberana: String?,
    val miniaturaSoberana: String?,
    val estaOnlineSoberano: Boolean,
    val estaVerificadoSoberano: Boolean,
    val idsCategoriasSoberanas: List<String>
)
