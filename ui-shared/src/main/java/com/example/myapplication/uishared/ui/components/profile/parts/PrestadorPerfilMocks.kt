package com.example.myapplication.uishared.ui.components.profile.parts

import com.example.myapplication.core.dominio.modelos.*

/**
 * --- DATOS MOCK DE PERFIL (Ley #10) ---
 */
object PrestadorPerfilMocks {

    val elenaRodriguez = PrestadorDominio(
        id = "mock_elena",
        idPropietario = "mock_uid_1",
        titulo = "Elena Rodríguez",
        subtitulo = "Especialista en Diseño de Interiores",
        biografia = "Transformo espacios vacíos en hogares llenos de vida con un enfoque minimalista.",
        reputacion = 4.9f,
        totalReseñas = 142,
        trabajosRealizados = 350,
        estaVerificado = true,
        estaOnline = true,
        atiende24h = false,
        visitaADomicilio = true,
        realizaEnvios = false,
        direccionVisible = "Madrid, Centro, España",
        correo = "elena.design@maverick.com",
        numeroTelefono = "+34 600 000 000",
        insignias = listOf(
            PerfilPrestadorInsignia("serv", "🛠️", "Servicios", true),
            PerfilPrestadorInsignia("visit", "🏠", "A Domicilio", true),
            PerfilPrestadorInsignia("date", "📅", "Turnos Online", true)
        )
    )

    val empresaTech = PrestadorDominio(
        id = "mock_empresa",
        idPropietario = "mock_uid_1",
        titulo = "Maverick Tech S.A.",
        subtitulo = "Soluciones Digitales",
        reputacion = 5.0f,
        estaVerificado = true,
        tipo = TipoPrestador.EMPRESA,
        textoEstado = "EMPRESA ACTIVA ✅"
    )
}

































