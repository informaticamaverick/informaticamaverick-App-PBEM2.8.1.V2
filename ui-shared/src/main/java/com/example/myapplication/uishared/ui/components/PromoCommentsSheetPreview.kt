/*
package com.example.myapplication.uishared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.core.dominio.modelos.PromoComment
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- PREVIEW DE COMENTARIOS (INSTAGRAM STYLE) ---
 * [ELITE v2026.7]: Permite visualizar el diseño de la caja de comentarios
 * con el nuevo input bar y la fila de emojis.
 * [LEY #9]: Adaptado al estándar Mav (Español).
 */
@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun PromoCommentsSheetPreview() {
    val mockComments = listOf(
        PromoComment(
            id = "c1",
            nombreUsuario = "maverick.dev",
            urlFotoUsuario = "https://picsum.photos/100",
            texto = "¡Excelente promoción! El diseño es de otro nivel. 🔥🔥🔥",
            marcaTiempo = System.currentTimeMillis() - 3600000
        ),
        PromoComment(
            id = "c2",
            nombreUsuario = "elite.studio",
            urlFotoUsuario = "https://picsum.photos/101",
            texto = "¿Tienen disponibilidad para la próxima semana? 🙌",
            marcaTiempo = System.currentTimeMillis() - 7200000
        ),
        PromoComment(
            id = "c3",
            nombreUsuario = "app_user",
            urlFotoUsuario = "https://picsum.photos/102",
            texto = "Súper recomendado el servicio.",
            marcaTiempo = System.currentTimeMillis() - 10800000
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SharedPalette.DarkBg)
    ) {
        // Simulamos la apertura de la sheet
        PromoCommentsSheet(
            promocionId = "preview_id",
            onDismiss = {},
            comments = mockComments,
            onSendComment = {},
            currentUserPhoto = "https://picsum.photos/200"
        )
    }
}
*/
































