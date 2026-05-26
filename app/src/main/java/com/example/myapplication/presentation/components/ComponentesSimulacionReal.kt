package com.example.myapplication.presentation.components

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.entity.*
import com.example.myapplication.data.local.seed.SembradoServiciosInicia
import com.example.myapplication.core.domain.model.*
import com.example.myapplication.core.data.repository.*
import com.example.myapplication.core.notifications.NotificationHelper

import com.example.myapplication.presentation.features.home.CategoryVisuals
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.yield
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/**
 * --- MOTOR DE SIMULACIÓN MAVERICK ULTRA 2.0 (REFACTORIZADO) ---
 * Genera flujos de trabajo hiper-realistas:
 * 1. Mensajería con delays humanos.
 * 2. Presupuestos con desgloses técnicos por categoría.
 * 3. Variabilidad económica y sembrado masivo de prestadores.
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

    private val CHAT_SIM_DELAY = 2500L

    // ==================================================================================
    // --- 🚀 SECCIÓN: MIGRACIÓN DE CATEGORÍAS A FIRESTORE ---
    // ==================================================================================
    fun uploadCategoriesToFirestore() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val batch = db.batch()
                val collectionRef = db.collection("Servicios")

                SembradoServiciosInicia.categories.forEach { item ->
                    val docRef = collectionRef.document()
                    val dto = hashMapOf(
                        "name" to item.name,
                        "icon" to item.icon,
                        "description" to item.description,
                        "superCategory" to item.superCategory,
                        "superCategoryIcon" to item.superCategoryIcon,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, dto)
                }

                val metadataRef = db.collection("config").document("metadata")
                batch.set(
                    metadataRef,
                    mapOf("categoriesVersion" to System.currentTimeMillis() / 1000)
                )

                batch.commit().await()
                Toast.makeText(
                    application,
                    "¡Sincronización Firestore completada!",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Toast.makeText(application, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ==================================================================================
    // --- 💬 SECCIÓN: SIMULACIÓN DE CHATS Y PRESUPUESTOS ---
    // ==================================================================================

    fun simulateFullChatFlow() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
           // val provider = selectRealisticProvider(currentUserId) ?: return@launch
           // val chatId = "chat_${currentUserId}_${provider.id}"

            val greetings = listOf(
                "¡Hola! Vi tu consulta. Preparé un presupuesto detallado.",
                "¿Qué tal? Analicé tu caso y aquí tienes la cotización.",
                "Buenas tardes. Te envío el presupuesto con materiales incluidos."
            ).random()

           // simulateMessage(chatId, currentUserId, provider, greetings, MessageType.TEXT)
            delay(CHAT_SIM_DELAY)

          //  val newBudget = createUltraRealisticBudget(currentUserId, provider, null)
           // budgetRepository.receiveBudgetFromChat(newBudget)

            //simulateMessage(
                //chatId = chatId,
               // currentUserId = currentUserId,
                //provider = provider,
                //text = "📄 Presupuesto Técnico #${newBudget.budgetId.takeLast(4)}",
                //type = MessageType.BUDGET,
                //relatedId = newBudget.budgetId
           // )

            //notificationHelper.showNotification(
               // "Maverick",
             //   "${provider.displayName} te envió una propuesta."
            //)
        }
    }

    fun simulateFiveDirectBudgetsToChat() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val allProviders = providerRepository.allProviders.first()

            if (allProviders.size < 5) {
                Toast.makeText(
                    application,
                    "Se requieren al menos 5 prestadores.",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            allProviders.take(5).forEach { provider ->
                val chatId = "chat_${currentUserId}_${provider.id}"
                simulateMessage(
                    chatId,
                    currentUserId,
                    provider,
                    "Hola, te envío mi propuesta.",
                    MessageType.TEXT
                )
                delay(1500)

                val budget = createUltraRealisticBudget(currentUserId, provider, null)
                budgetRepository.receiveBudgetFromChat(budget)

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
            notificationHelper.showNotification("Simulación", "Recibiste 5 nuevos presupuestos.")
        }
    }

    fun simulateTenderResponsesForEachActive() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val openTenders = budgetRepository.getOpenTenders()

            if (openTenders.isEmpty()) {
                Toast.makeText(application, "No hay licitaciones abiertas.", Toast.LENGTH_LONG)
                    .show()
                return@launch
            }

            openTenders.forEach { tender ->
                val matchingProviders =
                    providerRepository.getProvidersByCategory(tender.category).shuffled()
                val selectedProviders = matchingProviders.take(5)
                    .ifEmpty { providerRepository.allProviders.first().shuffled().take(5) }

                selectedProviders.forEach { provider ->
                    val budget = createUltraRealisticBudget(
                        currentUserId,
                        provider,
                        tender.tenderId,
                        tender.category
                    )
                    budgetRepository.receiveBudgetFromChat(budget)

                    val chatId = "chat_${currentUserId}_${provider.id}"
                    simulateMessage(
                        chatId,
                        currentUserId,
                        provider,
                        "Envié una oferta para '${tender.title}'.",
                        MessageType.TEXT
                    )
                }
            }
            notificationHelper.showNotification(
                "Licitaciones",
                "Se generaron ofertas para tus licitaciones."
            )
        }
    }

    // ==================================================================================
    // --- 🏗️ SECCIÓN: SIMULACIÓN MASIVA DE PRESTADORES ---
    // ==================================================================================

    fun simulateMassiveProviders(categories: List<String>, areaCode: String, providerCount: Int) {
        viewModelScope.launch {
            val NOMBRES =
                listOf("Juan", "Pedro", "María", "Ana", "Carlos", "Lucía", "Diego", "Elena")
            val APELLIDOS = listOf("García", "Rodríguez", "López", "Martínez", "Sánchez", "Pérez")
            val TITULOS =
                listOf("Técnico Matriculado", "Especialista Senior", "Certificado Oficial")
            val calles = listOf("San Martin", "Catamarca", "Salta", "Jujuy", "Av. Sarmiento")

            val providerList = mutableListOf<Provider>()

            repeat(providerCount) {
                yield()
                val nombre = NOMBRES.random()
                val apellido = APELLIDOS.random()
                val id = "SIM-P-${UUID.randomUUID().toString().take(6)}"

                val baseLat = -26.82414
                val baseLon = -65.22260
                val lat = baseLat + (Random.nextDouble() - 0.5) * 0.03
                val lon = baseLon + (Random.nextDouble() - 0.5) * 0.03

                val address = AddressProvider(
                    id = UUID.randomUUID().toString(),
                    calle = calles.random(),
                    numero = (50..2500).random().toString(),
                    localidad = "San Miguel de Tucumán",
                    provincia = "Tucumán",
                    pais = "Argentina",
                    codigoPostal = areaCode,
                    latitude = lat,
                    longitude = lon
                )

                val provider = Provider(
                    uid = id,
                    email = "${nombre.lowercase()}@maverick-sim.com",
                    phoneNumber = "+54 9 381 " + (4000000..6999999).random().toString(),
                    displayName = "$nombre $apellido",
                    name = nombre,
                    lastName = apellido,
                    titulo = TITULOS.random(),
                    address = address,
                    addresses = listOf(address),
                    categories = categories,
                    isOnline = Random.nextBoolean(),
                    isSubscribed = Random.nextBoolean(),
                    rating = (35..50).random() / 10f,
                    photoUrl = "https://picsum.photos/seed/$id/200/200",
                    createdAt = System.currentTimeMillis()
                )
                providerList.add(provider)
            }

            // providerList.forEach { providerRepository.saveProviderProfile(it) }
            Toast.makeText(
                application,
                "Sembrado de $providerCount prestadores completado.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ==================================================================================
    // --- 🛠️ LÓGICA DE APOYO ---
    // ==================================================================================
/**
    private suspend fun selectRealisticProvider(myId: String): Provider? {
        val all = providerRepository.allProviders.first()
        if (all.isEmpty()) return null
        val activeIds = chatRepository.getActiveChatIds(myId)//.first()
        //return all.find { it.id !in activeIds } ?: all.random()
    }
*/
    private suspend fun simulateMessage(chatId: String, currentUserId: String, provider: Provider, text: String, type: MessageType, relatedId: String? = null) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = provider.id,
            receiverId = currentUserId,
            type = type,
            content = text,
            timestamp = System.currentTimeMillis(),
            relatedId = relatedId
        )
        chatRepository.sendMessage(message)
    }

    private fun createUltraRealisticBudget(clientId: String, provider: Provider, tenderId: String?, forceCategory: String? = null): BudgetEntity {
        val profileFactor = listOf(0.8, 1.0, 1.2, 2.0).random() * Random.nextDouble(0.9, 1.1)
        val category = forceCategory ?: provider.categories.firstOrNull() ?: "General"
        
        val items = mutableListOf<BudgetItem>()
        val services = mutableListOf<BudgetService>()

        items.add(BudgetItem(description = "Insumos Técnicos Especializados", quantity = 1, unitPrice = 25000.0 * profileFactor))
        services.add(BudgetService(description = "Mano de Obra y Configuración", total = 15000.0 * profileFactor))

        val subtotal = items.sumOf { it.unitPrice * it.quantity } + services.sumOf { it.total }
        val taxes = subtotal * 0.21

        return BudgetEntity(
            budgetId = "SIM-${UUID.randomUUID().toString().take(6).uppercase()}",
            clientId = clientId,
            providerId = provider.id,
            tenderId = tenderId,
            category = category,
            providerName = provider.displayName,
            providerCompanyName = provider.companies.firstOrNull()?.name ?: "${provider.lastName} Soluciones",
            providerPhotoUrl = provider.photoUrl,
            items = items,
            services = services,
            subtotal = subtotal,
            taxAmount = taxes,
            grandTotal = subtotal + taxes,
            status = BudgetStatus.PENDIENTE,
            dateTimestamp = System.currentTimeMillis()
        )
    }
}
