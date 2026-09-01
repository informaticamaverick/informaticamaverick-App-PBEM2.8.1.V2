package com.example.myapplication.core.dominio.motores

import androidx.room.withTransaction
import com.example.myapplication.core.datos.local.AppDatabase
import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.mapeadores.EventoMappers
import com.example.myapplication.core.utilidades.CompresorPresupuesto
import com.example.myapplication.core.datos.repositorios.EventoRepositorio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE PERSISTENCIA DE CHAT (V2026.ELITE) ---
 * [RESPONSABILIDAD]: Único punto de impacto en Room para datos de mensajería.
 * [INTEGRIDAD]: Maneja la promoción atómica a Agenda y Transacciones Comerciales.
 */
@Singleton
class ChatMotorSincLocal @Inject constructor(
    private val db: AppDatabase,
    private val auth: com.google.firebase.auth.FirebaseAuth, // 🔥 [ELITE] Para identificar identidad local
    private val motorSincCuentas: MotorSincLocal,
    private val repositorioEvento: EventoRepositorio
) {
    private val chatDao = db.ChatDao()
    private val presupuestoFinalDao = db.presupuestoFinalDao()
    private val usuarioDao = db.usuarioDao()
    private val prestadorDao = db.prestadorDao()
    private val sucursalDao = db.sucursalDao()

    /**
     * 🔥 [ELITE]: Guarda un mensaje y sincroniza todo el ecosistema relacionado.
     */
    suspend fun impactarMensaje(mensaje: MensajeEntity) {
        android.util.Log.d("CHAT_AUDIT_LOCAL", "📦 [REPO -> ROOM] Intentando impactar mensaje ID: ${mensaje.id} en chat ${mensaje.idChat}")
        db.withTransaction {
            // 1. Persistir el mensaje base
            chatDao.insertarMensaje(mensaje)

            // 2. Resolver y asegurar identidad remota en Room (Ley #1 SSOT)
            asegurarIdentidadRemota(mensaje)

            // 3. Promoción Atómica: Operativo (Turnos/Visitas)
            EventoMappers.deMensajeAEntidad(mensaje)?.let { evento ->
                repositorioEvento.insertar(evento)
                android.util.Log.d("ChatMotorLocal", "🗓️ [AGENDA_PROMO] Evento sincronizado: ${evento.id}")
            }

            // 4. Promoción Atómica: Comercial (Presupuestos)
            if (mensaje.tipo == TipoMensaje.PRESUPUESTO) {
                CompresorPresupuesto.descomprimir(mensaje.contenido)?.let { pRel ->
                    presupuestoFinalDao.guardarPresupuestoCompleto(pRel.cabecera, pRel.lineas, pRel.finanzas)
                    android.util.Log.i("ChatMotorLocal", "💰 [ELITE_AUDIT_TRAIL] [PERSISTED] [BUDGET] ID: ${pRel.cabecera.idPresupuesto} | Líneas: ${pRel.lineas.size} | Finanzas: ${pRel.finanzas.size}")
                }
            }

            // 5. Actualizar Resumen de Bandeja
            actualizarResumenBandeja(mensaje)
        }
    }

    /**
     * Asegura que el contacto exista en Room para evitar "Usuario Maverick" en la UI.
     */
    private suspend fun asegurarIdentidadRemota(mensaje: MensajeEntity) {
        val miUid = auth.currentUser?.uid ?: ""
        
        // --- LÓGICA DE IDENTIDAD REMOTA (SSOT v2026) ---
        // Si yo soy el dueño del emisor, el remoto es el receptor.
        // Si no soy el dueño del emisor, el remoto es el emisor.
        val idRemoto = if (mensaje.idPropietarioEmisor == miUid) {
            mensaje.idReceptor 
        } else {
            mensaje.idEmisor
        }
        
        if (idRemoto.isEmpty() || idRemoto == "SISTEMA" || idRemoto.startsWith("SISTEMA_")) return

        if (prestadorDao.obtenerPorIdSync(idRemoto) == null && 
            sucursalDao.obtenerPorIdSync(idRemoto) == null && 
            usuarioDao.obtenerPorIdSync(idRemoto) == null) {
            
            android.util.Log.d("ChatMotorLocal", "☁️ [FETCH_ID] Identidad $idRemoto no encontrada. Disparando Pull Shallow.")
            motorSincCuentas.impactarPrestadorShallow(idRemoto)
            motorSincCuentas.impactarUsuarioShallow(idRemoto)
        }
    }

    /**
     * Mantiene la tabla de conversaciones al día con el último mensaje.
     */
    private suspend fun actualizarResumenBandeja(mensaje: MensajeEntity) {
        val miUid = auth.currentUser?.uid ?: ""
        
        // --- CÁLCULO DE SOBERANÍA (ELITE v2026) ---
        val idLocalBandeja = if (mensaje.idPropietarioEmisor == miUid) {
            mensaje.idEmisor 
        } else if (mensaje.idPropietarioReceptor == miUid) {
            mensaje.idReceptor
        } else {
            if (mensaje.idEmisor.startsWith("P_") || mensaje.idEmisor.length > 20) mensaje.idReceptor else mensaje.idEmisor
        }

        val idRemoto = if (mensaje.idEmisor == idLocalBandeja) mensaje.idReceptor else mensaje.idEmisor
        val conversacionExistente = chatDao.obtenerConversacionPorId(mensaje.idChat)

        // 🔥 [ELITE v2026]: Si es un mensaje de SISTEMA, solo actualizamos el contenido.
        if (mensaje.idEmisor == "SISTEMA" || mensaje.idPropietarioEmisor == "SISTEMA" || 
            mensaje.idEmisor.startsWith("SISTEMA_") || mensaje.idPropietarioEmisor.startsWith("SISTEMA_")) {
            conversacionExistente?.let {
                val resumen = it.copy(
                    ultimoMensaje = mensaje.contenido,
                    fechaUltimoMensaje = mensaje.marcaTiempo,
                    tipoUltimoMensaje = mensaje.tipo.name
                )
                chatDao.insertarOActualizarConversacion(resumen)
            }
            return
        }

        // [v2026.ELITE]: Minimalismo en el impacto. 
        // Delegamos la resolución de metadatos a ConversacionResumenSQLView (SSOT).
        // Solo guardamos datos efímeros o de referencia para FTS.
        val resumen = ConversacionEntity(
            idChat = mensaje.idChat,
            idIdentidadLocal = idLocalBandeja,
            idIdentidadRemota = idRemoto,
            ultimoMensaje = mensaje.contenido,
            fechaUltimoMensaje = mensaje.marcaTiempo,
            tipoUltimoMensaje = mensaje.tipo.name,
            nombreRemoto = conversacionExistente?.nombreRemoto ?: "", 
            fotoRemotaUrl = conversacionExistente?.fotoRemotaUrl,
            miniaturaRemotaBase64 = conversacionExistente?.miniaturaRemotaBase64,
            idCategoriaRemota = conversacionExistente?.idCategoriaRemota,
            contadorNoLeidos = if (mensaje.idReceptor == idLocalBandeja && mensaje.idEmisor != idLocalBandeja) {
                (conversacionExistente?.contadorNoLeidos ?: 0) + 1 
            } else {
                conversacionExistente?.contadorNoLeidos ?: 0
            }
        )

        chatDao.insertarOActualizarConversacion(resumen)
        android.util.Log.d("CHAT_AUDIT_LOCAL", "🗂️ [ROOM_UPDATE] Resumen actualizado para chat ${mensaje.idChat}. Identidad delegada a SQL View.")
    }

    suspend fun marcarHiloComoLeido(idChat: String) {
        chatDao.marcarMensajesComoLeidos(idChat)
    }

    suspend fun eliminarHilo(idChat: String) {
        chatDao.eliminarConversacion(idChat)
    }
}
