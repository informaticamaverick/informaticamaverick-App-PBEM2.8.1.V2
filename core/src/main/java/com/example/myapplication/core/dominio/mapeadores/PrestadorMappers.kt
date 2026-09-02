package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.DireccionEntity
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.datos.local.entidades.ReviewEntity
import com.example.myapplication.core.datos.local.relaciones.PrestadorConDireccionesRelacionesBD
import com.example.myapplication.core.datos.local.relaciones.PrestadorCompletoRelacionesBD
import com.example.myapplication.core.dominio.modelos.descubrimiento.ResultadoIndiceBusquedaShallowDominio
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.dominio.ubicacion.CalculadoraGeografica
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.core.dominio.mapeadores.HorarioMappers

/**
 * --- PRESTADOR MAPPER (v2026.FINAL) ---
 * [PROPÓSITO]: Transformar identidades profesionales (Deep y Shallow) en modelos de dominio.
 * [LEY #3]: Implementa Carga Dual respetando el SSOT.
 */
object PrestadorMappers {

    /**
     * Mapeo desde Relación de Room (Prestador + Direcciones).
     */
    fun deRelacionADominio(
        relacion: PrestadorConDireccionesRelacionesBD,
        cuenta: com.example.myapplication.core.datos.local.entidades.CuentaEntity? = null
    ): PrestadorDominio {
        val base = deEntidadAModeloUi(relacion.prestador, cuenta)
        return base.copy(
            direcciones = relacion.direcciones.map { DireccionMappers.deEntidadAModelo(it) }
        )
    }

    /**
     * Mapeo desde Relación Deep (Room).
     */
    fun deRelacionADominioCompleto(
        relacion: PrestadorCompletoRelacionesBD,
        cuenta: com.example.myapplication.core.datos.local.entidades.CuentaEntity? = null
    ): PrestadorDominioCompleto {
        return PrestadorDominioCompleto(
            perfil = deEntidadAModeloUi(relacion.prestador, cuenta),
            direcciones = relacion.direcciones.map { DireccionMappers.deEntidadAModelo(it) },
            horario = relacion.horario?.let { HorarioMappers.deEntidadAModelo(it) },
            reseñas = relacion.reseñas.map { ReviewMappers.deEntidadADominio(it) }
        )
    }

    /**
     * Mapeo desde Identidad Deep (Entidad Completa de Room).
     */
    fun deEntidadAModeloUi(
        entidad: IdentidadPrestadorEntity,
        cuenta: com.example.myapplication.core.datos.local.entidades.CuentaEntity? = null
    ): PrestadorDominio {
        val pathFoto = entidad.urlFotoPerfil
        val miniatura = entidad.miniaturaBase64
        
        // [v2026.ELITE]: Segregación Total de Fuentes (Ley #3)
        // No mezclamos fuentes para evitar pixelado. El componente de UI decide el fallback.
        val fotoSource = ImageUtils.processImageSource(pathFoto)
        val miniaturaSource = ImageUtils.processImageSource(miniatura)

        return PrestadorDominio(
            id = entidad.id,
            idPropietario = entidad.id,
            nombre = entidad.nombre,
            apellido = entidad.apellido,
            titulo = entidad.nombreVisible,
            subtitulo = "Profesional Independiente",
            biografia = entidad.biografia,
            urlFoto = fotoSource,
            urlMiniatura = miniaturaSource,
            correo = if (entidad.correoElectronico.isNotBlank()) entidad.correoElectronico else (cuenta?.correoGoogle ?: ""),
            esGoogle = cuenta?.correoGoogle?.isNotBlank() == true,
            numeroTelefono = entidad.numeroTelefono,
            reputacion = entidad.reputacion,
            totalReseñas = entidad.totalReseñas,
            trabajosRealizados = entidad.trabajosRealizados,
            likes = entidad.likes,
            dislikes = entidad.dislikes,
            nivelElite = entidad.nivelElite,
            cuitCuil = entidad.cuitCuil,
            matricula = entidad.matricula,
            matriculaFotoUrl = ImageUtils.processImageSource(entidad.matriculaFotoUrl),
            estaVerificado = entidad.estaVerificado,
            estaOnline = entidad.estaEnLinea,
            estaSuscrito = cuenta?.estaSuscrito ?: false,
            esCargaCompleta = entidad.esCargaCompleta,
            brindaServicio = entidad.brindaServicio,
            brindaProducto = entidad.brindaProducto,
            atiende24h = entidad.atiende24Horas,
            visitaADomicilio = entidad.visitaADomicilio,
            realizaEnvios = entidad.realizaEnvios,
            brindaTurnos = entidad.brindaTurnos,
            tipo = TipoPrestador.INDIVIDUAL,
            insignias = PerfilPrestadorInsignia.crearPackEstandar(
                brindaServicio = entidad.brindaServicio,
                brindaProducto = entidad.brindaProducto,
                atiende24h = entidad.atiende24Horas,
                visitaADomicilio = entidad.visitaADomicilio,
                realizaEnvios = entidad.realizaEnvios,
                brindaTurnos = entidad.brindaTurnos,
                tieneLocalFisico = entidad.tieneLocalFisico
            ),
            textoEstado = if (entidad.atiende24Horas) "ABIERTO 24HS ✅" else "CONSULTAR HORARIO 🕒",
            idCategorias = entidad.idCategorias
        )
    }

