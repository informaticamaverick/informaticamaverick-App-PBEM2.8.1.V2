package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.dao.SuperCategoriaShallow
import com.example.myapplication.core.dominio.modelos.SuperCategoriaDominio

/**
 * --- SUPER CATEGORÍA MAPPER (ELITE v2026.8) ---
 * [ELITE SSOT]: Centraliza la transformación de Carpetas Bento.
 */
object SuperCategoriaMappers {

    fun aUiModel(shallow: SuperCategoriaShallow): SuperCategoriaDominio {
        return SuperCategoriaDominio(
            id = shallow.id,
            titulo = shallow.titulo,
            icono = shallow.icono,
            color = shallow.color,
            totalItems = shallow.totalItems
        )
    }
}




































