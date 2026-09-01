package com.example.myapplication.prestador.datos.local.dao

import androidx.room.Dao
import com.example.myapplication.core.datos.local.dao.ExcepcionHorariaDao

/**
 * Extensión del DAO de Excepciones Horarias para evitar conflictos de duplicidad de clases
 * generadas en el módulo :core.
 */
@Dao
interface PrestadorExcepcionHorariaDao : ExcepcionHorariaDao