    /**
     * Mapeo desde Prestador Dominio Completo.
     */
    fun deCompletoAModeloUi(completo: PrestadorDominioCompleto): PrestadorDominio {
        val dirBase = completo.direcciones.find { it.idReferencia == completo.perfil.id } ?: completo.direcciones.firstOrNull()
        return completo.perfil.copy(
            direcciones = completo.direcciones,
            direccionVisible = dirBase?.aTextoCompleto(),
            codigoPostal = dirBase?.codigoPostal,
            horario = completo.horario,
            reseñas = completo.reseñas
        )
    }

    /**
     * Mapeo desde Empresa Dominio Completo.
     */
    fun deEmpresaAModeloUi(completo: EmpresaDominioCompleto, esComercial: Boolean = true): PrestadorDominio {
        val e = completo.empresa
        // [SUPREME.FIX]: Eliminamos .toString() para evitar corrupción de ByteArray [B@...
        val fotoSource = ImageUtils.processImageSource(e.urlFoto)
        val miniSource = ImageUtils.processImageSource(e.urlMiniatura ?: e.urlFoto)

        return PrestadorDominio(
            id = e.id,
            idPropietario = e.idPropietario,
            titulo = e.nombre,
            subtitulo = if (esComercial) "Empresa Verificada" else "Identidad Corporativa",
            urlFoto = fotoSource,
            urlMiniatura = miniSource,
            reputacion = e.reputacion,
            totalReseñas = e.totalReseñas,
            trabajosRealizados = e.trabajosRealizados,
            likes = 0, 
            dislikes = 0,
            nivelElite = e.nivelElite,
            cuitCuil = e.cuit,
            estaVerificado = e.estaVerificada,
            tipo = TipoPrestador.EMPRESA,
            insignias = emptyList(),
            textoEstado = if (esComercial) "EMPRESA ACTIVA ✅" else "PERFIL DE FACTURACIÓN",
            idCategorias = e.idCategorias,
            esPerfilComercial = esComercial,
            horario = null, 
            reseñas = emptyList()
        )
    }

    /**
     * Mapeo desde Sucursal Dominio Completo.
     */
    fun deSucursalAModeloUi(completo: SucursalDominioCompleto, esComercial: Boolean = true): PrestadorDominio {
        val s = completo.sucursal
        val dir = completo.direccion
        
        return PrestadorDominio(
            id = s.id,
            idPropietario = s.idPropietario,
            idEmpresa = s.idEmpresaPadre,
            titulo = s.nombre,
            subtitulo = if (esComercial) "Sucursal Oficial" else "Punto de Gestión",
            reputacion = s.reputacion,
            totalReseñas = s.totalReseñas,
            trabajosRealizados = s.trabajosRealizados,
            likes = s.likes,
            dislikes = s.dislikes,
            direcciones = listOfNotNull(dir),
            direccionVisible = dir?.aTextoCompleto(),
            codigoPostal = dir?.codigoPostal,
            nombreSucursal = s.nombre,
            estaOnline = s.estaEnLinea,
            atiende24h = s.atiende24Horas,
            visitaADomicilio = s.visitaADomicilio,
            realizaEnvios = s.realizaEnvios,
            brindaTurnos = s.brindaTurnos,
            brindaServicio = s.brindaServicio,
            brindaProducto = s.brindaProducto,
            tipo = TipoPrestador.SUCURSAL,
            insignias = if (esComercial) {
                PerfilPrestadorInsignia.crearPackEstandar(
                    brindaServicio = s.brindaServicio,
                    brindaProducto = s.brindaProducto,
                    atiende24h = s.atiende24Horas,
                    visitaADomicilio = s.visitaADomicilio,
                    realizaEnvios = s.realizaEnvios,
                    brindaTurnos = s.brindaTurnos,
                    tieneLocalFisico = true
                )
            } else emptyList(),
            textoEstado = if (esComercial) (if (s.atiende24Horas) "ABIERTO 24HS ✅" else "CONSULTAR HORARIO 🕒") else null,
            esPerfilComercial = esComercial,
            horario = completo.horario,
            reseñas = completo.reseñas
        )
    }

