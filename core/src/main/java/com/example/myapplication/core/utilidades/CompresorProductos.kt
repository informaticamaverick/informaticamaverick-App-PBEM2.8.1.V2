package com.example.myapplication.core.utilidades

import com.example.myapplication.core.datos.local.entidades.*
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import android.util.Base64
import java.util.UUID

/**
 * --- COMPRESOR DE PRODUCTOS MAVERICK (V2026.ELITE) ---
 * [ELITE]: Utiliza un flujo de compresión GZIP + Base64 para tránsito ultra-eficiente de productos en el chat.
 */
object CompresorProductos {

    /**
     * Comprime un objeto de producto en una cadena Base64 comprimida.
     */
    fun comprimir(p: ProductoEliteSnapshot): String {
        return try {
            val json = JSONObject()
            json.put("id", p.id)
            json.put("pr", p.idPropietario)
            json.put("nom", p.nombre)
            json.put("des", p.descripcion)
            json.put("pv", p.precioVenta)
            json.put("pc", p.precioCosto)
            json.put("idp", p.impuestoDefault)
            json.put("dep", p.descuentoDefault)
            json.put("sku", p.sku ?: "")
            json.put("idCat", p.idCategoria)
            json.put("tip", p.tipo.name)
            json.put("stk", p.stockActual)
            json.put("img", p.urlImagen ?: "")
            json.put("min", p.miniaturaBase64 ?: "")
            
            // 🔥 [ELITE v2026]: Campos extra para Burbuja Avanzada
            json.put("ct", "") // cuotasTexto placeholder
            json.put("eg", false) // envioGratis placeholder
            json.put("pa", 0.0) // precioAnterior placeholder

            val rawData = json.toString().toByteArray(Charsets.UTF_8)
            val bos = ByteArrayOutputStream()
            val gzip = GZIPOutputStream(bos)
            gzip.write(rawData)
            gzip.close()
            
            Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            android.util.Log.e("CompresorProductos", "❌ Error al comprimir: ${e.message}")
            ""
        }
    }

    /**
     * Descomprime una cadena Base64 y la reconstruye como Snapshot.
     */
    fun descomprimir(compacto: String): ProductoEliteSnapshot? {
        if (compacto.isBlank()) return null
        
        return try {
            val rawJson = if (compacto.startsWith("{")) {
                compacto 
            } else {
                val compressedData = Base64.decode(compacto, Base64.DEFAULT)
                val bis = ByteArrayInputStream(compressedData)
                val gzip = GZIPInputStream(bis)
                val bos = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var len: Int
                while (gzip.read(buffer).also { len = it } > 0) {
                    bos.write(buffer, 0, len)
                }
                gzip.close()
                bos.toString("UTF-8")
            }

            val json = JSONObject(rawJson)
            
            return ProductoEliteSnapshot(
                id = json.optString("id", UUID.randomUUID().toString()),
                idPropietario = json.optString("pr", ""),
                nombre = json.optString("nom", "Producto"),
                descripcion = json.optString("des", ""),
                precioVenta = json.optDouble("pv", 0.0),
                precioCosto = json.optDouble("pc", 0.0),
                impuestoDefault = json.optDouble("idp", 0.0),
                descuentoDefault = json.optDouble("dep", 0.0),
                sku = json.optString("sku").takeIf { it.isNotBlank() },
                idCategoria = json.optString("idCat", "GENERAL"),
                tipo = TipoProducto.valueOf(json.optString("tip", "PRODUCTO")),
                stockActual = json.optInt("stk", 0),
                urlImagen = json.optString("img").takeIf { it.isNotBlank() },
                miniaturaBase64 = json.optString("min").takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            android.util.Log.e("CompresorProductos", "❌ Error al descomprimir: ${e.message}")
            null
        }
    }

    /**
     * 🔥 [ELITE v2026]: Extrae metadatos extendidos del JSON de producto.
     */
    fun extraerMetadatosElite(jsonStr: String): Map<String, Any?> {
        return try {
            val json = JSONObject(jsonStr)
            val mapa = mutableMapOf<String, Any?>()
            mapa["cuotas"] = json.optString("ct", "")
            mapa["envioGratis"] = json.optBoolean("eg", false)
            mapa["precioAnterior"] = json.optDouble("pa", 0.0)
            mapa["porcentaje"] = json.optInt("des", 0)
            mapa["marca"] = json.optString("mar", "Maverick")
            mapa
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

