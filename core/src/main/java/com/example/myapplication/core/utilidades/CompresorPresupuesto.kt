package com.example.myapplication.core.utilidades

import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import android.util.Base64

/**
 * --- COMPRESOR DE PRESUPUESTOS MAVERICK (V2026.ELITE) ---
 * [ELITE]: Utiliza un flujo de compresión GZIP + Base64 para tránsito ultra-eficiente.
 * Reduce el tamaño del mensaje hasta un 85%.
 */
object CompresorPresupuesto {

    /**
     * Comprime un objeto Presupuesto en una cadena Base64 comprimida.
     */
    fun comprimir(relacion: PresupuestoConItems): String {
        return try {
            val p = relacion.cabecera
            val json = JSONObject()
            json.put("id", p.idPresupuesto)
            json.put("cl", p.idCliente)
            json.put("pr", p.idPrestador)
            json.put("co", p.idConcurso ?: "")
            json.put("tit", p.tituloTrabajo ?: "")
            json.put("tot", p.totalGeneral)
            json.put("sub", p.subtotal) // 🔥 [SUPREME]
            json.put("idCat", p.idCategoria ?: "")
            json.put("nPr", p.nombrePrestador)
            json.put("nEm", p.nombreEmpresaPrestador ?: "")
            json.put("num", p.numeroPresupuesto ?: "")
            json.put("dv", p.diasValidez)
            json.put("not", p.notas ?: "")
            
            // 🔥 [ANALYTICS v2026.SUPREME]
            json.put("sArt", p.subtotalArticulos)
            json.put("sSvc", p.subtotalServicios)
            json.put("sGst", p.subtotalGastos)
            json.put("tImp", p.totalImpuestos)
            json.put("tInt", p.totalIntereses)
            json.put("tDes", p.totalDescuentos)
            json.put("eMo", p.etiquetaManoObra)
            json.put("tp", p.tipo.name)
            
            val lineas = relacion.lineas.joinToString("|") { 
                "${it.nombreCopiado};${it.descripcionCopiada};${it.cantidad};${it.precioSnapshot};${it.porcentajeImpuesto};${it.porcentajeDescuento};${it.tipoItem.name}" 
            }
            json.put("ls", lineas)

            val finanzas = relacion.finanzas.joinToString("|") {
                "${it.etiqueta};${it.monto};${it.tipo.name}"
            }
            json.put("fs", finanzas)

            val rawData = json.toString().toByteArray(Charsets.UTF_8)
            val bos = ByteArrayOutputStream()
            val gzip = GZIPOutputStream(bos)
            gzip.write(rawData)
            gzip.close()
            
            Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            android.util.Log.e("CompresorMav", "❌ Error al comprimir: ${e.message}")
            ""
        }
    }

    /**
     * Descomprime una cadena Base64 y la reconstruye como Entidad.
     */
    fun descomprimir(compacto: String): PresupuestoConItems? {
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
            val idPresupuesto = json.getString("id")
            
            val lsRaw = json.optString("ls")
            val lineas = if (lsRaw.isBlank()) emptyList() else lsRaw.split("|").mapNotNull { 
                val p = it.split(";")
                if (p.size >= 7) ProductoFinalEntity(
                    idPresupuesto = idPresupuesto,
                    nombreCopiado = p[0], 
                    descripcionCopiada = p[1], 
                    cantidad = p[2].toIntOrNull() ?: 1, 
                    precioSnapshot = p[3].toDoubleOrNull() ?: 0.0, 
                    porcentajeImpuesto = p[4].toDoubleOrNull() ?: 0.0, 
                    porcentajeDescuento = p[5].toDoubleOrNull() ?: 0.0,
                    tipoItem = try { TipoProductoFinal.valueOf(p[6]) } catch(e: Exception) { TipoProductoFinal.PRODUCTO }
                ) else null
            }

            val fsRaw = json.optString("fs")
            val finanzas = if (fsRaw.isBlank()) emptyList() else fsRaw.split("|").mapNotNull {
                val p = it.split(";")
                if (p.size >= 3) FinanzaFinalEntity(
                    idPresupuesto = idPresupuesto,
                    etiqueta = p[0],
                    monto = p[1].toDoubleOrNull() ?: 0.0,
                    tipo = try { TipoFinanzaFinal.valueOf(p[2]) } catch(e: Exception) { TipoFinanzaFinal.IMPUESTO }
                ) else null
            }

            val header = PresupuestoFinalEntity(
                idPresupuesto = idPresupuesto,
                idCliente = json.getString("cl"),
                idPrestador = json.getString("pr"),
                idConcurso = json.optString("co").takeIf { it.isNotBlank() },
                tituloTrabajo = json.optString("tit"),
                totalGeneral = json.optDouble("tot", 0.0),
                subtotal = json.optDouble("sub", 0.0), // 🔥 [SUPREME]
                idCategoria = json.optString("idCat").takeIf { it.isNotBlank() },
                nombrePrestador = json.optString("nPr"),
                nombreEmpresaPrestador = json.optString("nEm").takeIf { it.isNotBlank() },
                numeroPresupuesto = json.optString("num").takeIf { it.isNotBlank() },
                diasValidez = json.optInt("dv", 7),
                notas = json.optString("not").takeIf { it.isNotBlank() },
                
                // 🔥 [ANALYTICS v2026.SUPREME]
                subtotalArticulos = json.optDouble("sArt", 0.0),
                subtotalServicios = json.optDouble("sSvc", 0.0),
                subtotalGastos = json.optDouble("sGst", 0.0),
                totalImpuestos = json.optDouble("tImp", 0.0),
                totalIntereses = json.optDouble("tInt", 0.0),
                totalDescuentos = json.optDouble("tDes", 0.0),
                etiquetaManoObra = json.optString("eMo", "MANO DE OBRA"),
                tipo = try { TipoPresupuesto.valueOf(json.optString("tp", "NUEVO")) } catch(e: Exception) { TipoPresupuesto.NUEVO },
                
                marcaTiempo = System.currentTimeMillis()
            )
            
            PresupuestoConItems(cabecera = header, lineas = lineas, finanzas = finanzas)
        } catch (e: Exception) {
            android.util.Log.e("CompresorMav", "❌ Error al descomprimir: ${e.message}")
            null
        }
    }
}

































