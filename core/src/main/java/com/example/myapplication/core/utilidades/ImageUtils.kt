package com.example.myapplication.core.utilidades

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
     * Comprime una imagen desde bytes a formato WebP.
     */
    fun compressBytesToWebP(
        bytes: ByteArray,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 50
    ): ByteArray? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            var inSampleSize = 1
            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)

            bitmap?.let {
                val outputStream = ByteArrayOutputStream()
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
     * [ELITE]: Limpia una URL de foto de Google para obtener la imagen original
     * (sin parámetros de recorte o tamaño que suelen venir por defecto).
     */
    fun sanitizeGooglePhotoUrl(url: String): String {
        return if (url.contains("googleusercontent.com")) {
            // Eliminamos parámetros como =s96-c, =s32, etc.
            url.substringBeforeLast('=')
        } else url
    }

    /**
     * Obtiene los bytes de una imagen desde una URI (Local o Remota).
     * [v2026.ELITE]: Maneja descarga táctica para fotos de Google/Red.
     */
    suspend fun getBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val scheme = uri.scheme?.lowercase()
                val uriString = uri.toString()
                
                if (scheme == "http" || scheme == "https" || uriString.startsWith("http")) {
                    android.util.Log.d("ImageUtils", "🌐 Descargando imagen remota: $uriString")
                    val connection = java.net.URL(uriString).openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.doInput = true
                    connection.connect()
                    connection.inputStream.readBytes()
                } else {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

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
     * [SUPREME COMPRESSION]: Para fotos de perfil de alta visibilidad.
     * Calidad 90% y resolución 1200px.
     */
    fun compressSupreme(context: Context, uri: Uri): ByteArray? {
        return compressImageToWebP(
            context = context,
            uri = uri,
            maxWidth = 1200,
            maxHeight = 1200,
            quality = 90
        )
    }

    /**
     * [ELITE THUMBNAIL]: Genera una previsualización de ultra-baja resolución.
     * Resolución: 50px | Calidad: 20% | Formato: WebP
     */
    fun generateThumbnailFromBytes(bytes: ByteArray): String? {
        val compressed = compressBytesToWebP(
            bytes = bytes,
            maxWidth = 50,
            maxHeight = 50,
            quality = 20
        )
        return compressed?.let { bytesToBase64(it) }
    }

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
     * Convierte un archivo local o URI a una cadena Base64.
     */
    fun fileToBase64(context: Context, filePath: String): String? {
        return try {
            if (filePath.startsWith("content://") || filePath.startsWith("file://")) {
                val uri = Uri.parse(filePath)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    bytesToBase64(inputStream.readBytes())
                }
            } else {
                val file = File(filePath)
                if (file.exists()) {
                    bytesToBase64(file.readBytes())
                } else {
                    val uri = Uri.parse(filePath)
                    if (uri.scheme != null) {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            bytesToBase64(inputStream.readBytes())
                        }
                    } else null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte un archivo local a una cadena Base64.
     */
    fun fileToBase64(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                bytesToBase64(file.readBytes())
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte cualquier Uri (content://, file://, etc) a una cadena Base64 comprimida en WebP.
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        val bytes = compressElite(context, uri) ?: return null
        return bytesToBase64(bytes)
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
            val directory = File(context.filesDir, "app_media")
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
            val directory = File(context.filesDir, "app_media")
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
     * [SUPREME.FIX]: Prepara un objeto de imagen para ser guardado como String en la DB.
     * Detecta y anula punteros de memoria corruptos [B@... y convierte ByteArray a Base64.
     */
    fun prepareForStorage(source: Any?): String? {
        if (source == null) return null
        return when (source) {
            is ByteArray -> bytesToBase64(source)
            else -> {
                val s = source.toString().trim()
                if (s.isBlank() || s.startsWith("[B@")) null else s
            }
        }
    }

    /**
     * [ELITE SSOT]: Procesa un string de imagen (URL o Base64) y lo convierte
     * en un objeto listo para ser consumido por Coil (String o ByteArray).
     * Incluye una caché interna para evitar re-decodificar el mismo Base64 (Ley #3).
     */
    private val decodingCache = LruCache<String, ByteArray>(50)

    fun processImageSource(source: Any?): Any? {
        if (source == null) return null
        if (source is ByteArray) return source
        
        val srcStr = source.toString()
        if (srcStr.isBlank()) return null
        
        val trimmed = srcStr.trim()

        // [SUPREME.FIX]: Detectamos y matamos punteros de memoria corruptos [B@...
        if (trimmed.startsWith("[B@")) {
            android.util.Log.e("ImageUtils", "⚠️ Detectada imagen corrupta [B@... Anulando para evitar crash.")
            return null
        }

        // 1. URLs de red o esquemas conocidos
        if (trimmed.startsWith("http") || trimmed.startsWith("content://") || trimmed.startsWith("file://")) {
            return trimmed
        }

        // 2. Rutas locales absolutas
        if (trimmed.startsWith("/")) {
            val file = java.io.File(trimmed)
            return if (file.exists()) "file://$trimmed" else null
        }

        // 3. Base64 (Ley #3: Miniaturas y Tránsito Efímero)
        // [ELITE] Solo intentamos decodificar si parece Base64 (sin caracteres de URL como : o .)
        val isLikelyBase64 = !trimmed.contains(":") && !trimmed.contains(".") && (trimmed.length > 20)

        if (isLikelyBase64) {
            return try {
                decodingCache.get(trimmed) ?: android.util.Base64.decode(trimmed, android.util.Base64.DEFAULT).also {
                    decodingCache.put(trimmed, it)
                }
            } catch (e: Exception) {
                trimmed // Fallback al original
            }
        }

        return trimmed
    }

    /**
     * Descarga una imagen desde una URL y la guarda localmente.
     * [ELITE]: Útil para sincronización inicial y persistencia de perfiles remotos.
     */
    suspend fun downloadAndSave(context: Context, url: String, fileName: String, prefix: String = "ORIG_"): String? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.doInput = true
                connection.connect()
                val bytes = connection.inputStream.readBytes()
                saveBytesToFile(context, bytes, fileName, prefix = prefix)
            } catch (e: Exception) {
                null
            }
        }
    }
}

