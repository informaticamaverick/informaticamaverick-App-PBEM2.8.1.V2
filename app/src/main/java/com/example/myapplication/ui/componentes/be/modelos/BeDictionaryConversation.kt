/*
package com.example.myapplication.ui.componentes.be.modelos

import androidx.compose.ui.graphics.Color

object BeDictionaryConversation {
    val DefaultMessages = listOf(
        MensajeBe("👋", "¡Hola! Soy Be, tu asistente táctico.", null, Color(0xFF22D3EE), emocion = EmocionBe.FELIZ),
        MensajeBe("🚀", "¡Vamos a encontrar ese servicio que necesitás!", null, Color(0xFF22D3EE), emocion = EmocionBe.PENSANDO)
    )
    val HomeMessages = listOf(
        MensajeBe("🏠", "¡Bienvenido! ¿Qué rubro buscamos hoy?", null, Color(0xFF22D3EE), emocion = EmocionBe.NORMAL),
        MensajeBe("⚡", "Si tenés una urgencia, dale al botón FAST.", "IR A FAST", Color(0xFF22D3EE), emocion = EmocionBe.FELIZ)
    )
    val ChatMessages = listOf(MensajeBe("💬", "Tus charlas están bajo llave y bien seguras. 🔐", null, Color(0xFF22D3EE), emocion = EmocionBe.SONROJADO))
    val BudgetMessages = listOf(MensajeBe("💰", "Acá tenés tus presupuestos y licitaciones a mano.", null, Color(0xFF22D3EE), emocion = EmocionBe.PENSANDO))
    val CalendarMessages = listOf(MensajeBe("📅", "¡Ojo! Que no se te pase ningún compromiso.", null, Color(0xFF22D3EE), emocion = EmocionBe.NORMAL))
    val PromoMessages = listOf(MensajeBe("🔥", "¡Mirá estas ofertas bomba cerca tuyo!", null, Color(0xFF22D3EE), emocion = EmocionBe.SORPRENDIDO))
    
    val SelectionMessages = listOf(
        MensajeBe("🎯", "¡Listo! Modo selección activado.", null, Color(0xFF22D3EE), emocion = EmocionBe.NORMAL),
        MensajeBe("🛠️", "Tenés acciones masivas acá a la derecha.", null, Color(0xFF22D3EE), emocion = EmocionBe.PENSANDO)
    )

    val CategoryDetailMessages = listOf(
        MensajeBe("🌾", "¡Excelente rubro! Estamos buscando a los mejores acá.", null, Color(0xFF22D3EE), emocion = EmocionBe.FELIZ),
        MensajeBe("🔍", "Podés filtrar o usar el buscador táctico arriba.", null, Color(0xFF22D3EE), emocion = EmocionBe.PENSANDO)
    )

    fun getResponse(query: String): MensajeBe {
        return when {
            query.contains("hola", ignoreCase = true) -> MensajeBe("👋", "¡Hola! ¿En qué puedo ayudarte?", null, Color(0xFF22D3EE), emocion = EmocionBe.FELIZ)
            query.contains("precio", ignoreCase = true) || query.contains("cuanto", ignoreCase = true) -> MensajeBe("💰", "Los precios varían según el profesional.", null, Color(0xFF22D3EE), emocion = EmocionBe.PENSANDO)
            else -> MensajeBe("🤖", "Estoy analizando tu consulta...", null, Color(0xFF22D3EE), emocion = EmocionBe.PENSANDO)
        }
    }
}
*/
