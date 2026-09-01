package com.example.myapplication.core.dominio.mapeadores

import android.content.Context
import android.util.Log
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.modelos.Promocion
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.core.utilidades.CompresorPresupuesto
import com.google.firebase.database.DataSnapshot
import org.json.JSONObject
import java.util.UUID

/**
 * --- MAPEADOR DE MENSAJERÍA MAVERICK (V2026.FINAL) ---
 */
object MensajeMappers {

    private const val TAG = "MensajeMappers"

    fun parsearPromocionJson(jsonTexto: String): Promocion? {
        return try {
            com.google.gson.Gson().fromJson(jsonTexto, Promocion::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun parsearPresupuestoJson(
        jsonTexto: String,
        idPresupuesto: String,
        idPrestador: String,
        idCliente: String
    ): PresupuestoConItems? {
        CompresorPresupuesto.descomprimir(jsonTexto)?.let { return it }

        return try {
            val json = JSONObject(jsonTexto)
            val gson = com.google.gson.Gson()

            fun parsearArticulos(raw: String): List<ArticuloPresupuesto> {
                if (raw.isBlank()) return emptyList()
                return try {
                    val tipoLista = object : com.google.gson.reflect.TypeToken<List<ArticuloPresupuesto>>() {}.type
                    gson.fromJson(raw, tipoLista) ?: emptyList()
                } catch (e: Exception) { emptyList() }
            }

            val header = PresupuestoFinalEntity(
                idPresupuesto = idPresupuesto,
                idCliente = idCliente,
                idPrestador = idPrestador,
                nombrePrestador = json.optString("nombrePrestador", "Prestador"),
                nombreEmpresaPrestador = json.optString("nombreEmpresa").takeIf { it.isNotBlank() },
                idCategoria = json.optString("idCategoria"),
                numeroPresupuesto = json.optString("numeroPresupuesto"),
                tituloTrabajo = json.optString("tituloTrabajo"),
                diasValidez = json.optInt("diasValidez", 7),
                totalGeneral = json.optDouble("totalGeneral", 0.0),
                subtotal = json.optDouble("subtotal", 0.0),
                subtotalArticulos = json.optDouble("subtotalArticulos", 0.0),
                subtotalServicios = json.optDouble("subtotalServicios", 0.0),
                subtotalGastos = json.optDouble("subtotalGastos", 0.0),
                totalImpuestos = json.optDouble("totalImpuestos", 0.0),
                totalIntereses = json.optDouble("totalIntereses", 0.0),
                totalDescuentos = json.optDouble("totalDescuentos", 0.0),
                etiquetaManoObra = json.optString("etiquetaManoObra", "MANO DE OBRA"),
                tipo = try { TipoPresupuesto.valueOf(json.optString("tipo", "NUEVO")) } catch(e: Exception) { TipoPresupuesto.NUEVO },
                estado = EstadoPresupuesto.PENDIENTE,
                marcaTiempo = System.currentTimeMillis()
            )
            
            val articulosRaw = parsearArticulos(json.optString("articulos"))
            val lineas = articulosRaw.map { 
                ProductoFinalEntity(
                    idPresupuesto = idPresupuesto,
                    nombreCopiado = it.descripcion,
                    cantidad = it.cantidad,
                    precioSnapshot = it.precioUnitario,
                    tipoItem = TipoProductoFinal.PRODUCTO
                )
            }

            PresupuestoConItems(cabecera = header, lineas = lineas, finanzas = emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "❌ [BUDGET_PARSE_ERROR] ${e.message}")
            null
        }
    }

    fun mapearDesdeFirebase(
        instantanea: DataSnapshot, 
        idChat: String, 
        contexto: Context? = null
    ): MensajeEntity? {
        return try {
            val idEmisor = instantanea.child("idEmisor").getValue(String::class.java) ?: ""
            if (idEmisor.isBlank()) {
                Log.w(TAG, "⚠️ [MAPEO_IGNORADO] Mensaje sin idEmisor legal.")
                return null
            }

            val id = instantanea.child("id").getValue(String::class.java) ?: instantanea.key ?: UUID.randomUUID().toString()
            val tipoStr = instantanea.child("tipo").getValue(String::class.java) ?: "TEXTO"
            val tipo = try { TipoMensaje.valueOf(tipoStr) } catch (e: Exception) { TipoMensaje.TEXTO }

            var contenidoFinal = instantanea.child("contenido").getValue(String::class.java) ?: ""
            var rutaMediaLocal: String? = null
            var miniaturaExtracted: String? = null
            var idCategoriaExtracted = instantanea.child("idCategoria").getValue(String::class.java)
                ?: instantanea.child("categoriaId").getValue(String::class.java)
            var precioExtracted = instantanea.getDoubleSafe("precioReferencia")
            var subtipoExtracted: String? = null

            // 🔥 [ELITE]: Persistencia multimedia forzada con herramientas Core
            if (contexto != null && (tipo == TipoMensaje.IMAGEN || tipo == TipoMensaje.AUDIO)) {
                if (contenidoFinal.length > 500) { // Es Base64 real, no un placeholder
                    val prefijo = if (tipo == TipoMensaje.IMAGEN) "IMG_" else "AUD_"
                    val extension = if (tipo == TipoMensaje.IMAGEN) ".webp" else ".m4a"
                    rutaMediaLocal = ImageUtils.saveBase64ToFile(contexto, contenidoFinal, id, prefijo, extension)
                    contenidoFinal = if (tipo == TipoMensaje.IMAGEN) "[Imagen]" else "[Audio]"
                }
            }

            // 🔥 [ELITE]: Extracción de metadatos para PRODUCTO (Ley #10)
            if (tipo == TipoMensaje.PRODUCTO) {
                android.util.Log.d(TAG, "📦 [MAP_PRODUCT] Procesando contenido: ${contenidoFinal.take(50)}...")
                com.example.myapplication.core.utilidades.CompresorProductos.descomprimir(contenidoFinal)?.let { p ->
                    idCategoriaExtracted = p.idCategoria
                    precioExtracted = p.precioVenta
                    miniaturaExtracted = p.miniaturaBase64
                    subtipoExtracted = p.tipo.name
                    if (rutaMediaLocal == null) rutaMediaLocal = p.urlImagen
                }
            }

            val esLeidoNube = instantanea.child("esLeido").getValue(Boolean::class.java) ?: false
            
            val direccionTextoNube = instantanea.child("direccionTexto").getValue(String::class.java)
                ?: instantanea.child("contenido").getValue(String::class.java)

            val finalUrlImagen = rutaMediaLocal ?: instantanea.child("urlImagen").getValue(String::class.java)

            MensajeEntity(
                id = id,
                idChat = instantanea.child("idChat").getValue(String::class.java) ?: idChat,
                idEmisor = idEmisor,
                idReceptor = instantanea.child("idReceptor").getValue(String::class.java) ?: "",
                idPropietarioEmisor = instantanea.child("idPropietarioEmisor").getValue(String::class.java) ?: idEmisor,
                idPropietarioReceptor = instantanea.child("idPropietarioReceptor").getValue(String::class.java) ?: "",
                tipo = tipo,
                contenido = contenidoFinal,
                urlMedia = finalUrlImagen,
                miniaturaBase64 = miniaturaExtracted ?: instantanea.child("miniaturaBase64").getValue(String::class.java),
                latitud = instantanea.getDoubleSafe("latitud"),
                longitud = instantanea.getDoubleSafe("longitud"),
                direccionTexto = direccionTextoNube,
                duracionSegundos = instantanea.getIntSafe("duracionSegundos"),
                idReferencia = instantanea.child("idReferencia").getValue(String::class.java) ?: instantanea.child("idRelacionado").getValue(String::class.java),
                idPresupuestoAsociado = instantanea.child("idPresupuestoAsociado").getValue(String::class.java),
                precioReferencia = precioExtracted,
                idCategoria = idCategoriaExtracted,
                subtipoOperativo = subtipoExtracted,
                estadoCita = instantanea.child("estadoCita").getValue(String::class.java),
                fechaCita = instantanea.child("fechaCita").getValue(String::class.java),
                horaCita = instantanea.child("horaCita").getValue(String::class.java),
                codigoVerificacion = instantanea.child("codigoVerificacion").getValue(String::class.java),
                nombreRecurso = instantanea.child("nombreRecurso").getValue(String::class.java),
                urlFotoRecurso = instantanea.child("urlFotoRecurso").getValue(String::class.java),
                cargoRecurso = instantanea.child("cargoRecurso").getValue(String::class.java),
                esVisitaTecnica = instantanea.child("esVisitaTecnica").getValue(Boolean::class.java),
                nombreEmisorRespuesta = instantanea.child("nombreEmisorRespuesta").getValue(String::class.java),
                respondidoAId = instantanea.child("respondidoAId").getValue(String::class.java),
                respondidoAContenido = instantanea.child("respondidoAContenido").getValue(String::class.java),
                estado = if (esLeidoNube) EstadoMensaje.LEIDO else EstadoMensaje.ENTREGADO,
                marcaTiempo = instantanea.getLongSafe("fechaEnvio") ?: System.currentTimeMillis(),
                esMio = false 
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ [MAP_ERROR] Error mapeando mensaje: ${e.message}")
            null
        }
    }

    private fun DataSnapshot.getDoubleSafe(key: String): Double? {
        val value = child(key).value ?: return null
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    private fun DataSnapshot.getIntSafe(key: String): Int? {
        val value = child(key).value ?: return null
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }

    private fun DataSnapshot.getLongSafe(key: String): Long? {
        val value = child(key).value ?: return null
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }
}

