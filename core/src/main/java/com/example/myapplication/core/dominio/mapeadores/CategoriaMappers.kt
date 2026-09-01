package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.vistas.CategoriaResumenSQLView
import com.example.myapplication.core.dominio.modelos.CategoriaDominio

/**
 * --- CATEGORÍA MAPPER (ELITE v2026.FINAL) ---
 * [ELITE SSOT]: Centraliza la transformación de Rubros con Herencia de Color.
 */
object CategoriaMappers {

    /**
     * Transforma una entidad en un modelo de Dominio.
     */
    fun deEntidadADominio(
        entidad: CategoriaEntity, 
        nombreSuper: String = "Otros",
        colorHeredado: Long = 0xFF1A1F26
    ): CategoriaDominio {
        return CategoriaDominio(
            id = entidad.id,
            nombre = entidad.nombre,
            icono = entidad.icono,
            idSuperCategoria = entidad.idSuperCategoria,
            superCategoria = nombreSuper,
            descripcion = entidad.descripcion,
            esNueva = entidad.esNueva,
            color = colorHeredado
        )
    }

    /**
     * 🔥 [ELITE]: Transforma una vista SQL en un modelo de Dominio (Sin overhead).
     */
    fun deVistaADominio(vista: CategoriaResumenSQLView): CategoriaDominio {
        return CategoriaDominio(
            id = vista.id,
            nombre = vista.nombre,
            icono = vista.icono,
            idSuperCategoria = vista.idSuperCategoria,
            superCategoria = vista.superCategoriaNombre,
            descripcion = vista.descripcion,
            esNueva = vista.esNueva,
            color = vista.superCategoriaColor
        )
    }

    /**
     * Transforma un modelo de Dominio de vuelta a su entidad base.
     */
    fun deDominioAEntidad(ui: CategoriaDominio): CategoriaEntity {
        return CategoriaEntity(
            id = ui.id,
            nombre = ui.nombre,
            icono = ui.icono,
            idSuperCategoria = ui.idSuperCategoria,
            descripcion = ui.descripcion,
            esNueva = ui.esNueva
        )
    }
}




































