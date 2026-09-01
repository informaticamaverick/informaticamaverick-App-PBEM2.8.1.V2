package com.example.myapplication.core.dominio.modelos

/**
 * [LEY #9]: MODELO DE DOMINIO SOBERANO (v2026.FINAL)
 * 
 * PROPÓSITO: Representar un hilo de conversación en la bandeja de entrada
 * de forma agnóstica a la persistencia o la UI.
 */
data class ConversacionHiloMDominio(
    val idChat: String,
    val idUsuarioRemoto: String,
    val nombreVisible: String,
    val urlFoto: String?,
    val urlMiniatura: String? = null,
    val ultimoMensaje: String,
    val marcaTiempoUltimo: Long,
    val estaOnline: Boolean,
    val estaVerificado: Boolean,
    val idIdentidadLocal: String,
    val idSucursalRemota: String?,
    val idCategoriaPrincipal: String?,
    val conteoNoLeidos: Int = 0,
    val identidadCompleta: PrestadorDominio // SSOT del prestador/contacto
)
