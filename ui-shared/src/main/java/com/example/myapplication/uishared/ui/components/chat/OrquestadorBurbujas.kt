package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.uishared.ui.components.ServiceCompletionBubble
import com.example.myapplication.core.dominio.modelos.ProductoMensajeDominio

/**
 * --- ORQUESTADOR DE BURBUJAS (V2026.FINAL) ---
 * Centraliza el renderizado de mensajes según su tipo operativo.
 */
@Composable
fun OrquestadorBurbujas(
    mensaje: MensajeEntity,
    idUsuarioActual: String,
    presupuesto: PresupuestoResumenDominio? = null,
    colorFondoMio: Color,
    colorFondoOtro: Color,
    colorContenido: Color = Color.White,
    idAudioReproduciendo: String? = null,
    progresoAudio: Float = 0f,
    alHacerClickImagen: (String) -> Unit = {},
    alHacerClickAudio: (String, String?, String?) -> Unit = { _, _, _ -> },
    alHacerClickMapa: (String) -> Unit = {},
    alHacerClickPresupuesto: (String) -> Unit = {},
    alAceptarCita: (String) -> Unit = {},
    alRechazarCita: (String) -> Unit = {},
    alVerCalendarioCita: (String) -> Unit = {},
    alHacerClickCuerpoCita: (String) -> Unit = {},
    alCrearPresupuestoDesdeSolicitud: (String) -> Unit = {},
    alGenerarVisitaDesdeMapa: (MensajeEntity) -> Unit = {},
    alResponderMensaje: (MensajeEntity) -> Unit = {},
    alHacerClickSistema: (String?) -> Unit = {}, 
    alSolicitarCompraProducto: (ProductoMensajeDominio) -> Unit = {}, 
    nombreCliente: String = "Cliente",
    nombrePrestador: String = "Prestador",
    fotoRemota: Any? = null,
    fotoLocal: Any? = null,
    mostrarAccionesComerciales: Boolean = false, 
    nombreCategoria: String? = null, 
    iconoCategoria: String? = null   
) {
    val esMio = mensaje.idEmisor == idUsuarioActual
    val fotoRemotaEfectiva = fotoRemota
    val fotoLocalEfectiva = fotoLocal
    val colorFondoEfectivo = if (esMio) colorFondoMio else colorFondoOtro
    val callbackRespuesta = { alResponderMensaje(mensaje) }

    when (mensaje.tipo) {
        TipoMensaje.TEXTO -> {
            BurbujaTexto(
                texto = mensaje.contenido,
                esMio = esMio,
                marcaTiempo = mensaje.marcaTiempo,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                estaLeido = mensaje.esLeido,
                estaEntregado = mensaje.estaSincronizado,
                estaSincronizado = mensaje.estaSincronizado,
                nombreRespuesta = mensaje.nombreEmisorRespuesta,
                contenidoRespuesta = mensaje.respondidoAContenido,
                alHacerSwipeRespuesta = callbackRespuesta
            )
        }
        TipoMensaje.IMAGEN -> {
            BurbujaImagen(
                urlImagen = mensaje.urlMedia,
                texto = mensaje.contenido,
                esMio = esMio,
                marcaTiempo = mensaje.marcaTiempo,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                estaLeido = mensaje.esLeido,
                estaEntregado = mensaje.estaSincronizado,
                estaSincronizado = mensaje.estaSincronizado,
                alHacerClick = { mensaje.urlMedia?.let { alHacerClickImagen(it) } },
                alHacerSwipeRespuesta = callbackRespuesta
            )
        }
        TipoMensaje.AUDIO -> {
            val estaReproduciendo = idAudioReproduciendo == mensaje.id
            BurbujaAudio(
                duracion = String.format("%02d:%02d", (mensaje.duracionSegundos ?: 0) / 60, (mensaje.duracionSegundos ?: 0) % 60),
                reproduciendo = estaReproduciendo,
                progreso = if (estaReproduciendo) progresoAudio else 0f,
                esMio = esMio,
                marcaTiempo = mensaje.marcaTiempo,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                estaLeido = mensaje.esLeido,
                estaEntregado = mensaje.estaSincronizado,
                estaSincronizado = mensaje.estaSincronizado,
                alHacerClickPlay = { alHacerClickAudio(mensaje.id, mensaje.urlMedia, mensaje.urlMedia) },
                alHacerSwipeRespuesta = callbackRespuesta
            )
        }
        TipoMensaje.UBICACION -> {
            BurbujaMapa(
                latitud = mensaje.latitud ?: 0.0,
                longitud = mensaje.longitud ?: 0.0,
                direccion = mensaje.direccionTexto,
                esMio = esMio,
                marcaTiempo = mensaje.marcaTiempo,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                estaLeido = mensaje.esLeido,
                estaEntregado = mensaje.estaSincronizado,
                estaSincronizado = mensaje.estaSincronizado,
                alHacerClick = { alHacerClickMapa("geo:${mensaje.latitud},${mensaje.longitud}?q=${mensaje.direccionTexto}") },
                alHacerClickHerramienta = { alGenerarVisitaDesdeMapa(mensaje) }
            )
        }
        TipoMensaje.PRESUPUESTO -> {
            BurbujaPresupuestoDocumento(
                titulo = presupuesto?.tituloTrabajo ?: mensaje.contenido,
                total = "$ ${String.format(java.util.Locale.getDefault(), "%,.2f", presupuesto?.totalGeneral ?: mensaje.precioReferencia ?: 0.0)}",
                estado = presupuesto?.estado?.name ?: "PENDIENTE",
                esMio = esMio,
                marcaTiempo = mensaje.marcaTiempo,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                estaLeido = mensaje.esLeido,
                estaEntregado = mensaje.estaSincronizado,
                estaSincronizado = mensaje.estaSincronizado,
                miniaturaBase64 = presupuesto?.urlMiniatura ?: mensaje.miniaturaBase64,
                alVer = { alHacerClickPresupuesto(mensaje.idRelacionado ?: mensaje.id) },
                alGuardar = { },
                alHacerSwipeRespuesta = callbackRespuesta
            )
        }
        TipoMensaje.TURNO -> {
            BurbujaTurnoLocal(
                estado = mensaje.estadoCita ?: "PENDIENTE",
                fecha = mensaje.fechaCita,
                hora = mensaje.horaCita,
                direccion = mensaje.direccionCitaOverride ?: mensaje.direccionTexto,
                nombreCategoria = nombreCategoria,
                idCategoria = mensaje.idCategoria,
                iconoCategoria = iconoCategoria,
                codigoVerificacion = mensaje.codigoVerificacion,
                idRecurso = mensaje.idReferencia,
                nombreRecurso = mensaje.nombreRecurso,
                esMio = esMio,
                marcaTiempo = mensaje.marcaTiempo,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                estaLeido = mensaje.esLeido,
                estaEntregado = mensaje.estaSincronizado,
                estaSincronizado = mensaje.estaSincronizado,
                esAgendaAbierta = mensaje.subtipoOperativo == "AGENDA_ABIERTA",
                alAceptar = { alAceptarCita(mensaje.id) },
                alRechazar = { alRechazarCita(mensaje.id) },
                alHacerClickMapa = alHacerClickMapa,
                alHacerSwipeRespuesta = callbackRespuesta
            )
        }
        TipoMensaje.VISITA -> {
            BurbujaVisitaTecnica(
                estado = mensaje.estadoCita ?: "PENDIENTE",
                fecha = mensaje.fechaCita,
                hora = mensaje.horaCita,
                direccion = mensaje.direccionCitaOverride ?: mensaje.direccionTexto,
                nombreCategoria = nombreCategoria, 
                idCategoria = mensaje.idCategoria,
                iconoCategoria = iconoCategoria,
                codigoVerificacion = mensaje.codigoVerificacion,
                codigoPresupuesto = presupuesto?.numeroPresupuesto ?: mensaje.idPresupuestoAsociado?.takeLast(8),
                nombreResponsable = mensaje.nombreRecurso ?: nombrePrestador,
                fotoResponsable = mensaje.urlFotoRecurso ?: (if (esMio) fotoLocalEfectiva else fotoRemotaEfectiva),
                cargoResponsable = mensaje.cargoRecurso,
                nombreEmpresa = nombrePrestador,
                esMio = esMio,
                marcaTiempo = mensaje.marcaTiempo,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                estaLeido = mensaje.esLeido,
                estaEntregado = mensaje.estaSincronizado,
                estaSincronizado = mensaje.estaSincronizado,
                esAgendaAbierta = mensaje.subtipoOperativo == "AGENDA_ABIERTA",
                alAceptar = { alAceptarCita(mensaje.id) },
                alRechazar = { alRechazarCita(mensaje.id) },
                alHacerClickMapa = alHacerClickMapa,
                alHacerSwipeRespuesta = callbackRespuesta
            )
        }
        TipoMensaje.ENVIO -> {
            BurbujaCitaElite(
                tipo = TipoCitaElite.ENVIO,
                estado = mensaje.estadoCita ?: "PENDIENTE",
                fecha = mensaje.fechaCita,
                hora = mensaje.horaCita,
                direccion = mensaje.direccionCitaOverride ?: mensaje.direccionTexto,
                nombreCategoria = nombreCategoria,
                idCategoria = mensaje.idCategoria,
                codigoVerificacion = mensaje.codigoVerificacion,
                esMio = esMio,
                marcaTiempo = mensaje.marcaTiempo,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                estaLeido = mensaje.esLeido,
                estaEntregado = mensaje.estaSincronizado,
                estaSincronizado = mensaje.estaSincronizado,
                alAceptar = { alAceptarCita(mensaje.id) },
                alRechazar = { alRechazarCita(mensaje.id) },
                alVerCalendario = { alVerCalendarioCita(mensaje.id) },
                alHacerClickCuerpo = { alHacerClickCuerpoCita(mensaje.id) },
                alHacerSwipeRespuesta = callbackRespuesta,
                nombreRecurso = mensaje.nombreRecurso,
                nombreProfesional = if (esMio) "Yo" else nombrePrestador
            )
        }
        TipoMensaje.APPOINTMENT_RECEIPT -> {
            BurbujaComprobanteCita(
                titulo = if (mensaje.esVisitaTecnica == true) "Visita Técnica" else "Turno en Local",
                fecha = mensaje.fechaCita,
                hora = mensaje.horaCita,
                direccion = mensaje.direccionTexto,
                codigo = mensaje.codigoVerificacion,
                nombreCategoria = nombreCategoria,
                idCategoria = mensaje.idCategoria,
                esMio = esMio,
                marcaTiempo = mensaje.marcaTiempo,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                estaLeido = mensaje.esLeido,
                estaEntregado = mensaje.estaSincronizado,
                estaSincronizado = mensaje.estaSincronizado,
                alHacerSwipeRespuesta = callbackRespuesta
            )
        }
        TipoMensaje.BUDGET_REQUEST -> {
            BurbujaSolicitudPresupuesto(
                descripcion = mensaje.contenido,
                direccion = mensaje.direccionTexto,
                nombreCliente = if (esMio) "Yo" else nombreCliente,
                marcaTiempo = mensaje.marcaTiempo,
                esMio = esMio,
                colorFondo = colorFondoEfectivo,
                colorContenido = colorContenido,
                alHacerClickAccion = { alCrearPresupuestoDesdeSolicitud(mensaje.id) }
            )
        }
        TipoMensaje.SYSTEM -> {
            val subtipo = mensaje.idReferencia?.let { ref ->
                try { TipoAvisoEstado.valueOf(ref) } catch (e: Exception) { null }
            }

            if (subtipo != null) {
                BurbujaAvisoEstado(
                    tipoEstado = subtipo,
                    mensaje = mensaje.contenido,
                    esMio = esMio,
                    marcaTiempo = mensaje.marcaTiempo,
                    colorFondo = colorFondoEfectivo,
                    colorContenido = colorContenido,
                    estaLeido = mensaje.esLeido,
                    estaEntregado = mensaje.estaSincronizado,
                    estaSincronizado = mensaje.estaSincronizado
                )
            } else {
                // 🔥 [ELITE]: Los avisos de sistema son universales en el hilo de chat.
                val esDeSistema = mensaje.idEmisor == "SISTEMA"
                val esParaMi = mensaje.idReceptor == idUsuarioActual || mensaje.idReceptor.isEmpty() || esDeSistema
                
                if (esParaMi) {
                    BurbujaSistema(
                        texto = mensaje.contenido,
                        emoji = if (mensaje.idReferencia != null) "📍" else null,
                        onClick = if (mensaje.idReferencia != null) { { alHacerClickSistema(mensaje.idReferencia) } } else null
                    )
                }
            }
        }
        TipoMensaje.FINALIZACION_TRABAJO -> {
            ServiceCompletionBubble(
                isFromMe = esMio,
                evidenceUrl = mensaje.urlMedia,
                onRateClick = { }
            )
        }
        TipoMensaje.TENDER_INVITATION -> {
            if (mensaje.idReceptor == idUsuarioActual) {
                BurbujaSistema(
                    texto = "Invitación a Licitación: ${mensaje.contenido}",
                    emoji = "📩",
                    onClick = { alHacerClickSistema(mensaje.idReferencia) }
                )
            }
        }
        TipoMensaje.PRODUCTO -> {
            val jsonStr = mensaje.contenido
            val metaElite = com.example.myapplication.core.utilidades.CompresorProductos.extraerMetadatosElite(jsonStr)
            val productoBasico = com.example.myapplication.core.utilidades.CompresorProductos.descomprimir(jsonStr)
            
            val hora = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(mensaje.marcaTiempo))

            val uiModel = ProductoMensajeDominio(
                idProducto = productoBasico?.sku ?: mensaje.idReferencia ?: "N/A",
                idMensajeOriginal = mensaje.id, 
                titulo = productoBasico?.nombre ?: mensaje.contenido,
                descripcion = productoBasico?.descripcion ?: "", 
                marca = (metaElite["marca"] as? String) ?: "Maverick",
                idCategoria = productoBasico?.idCategoria ?: mensaje.idCategoria ?: "GENERAL",
                esServicio = (productoBasico?.tipo == TipoProducto.SERVICIO) || (mensaje.subtipoOperativo == "SERVICIO"),
                urlImagen = productoBasico?.urlImagen ?: mensaje.urlMedia ?: "",
                miniaturaBase64 = productoBasico?.miniaturaBase64 ?: mensaje.miniaturaBase64, 
                precioActual = productoBasico?.precioVenta ?: mensaje.precioReferencia ?: 0.0,
                precioAnterior = (metaElite["precioAnterior"] as? Double)?.takeIf { it > 0.0 },
                porcentajeDescuento = (metaElite["porcentaje"] as? Int) ?: 0,
                cuotasTexto = (metaElite["cuotas"] as? String) ?: "",
                envioGratis = (metaElite["envioGratis"] as? Boolean) ?: false,
                estaSolicitado = mensaje.estadoCita == "SOLICITADO"
            )
            
            ProductoChatBubble(
                producto = uiModel,
                esEntrante = !esMio,
                horaMensaje = hora,
                mostrarBotonComprar = mostrarAccionesComerciales, 
                onComprar = { alSolicitarCompraProducto(it) } 
            )
        }
    }
}
