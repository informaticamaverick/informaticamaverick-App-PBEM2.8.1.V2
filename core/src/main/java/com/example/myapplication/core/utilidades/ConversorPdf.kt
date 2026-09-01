package com.example.myapplication.core.utilidades

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.TipoProductoFinal
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * --- CONVERSOR DE PRESUPUESTO A PDF (v2026.ELITE) ---
 * PROPÓSITO: Generar archivos físicos para exportación y envío.
 * LEY #9: Nombres en español y lógica atómica.
 */
object ConversorPdfMav {

    fun generarPdfDesdePresupuesto(context: Context, relacion: PresupuestoConItems): File? {
        val presupuesto = relacion.cabecera
        val lineas = relacion.lineas
        val documento = PdfDocument()
        
        // Configuración de página A4 (Aprox 595x842 puntos a 72dpi)
        val infoPagina = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val pagina = documento.startPage(infoPagina)
        val lienzo = pagina.canvas
        
        val pincel = Paint()
        val pincelNegrita = Paint().apply { isFakeBoldText = true }
        
        // --- CABECERA ---
        pincel.textSize = 18f
        pincel.color = Color.BLACK
        lienzo.drawText(presupuesto.nombreEmpresaPrestador ?: presupuesto.nombrePrestador, 50f, 50f, pincel)
        
        pincel.textSize = 10f
        pincel.color = Color.GRAY
        lienzo.drawText("PRESUPUESTO N° ${presupuesto.idPresupuesto.takeLast(8).uppercase()}", 400f, 50f, pincel)
        
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(presupuesto.marcaTiempo))
        lienzo.drawText("Fecha: $fecha", 400f, 65f, pincel)

        // --- CUERPO ---
        pincel.color = Color.BLACK
        pincel.textSize = 12f
        lienzo.drawText("Trabajo: ${presupuesto.tituloTrabajo ?: "Sin título"}", 50f, 100f, pincel)
        
        var yPos = 140f
        pincel.textSize = 10f
        pincelNegrita.textSize = 10f
        
        // Encabezado de tabla
        lienzo.drawText("Cant.", 50f, yPos, pincelNegrita)
        lienzo.drawText("Descripción", 100f, yPos, pincelNegrita)
        lienzo.drawText("Total", 500f, yPos, pincelNegrita)
        
        yPos += 20f
        lienzo.drawLine(50f, yPos - 10f, 550f, yPos - 10f, pincel)

        // Items
        lineas.forEach { item ->
            lienzo.drawText(item.cantidad.toString(), 50f, yPos, pincel)
            lienzo.drawText(item.nombreCopiado, 100f, yPos, pincel)
            lienzo.drawText("$ ${String.format(Locale.getDefault(), "%,.2f", item.precioSnapshot * item.cantidad)}", 500f, yPos, pincel)
            yPos += 15f
        }

        // --- TOTAL ---
        yPos += 30f
        lienzo.drawLine(350f, yPos, 550f, yPos, pincel)
        yPos += 20f
        pincelNegrita.textSize = 14f
        lienzo.drawText("TOTAL GENERAL:", 350f, yPos, pincelNegrita)
        lienzo.drawText("$ ${String.format(Locale.getDefault(), "%,.2f", presupuesto.totalGeneral)}", 500f, yPos, pincelNegrita)

        documento.finishPage(pagina)

        // Guardar en archivos de la app
        val carpetaPdfs = File(context.filesDir, "presupuestos_pdf")
        if (!carpetaPdfs.exists()) carpetaPdfs.mkdirs()
        
        val archivo = File(carpetaPdfs, "presupuesto_${presupuesto.idPresupuesto.takeLast(8)}.pdf")
        
        return try {
            val stream = FileOutputStream(archivo)
            documento.writeTo(stream)
            documento.close()
            stream.close()
            archivo
        } catch (e: Exception) {
            documento.close()
            null
        }
    }
}
