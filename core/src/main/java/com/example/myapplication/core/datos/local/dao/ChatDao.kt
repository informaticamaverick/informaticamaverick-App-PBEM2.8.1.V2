package com.example.myapplication.core.datos.local.dao

import androidx.room.*
import com.example.myapplication.core.datos.local.entidades.ConversacionEntity
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA CHAT (UNIFICADO 2026) ---
 */
@Dao
interface ChatDao {

    // --- GESTIÓN DE MENSAJES ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMensaje(mensaje: MensajeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMensajes(mensajes: List<MensajeEntity>)

    @Query("SELECT * FROM mensajes WHERE idChat = :idChat ORDER BY marcaTiempo DESC")
    fun obtenerMensajesPorChat(idChat: String): Flow<List<MensajeEntity>>

    @Query("SELECT * FROM mensajes WHERE id = :idMensaje")
    suspend fun obtenerMensajePorId(idMensaje: String): MensajeEntity?

    @Query("SELECT * FROM mensajes WHERE idChat = :idChat ORDER BY marcaTiempo DESC")
    fun obtenerMensajesPaginados(idChat: String): androidx.paging.PagingSource<Int, MensajeEntity>

    @Query("SELECT MAX(marcaTiempo) FROM mensajes WHERE idChat = :idChat")
    suspend fun obtenerUltimaMarcaTiempo(idChat: String): Long?

    @Query("SELECT * FROM mensajes WHERE idChat = :idChat AND tipo = :tipo ORDER BY marcaTiempo DESC")
    fun obtenerMensajesPorTipo(idChat: String, tipo: com.example.myapplication.core.datos.local.entidades.TipoMensaje): Flow<List<MensajeEntity>>

    @Query("""
        SELECT m.* FROM mensajes m
        JOIN conversaciones c ON m.idChat = c.idChat
        WHERE (:idRemoto = 'global' OR (c.idIdentidadRemota = :idRemoto))
        AND (c.idIdentidadLocal = :idLocal)
        AND m.tipo = 'IMAGEN'
        AND (:consulta = '' OR m.contenido LIKE '%' || :consulta || '%')
        ORDER BY m.marcaTiempo DESC
    """)
    fun obtenerImagenesSoberanasPaginadas(
        idLocal: String,
        idRemoto: String,
        consulta: String
    ): androidx.paging.PagingSource<Int, MensajeEntity>

    @Query("""
        SELECT m.id FROM mensajes m
        JOIN conversaciones c ON m.idChat = c.idChat
        WHERE (:idRemoto = 'global' OR (c.idIdentidadRemota = :idRemoto))
        AND (c.idIdentidadLocal = :idLocal)
        AND m.tipo = 'IMAGEN'
        AND (:consulta = '' OR m.contenido LIKE '%' || :consulta || '%')
    """)
    fun obtenerImagenesSoberanasIds(
        idLocal: String,
        idRemoto: String,
        consulta: String
    ): Flow<List<String>>

    @Query("SELECT * FROM mensajes WHERE idChat = :idChat AND tipo = 'PRODUCTO' ORDER BY marcaTiempo DESC")
    fun obtenerSoloProductos(idChat: String): Flow<List<MensajeEntity>>

    @Query("UPDATE mensajes SET estado = 'LEIDO' WHERE idChat = :idChat AND estado != 'LEIDO'")
    suspend fun marcarMensajesComoLeidos(idChat: String)

    // [FIX]: contadorNoLeidos es un campo aparte del estado de cada mensaje — marcar los
    // mensajes como leídos nunca lo reseteaba, así que el contador de la bandeja solo subía
    // y nunca bajaba, aunque el usuario ya hubiera leído todo.
    @Query("UPDATE conversaciones SET contadorNoLeidos = 0 WHERE idChat = :idChat")
    suspend fun resetearContadorNoLeidos(idChat: String)

    // --- GESTIÓN DE CONVERSACIONES ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizarConversacion(conversacion: ConversacionEntity)

    @Query("SELECT * FROM conversaciones WHERE idIdentidadLocal = :idIdentidadLocal ORDER BY fechaUltimoMensaje DESC")
    fun obtenerConversacionesPorIdentidad(idIdentidadLocal: String): Flow<List<ConversacionEntity>>

