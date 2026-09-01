package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.dominio.modelos.PerfilPrestadorDeepModelo
import com.example.myapplication.core.datos.local.entidades.CuentaEntity
import com.example.myapplication.core.dominio.modelos.PrestadorDominioCompleto
import com.example.myapplication.core.dominio.modelos.EmpresaDominioCompleto

/**
 * --- MAPPER PERFIL PRESTADOR DEEP (v2026.ELITE) ---
 * [PROPÓSITO]: Transformar el ecosistema completo desde diferentes fuentes centralizadas.
 */
object PerfilPrestadorDeepMappers {

    /**
     * 🔥 [ELITE]: Crea el Modelo Deep compartido desde sus componentes atómicos.
     */
    fun deComponentesADeep(
        cuenta: CuentaEntity,
        prestador: PrestadorDominioCompleto,
        empresas: List<EmpresaDominioCompleto>
    ): PerfilPrestadorDeepModelo {
        return PerfilPrestadorDeepModelo(
            cuenta = cuenta,
            prestador = prestador,
            empresas = empresas
        )
    }
}




