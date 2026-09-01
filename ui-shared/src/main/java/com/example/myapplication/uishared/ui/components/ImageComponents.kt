package com.example.myapplication.uishared.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import android.util.Base64

/**
 * Convierte un string de foto (URL, content://, base64 raw) al modelo correcto para Coil.
 */
@Composable
fun rememberImageModel(photoString: String?): Any? {
    return remember(photoString) {
        if (photoString.isNullOrBlank()) return@remember null

        // [COSTO ZERO] Decodificamos Base64 si no es una URL/URI
        if (photoString.length > 100 && !photoString.startsWith("http") && !photoString.startsWith("content")) {
            try {
                Base64.decode(photoString, Base64.DEFAULT)
            } catch (_: Exception) { photoString }
        } else {
            photoString
        }
    }
}

/**
 * Extension function para convertir un String? a un modelo compatible con Coil (String o ByteArray).
 */
fun String?.asCoilModel(): Any? {
    if (this.isNullOrBlank()) return null
    
    if (this.startsWith("http") || this.startsWith("content://") || this.startsWith("file://")) {
        return this
    }

    return try {
        if (this.length > 100) {
            Base64.decode(this, Base64.DEFAULT)
        } else {
            this
        }
    } catch (e: Exception) {
        this
    }
}

































