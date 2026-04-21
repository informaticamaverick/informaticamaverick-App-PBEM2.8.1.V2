package com.example.myapplication.presentation.components

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.*
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.ProviderRepository
import com.example.myapplication.presentation.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.myapplication.presentation.client.CategoryVisuals
import com.example.myapplication.data.local.SembradoServiciosInicia
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/**
 * --- MOTOR DE SIMULACIÓN MAVERICK ULTRA 2.0 ---
 * Genera flujos de trabajo hiper-realistas:
 * 1. Mensajería con delays humanos.
 * 2. Presupuestos con desgloses técnicos por categoría.
 * 3. Variabilidad económica (Perfiles: Económico, Normal, Premium).
 */
@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val chatRepository: ChatRepository,
    private val budgetRepository: BudgetRepository,
    private val providerRepository: ProviderRepository,
    private val application: Application
) : ViewModel() {

    private val notificationHelper = NotificationHelper(application)

    // ==================================================================================
    // --- 🚀 SECCIÓN: MIGRACIÓN DE CATEGORÍAS A FIRESTORE (PLAN DE ACCIÓN) ---
    // ==================================================================================
    /**
     * Sube todas las categorías hardcoded de CategorySampleDataFalso a Firestore.
     * Colección: "Servicios"
     * Se utiliza Batch para mayor eficiencia.
     */
    fun uploadCategoriesToFirestore() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val batch = db.batch()
                val collectionRef = db.collection("Servicios")

                SembradoServiciosInicia.categories.forEach { item ->
                    val docRef = collectionRef.document() // Genera ID automático
                    
                    // Convertimos el Color a String Hex para Firestore
                  //  val colorHex = String.format("#%08X", item.color.value.toLong())

                    val dto = hashMapOf(
                        "name" to item.name,
                        "icon" to item.icon,
                        "description" to item.description, // [CORRECCIÓN] Se agrega la descripción
                        "superCategory" to item.superCategory,
                        "superCategoryIcon" to item.superCategoryIcon,
                       // "imageUrl" to item.imageUrl,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, dto)
                }

                // Actualizamos la versión en config/metadata
                val metadataRef = db.collection("config").document("metadata")
                batch.set(metadataRef, mapOf("categoriesVersion" to System.currentTimeMillis() / 1000))

                batch.commit().await()
                
                Toast.makeText(application, "¡MIGRACIÓN EXITOSA! Categorías en 'Servicios'", Toast.LENGTH_LONG).show()
                notificationHelper.showNotification("Admin", "Sincronización Firestore completada.")
                
            } catch (e: Exception) {
                Toast.makeText(application, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun simulateFullChatFlow() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val provider = selectRealisticProvider(currentUserId) ?: return@launch
            val chatId = "chat_${currentUserId}_${provider.id}"

            val greetings = listOf(
                "¡Hola! Vi tu consulta. Preparé un presupuesto detallado para la solución.",
                "¿Qué tal? Analicé tu caso y aquí tienes la cotización completa.",
                "Buenas tardes. Te envío el presupuesto con materiales y mano de obra incluidos."
            ).random()
            
            simulateMessage(chatId, currentUserId, provider, greetings, MessageType.TEXT)
            
            delay(3000) // Simula realismo

            val newBudget = createUltraRealisticBudget(currentUserId, provider, null)
            budgetRepository.receiveBudgetFromChat(newBudget)

            simulateMessage(
                chatId = chatId,
                currentUserId = currentUserId,
                provider = provider,
                text = "Presupuesto Técnico #${newBudget.budgetId.takeLast(4)}",
                type = MessageType.BUDGET,
                relatedId = newBudget.budgetId
            )

            notificationHelper.showNotification("Maverick", "${provider.displayName} te envió una propuesta.")
        }
    }

    /**
     * 🔥 [NUEVO] Simulación de 5 presupuestos directos de distintos prestadores al chat.
     * Garantiza el envío de al menos 5 mensajes de distintos prestadores con presupuestos.
     */
    fun simulateFiveDirectBudgetsToChat() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val allProviders = providerRepository.allProviders.first().shuffled()
            
            if (allProviders.size < 5) {
                Toast.makeText(application, "Necesitas al menos 5 prestadores para esta simulación.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val selectedProviders = allProviders.take(5)

            selectedProviders.forEachIndexed { index, provider ->
                val chatId = "chat_${currentUserId}_${provider.id}"
                
                // Mensaje de saludo realista
                val greetings = listOf(
                    "Hola! Analicé lo que necesitabas y te armé este presupuesto.",
                    "Buenas tardes, un gusto. Aquí te envío mi propuesta detallada.",
                    "Hola, vi tu pedido. Te adjunto el presupuesto para que lo revises.",
                    "¿Cómo estás? Te paso la cotización por el servicio solicitado.",
                    "Hola! Te envío el presupuesto técnico con el desglose de materiales."
                ).random()
                
                simulateMessage(chatId, currentUserId, provider, greetings, MessageType.TEXT)
                
                delay(Random.nextLong(1000, 2500)) // Delay para simular escritura

                // Crear presupuesto con precio variado
                val budget = createUltraRealisticBudget(currentUserId, provider, null)
                budgetRepository.receiveBudgetFromChat(budget)

                // Enviar mensaje del presupuesto al chat
                simulateMessage(
                    chatId = chatId,
                    currentUserId = currentUserId,
                    provider = provider,
                    text = "📄 Presupuesto Directo #${budget.budgetId.takeLast(4)}",
                    type = MessageType.BUDGET,
                    relatedId = budget.budgetId
                )
                
                delay(800)
            }
            notificationHelper.showNotification("Simulación", "Has recibido 5 nuevos presupuestos en tus chats.")
        }
    }

    /**
     * 🔥 [NUEVO] Simulación de respuestas para TODAS las licitaciones activas.
     * Envía por lo menos 5 presupuestos de distintos prestadores por cada licitación abierta.
     */
    fun simulateTenderResponsesForEachActive() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val openTenders = budgetRepository.getOpenTenders()
            
            if (openTenders.isEmpty()) {
                Toast.makeText(application, "No tienes licitaciones ABIERTAS para simular respuestas.", Toast.LENGTH_LONG).show()
                return@launch
            }

            openTenders.forEach { tender ->
                // Buscamos prestadores de la misma categoría de la licitación
                val matchingProviders = providerRepository.getProvidersByCategory(tender.category).shuffled()
                
                // Si no hay suficientes en la categoría, completamos con generales para llegar a 5
                val selectedProviders = if (matchingProviders.size >= 5) {
                    matchingProviders.take(5)
                } else {
                    val all = providerRepository.allProviders.first().shuffled()
                    (matchingProviders + all.filter { it.id !in matchingProviders.map { p -> p.id } }).take(5)
                }

                selectedProviders.forEach { provider ->
                    // Crear presupuesto vinculado a la licitación con alta variabilidad de precio
                    val budget = createUltraRealisticBudget(currentUserId, provider, tender.tenderId, tender.category)
                    budgetRepository.receiveBudgetFromChat(budget)
                    
                    // También enviamos un aviso al chat para mayor realismo
                    val chatId = "chat_${currentUserId}_${provider.id}"
                    simulateMessage(
                        chatId = chatId,
                        currentUserId = currentUserId,
                        provider = provider,
                        text = "¡Hola! He enviado una propuesta para tu licitación de '${tender.title}'. Quedo a tu disposición.",
                        type = MessageType.TEXT
                    )
                }
            }

            notificationHelper.showNotification("Licitaciones", "Se han generado ofertas para todas tus licitaciones activas (${openTenders.size}).")
        }
    }

    fun simulateMassiveTenderResponses() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val openTenders = budgetRepository.getOpenTenders()
            val allProviders = providerRepository.allProviders.first()

            if (openTenders.isEmpty()) {
                Toast.makeText(application, "Crea una licitación ABIERTA primero.", Toast.LENGTH_LONG).show()
                return@launch
            }

            val targetTender = openTenders.random()
            repeat(15) {
                val provider = allProviders.random()
                val budget = createUltraRealisticBudget(currentUserId, provider, targetTender.tenderId, targetTender.category)
                budgetRepository.receiveBudgetFromChat(budget)
            }

            notificationHelper.showNotification("Licitación", "Recibiste 15 nuevas ofertas reales.")
        }
    }

    fun simulateNewPromotions() {
        viewModelScope.launch {
            notificationHelper.showNotification("Promo Flash", "¡Nuevos descuentos del 30% en Climatización detectados!")
        }
    }

    private suspend fun selectRealisticProvider(myId: String): Provider? {
        val all = providerRepository.allProviders.first()
        return if (all.isEmpty()) null else all.random()
    }

    private suspend fun simulateMessage(chatId: String, currentUserId: String, provider: Provider, text: String, type: MessageType, relatedId: String? = null) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = provider.id,
            receiverId = currentUserId,
            type = type,
            content = text,
            timestamp = System.currentTimeMillis(),
            relatedId = relatedId,
            status = "SENT"
        )
        chatRepository.sendMessage(message)
    }

    private fun createUltraRealisticBudget(clientId: String, provider: Provider, tenderId: String?, forceCategory: String? = null): BudgetEntity {
        // Mayor variabilidad de precios: Factor base + micro-variabilidad aleatoria
        val baseFactor = listOf(0.7, 0.9, 1.1, 1.3, 1.8, 2.5).random()
        val microVariability = Random.nextDouble(0.9, 1.15)
        val profileFactor = baseFactor * microVariability

        val category = forceCategory ?: listOf("Electricidad", "Plomería", "Climatización").random()
        
        val items = mutableListOf<BudgetItem>()
        val services = mutableListOf<BudgetService>()
        val professionalFees = mutableListOf<BudgetProfessionalFee>()

        when(category) {
            "Electricidad" -> {
                items.add(BudgetItem(description = "Cable Unipolar 2.5mm (Normalizado)", quantity = 1, unitPrice = 45000.0 * profileFactor))
                items.add(BudgetItem(description = "Térmica Sica 2x20A", quantity = 2, unitPrice = 8500.0 * profileFactor))
                services.add(BudgetService(description = "Instalación de Tablero y Cableado Técnico", total = 30000.0 * profileFactor))
                if (profileFactor > 1.5) professionalFees.add(BudgetProfessionalFee(description = "Certificado de Aptitud Eléctrica", total = 12000.0))
            }
            "Plomería" -> {
                items.add(BudgetItem(description = "Kit Termofusión Agua Fría/Caliente", quantity = 1, unitPrice = 15000.0 * profileFactor))
                items.add(BudgetItem(description = "Grifería Monocomando Premium", quantity = 1, unitPrice = 85000.0 * profileFactor))
                services.add(BudgetService(description = "Mano de Obra: Instalación Sanitaria", total = 45000.0 * profileFactor))
            }
            "Climatización" -> {
                items.add(BudgetItem(description = "Carga Gas Refrigerante R410", quantity = 1, unitPrice = 28000.0 * profileFactor))
                services.add(BudgetService(description = "Mantenimiento y Limpieza de Filtros", total = 18000.0 * profileFactor))
            }
            else -> {
                items.add(BudgetItem(description = "Insumos Varios de Obra", quantity = 1, unitPrice = 15000.0 * profileFactor))
                services.add(BudgetService(description = "Servicio Técnico General", total = 25000.0 * profileFactor))
            }
        }

        val subtotal = items.sumOf { it.unitPrice * it.quantity } + services.sumOf { it.total } + professionalFees.sumOf { it.total }
        val taxes = subtotal * 0.21

        return BudgetEntity(
            budgetId = "SIM-${UUID.randomUUID().toString().take(5).uppercase()}",
            clientId = clientId,
            providerId = provider.id,
            tenderId = tenderId,
            category = category, // 🔥 Fundamental para el filtrado inteligente
            providerName = provider.displayName,
            providerCompanyName = provider.companies.firstOrNull()?.name ?: "${provider.lastName} Soluciones Técnicas",
            providerPhotoUrl = provider.photoUrl,
            items = items,
            services = services,
            professionalFees = professionalFees,
            subtotal = subtotal,
            taxAmount = taxes,
            grandTotal = subtotal + taxes,
            notes = if(profileFactor > 2.0) "Presupuesto Premium con garantía extendida de 24 meses." else "Válido por 48 horas.",
            status = BudgetStatus.PENDIENTE,
            dateTimestamp = System.currentTimeMillis()
        )
    }
}
