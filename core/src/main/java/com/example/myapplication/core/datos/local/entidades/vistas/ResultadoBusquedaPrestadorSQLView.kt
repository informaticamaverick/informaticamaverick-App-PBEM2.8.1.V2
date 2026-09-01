package com.example.myapplication.core.datos.local.entidades.vistas

import androidx.room.DatabaseView
import com.example.myapplication.core.dominio.modelos.PerfilPrestadorInsignia
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.TipoPrestador
import com.example.myapplication.core.dominio.ubicacion.CalculadoraGeografica

/**
 * --- RESULTADO BÚSQUEDA PRESTADOR SQL VIEW (ELITE Relacional) ---
 */
@DatabaseView("""
    SELECT 
        p.id as id, 
        p.id as idPropietario, 
        NULL as idEmpresa, 
        'INDIVIDUAL' as tipo,
        p.nombreVisible as titulo, 
        'Profesional Independiente' as subtitulo,
        p.urlFotoPerfil as urlFoto, 
        p.miniaturaBase64 as urlMiniatura,
        p.reputacion as reputacion, 
        p.totalReseñas as totalResenas, 
        p.trabajosRealizados as trabajosRealizados,
        p.estaVerificado as estaVerificado, 
        p.estaEnLinea as estaOnline, 
        CAST(IFNULL(c.estaSuscrito, 0) AS INTEGER) as estaSuscrito,
        p.atiende24Horas as atiende24h, 
        p.visitaADomicilio as visitaADomicilio, 
        p.realizaEnvios as realizaEnvios,
        p.brindaServicio as brindaServicio, 
        p.brindaProducto as brindaProducto, 
        p.brindaTurnos as brindaTurnos,
        d.codigoPostal as codigoPostal, 
        d.latitud as latitud, 
        d.longitud as longitud, 
        p.tieneLocalFisico as tieneLocalFisico,
        p.idCategorias as idCategorias,
        d.calle as calle, 
        d.numero as numero
    FROM prestadores p
    LEFT JOIN (
        SELECT idPropietario, MAX(calle) as calle, MAX(numero) as numero, MAX(codigoPostal) as codigoPostal, MAX(latitud) as latitud, MAX(longitud) as longitud 
        FROM direcciones 
        WHERE idSucursal IS NULL OR idSucursal = ''
        GROUP BY idPropietario
    ) d ON p.id = d.idPropietario
    LEFT JOIN cuentas c ON p.id = c.id
    
    UNION ALL
    
    SELECT 
        s.id as id, 
        s.idPropietario as idPropietario, 
        s.idEmpresaPadre as idEmpresa, 
        'SUCURSAL' as tipo,
        s.nombre as titulo, 
        e.nombre as subtitulo,
        e.urlFoto as urlFoto, 
        e.miniaturaBase64 as urlMiniatura,
        s.reputacion as reputacion, 
        s.totalReseñas as totalResenas, 
        s.trabajosRealizados as trabajosRealizados,
        1 as estaVerificado, 
        s.estaEnLinea as estaOnline, 
        CAST(IFNULL(c.estaSuscrito, 0) AS INTEGER) as estaSuscrito,
        s.atiende24Horas as atiende24h, 
        s.visitaADomicilio as visitaADomicilio, 
        s.realizaEnvios as realizaEnvios,
        s.brindaServicio as brindaServicio, 
        s.brindaProducto as brindaProducto, 
        s.brindaTurnos as brindaTurnos,
        d.codigoPostal as codigoPostal, 
        d.latitud as latitud, 
        d.longitud as longitud, 
        1 as tieneLocalFisico,
        e.idCategorias as idCategorias,
        d.calle as calle, 
        d.numero as numero
    FROM sucursales s
    INNER JOIN empresas e ON s.idEmpresaPadre = e.id
    LEFT JOIN (
        SELECT idSucursal, MAX(calle) as calle, MAX(numero) as numero, MAX(codigoPostal) as codigoPostal, MAX(latitud) as latitud, MAX(longitud) as longitud 
        FROM direcciones 
        WHERE idSucursal IS NOT NULL AND idSucursal != ''
        GROUP BY idSucursal
    ) d ON s.id = d.idSucursal
    LEFT JOIN cuentas c ON s.idPropietario = c.id
""")
data class ResultadoBusquedaPrestadorSQLView(
    val id: String,
    val idPropietario: String,
    val idEmpresa: String?,
    val tipo: String,
    val titulo: String,
    val subtitulo: String,
    val urlFoto: String?,
    val urlMiniatura: String?,
    val reputacion: Float,
    val totalResenas: Int,
    val trabajosRealizados: Int,
    val estaVerificado: Boolean,
    val estaOnline: Boolean,
    val estaSuscrito: Boolean,
    val atiende24h: Boolean,
    val visitaADomicilio: Boolean,
    val realizaEnvios: Boolean,
    val brindaServicio: Boolean,
    val brindaProducto: Boolean,
    val brindaTurnos: Boolean,
    val codigoPostal: String?,
    val latitud: Double,
    val longitud: Double,
    val tieneLocalFisico: Boolean,
    val idCategorias: List<String>,
    
    val calle: String?,
    val numero: String?
) {
    fun aModeloDominio(latUsuario: Double? = null, lngUsuario: Double? = null): PrestadorDominio {
        val distancia = if (latUsuario != null && lngUsuario != null && latitud != 0.0) {
            CalculadoraGeografica.calcularDistanciaKm(latUsuario, lngUsuario, latitud, longitud)
        } else null

        val direccionTexto = if (!calle.isNullOrBlank()) {
            "$calle ${numero ?: ""}".trim()
        } else {
            "C.P. ${codigoPostal ?: ""}"
        }

        return PrestadorDominio(
            id = id,
            idPropietario = idPropietario,
            idEmpresa = idEmpresa,
            tipo = if (tipo == "SUCURSAL") TipoPrestador.SUCURSAL else TipoPrestador.INDIVIDUAL,
            titulo = titulo,
            subtitulo = subtitulo,
            urlMiniatura = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(urlMiniatura ?: urlFoto),
            urlFoto = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(urlFoto),
            reputacion = reputacion,
            totalReseñas = totalResenas,
            trabajosRealizados = trabajosRealizados,
            estaVerificado = estaVerificado,
            estaOnline = estaOnline,
            estaSuscrito = estaSuscrito,
            atiende24h = atiende24h,
            visitaADomicilio = visitaADomicilio,
            realizaEnvios = realizaEnvios,
            brindaServicio = brindaServicio,
            brindaProducto = brindaProducto,
            brindaTurnos = brindaTurnos,
            tieneLocalFisico = tieneLocalFisico,
            idCategorias = idCategorias,
            codigoPostal = codigoPostal,
            distanciaKm = distancia,
            latitud = if (latitud != 0.0) latitud else null,
            longitud = if (longitud != 0.0) longitud else null,
            direccionVisible = if (distancia != null) "$direccionTexto (A ${"%.1f".format(distancia)} km)" else direccionTexto,
            insignias = PerfilPrestadorInsignia.crearPackEstandar(
                brindaServicio = brindaServicio,
                brindaProducto = brindaProducto,
                atiende24h = atiende24h,
                tieneLocalFisico = tieneLocalFisico,
                visitaADomicilio = visitaADomicilio,
                realizaEnvios = realizaEnvios,
                brindaTurnos = brindaTurnos
            )
        )
    }
}
