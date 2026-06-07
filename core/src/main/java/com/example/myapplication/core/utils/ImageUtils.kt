package com.example.myapplication.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import androidx.exifinterface.media.ExifInterface
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
     * [FIX v8.9]: Ahora maneja correctamente la orientación EXIF.
     */
    fun compressImageToWebP(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 50
    ): ByteArray? {
        return try {
            // 1. Resolver orientación EXIF antes de nada
            val rotation = getExifRotation(context, uri)

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
            var bitmap = BitmapFactory.decodeStream(scaledInputStream, null, decodeOptions)
            scaledInputStream?.close()

            // 2. Aplicar rotación si es necesaria
            if (rotation != 0 && bitmap != null) {
                bitmap = rotateBitmap(bitmap, rotation)
            }

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

    private fun getExifRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * [ELITE COMPRESSION]: Versión optimizada para chat y licitaciones.
     * Limita el tamaño a 1024px y usa calidad balanceada (75%) para "Costo Zero".
     */
    fun compressElite(context: Context, uri: Uri): ByteArray? {
        return compressImageToWebP(
            context = context,
            uri = uri,
            maxWidth = 1024,
            maxHeight = 1024,
            quality = 80 // Protocolo v3.5
        )
    }

    /**
     * [ELITE THUMBNAIL]: Genera una previsualización de ultra-baja resolución.
     * Resolución: 50px | Calidad: 20% | Formato: WebP
     * Ideal para mostrar instantáneamente mientras carga el Base64 completo.
     */
    fun generateThumbnailBase64(context: Context, uri: Uri): String? {
        val bytes = compressImageToWebP(
            context = context,
            uri = uri,
            maxWidth = 50,
            maxHeight = 50,
            quality = 20
        )
        return bytes?.let { bytesToBase64(it) }
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

    /**
     * [ELITE SSOT]: Procesa un string de imagen (URL o Base64) y lo convierte
     * en un objeto listo para ser consumido por Coil (String o ByteArray).
     * Incluye una caché interna para evitar re-decodificar el mismo Base64 (Ley #3).
     */
    private val decodingCache = LruCache<String, ByteArray>(50)

    fun processImageSource(source: String?): Any? {
        if (source.isNullOrBlank()) return null
        
        // Si es una URL o una URI de contenido, la devolvemos tal cual
        if (source.startsWith("http") || source.startsWith("content://") || source.startsWith("file://")) {
            return source
        }

        // 🔥 [FIX v8.8] Si es una ruta absoluta, anteponemos file:// para Coil
        if (source.startsWith("/")) {
            return "file://$source"
        }

        // Si es un Base64 (detectado por longitud y falta de prefijo de red), decodificamos a ByteArray
        return try {
            if (source.length > 100) {
                // [CACHE ELITE]: Evitamos generar nuevos ByteArrays si el string es el mismo
                decodingCache.get(source) ?: Base64.decode(source, Base64.DEFAULT).also {
                    decodingCache.put(source, it)
                }
            } else {
                source
            }
        } catch (e: Exception) {
            source // Fallback al string original si falla la decodificación
        }
    }
}
