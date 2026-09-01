package com.example.myapplication.core.dominio.modelos

import android.net.Uri

/**
 * --- ESTADO DE EDICIÓN DE PERFIL (UI) ---
 * [LEY #9]: Estándar Mav en Español.
 */
data class EstadoEdicionPerfil(
    val id: String = "",
    val tipoIdentidad: String = "USUARIO",
    val nombreVisible: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val nombreComercial: String = "",
    val biografia: String = "",
    val cuitCuil: String = "",
    val correoContacto: String = "",
    val numeroTelefono: String = "",
    val especialidades: String = "",
    val matricula: String = "",
    val capacidadSimultanea: Int = 1,
    val categorias: List<String> = emptyList(),
    val direccion: DireccionDominio = DireccionDominio(),
    val direccionesAdicionales: List<DireccionDominio> = emptyList(),
    val urlFotoPerfil: String? = null,
    val miniaturaBase64: String? = null,
    val brindaServicio: Boolean = false,
    val brindaProducto: Boolean = false,
    val atiende24Horas: Boolean = false,
    val visitaADomicilio: Boolean = false,
    val realizaEnvios: Boolean = false,
    val brindaTurnos: Boolean = false,
    val usaAgendaRecursos: Boolean = false,
    val horarios: HorarioDominio? = null,
    val recursos: List<RecursoDominio> = emptyList(),
    val equipoTrabajo: List<EquipoTrabajoDominio> = emptyList(),
    val priorizarEmpresa: Boolean = false,
    val nuevaFotoLocalUri: Uri? = null
)


































