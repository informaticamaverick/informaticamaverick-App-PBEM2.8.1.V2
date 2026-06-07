package com.example.myapplication.presentation.registry

import androidx.compose.ui.graphics.Color
import com.example.myapplication.core.utils.matchesSmart
import com.example.myapplication.core.utils.prepareForSearch
import com.example.myapplication.presentation.components.BeMessage
import com.example.myapplication.presentation.components.BeEmotion

/**
 * --- BE DICTIONARY CONVERSATION (EL DICCIONARIO DE LAS PALABRAS) ---
 * Centraliza los textos, tips y respuestas del asistente Be.
 * Separa la lógica conversacional de los activos visuales.
 */
object BeDictionaryConversation {

    // ======================================================================================
    // --- SECCIÓN 1: MENSAJERÍA POR CONTEXTO ---
    // ======================================================================================

    val HomeMessages = listOf(
        BeMessage("🏠", "Bienvenido a tu centro de operaciones.", null, Color(0xFF00F0FF)),
        BeMessage("🚀", "¿Buscas algo específico hoy?", "EXPLORAR", Color(0xFF10B981))
    )

    val BudgetMessages = listOf(
        BeMessage("⚖️", "Gestiona tus licitaciones con precisión.", null, Color(0xFF2197F5)),
        BeMessage("📊", "Compara presupuestos para decidir mejor.", "ANALIZAR", Color(0xFFFACC15))
    )

    val ChatMessages = listOf(
        BeMessage("💬", "Tus conversaciones activas están aquí.", null, Color(0xFF22D3EE)),
        BeMessage("📩", "No olvides revisar los presupuestos directos.", "VER RECIBIDOS", Color(0xFF8B5CF6))
    )

    val CalendarMessages = listOf(
        BeMessage("🗓️", "Mantén tu agenda bajo control.", null, Color(0xFFEC4899)),
        BeMessage("⏳", "Tienes compromisos próximos a vencer.", "REVISAR", Color(0xFFF59E0B))
    )

    val DefaultMessages = listOf(
        BeMessage("🤖", "Estoy aquí para ayudarte, Maverick.", null, Color(0xFF00F0FF))
    )

    // ======================================================================================
    // --- SECCIÓN 2: LÓGICA DE RESPUESTAS (MOCK AI) ---
    // ======================================================================================

    /**
     * Resuelve una respuesta conversacional basada en la consulta del usuario.
     * [ELITE] Utiliza coincidencia inteligente para ignorar acentos y ser flexible.
     */
    fun getResponse(query: String): BeMessage {
        val norm = query.prepareForSearch()
        
        // 1. Verificamos Huevos de Pascua (Matching exacto sobre normalizado)
        EasterEggs[norm]?.let {
            return BeMessage("🥚", it, null, Color(0xFFFFD700), emotion = BeEmotion.SURPRISED)
        }

        // 2. Lógica de intenciones básicas con matchesSmart
        return when {
            query.matchesSmart("hola") || query.matchesSmart("buenos dias") -> 
                BeMessage("👋", "¡Hola Maverick! ¿En qué puedo asistirte hoy?", null, Color(0xFF22D3EE))
            
            query.matchesSmart("presupuesto") || query.matchesSmart("licitacion") || query.matchesSmart("oferta") -> 
                BeMessage("📊", "Puedo ayudarte a comparar ofertas o crear una nueva licitación táctica.", "NUEVA LIC", Color(0xFF2197F5))
            
            query.matchesSmart("ayuda") || query.matchesSmart("que haces") ->
                SearchConversationalMessages.Help

            else -> BeMessage("🤖", "Entendido. Estoy procesando tu solicitud...", null, Color(0xFF22D3EE))
        }
    }

    // ======================================================================================
    // --- SECCIÓN 3: ESTRUCTURAS COMPLEJAS (HUEVOS Y BÚSQUEDA) ---
    // ======================================================================================

    object EasterEggMessages {
        val Step1 = BeMessage("🤖", "Porque soy el primer paso del Abecedario... y tu asistente Elite.", null, Color(0xFF22D3EE))
        val Step2 = BeMessage("🎉", "¡Exacto! Me alegra que lo entiendas. Maverick mode activado.", "CELEBRAR", Color(0xFFFACC15))
        val Failure = BeMessage("😕", "Parece que no captaste la ironía táctica... Seguimos operando.", null, Color(0xFFEF4444))
    }

    object SearchConversationalMessages {
        val Welcome = BeMessage("👋", "¡Hola Maverick! ¿En qué puedo asistirte hoy?", null, Color(0xFF22D3EE))
        val Help = BeMessage("🛠️", "Puedo ayudarte a buscar servicios, comparar presupuestos o gestionar tu agenda.", "VER TIPS", Color(0xFFF59E0B))
        val WhoAmI = BeMessage("🤖", "Soy Be, tu enlace con la infraestructura Maverick.", null, Color(0xFF22D3EE))
        val NotFound = BeMessage("🔍", "No he encontrado resultados exactos, pero puedo buscar en categorías similares.", "BUSCAR", Color(0xFF22D3EE))
    }

    // ======================================================================================
    // --- SECCIÓN 4: HUEVOS DE PASCUA (SISTEMA ELITE - MAPA) ---
    // ======================================================================================
    
    val EasterEggs = mapOf(
        "maverick_mode" to "Aumentando rendimiento al 120%...",
        "be_sleepy" to "Casi es hora de apagar los sistemas...",
        "coffee_break" to "Inyectando cafeína en los hilos de ejecución..."
    )
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
