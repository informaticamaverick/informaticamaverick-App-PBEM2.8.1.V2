package com.example.myapplication.core.datos.local.entidades.vistas

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity

/**
 * --- VISTA RESUMEN DE CONCURSOS (ELITE v2026) ---
 */
@DatabaseView("""
    SELECT 
        c.*,
        (SELECT COUNT(*) FROM presupuestos_finales p WHERE p.idConcurso = c.idConcurso) as totalOfertas,
        (SELECT COUNT(*) FROM presupuestos_finales p WHERE p.idConcurso = c.idConcurso AND p.leido = 0) as ofertasNuevas
    FROM concursos_publicos c
""")
data class ConcursoPublicoResumenSQLView(
    @Embedded val concurso: ConcursoPublicoEntity,
    val totalOfertas: Int,
    val ofertasNuevas: Int
)
