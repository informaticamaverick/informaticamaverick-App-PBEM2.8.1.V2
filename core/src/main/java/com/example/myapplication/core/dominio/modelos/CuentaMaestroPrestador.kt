/*
package com.example.myapplication.core.dominio.modelos

import com.example.myapplication.core.datos.local.entidades.CuentaEntity

/**
 * --- CUENTA MAESTRO PRESTADOR (SSOT Profesional) ---
 * [LEY #9]: Estándar Mav (Idioma Español).
 * Objeto definitivo para la App del Prestador.
 * Aísla completamente el ecosistema de negocios del perfil de usuario personal.
 */
data class CuentaMaestroPrestador(
    val cuenta: CuentaEntity,
    val prestador: PrestadorDominioCompleto,
    val empresas: List<EmpresaDominioCompleto> = emptyList()
) {
    /**
     * [ELITE MAPPING]: Convierte la jerarquía del prestador a la lista plana de identidades.
     * Sincronizado con el protocolo Multi-Identidad de ui-shared.
     */
    fun aModelosUi(): List<PrestadorDominio> {
        val lista = mutableListOf<PrestadorDominio>()
        
        // 1. Identidad Profesional Base (Root)
        val identidadBase = com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deCompletoAModeloUi(prestador)
        lista.add(identidadBase.copy(
            correo = if (identidadBase.correo.isBlank()) cuenta.correoGoogle else identidadBase.correo,
            esGoogle = cuenta.correoGoogle.isNotBlank(),
            estaSuscrito = cuenta.estaSuscrito
        ))
        
        // 2. Empresas y Sucursales vinculadas
        empresas.forEach { empComp ->
            lista.add(com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deEmpresaAModeloUi(empComp))
            
            empComp.sucursales.forEach { sucComp ->
                lista.add(com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deSucursalAModeloUi(sucComp))
            }
        }
        
        return lista
    }
}
*/



