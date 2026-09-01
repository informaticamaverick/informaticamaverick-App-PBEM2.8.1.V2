package com.example.myapplication.core.dominio.modelos

import com.example.myapplication.core.datos.local.entidades.CuentaEntity
import androidx.annotation.Keep

/**
 * --- MODELO DE PERFIL PRESTADOR DEEP (SSOT v2026.ELITE) ---
 * [PROPÓSITO]: Representar el ecosistema profesional completo (Los 5 Pilares) 
 * para procesos de descarga profunda y visualización detallada.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
data class PerfilPrestadorDeepModelo(
    val cuenta: CuentaEntity,
    val prestador: PrestadorDominioCompleto,
    val empresas: List<EmpresaDominioCompleto> = emptyList()
) {
    /**
     * 🔥 [ELITE]: Aplana la jerarquía profunda en una lista de identidades consumibles por la UI.
     */
    fun aModelosUi(): List<PrestadorDominio> {
        val lista = mutableListOf<PrestadorDominio>()
        
        // 1. Identidad Profesional Base
        val identidadBase = com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deCompletoAModeloUi(prestador)
        lista.add(identidadBase.copy(
            correo = if (identidadBase.correo.isBlank()) cuenta.correoGoogle else identidadBase.correo,
            esGoogle = cuenta.correoGoogle.isNotBlank(),
            estaSuscrito = cuenta.estaSuscrito
        ))
        
        // 2. Empresas y sus Sucursales
        empresas.forEach { empComp ->
            lista.add(com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deEmpresaAModeloUi(empComp))
            empComp.sucursales.forEach { sucComp ->
                lista.add(com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deSucursalAModeloUi(sucComp))
            }
        }
        
        return lista
    }
}