    @Query("SELECT * FROM conversaciones")
    suspend fun obtenerTodasLasConversacionesSync(): List<ConversacionEntity>

    @Query("SELECT * FROM conversaciones WHERE idIdentidadLocal = :idIdentidadLocal")
    suspend fun obtenerListaConversacionesSync(idIdentidadLocal: String): List<ConversacionEntity>

    @Query("SELECT * FROM conversaciones WHERE idChat = :idChat")
    suspend fun obtenerConversacionPorId(idChat: String): ConversacionEntity?

    @Query("""
        UPDATE conversaciones 
        SET ultimoMensaje = :mensaje, 
            fechaUltimoMensaje = :fecha, 
            tipoUltimoMensaje = :tipo,
            contadorNoLeidos = CASE WHEN idIdentidadLocal = :idReceptor THEN contadorNoLeidos + 1 ELSE contadorNoLeidos END
        WHERE idChat = :idChat
    """)
    suspend fun actualizarResumenConversacion(idChat: String, mensaje: String, fecha: Long, tipo: String, idReceptor: String)

    @Query("DELETE FROM conversaciones WHERE idChat = :idChat")
    suspend fun eliminarConversacion(idChat: String)

    @Query("DELETE FROM mensajes WHERE id = :idMensaje")
    suspend fun eliminarMensajePorId(idMensaje: String)

    @Query("DELETE FROM mensajes WHERE idChat = :idChat")
    suspend fun eliminarMensajesDeChat(idChat: String)

    @Query("""
        SELECT c.* FROM conversaciones c
        JOIN conversaciones_fts fts ON c.rowid = fts.rowid
        WHERE conversaciones_fts MATCH :consulta
    """)
    fun buscarConversacionesMatch(consulta: String): Flow<List<ConversacionEntity>>

    @Query("SELECT idChat, COUNT(*) as count FROM mensajes WHERE idReceptor = :idLocal AND estado != 'LEIDO' GROUP BY idChat")
    fun obtenerConteosNoLeidosPorChat(idLocal: String): Flow<List<ConteoNoLeidos>>

    @Query("""
        SELECT v.* FROM ConversacionResumenSQLView v
        WHERE v.idIdentidadLocal = :idLocal
        AND (:consulta = '' OR v.rowid IN (SELECT rowid FROM conversaciones_fts WHERE conversaciones_fts MATCH :consulta))
        AND (:soloNoLeidos = 0 OR v.contadorNoLeidos > 0)
        AND (:soloOnline = 0 OR v.estaOnlineSoberano = 1)
        AND (:soloVerificados = 0 OR v.estaVerificadoSoberano = 1)
        AND (:idCategoria IS NULL OR v.idsCategoriasSoberanas LIKE '%"' || :idCategoria || '"%')
        ORDER BY 
            CASE WHEN :orden = 'sort_date' THEN v.fechaUltimoMensaje END DESC,
            CASE WHEN :orden = 'sort_alpha' THEN v.nombreSoberano END ASC,
            v.fechaUltimoMensaje DESC
    """)
    fun buscarConversacionesSoberanas(
        idLocal: String,
        consulta: String,
        soloNoLeidos: Boolean,
        soloOnline: Boolean,
        soloVerificados: Boolean,
        idCategoria: String?,
        orden: String
    ): Flow<List<com.example.myapplication.core.datos.local.entidades.vistas.ConversacionResumenSQLView>>

    @Query("SELECT COUNT(*) FROM mensajes WHERE idReceptor = :idLocal AND estado != 'LEIDO'")
    fun obtenerTotalNoLeidos(idLocal: String): Flow<Long>

    @Query("SELECT COUNT(*) FROM mensajes WHERE idPropietarioReceptor = :idPropietario AND estado != 'LEIDO'")
    fun obtenerTotalNoLeidosGlobal(idPropietario: String): Flow<Int>

    @Query("SELECT * FROM mensajes WHERE idChat = :idChat AND esMio = 0 AND estado != 'LEIDO' AND (tipo = 'IMAGEN' OR tipo = 'AUDIO' OR tipo = 'PRESUPUESTO')")
    suspend fun obtenerMultimediaNoLeidosDeOtro(idChat: String): List<MensajeEntity>
}

data class ConteoNoLeidos(
    val idChat: String,
    val count: Long
)
