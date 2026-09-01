package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.mapeadores.MensajeMappers
import com.example.myapplication.core.servicios.notificaciones.Notificador
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.core.dominio.motores.ChatMotorSincLocal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE SINCRONIZACIÓN DE CHAT (RED - V2026.ELITE) ---
 * [RESPONSABILIDAD]: Punto único de entrada para conexión con Firebase RTDB.
 * [REQUISITO]: Delegar toda la persistencia a ChatMotorSincLocal.
 */
@Singleton
class ChatMotorSincRepositorio @Inject constructor(
    private val chatDao: ChatDao, // Mantenemos temporalmente para flujos de lectura de UI
    private val auth: FirebaseAuth,
    private val motorLocal: ChatMotorSincLocal,
    private val notificador: Notificador,
    @dagger.hilt.android.qualifiers.ApplicationContext private val contexto: android.content.Context
) {
    private val nubeRealtime = FirebaseDatabase.getInstance().reference
    private val listenersActivos = mutableMapOf<String, ChildEventListener>()
    private val identidadesEscuchadas = mutableSetOf<String>() // 🔥 [NEW v2026.ELITE]
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val horaInicioSesion = System.currentTimeMillis()

    fun obtenerConversaciones(idIdentidadLocal: String): Flow<List<ConversacionEntity>> =
        chatDao.obtenerConversacionesPorIdentidad(idIdentidadLocal)

    /**
     * 🔥 [ELITE]: Obtiene un flujo paginado de mensajes (Ley #3).
     */
    fun obtenerFlujoMensajesPaginados(idChat: String): Flow<PagingData<MensajeEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false, prefetchDistance = 5),
            pagingSourceFactory = { chatDao.obtenerMensajesPaginados(idChat) }
        ).flow
    }

    fun obtenerSoloImagenes(idChat: String): Flow<List<MensajeEntity>> =
        chatDao.obtenerMensajesPorTipo(idChat, TipoMensaje.IMAGEN)

    fun obtenerSoloUbicaciones(idChat: String): Flow<List<MensajeEntity>> =
        chatDao.obtenerMensajesPorTipo(idChat, TipoMensaje.UBICACION)

    fun obtenerSoloProductos(idChat: String): Flow<List<MensajeEntity>> =
        chatDao.obtenerSoloProductos(idChat)

    fun obtenerConteoNoLeidosGlobal(idPropietario: String): Flow<Int> =
        chatDao.obtenerTotalNoLeidosGlobal(idPropietario)

    fun obtenerConteosNoLeidos(idLocal: String): Flow<List<ConteoNoLeidos>> =
        chatDao.obtenerConteosNoLeidosPorChat(idLocal)

    fun obtenerConversacionesSoberanas(
        idLocal: String,
        consulta: String,
        soloNoLeidos: Boolean,
        soloOnline: Boolean,
        soloVerificados: Boolean,
        idCategoria: String?,
        orden: String
    ) = chatDao.buscarConversacionesSoberanas(idLocal, consulta, soloNoLeidos, soloOnline, soloVerificados, idCategoria, orden)

    fun obtenerImagenesSoberanasPaginadas(
        idLocal: String,
        idRemoto: String,
        consulta: String
    ) = chatDao.obtenerImagenesSoberanasPaginadas(idLocal, idRemoto, consulta)

    fun obtenerImagenesSoberanasIds(
        idLocal: String,
        idRemoto: String,
        consulta: String
    ) = chatDao.obtenerImagenesSoberanasIds(idLocal, idRemoto, consulta)

    suspend fun eliminarConversacion(idChat: String) {
        motorLocal.eliminarHilo(idChat)
    }

    suspend fun eliminarMensaje(idMensaje: String) {
        chatDao.eliminarMensajePorId(idMensaje)
    }

    /**
     * 🔥 [ELITE]: Activa la escucha en tiempo real para un chat específico.
     */
    fun observarChat(idChat: String) {
        if (listenersActivos.containsKey(idChat)) return

        ioScope.launch {
            // [REPARACIÓN]: Eliminamos startAfter para permitir que onChildChanged capture 
            // actualizaciones de estado en mensajes existentes.
            val query = nubeRealtime.child("chats").child(idChat)
                .orderByChild("fechaEnvio")
                .limitToLast(100)

            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    procesarSnapshotMensaje(snapshot, idChat)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    procesarSnapshotMensaje(snapshot, idChat)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("ChatMotorRed", "❌ [RTDB_ERROR] Fallo en hilo $idChat: ${error.message}")
                }
            }

            query.addChildEventListener(listener)
            listenersActivos[idChat] = listener
        }
    }

    private fun procesarSnapshotMensaje(snapshot: DataSnapshot, idChat: String) {
        val rawData = snapshot.value?.toString() ?: ""
        val pesoBytes = rawData.length // Estimación rápida Ley #7
        
        val mensaje = MensajeMappers.mapearDesdeFirebase(snapshot, idChat, contexto)
        mensaje?.let { 
            android.util.Log.i("CHAT_AUDIT_RED", "☁️ [FIREBASE -> REPO] Recibido mensaje [${it.tipo}] ID: ${it.id} en chat: $idChat | PESO: $pesoBytes bytes")
            ioScope.launch {
                // DELEGACIÓN AL MOTOR LOCAL (BIG LEAGUE)
                motorLocal.impactarMensaje(it)
                
                // Notificación reactiva
                val miUid = auth.currentUser?.uid ?: ""
                if (it.idReceptor == miUid && it.marcaTiempo > (horaInicioSesion - 1000)) {
                    // Buscamos nombre de remitente desde el DAO para no bloquear el hilo de red
                    val remitente = chatDao.obtenerConversacionPorId(idChat)?.nombreRemoto ?: "App"
                    notificador.mostrarAvisoChat(idChat, remitente, it.contenido)
                }
            }
        }
    }

    fun detenerObservacionChat(idChat: String) {
        listenersActivos.remove(idChat)?.let { 
            nubeRealtime.child("chats").child(idChat).removeEventListener(it)
        }
    }

    suspend fun enviarMensajeTexto(
        idChat: String,
        emisor: String,
        receptor: String,
        propietarioEmisor: String,
        propietarioReceptor: String,
        texto: String,
        respondidoAId: String? = null,
        respondidoAContenido: String? = null
    ): Result<Unit> = try {
        val mensaje = MensajeEntity(
            id = UUID.randomUUID().toString(),
            idChat = idChat,
            idEmisor = emisor,
            idReceptor = receptor,
            idPropietarioEmisor = propietarioEmisor,
            idPropietarioReceptor = propietarioReceptor,
            tipo = TipoMensaje.TEXTO,
            contenido = texto,
            respondidoAId = respondidoAId,
            respondidoAContenido = respondidoAContenido,
            estado = EstadoMensaje.ENVIANDO
        )

        // 1. Impacto Local Inmediato
        motorLocal.impactarMensaje(mensaje)

        // 2. Envío a la Nube
        val mapaMensaje = mutableMapOf<String, Any?>(
            "id" to mensaje.id,
            "idChat" to idChat,
            "idEmisor" to mensaje.idEmisor,
            "idReceptor" to mensaje.idReceptor,
            "idPropietarioEmisor" to mensaje.idPropietarioEmisor,
            "idPropietarioReceptor" to mensaje.idPropietarioReceptor,
            "tipo" to mensaje.tipo.name,
            "contenido" to mensaje.contenido,
            "fechaEnvio" to ServerValue.TIMESTAMP,
            "esLeido" to false
        )
        respondidoAId?.let { mapaMensaje["respondidoAId"] = it }
        respondidoAContenido?.let { mapaMensaje["respondidoAContenido"] = it }

        val pesoBytes = mapaMensaje.toString().length // Estimación táctica
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [TEXTO] | ID: ${mensaje.id} | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(mensaje.id).setValue(mapaMensaje).await()
        nubeRealtime.child("inbox_signals").child(receptor).child(idChat).setValue(true)

        // 3. Confirmar estado local
        motorLocal.impactarMensaje(mensaje.copy(estado = EstadoMensaje.ENTREGADO))
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun enviarMensajeImagen(idChat: String, emisor: String, receptor: String, uriLocal: String): Result<Unit> = try {
        val uri = android.net.Uri.parse(uriLocal)
        val compressedBytes = ImageUtils.compressElite(contexto, uri)
        val base64 = compressedBytes?.let { ImageUtils.bytesToBase64(it) } ?: ""
        val miniatura = compressedBytes?.let { ImageUtils.generateThumbnailFromBytes(it) }

        val idMensaje = UUID.randomUUID().toString()
        val rutaLocalPersistente = compressedBytes?.let {
            ImageUtils.saveBytesToFile(contexto, it, idMensaje, "IMG_", ".webp")
        } ?: uriLocal

        val mensaje = MensajeEntity(
            id = idMensaje, idChat = idChat, idEmisor = emisor, idReceptor = receptor,
            idPropietarioEmisor = auth.currentUser?.uid ?: emisor, idPropietarioReceptor = receptor,
            tipo = TipoMensaje.IMAGEN, contenido = "[Imagen]", urlMedia = rutaLocalPersistente,
            miniaturaBase64 = miniatura, estado = EstadoMensaje.ENVIANDO
        )

        motorLocal.impactarMensaje(mensaje)

        val mapaMensaje = mapOf(
            "id" to mensaje.id, "idChat" to idChat, "idEmisor" to emisor, "idReceptor" to receptor,
            "idPropietarioEmisor" to mensaje.idPropietarioEmisor, "idPropietarioReceptor" to receptor,
            "tipo" to mensaje.tipo.name, "contenido" to base64, "miniaturaBase64" to miniatura,
            "fechaEnvio" to ServerValue.TIMESTAMP, "esLeido" to false
        )

        val pesoBytes = mapaMensaje.toString().length
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [IMAGEN] | ID: ${mensaje.id} | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(mensaje.id).setValue(mapaMensaje).await()
        motorLocal.impactarMensaje(mensaje.copy(estado = EstadoMensaje.ENTREGADO))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun enviarMensajeUbicacion(
        idChat: String, emisor: String, receptor: String, lat: Double, lng: Double, direccion: String
    ): Result<Unit> = try {
        val mensaje = MensajeEntity(
            id = UUID.randomUUID().toString(), idChat = idChat, idEmisor = emisor, idReceptor = receptor,
            idPropietarioEmisor = auth.currentUser?.uid ?: emisor, idPropietarioReceptor = receptor,
            tipo = TipoMensaje.UBICACION, contenido = "[Ubicación]", latitud = lat, longitud = lng,
            direccionTexto = direccion, estado = EstadoMensaje.ENVIANDO
        )
        motorLocal.impactarMensaje(mensaje)

        val mapaMensaje = mapOf(
            "id" to mensaje.id, "idChat" to idChat, "idEmisor" to emisor, "idReceptor" to receptor,
            "idPropietarioEmisor" to mensaje.idPropietarioEmisor, "idPropietarioReceptor" to receptor,
            "tipo" to mensaje.tipo.name, "contenido" to direccion, "direccionTexto" to direccion,
            "latitud" to lat, "longitud" to lng, "fechaEnvio" to ServerValue.TIMESTAMP, "esLeido" to false
        )
        
        val pesoBytes = mapaMensaje.toString().length
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [UBICACION] | ID: ${mensaje.id} | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(mensaje.id).setValue(mapaMensaje).await()
        motorLocal.impactarMensaje(mensaje.copy(estado = EstadoMensaje.ENTREGADO))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun enviarMensajeAudio(
        idChat: String, emisor: String, receptor: String, pathLocal: String, duracionMs: Long
    ): Result<Unit> = try {
        val base64 = ImageUtils.fileToBase64(contexto, pathLocal) ?: ""
        val mensaje = MensajeEntity(
            id = UUID.randomUUID().toString(), idChat = idChat, idEmisor = emisor, idReceptor = receptor,
            idPropietarioEmisor = auth.currentUser?.uid ?: emisor, idPropietarioReceptor = receptor,
            tipo = TipoMensaje.AUDIO, contenido = "[Audio]", urlMedia = pathLocal,
            duracionSegundos = (duracionMs / 1000).toInt(), estado = EstadoMensaje.ENVIANDO
        )
        motorLocal.impactarMensaje(mensaje)

        val mapaMensaje = mapOf(
            "id" to mensaje.id, "idChat" to idChat, "idEmisor" to emisor, "idReceptor" to receptor,
            "idPropietarioEmisor" to mensaje.idPropietarioEmisor, "idPropietarioReceptor" to receptor,
            "tipo" to mensaje.tipo.name, "contenido" to base64, "duracionSegundos" to mensaje.duracionSegundos,
            "fechaEnvio" to ServerValue.TIMESTAMP, "esLeido" to false
        )

        val pesoBytes = mapaMensaje.toString().length
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [AUDIO] | ID: ${mensaje.id} | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(mensaje.id).setValue(mapaMensaje).await()
        motorLocal.impactarMensaje(mensaje.copy(estado = EstadoMensaje.ENTREGADO))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun enviarMensajeProducto(
        idChat: String, emisor: String, receptor: String, producto: ProductoEliteSnapshot
    ): Result<Unit> = try {
        val contenidoCompreso = com.example.myapplication.core.utilidades.CompresorProductos.comprimir(producto)
        val mensaje = MensajeEntity(
            id = UUID.randomUUID().toString(), idChat = idChat, idEmisor = emisor, idReceptor = receptor,
            idPropietarioEmisor = auth.currentUser?.uid ?: emisor, idPropietarioReceptor = receptor,
            tipo = TipoMensaje.PRODUCTO, contenido = contenidoCompreso, idReferencia = producto.id,
            precioReferencia = producto.precioVenta, idCategoria = producto.idCategoria, 
            subtipoOperativo = producto.tipo.name,
            miniaturaBase64 = producto.miniaturaBase64,
            estado = EstadoMensaje.ENVIANDO
        )
        motorLocal.impactarMensaje(mensaje)

        val mapaMensaje = mapOf(
            "id" to mensaje.id, "idChat" to idChat, "idEmisor" to emisor, "idReceptor" to receptor,
            "idPropietarioEmisor" to mensaje.idPropietarioEmisor, "idPropietarioReceptor" to receptor,
            "tipo" to mensaje.tipo.name, "contenido" to contenidoCompreso, "idReferencia" to producto.id,
            "precioReferencia" to producto.precioVenta, "idCategoria" to producto.idCategoria,
            "subtipoOperativo" to producto.tipo.name,
            "miniaturaBase64" to producto.miniaturaBase64,
            "fechaEnvio" to ServerValue.TIMESTAMP, "esLeido" to false
        )

        val pesoBytes = mapaMensaje.toString().length
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [PRODUCTO] | ID: ${mensaje.id} | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(mensaje.id).setValue(mapaMensaje).await()
        motorLocal.impactarMensaje(mensaje.copy(estado = EstadoMensaje.ENTREGADO))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * 🔥 [ELITE v2026]: Envío de producto con JSON crudo (permite campos dinámicos).
     */
    suspend fun enviarMensajeProductoElite(
        idChat: String, emisor: String, receptor: String, jsonContenido: String,
        idReferencia: String, precio: Double, categoria: String, subtipo: String,
        miniatura: String?, urlImg: String?
    ): Result<Unit> = try {
        android.util.Log.d("ChatMotorRed", "📦 [SEND_PRODUCT_ELITE] Contenido JSON: $jsonContenido")
        android.util.Log.d("ChatMotorRed", "📦 [SEND_PRODUCT_ELITE] URL Imagen: $urlImg | Miniatura: ${miniatura?.take(20)}...")

        val mensaje = MensajeEntity(
            id = UUID.randomUUID().toString(), idChat = idChat, idEmisor = emisor, idReceptor = receptor,
            idPropietarioEmisor = auth.currentUser?.uid ?: emisor, idPropietarioReceptor = receptor,
            tipo = TipoMensaje.PRODUCTO, contenido = jsonContenido, idReferencia = idReferencia,
            precioReferencia = precio, idCategoria = categoria, subtipoOperativo = subtipo,
            miniaturaBase64 = miniatura, urlMedia = urlImg, estado = EstadoMensaje.ENVIANDO
        )
        motorLocal.impactarMensaje(mensaje)

        val mapaMensaje = mapOf(
            "id" to mensaje.id, "idChat" to idChat, "idEmisor" to emisor, "idReceptor" to receptor,
            "idPropietarioEmisor" to mensaje.idPropietarioEmisor, "idPropietarioReceptor" to receptor,
            "tipo" to "PRODUCTO", "contenido" to jsonContenido, "idReferencia" to idReferencia,
            "precioReferencia" to precio, "idCategoria" to categoria, "subtipoOperativo" to subtipo,
            "miniaturaBase64" to miniatura, "urlImagen" to urlImg,
            "fechaEnvio" to ServerValue.TIMESTAMP, "esLeido" to false
        )

        val pesoBytes = mapaMensaje.toString().length
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [PRESUPUESTO] | ID: ${mensaje.id} | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(mensaje.id).setValue(mapaMensaje).await()
        motorLocal.impactarMensaje(mensaje.copy(estado = EstadoMensaje.ENTREGADO))
        Result.success(Unit)
    } catch (e: Exception) { 
        android.util.Log.e("ChatMotorRed", "❌ [SEND_PRODUCT_ERROR] ${e.message}")
        Result.failure(e) 
    }

    suspend fun enviarMensajePresupuesto(
        idChat: String, emisor: String, receptor: String, presupuesto: com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
    ): Result<Unit> = try {
        val cabecera = presupuesto.cabecera
        val contenidoCompreso = com.example.myapplication.core.utilidades.CompresorPresupuesto.comprimir(presupuesto)
        val mensaje = MensajeEntity(
            id = UUID.randomUUID().toString(), idChat = idChat, idEmisor = emisor, idReceptor = receptor,
            idPropietarioEmisor = auth.currentUser?.uid ?: emisor, idPropietarioReceptor = receptor,
            tipo = TipoMensaje.PRESUPUESTO, contenido = contenidoCompreso, 
            idReferencia = cabecera.idPresupuesto,
            precioReferencia = cabecera.totalGeneral, 
            idCategoria = cabecera.idCategoria,
            miniaturaBase64 = cabecera.urlMiniatura,
            estado = EstadoMensaje.ENVIANDO
        )
        motorLocal.impactarMensaje(mensaje)

        val mapaMensaje = mapOf(
            "id" to mensaje.id, "idChat" to idChat, "idEmisor" to emisor, "idReceptor" to receptor,
            "idPropietarioEmisor" to mensaje.idPropietarioEmisor, "idPropietarioReceptor" to receptor,
            "tipo" to "PRESUPUESTO", "contenido" to contenidoCompreso, "idReferencia" to cabecera.idPresupuesto,
            "precioReferencia" to cabecera.totalGeneral, "idCategoria" to cabecera.idCategoria,
            "miniaturaBase64" to cabecera.urlMiniatura,
            "fechaEnvio" to ServerValue.TIMESTAMP, "esLeido" to false
        )

        val pesoBytes = mapaMensaje.toString().length
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [PRESUPUESTO] | ID: ${mensaje.id} | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(mensaje.id).setValue(mapaMensaje).await()
        motorLocal.impactarMensaje(mensaje.copy(estado = EstadoMensaje.ENTREGADO))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun enviarMensajeFinalizacionServicio(
        idChat: String, emisor: String, receptor: String, urlEvidencia: String? = null
    ): Result<Unit> = try {
        val mensaje = MensajeEntity(
            id = UUID.randomUUID().toString(), idChat = idChat, idEmisor = emisor, idReceptor = receptor,
            idPropietarioEmisor = auth.currentUser?.uid ?: emisor, idPropietarioReceptor = receptor,
            tipo = TipoMensaje.FINALIZACION_TRABAJO, contenido = "¡TRABAJO FINALIZADO!",
            urlMedia = urlEvidencia, estado = EstadoMensaje.ENVIANDO
        )
        motorLocal.impactarMensaje(mensaje)

        val mapaMensaje = mapOf(
            "id" to mensaje.id, "idChat" to idChat, "idEmisor" to emisor, "idReceptor" to receptor,
            "idPropietarioEmisor" to mensaje.idPropietarioEmisor, "idPropietarioReceptor" to receptor,
            "tipo" to "FINALIZACION_TRABAJO", "contenido" to mensaje.contenido, "urlMedia" to urlEvidencia,
            "fechaEnvio" to ServerValue.TIMESTAMP, "esLeido" to false
        )

        val pesoBytes = mapaMensaje.toString().length
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [PRESUPUESTO] | ID: ${mensaje.id} | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(mensaje.id).setValue(mapaMensaje).await()
        motorLocal.impactarMensaje(mensaje.copy(estado = EstadoMensaje.ENTREGADO))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun enviarMensajeOperativo(
        idChat: String, emisor: String, receptor: String, tipo: TipoMensaje,
        fecha: String, hora: String, direccion: String, categoria: String? = null,
        nombreRecurso: String? = null, idReferencia: String? = null,
        urlFotoRecurso: String? = null, cargoRecurso: String? = null,
        idPresupuestoAsociado: String? = null,
        codigoVerificacion: String? = null,
        subtipoOperativo: String? = null,
        contenidoOverride: String? = null
    ): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: emisor
        val idMensaje = UUID.randomUUID().toString()
        
        val mensaje = MensajeEntity(
            id = idMensaje, idChat = idChat, idEmisor = emisor, idReceptor = receptor,
            idPropietarioEmisor = uid, idPropietarioReceptor = receptor, tipo = tipo,
            contenido = contenidoOverride ?: "[Propuesta de ${tipo.name}]", 
            fechaCita = fecha, horaCita = hora,
            direccionTexto = direccion, idCategoria = categoria, nombreRecurso = nombreRecurso,
            urlFotoRecurso = urlFotoRecurso, cargoRecurso = cargoRecurso,
            idReferencia = idReferencia, idPresupuestoAsociado = idPresupuestoAsociado,
            subtipoOperativo = subtipoOperativo,
            estadoCita = "PENDIENTE", 
            codigoVerificacion = codigoVerificacion,
            estado = EstadoMensaje.ENVIANDO
        )

        motorLocal.impactarMensaje(mensaje)

        val mapaMensaje = mutableMapOf<String, Any?>(
            "id" to mensaje.id, "idChat" to idChat, "idEmisor" to mensaje.idEmisor, "idReceptor" to mensaje.idReceptor,
            "idPropietarioEmisor" to mensaje.idPropietarioEmisor, "idPropietarioReceptor" to receptor,
            "tipo" to mensaje.tipo.name, "contenido" to mensaje.contenido, "fechaCita" to fecha,
            "horaCita" to hora, "direccionTexto" to direccion, "idCategoria" to categoria,
            "nombreRecurso" to nombreRecurso, "urlFotoRecurso" to urlFotoRecurso,
            "cargoRecurso" to cargoRecurso, "idReferencia" to idReferencia, 
            "idPresupuestoAsociado" to idPresupuestoAsociado,
            "subtipoOperativo" to subtipoOperativo,
            "estadoCita" to "PENDIENTE",
            "esVisitaTecnica" to (tipo == TipoMensaje.VISITA),
            "fechaEnvio" to ServerValue.TIMESTAMP, "esLeido" to false
        )
        codigoVerificacion?.let { mapaMensaje["codigoVerificacion"] = it }

        val pesoBytes = mapaMensaje.toString().length
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [$tipo] | ID: ${mensaje.id} | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(idMensaje).setValue(mapaMensaje).await()
        motorLocal.impactarMensaje(mensaje.copy(estado = EstadoMensaje.ENTREGADO))
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun responderACita(idChat: String, mensajeId: String, aceptada: Boolean): Result<Unit> = try {
        val nuevoEstado = if (aceptada) "ACEPTADO" else "RECHAZADO"
        val mensajeOriginal = chatDao.obtenerMensajePorId(mensajeId)
        
        mensajeOriginal?.let { mensaje ->
            motorLocal.impactarMensaje(mensaje.copy(estadoCita = nuevoEstado))
        }

        nubeRealtime.child("chats").child(idChat).child(mensajeId).updateChildren(mapOf("estadoCita" to nuevoEstado)).await()
        
        val emoji = if (aceptada) "✅" else "❌"
        val tipoEvento = if (mensajeOriginal?.esVisitaTecnica == true) "Visita Técnica" else "Turno"
        val accion = if (aceptada) "confirmado" else "cancelado"
        
        val miId = auth.currentUser?.uid ?: ""
        val suId = if (mensajeOriginal?.idEmisor == miId) (mensajeOriginal?.idReceptor ?: "") else (mensajeOriginal?.idEmisor ?: "")
        
        enviarMensajeSistema(idChat, miId, "Has $accion esta cita $emoji", mensajeId)
        val miNombreAccion = auth.currentUser?.displayName ?: "Alguien"
        enviarMensajeSistema(idChat, suId, "$tipoEvento $accion ($miNombreAccion) $emoji", mensajeId)

        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * 🔥 [SUPREME]: El cliente elige un slot de una propuesta abierta.
     */
    suspend fun responderACitaAbierta(
        idChat: String,
        mensajeId: String,
        fecha: String,
        hora: String,
        idRecurso: String,
        nombreConfirmador: String
    ): Result<Unit> = try {
        val mensajeOriginal = chatDao.obtenerMensajePorId(mensajeId)
        
        // Actualización de la burbuja con los datos finales elegidos
        val actualizaciones = mapOf(
            "estadoCita" to "ACEPTADO",
            "fechaCita" to fecha,
            "horaCita" to hora,
            "idReferencia" to idRecurso,
            "subtipoOperativo" to "AGENDA_CONFIRMADA"
        )

        mensajeOriginal?.let { 
            motorLocal.impactarMensaje(it.copy(
                estadoCita = "ACEPTADO",
                fechaCita = fecha,
                horaCita = hora,
                idReferencia = idRecurso,
                subtipoOperativo = "AGENDA_CONFIRMADA"
            ))
        }

        nubeRealtime.child("chats").child(idChat).child(mensajeId).updateChildren(actualizaciones).await()
        
        val miId = auth.currentUser?.uid ?: ""
        val suId = if (mensajeOriginal?.idEmisor == miId) (mensajeOriginal?.idReceptor ?: "") else (mensajeOriginal?.idEmisor ?: "")
        val tipo = if (mensajeOriginal?.esVisitaTecnica == true) "Visita" else "Turno"

        enviarMensajeSistema(idChat, miId, "Has confirmado tu $tipo para el $fecha a las $hora ✅", mensajeId)
        enviarMensajeSistema(idChat, suId, "¡$nombreConfirmador ha elegido horario! $tipo confirmado para el $fecha a las $hora ✅", mensajeId)

        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun solicitarPedido(idChat: String, mensajeId: String, nombrePrestador: String, nombreCliente: String): Result<Unit> = try {
        val mensajeOriginal = chatDao.obtenerMensajePorId(mensajeId)
        val miId = auth.currentUser?.uid ?: ""
        
        // 1. Actualizar estado de la burbuja original
        mensajeOriginal?.let { motorLocal.impactarMensaje(it.copy(estadoCita = "SOLICITADO")) }
        nubeRealtime.child("chats").child(idChat).child(mensajeId).updateChildren(mapOf("estadoCita" to "SOLICITADO")).await()

        // 2. Enviar mensaje de confirmación para el CLIENTE (Local)
        val emoji = if (mensajeOriginal?.subtipoOperativo == "SERVICIO") "🛠️" else "🛒"
        val tipoItem = if (mensajeOriginal?.subtipoOperativo == "SERVICIO") "servicio" else "producto"
        
        enviarMensajeSistema(
            idChat = idChat,
            receptor = miId, 
            texto = "Avisando a $nombrePrestador que quieres este $tipoItem $emoji",
            idReferencia = mensajeId
        )

        // 3. Enviar aviso de interés para el PRESTADOR (Con el nombre real del cliente)
        val receptorPrestador = mensajeOriginal?.idEmisor ?: ""
        if (receptorPrestador != miId) {
             enviarMensajeSistema(
                 idChat = idChat,
                 receptor = receptorPrestador,
                 texto = "¡$nombreCliente está interesado en este $tipoItem! $emoji",
                 idReferencia = mensajeId
             )
        }

        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * 🔥 [ELITE v2026]: Envía un aviso automático del sistema para cambios de estado.
     */
    suspend fun enviarMensajeSistema(
        idChat: String,
        receptor: String,
        texto: String,
        idReferencia: String? = null
    ): Result<Unit> = try {
        val idMensaje = UUID.randomUUID().toString()
        val emisorId = "SISTEMA"
        
        val mensaje = MensajeEntity(
            id = idMensaje, idChat = idChat, idEmisor = emisorId, idReceptor = receptor,
            idPropietarioEmisor = emisorId, idPropietarioReceptor = receptor,
            tipo = TipoMensaje.SYSTEM, contenido = texto, idReferencia = idReferencia,
            estado = EstadoMensaje.ENTREGADO
        )
        motorLocal.impactarMensaje(mensaje)

        val mapaMensaje = mapOf(
            "id" to idMensaje, "idChat" to idChat, "idEmisor" to emisorId, "idReceptor" to receptor,
            "idPropietarioEmisor" to emisorId, "idPropietarioReceptor" to receptor,
            "tipo" to "SYSTEM", "contenido" to texto, "idReferencia" to idReferencia,
            "fechaEnvio" to ServerValue.TIMESTAMP, "esLeido" to false
        )

        val pesoBytes = mapaMensaje.toString().length
        android.util.Log.i("ChatMotorRed", "🚀 [ELITE_AUDIT_TRAIL] [SENDING] [SYSTEM] | ID: $idMensaje | PESO: $pesoBytes bytes")

        nubeRealtime.child("chats").child(idChat).child(idMensaje).setValue(mapaMensaje).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun marcarComoLeido(idChat: String) {
        ioScope.launch {
            motorLocal.marcarHiloComoLeido(idChat)
            // Lógica efímera: Al leer, el receptor podría purgar la nube (Opcional Ley #8)
        }
    }

    fun inicializarEcosistemaChat(uid: String) {
        // [ELITE]: Escuchamos signals para el UID principal (Citas personales)
        // Esta es la "Ley de Carga Bajo Demanda": No observamos todos los chats,
        // solo esperamos señales de actividad en el buzón global.
        iniciarEscuchaBuzonGlobal(uid)
        
        ioScope.launch {
            // [ELITE]: Ya no cargamos todas las conversaciones al inicio.
            // Los chats se observarán solo cuando el usuario los abra o llegue una señal (Signal).
            // Esto reduce drásticamente el uso de red, CPU y memoria en el arranque.
            
            // val conversaciones = chatDao.obtenerTodasLasConversacionesSync()
            // conversaciones.forEach { observarChat(it.idChat) }
            
            // [ELITE]: Recuperamos hilos activos desde la nube solo si es necesario (ej: restauración)
            recuperarHilosActivos(uid)
        }
    }

    /**
     * 🔥 [ELITE v2026]: Busca en la nube todos los hilos donde el usuario participa.
     */
    private suspend fun recuperarHilosActivos(uid: String) {
        try {
            // Buscamos en signals (que pueden ser persistentes si no se borraron) 
            // O mejor aún, una tabla de 'inbox' persistente en RTDB.
            // Por ahora, Maverick usa signals efímeras + observación de hilos conocidos.
            // TODO: Implementar tabla 'user_chats/{uid}/chatIds' para restauración completa.
            android.util.Log.d("ChatMotorRed", "🔎 [RESTORE] Iniciando recuperación de hilos para $uid")
        } catch (e: Exception) { }
    }

    /**
     * 🔥 [ELITE v2026]: Permite al sistema registrar nuevas identidades (sucursales) 
     * para recibir señales de inbox en tiempo real.
     */
    fun agregarIdentidadASincronizacion(idIdentidad: String) {
        iniciarEscuchaBuzonGlobal(idIdentidad)
        android.util.Log.d("ChatMotorRed", "🔔 [IDENTITY_SYNC] Escuchando señales para: $idIdentidad")
    }

    private fun iniciarEscuchaBuzonGlobal(idIdentidad: String) {
        if (identidadesEscuchadas.contains(idIdentidad)) return
        identidadesEscuchadas.add(idIdentidad)

        val ref = nubeRealtime.child("inbox_signals").child(idIdentidad)
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatId = snapshot.key ?: return
                android.util.Log.d("ChatMotorRed", "🚀 [SIGNAL] Nuevo mensaje en $chatId para identidad $idIdentidad")
                observarChat(chatId)
                snapshot.ref.removeValue() // Tránsito Efímero
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}



