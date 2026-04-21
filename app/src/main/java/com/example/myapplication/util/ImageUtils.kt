package com.example.myapplication.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Utilidades para el manejo de imágenes, optimización y compresión.
 * [ACTUALIZADO] Soporte para WebP, Base64 y guardado local para evitar costos de nube.
 */
object ImageUtils {

    /**
     * Comprime una imagen desde una URI a formato WebP y retorna los bytes.
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
     */
    fun bytesToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Decodifica una cadena Base64 a un archivo local y retorna la ruta absoluta.
     * [SECCIÓN: PERSISTENCIA] Maneja tanto imágenes como audios.
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
            val directory = File(context.filesDir, "chat_media")
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
     * Guarda un ByteArray localmente (útil para el emisor antes de enviar).
     */
    fun saveBytesToFile(context: Context, bytes: ByteArray, fileName: String): String? {
        return try {
            val directory = File(context.filesDir, "chat_media")
            if (!directory.exists()) directory.mkdirs()
            
            val file = File(directory, "IMG_$fileName.webp")
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
