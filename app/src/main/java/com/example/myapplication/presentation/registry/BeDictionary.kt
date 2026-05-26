package com.example.myapplication.presentation.registry


import androidx.compose.ui.graphics.Color
import com.example.myapplication.presentation.components.BeEmotion
import com.example.myapplication.presentation.components.BeMessage





/** * --- DICCIONARIO CENTRALIZADO DE BE para burbuja de Conversación (MODO BÚSQUEDA) --- */
object BeConversacion {

    /**
     * Procesa la consulta del usuario y devuelve una respuesta coherente.
     * Si no encuentra coincidencias, devuelve un mensaje de confusión.
     */
    fun getResponse(query: String): BeMessage {
        val normQuery = query.lowercase().trim()

        return when {
            // 1. Saludos
            normQuery.contains("hola") || normQuery.contains("buen") -> 
                BeDictionary.SearchConversationalMessages.Welcome

            // 2. ¿Quién eres?
            normQuery.contains("quien eres") || normQuery.contains("que sos") || normQuery.contains("nombre") ->
                BeDictionary.SearchConversationalMessages.WhoAmI

            // 3. Ayuda / Tips
            normQuery.contains("ayuda") || normQuery.contains("como") || normQuery.contains("que puedes hacer") ->
                BeDictionary.SearchConversationalMessages.Help

            // 4. Manejo de texto incoherente o sin coincidencias (REQUISITO DEL PLAN)
            normQuery.length > 5 && !normQuery.contains(" ") && normQuery.any { it.isDigit() } ->
                BeMessage("🤔", "Estoy un poco confundido, ¿podrías explicarme de otra forma? 😅", null, Color.Gray, emotion = BeEmotion.THINKING)

            // 5. Fallback general
            else -> BeDictionary.SearchConversationalMessages.NotFound
        }
    }

}
/** * --- DICCIONARIO CENTRALIZADO DE BE para buebuja inferior por pantalla  --- */
object BeDictionary {
    val HomeMessages = listOf(
        BeMessage("💡", "Usa el Menú Táctico inferior para filtrar prestadores verificados.", null, Color(0xFF22D3EE), emotion = BeEmotion.NORMAL),
        BeMessage("💡", "Guía Rápida para el Usuario\n" +
                "• Toque Simple: Activa o alterna el estado del filtro/ordenamiento.\n" +
                "• Icono Gris: El filtro está desactivado.\n" +
                "• Icono Resaltado: El filtro está aplicando cambios en la lista actual.\n" +
                "• Botón Fecha: Mantén presionado para abrir el calendario personalizado.", null, Color(0xFF22D3EE), emotion = BeEmotion.NORMAL),
        BeMessage("🚀", "¡Nuevas categorías disponibles! Explora los servicios destacados hoy.", null, Color(0xFF10B981), emotion = BeEmotion.HAPPY)
    )
    val BudgetMessages = listOf(
        BeMessage("⚖️", "Selecciona múltiples ofertas para que yo pueda ayudarte a analizarlas y compararlas.", "ANALIZAR", Color(0xFF9B51E0), Color.White, BeEmotion.HAPPY),
        BeMessage("📋", "Recuerda revisar los detalles de cada presupuesto antes de aceptar.", null, Color(0xFFFACC15), emotion = BeEmotion.NORMAL)
    )
    val ChatMessages = listOf(
        BeMessage("💬", "Nunca compartas datos de tarjetas de crédito o contraseñas a través del chat.", null, Color(0xFFF43F5E), Color.White, BeEmotion.ANGRY),
        BeMessage("👀", "Si el prestador no responde, puedo ayudarte a buscar alternativas rápidas.", "BUSCAR", Color(0xFF22D3EE), emotion = BeEmotion.NORMAL)
    )
    val CalendarMessages = listOf(
        BeMessage("📅", "Recuerda que si cancelas un turno, el sistema le avisará automáticamente.", null, Color(0xFF10B981), emotion = BeEmotion.NORMAL),
        BeMessage("⏰", "Tienes turnos pendientes de confirmación. ¡No los pierdas!", "VER TURNOS", Color(0xFFF59E0B), emotion = BeEmotion.SURPRISED)
    )