    /**
     * Mapeo desde Identidad Shallow (Documento ligero de Firestore).
     */
    fun deShallowAModeloUi(
        shallow: ResultadoIndiceBusquedaShallowDominio,
        latUsuario: Double? = null,
        lngUsuario: Double? = null
    ): PrestadorDominio {
        val distancia = if (latUsuario != null && lngUsuario != null && shallow.latitud != 0.0) {
            CalculadoraGeografica.calcularDistanciaKm(latUsuario, lngUsuario, shallow.latitud, shallow.longitud)
        } else null

        val miniSource = ImageUtils.processImageSource(shallow.miniaturaBase64 ?: shallow.urlFoto)

        return PrestadorDominio(
            id = shallow.id,
            idPropietario = shallow.idPropietario,
            idEmpresa = shallow.idPadre,
            titulo = shallow.nombreVisible,
            subtitulo = when(shallow.tipoIdentidad) {
                "SUCURSAL" -> shallow.nombreEmpresa ?: "Sucursal Oficial"
                "EMPRESA" -> "Empresa Verificada"
                else -> "Profesional Independiente"
            },
            urlFoto = ImageUtils.processImageSource(shallow.urlFoto), 
            urlMiniatura = miniSource,
            reputacion = shallow.reputacion,
            trabajosRealizados = shallow.trabajosRealizados,
            totalReseñas = shallow.totalReseñas,
            likes = 0, // No disponible en shallow v2026
            dislikes = 0,
            direccionVisible = if (distancia != null) "A ${"%.1f".format(distancia)} km - C.P. ${shallow.codigoPostal}" else "C.P. ${shallow.codigoPostal}",
            distanciaKm = distancia,
            codigoPostal = shallow.codigoPostal,
            estaVerificado = shallow.estaVerificado,
            estaOnline = shallow.estaEnLinea,
            estaSuscrito = shallow.estaSuscrito,
            atiende24h = shallow.atiende24h,
            visitaADomicilio = shallow.visitaADomicilio,
            realizaEnvios = shallow.realizaEnvios,
            tieneLocalFisico = shallow.tieneLocalFisico,
            brindaServicio = shallow.brindaServicio,
            brindaProducto = shallow.brindaProducto,
            brindaTurnos = shallow.brindaTurnos,
            tipo = when(shallow.tipoIdentidad) {
                "EMPRESA" -> TipoPrestador.EMPRESA
                "SUCURSAL" -> TipoPrestador.SUCURSAL
                else -> TipoPrestador.INDIVIDUAL
            },
            insignias = PerfilPrestadorInsignia.crearPackEstandar(
                brindaServicio = shallow.brindaServicio,
                brindaProducto = shallow.brindaProducto,
                atiende24h = shallow.atiende24h,
                tieneLocalFisico = shallow.tieneLocalFisico,
                visitaADomicilio = shallow.visitaADomicilio,
                realizaEnvios = shallow.realizaEnvios,
                brindaTurnos = shallow.brindaTurnos
            ),
            textoEstado = if (shallow.atiende24h) "ABIERTO 24HS ✅" else "CONSULTAR HORARIO 🕒",
            idCategorias = shallow.idCategorias
        )
    }

    fun deDominioAEntidad(p: PrestadorDominio): IdentidadPrestadorEntity {
        return IdentidadPrestadorEntity(
            id = p.id,
            nombre = p.nombre,
            apellido = p.apellido,
            nombreVisible = p.titulo,
            biografia = p.biografia ?: "",
            cuitCuil = p.cuitCuil ?: "",
            correoElectronico = p.correo,
            numeroTelefono = p.numeroTelefono,
            urlFotoPerfil = ImageUtils.prepareForStorage(p.urlFoto),
            miniaturaBase64 = ImageUtils.prepareForStorage(p.urlMiniatura),
            idCategorias = p.idCategorias,
            matricula = p.matricula,
            matriculaFotoUrl = ImageUtils.prepareForStorage(p.matriculaFotoUrl),
            reputacion = p.reputacion,
            totalReseñas = p.totalReseñas,
            trabajosRealizados = p.trabajosRealizados,
            likes = p.likes,
            dislikes = p.dislikes,
            nivelElite = p.nivelElite,
            estaVerificado = p.estaVerificado,
            estaEnLinea = p.estaOnline,
            brindaServicio = p.brindaServicio,
            brindaProducto = p.brindaProducto,
            atiende24Horas = p.atiende24h,
            visitaADomicilio = p.visitaADomicilio,
            realizaEnvios = p.realizaEnvios,
            brindaTurnos = p.brindaTurnos,
            tieneLocalFisico = p.tieneLocalFisico,
            esCargaCompleta = p.esCargaCompleta,
            ultimaSincronizacion = System.currentTimeMillis()
        )
    }

    private fun ReviewEntity.aModelo() = ReseñaDominio(
        id = id,
        idAutor = reviewerId,
        nombreAutor = reviewerName,
        fotoAutorUrl = reviewerPhotoUrl,
        calificacion = rating,
        comentario = text,
        fechaUtc = timestamp,
        respuestaPrestador = response
    )
}




