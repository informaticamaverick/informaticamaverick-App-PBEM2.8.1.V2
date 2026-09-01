package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep
import com.example.myapplication.core.datos.local.entidades.CuentaEntity

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Cuenta Maestro Usuario (SSOT Cliente)
 * [PROPÓSITO]: Aísla el perfil personal y direcciones de envío del ecosistema profesional.
 */
@Keep
data class CuentaMaestroUsuario(
    val cuenta: CuentaEntity,
    val usuario: UsuarioDominioCompleto,
    val empresas: List<EmpresaDominioCompleto> = emptyList()
) {
    /**
     * [ELITE MAPPING]: Convierte la jerarquía de usuario a la lista plana de identidades
     * requerida por el HorizontalPager del perfil.
     */
    fun aModelosUi(): List<PrestadorDominio> {
        val lista = mutableListOf<PrestadorDominio>()
        
        // 1. Identidad Personal (Root) - Usamos el nuevo mapper táctico
        lista.add(com.example.myapplication.core.dominio.mapeadores.UsuarioMappers.deDominioAPrestadorUi(usuario.perfil, cuenta.estaSuscrito))
        
        // 2. Identidades Empresariales y Sucursales (Filtradas como No Comerciales)
        empresas.forEach { empComp ->
            lista.add(com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deEmpresaAModeloUi(empComp, esComercial = false))
            
            empComp.sucursales.forEach { sucComp ->
                lista.add(com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deSucursalAModeloUi(sucComp, esComercial = false))
            }
        }
        
        return lista
    }
}

/* 
Eliminado bridge táctico local en favor de UsuarioMappers.deEntidadAPrestadorUi
*/





































