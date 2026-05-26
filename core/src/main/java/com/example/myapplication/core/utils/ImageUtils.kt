package com.example.myapplication.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * --- IMAGE UTILS (COMPARTIDO) ---
 * Centraliza la lógica de manipulación, compresión y optimización de imágenes.
 * Fundamental para cumplir con la política de "Costo Zero" al minimizar el uso de Storage.
 */
object ImageUtils {

    /**
     * Comprime una imagen desde una URI a formato WebP y retorna los bytes.
     * WebP ofrece una reducción de tamaño superior a JPEG/PNG manteniendo la calidad.
     */
    fun compressImageToWebP(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 50
    ): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var inSampleSize = 1
            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val scaledInputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(scaledInputStream, null, decodeOptions)
            scaledInputStream?.close()

            bitmap?.let {
                val outputStream = ByteArrayOutputStream()
                // Usamos WEBP_LOSSY si está disponible (API 30+), si no WEBP
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    it.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, outputStream)
                } else {
                    @Suppress("DEPRECATION")
                    it.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
                }
                outputStream.toByteArray()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte bytes a una cadena Base64.
     * Utilizado para enviar previsualizaciones rápidas o archivos pequeños por chat.
     */
    fun bytesToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Decodifica una cadena Base64 a un archivo local y retorna la ruta absoluta.
     * [PERSISTENCIA LOCAL]: Evita descargar la misma imagen múltiples veces de la nube.
     */
    fun saveBase64ToFile(
        context: Context,
        base64String: String,
        fileName: String,
        prefix: String = "IMG_",
        extension: String = ".webp"
    ): String? {
        return try {
            val bytes = Base64.decode(base64String, Base64.NO_WRAP)
            val directory = File(context.filesDir, "maverick_media")
            if (!directory.exists()) directory.mkdirs()
            
            val file = File(directory, "$prefix$fileName$extension")
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Guarda un ByteArray localmente (caché local).
     */
    fun saveBytesToFile(
        context: Context, 
        bytes: ByteArray, 
        fileName: String, 
        prefix: String = "IMG_", 
        extension: String = ".webp"
    ): String? {
        return try {
            val directory = File(context.filesDir, "maverick_media")
            if (!directory.exists()) directory.mkdirs()
            
            val file = File(directory, "$prefix$fileName$extension")
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