    // ==========================================================================================
    // --- SECCIÓN: MENSAJES CONVERSACIONALES (Burbuja de búsqueda) ---
    // ==========================================================================================
    object SearchConversationalMessages {
        val Welcome = BeMessage("👋", "¡Hola! Estoy listo para ayudarte a encontrar lo que buscas.", null, Color(0xFF00FFFF), emotion = BeEmotion.HAPPY)
        val Help = BeMessage("💡", "Puedo filtrar por categorías, ordenar por precio o buscar lugares cercanos.", "VER TIPS", Color(0xFF00FFFF), emotion = BeEmotion.THINKING)
        val WhoAmI = BeMessage("✨", "Soy Be, tu asistente inteligente. Mi misión es optimizar tu búsqueda.", null, Color(0xFFE11D48), emotion = BeEmotion.HAPPY)
        val NotFound = BeMessage("❓", "No estoy muy seguro de qué buscas... ¿Podrías intentar con otra palabra?", null, Color.Gray, emotion = BeEmotion.THINKING)
        fun Searching(query: String) = BeMessage("🔍", "Buscando coincidencias para \"$query\"...", null, Color(0xFF00FFFF), emotion = BeEmotion.THINKING)
    }

    // ==========================================================================================
    // --- SECCIÓN: HUEVO DE PASCUA (MAXI - BEM ORIGINS) ---
    // ==========================================================================================
    object EasterEggMessages {
        val Step1 = BeMessage(
            icon = "💡",
            text = "Me llamo Be, por que Buscar se escribe con B 😂",
            bubbleColor = Color(0xFF22D3EE),
            emotion = BeEmotion.HAPPY
        )
        val Step2 = BeMessage(
            icon = "❤️",
            text = "🌟 ¡FELICIDADES! 🌟\n\n" +
                    "Has descubierto el secreto mejor guardado...\n" +
                    "El Huevo de Pascua de Maxi...\n" +
                    "Te voy a contar la verda de mi Nombre...\n" +
                    "Be, es un Acronimo !!!\n\n" +
                    "Son las primeras letras de los logros mas importantes de mi Desarrollador\n\n" +
                    "💖 B... por  Bautista 💖 \n" +
                    "💖 E... por  Emma 💖\n\n" +
                    "Ellos son su verdadera inspiración \n " +
                    "y esa es la razon de mi nombre, o a caso no es COOL BE ??? 😁 \n " +
                    "Pero te pido algo? ¡ Guarda el secreto Por Favor! 🙊\n\n" +
                    "En Maverick Developers trabajamos con el corazón y siempre dando nuestro MAXIMO ESFUERZO !!! \n " +
                    "para tratar de inspirar y ayudar a las personas \n " +
                    "y de esta manera TODOS tengamos las mismas oportunidades.\n\n" +
                    "¡GRACIAS POR SER PARTE DE ESTO Y USAR NUESTRA APP!\n" +
                    "🚀 GRACIAS TOTALES 🚀\n\n" +
                    "🎁 Ahora lo mejor , tu regalo !!! 🎁 \n" +
                    "por que nada es gratis en la vida, esta es la recompensa a tu esfuerzo por completar este desafio 🐣",
            bubbleColor = Color(0xFFFFB6C1),
            actionText = "OBTENER REGALO",
            emotion = BeEmotion.BLUSHING,
            isCentered = true
        )
        val Failure = BeMessage(
            icon = "😢",
            text = "esta bien esperaba que me dijeras que era un buen chite 😔😢😭",
            bubbleColor = Color.Gray,
            emotion = BeEmotion.SAD
        )
    }

    val DefaultMessages = listOf(
        BeMessage("🤖", "Hola, soy Be. Estoy aquí para asistirte en todo lo que necesites.", null, Color(0xFF22D3EE), emotion = BeEmotion.NORMAL)
    )
}
