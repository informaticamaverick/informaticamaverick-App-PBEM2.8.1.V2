package com.example.myapplication.core.dominio.mapeadores

import com.example.myapplication.core.datos.local.entidades.HorarioEntity
import com.example.myapplication.core.dominio.modelos.HorarioDominio

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: Mapeador de Horario
 * [PROPÓSITO]: Convertir entre la entidad de persistencia 'HorarioEntity' y el modelo 'HorarioDominio'.
 * [FUNCIONAMIENTO INTERNO]: Métodos estáticos de transformación bidireccional.
 * [RELACIÓN]: Fundamental para mantener la separación entre Room y la lógica de negocio.
 */
object HorarioMappers {

    fun deEntidadAModelo(entidad: HorarioEntity): HorarioDominio {
        return HorarioDominio(
            lunes = entidad.lunes,
            martes = entidad.martes,
            miercoles = entidad.miercoles,
            jueves = entidad.jueves,
            viernes = entidad.viernes,
            sabado = entidad.sabado,
            domingo = entidad.domingo,
            zonaHoraria = entidad.zonaHoraria
        )
    }

    fun deModeloAEntidad(
        modelo: HorarioDominio,
        idReferencia: String,
        idReferenciaPadre: String? = null,
        idSucursal: String? = null,
        tipo: com.example.myapplication.core.datos.local.entidades.TipoHorario = com.example.myapplication.core.datos.local.entidades.TipoHorario.Horario_Atencion
    ): HorarioEntity {
        return HorarioEntity(
            // [FIX]: los @Relation que arman el perfil (PrestadorCompletoRelacionesBD,
            // SucursalCompletaRelacionesBD) buscan el horario por "idPropietario" — este mapper
            // solo completaba "idReferencia" (el campo "de compatibilidad"), así que la fila se
            // guardaba bien pero la consulta reactiva de Perfil nunca la encontraba (siempre
            // null), aunque horarioDao.eliminarPorReferencia sí la borrara correctamente al
            // filtrar por idReferencia. Se completan los dos para que ambos caminos funcionen.
            idPropietario = idReferencia,
            idReferencia = idReferencia,
            idReferenciaPadre = idReferenciaPadre,
            idSucursal = idSucursal,
            tipo = tipo,
            lunes = modelo.lunes,
            martes = modelo.martes,
            miercoles = modelo.miercoles,
            jueves = modelo.jueves,
            viernes = modelo.viernes,
            sabado = modelo.sabado,
            domingo = modelo.domingo,
            zonaHoraria = modelo.zonaHoraria
        )
    }
}



