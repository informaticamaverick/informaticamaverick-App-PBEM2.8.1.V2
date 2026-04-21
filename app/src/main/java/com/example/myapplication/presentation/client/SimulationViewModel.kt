package com.example.myapplication.presentation.client

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.*
import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.ProviderRepository
import com.example.myapplication.presentation.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import com.example.myapplication.presentation.client.CategoryVisuals
import com.example.myapplication.data.local.SembradoServiciosInicia
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/**
 * --- MOTOR DE SIMULACIÓN PROFESIONAL MAVERICK ULTRA ---
 * Genera datos falsos realistas (Presupuestos desglosados, Chats, Promociones)
 * para probar el UI, basándose estrictamente en los modelos de datos locales.
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
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(application, "Error: Debes estar autenticado para migrar datos.", Toast.LENGTH_LONG).show()
                return@launch
            }

            try {
                val db = FirebaseFirestore.getInstance()
                val batch = db.batch()
                val collectionRef = db.collection("Servicios")

                // 1. Limpiar o preparar (Firestore no permite borrar colecciones desde el cliente masivamente de forma simple, 
                // pero aquí estamos creando documentos nuevos con IDs automáticos)
                SembradoServiciosInicia.categories.forEach { item ->
                    val docRef = collectionRef.document() 
                    
                    val dto = hashMapOf(
                        "name" to item.name,
                        "icon" to item.icon,
                        "description" to item.description, // [CORRECCIÓN] Se agrega la descripción
                        "superCategory" to item.superCategory,
                        "superCategoryIcon" to item.superCategoryIcon,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, dto)
                }

                // 2. Actualizar la versión global para disparar la sincronización en todos los clientes
                val metadataRef = db.collection("config").document("metadata")
                batch.set(metadataRef, mapOf("categoriesVersion" to System.currentTimeMillis() / 1000)) // Usamos timestamp como versión

                batch.commit().await()
                
                Toast.makeText(application, "¡MIGRACIÓN EXITOSA! Versión actualizada.", Toast.LENGTH_LONG).show()
                notificationHelper.showNotification("Maverick Admin", "Categorías sincronizadas con Firestore.")
                
            } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
                if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    Toast.makeText(application, "ERROR DE PERMISOS: Revisa las Reglas de Seguridad en la Consola de Firebase.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(application, "Error Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(application, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    // ==========================================================================================
    // --- PARÁMETROS AJUSTABLES PARA EL DESARROLLADOR ---
    // ==========================================================================================
    private val TENDER_MASSIVE_COUNT = 20      // Cantidad de presupuestos a generar en Licitaciones
    private val CHAT_SIM_DELAY = 2500L         // Tiempo de "escribiendo..." en el chat (ms)
    private val PRICE_RIDICULOUS_CHANCE = 0.20 // 20% probabilidad de precio carísimo
    private val PRICE_CHEAP_CHANCE = 0.15      // 15% probabilidad de muy barato/oferta
    // ==========================================================================================

    /**
     * SIMULACIÓN A: CHAT INDIVIDUAL
     * Simula que un prestador te saluda y te envía un presupuesto desglosado por chat.
     */
    fun simulateProviderWelcomeAndBudget(specificClientId: String? = null) {
        viewModelScope.launch {
            val currentUserId = specificClientId ?: auth.currentUser?.uid ?: "user_demo_66"

            val provider = selectProviderForSimulation(currentUserId)
            if (provider == null) {
                Toast.makeText(application, "Sin prestadores en la BD para simular.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val chatId = "chat_${currentUserId}_${provider.id}"

            // 1. Mensaje de bienvenida
            val welcomeText = "¡Hola! Soy ${provider.name}. Analicé tu solicitud y aquí te adjunto el presupuesto detallado con materiales, mano de obra e impuestos."
            simulateMessage(chatId, currentUserId, provider, welcomeText, MessageType.TEXT)

            delay(CHAT_SIM_DELAY)

            // 2. Crear presupuesto técnico respetando las nuevas Data Classes
            val newBudget = createProfessionalDesglosadoBudget(currentUserId, provider, null)
            budgetRepository.receiveBudgetFromChat(newBudget)

            // 3. Enviar la tarjeta del presupuesto al chat
            simulateMessage(
                chatId = chatId,
                currentUserId = currentUserId,
                provider = provider,
                text = "Propuesta Técnica: ${newBudget.providerCompanyName ?: provider.displayName}",
                type = MessageType.BUDGET,
                relatedId = newBudget.budgetId
            )

            notificationHelper.showNotification("Nuevo Mensaje", "${provider.displayName} te envió un presupuesto.")
            Toast.makeText(application, "Simulación de Chat enviada.", Toast.LENGTH_SHORT).show()
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

            selectedProviders.forEach { provider ->
                val chatId = "chat_${currentUserId}_${provider.id}"
                
                // Mensaje de saludo realista
                val greetings = listOf(
                    "¡Hola! Analicé lo que necesitabas y te armé este presupuesto.",
                    "Buenas tardes, un gusto. Aquí te envío mi propuesta detallada.",
                    "Hola, vi tu pedido. Te adjunto el presupuesto para que lo revises.",
                    "¿Cómo estás? Te paso la cotización por el servicio solicitado.",
                    "¡Hola! Te envío el presupuesto técnico con el desglose de materiales."
                ).random()
                
                simulateMessage(chatId, currentUserId, provider, greetings, MessageType.TEXT)
                
                delay(Random.nextLong(1500, 3000)) // Delay para simular escritura

                // Crear presupuesto con precio variado
                val budget = createProfessionalDesglosadoBudget(currentUserId, provider, null)
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
                
                delay(1000)
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
                    val budget = createProfessionalDesglosadoBudget(currentUserId, provider, tender.tenderId, tender.category)
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

            notificationHelper.showNotification("Licitaciones", "Se han generado ofertas para todas tus licitaciones activas.")
        }
    }

    /**
     * SIMULACIÓN B: GENERACIÓN MASIVA PARA LICITACIONES (20 Presupuestos)
     * Crea respuestas automáticas para probar la tabla comparativa de columnas.
     * 🔥 Nombre restaurado a simulateTenderResponses para mantener compatibilidad con HomeScreenCliente3
     */
    fun simulateTenderResponses() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val openTenders = budgetRepository.getOpenTenders()
            val allProviders = providerRepository.allProviders.first()

            if (openTenders.isEmpty()) {
                Toast.makeText(application, "Primero crea una Licitación ABIERTA.", Toast.LENGTH_LONG).show()
                return@launch
            }

            if (allProviders.isEmpty()) {
                Toast.makeText(application, "No hay prestadores en la base de datos.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            Toast.makeText(application, "Generando $TENDER_MASSIVE_COUNT presupuestos comparativos...", Toast.LENGTH_SHORT).show()

            val targetTender = openTenders.random()

            // Filtramos proveedores que coincidan con la categoría de la licitación
            val validProviders = allProviders.filter { provider ->
                provider.categories.any { it.equals(targetTender.category, ignoreCase = true) }
            }.ifEmpty { allProviders }

            repeat(TENDER_MASSIVE_COUNT) {
                val provider = validProviders.random()
                val budget = createProfessionalDesglosadoBudget(currentUserId, provider, targetTender.tenderId)
                budgetRepository.receiveBudgetFromChat(budget)
            }

            notificationHelper.showNotification("Licitación Completa", "Recibiste $TENDER_MASSIVE_COUNT nuevas ofertas para comparar.")
            Toast.makeText(application, "¡Presupuestos generados con éxito!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * SIMULACIÓN C: NUEVAS PROMOCIONES
     */
    fun simulateNewPromotions() {
        viewModelScope.launch {
            val allProviders = providerRepository.allProviders.first()
            if (allProviders.isEmpty()) return@launch

            val luckyProviders = allProviders.shuffled().take(3)
            luckyProviders.forEach {
                notificationHelper.showNotification("Oferta Flash 🔥", "¡${it.displayName} publicó un nuevo descuento en sus servicios!")
            }
            Toast.makeText(application, "Nuevas Promociones simuladas.", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================================================================
    // --- NUEVA SECCIÓN: SIMULACIÓN MASIVA DE PRESTADORES (SÉMBRADO DE DATOS) ---
    // ==========================================================================================

    /**
     * Genera prestadores ficticios con datos realistas para pruebas de filtros y distancia.
     * [CORRECCIÓN ULTRA] Geolocalización precisa en San Miguel de Tucumán y calles reales.
     * [ESTABILIDAD] Se optimizó la inserción masiva para evitar crasheos de memoria (SIGSEGV).
     * [MEJORA] Sincronizado con la lógica de PrestadorSampleDataFalso (Empresas, Sucursales, Staff, Galería).
     */
    fun simulateMassiveProviders(
        categories: List<String>,
        areaCode: String,
        providerCount: Int,
        companiesMax: Int = 3,
        branchesMax: Int = 3
    ) {
        viewModelScope.launch {
            val NOMBRES = listOf("Juan", "Pedro", "María", "Ana", "Carlos", "Lucía", "Diego", "Elena", "Roberto", "Sonia", "Facundo", "Martina", "Gonzalo", "Paola")
            val APELLIDOS = listOf("García", "Rodríguez", "López", "Martínez", "Sánchez", "Pérez", "Gómez", "Díaz", "Álvarez", "Nanterne", "Romero", "Sosa", "Torres")
            val TITULOS = listOf("Técnico Matriculado", "Ingeniero Especialista", "Maestro Mayor de Obras", "Especialista Senior", "Certificado Oficial")
            
            val callesTucuman = listOf("San Martin", "Catamarca", "Salta", "Jujuy", "Santiago del Estero", "9 de julio", "Congreso", "Crisostomo Alvarez", "Av. Sarmiento", "Av. Mitre")

            val providerListToInsert = mutableListOf<Provider>()

            repeat(providerCount) { pIdx ->
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
                    calle = callesTucuman.random(),
                    numero = (50..2500).random().toString(),
                    localidad = "San Miguel de Tucumán",
                    provincia = "Tucumán",
                    pais = "Argentina",
                    codigoPostal = areaCode,
                    latitude = lat,
                    longitude = lon
                )

                // --- GENERACIÓN DE EMPRESAS Y SUCURSALES (LÓGICA ACTUALIZADA) ---
                val simulatedCompanies = mutableListOf<CompanyProvider>()
                val hasCompany = Random.nextFloat() > 0.3f
                
                if (hasCompany) {
                    repeat(Random.nextInt(1, (companiesMax + 1))) { cIdx ->
                        val companyName = "Empresa ${APELLIDOS.random()} & Asociados"
                        val branches = mutableListOf<BranchProvider>()
                        
                        repeat(Random.nextInt(1, (branchesMax + 1))) { bIdx ->
                            val bLat = baseLat + (Random.nextDouble() - 0.5) * 0.04
                            val bLon = baseLon + (Random.nextDouble() - 0.5) * 0.04
                            
                            branches.add(BranchProvider(
                                id = "SIM-B-$id-$cIdx-$bIdx",
                                name = if(bIdx == 0) "Casa Central Tucumán" else "Sucursal ${callesTucuman.random()}",
                                address = address.copy(
                                    id = UUID.randomUUID().toString(), 
                                    calle = callesTucuman.random(),
                                    numero = (100..2800).random().toString(),
                                    latitude = bLat,
                                    longitude = bLon
                                ),
                                workingHours = "08:30 a 13:00 y 17:00 a 21:00 hs",
                                doesService = Random.nextBoolean(),
                                doesProduct = Random.nextBoolean(),
                                works24h = Random.nextBoolean(),
                                hasPhysicalLocation = true,
                                rating = (35..50).random() / 10f,
                                employees = List(Random.nextInt(1, 3)) { eIdx ->
                                    EmployeeProvider(
                                        name = NOMBRES.random(),
                                        lastName = APELLIDOS.random(),
                                        position = if (eIdx == 0) "Gerente" else "Técnico Especialista",
                                        photoUrl = "https://picsum.photos/seed/emp_${id}_${cIdx}_${bIdx}_$eIdx/200/200"
                                    )
                                },
                                galleryImages = List(Random.nextInt(2, 4)) { "https://picsum.photos/seed/br_${id}_${cIdx}_${bIdx}_$it/400/300" }
                            ))
                        }
                        
                        simulatedCompanies.add(CompanyProvider(
                            id = "SIM-C-$id-$cIdx",
                            name = companyName,
                            razonSocial = "$companyName S.R.L.",
                            description = "Líder regional en ${categories.joinToString(", ")}.",
                            categories = categories,
                            branches = branches,
                            isVerified = true,
                            photoUrl = "https://picsum.photos/seed/comp_${id}_$cIdx/200/200",
                            bannerImageUrl = "https://picsum.photos/seed/cb_${id}_$cIdx/800/400"
                        ))
                    }
                }

                val provider = Provider(
                    uid = id,
                    email = "${nombre.lowercase()}.${apellido.lowercase()}@maverick-sim.com.ar",
                    phoneNumber = "+54 9 381 " + (4000000..6999999).random().toString(),
                    displayName = "$nombre $apellido",
                    name = nombre,
                    lastName = apellido,
                    titulo = TITULOS.random(),
                    matricula = "MAT-TUC-" + (1000..9999).random(),
                    addresses = listOf(address, address.copy(id = UUID.randomUUID().toString(), calle = callesTucuman.random(), numero = "100")),
                    address = address,
                    categories = categories,
                    isOnline = Random.nextBoolean(),
                    isSubscribed = Random.nextBoolean(),
                    isVerified = Random.nextBoolean(),
                    doesService = true,
                    doesProduct = Random.nextBoolean(),
                    works24h = Random.nextBoolean(),
                    hasPhysicalLocation = Random.nextBoolean(),
                    doesHomeVisits = Random.nextBoolean(),
                    doesShipping = Random.nextBoolean(),
                    acceptsAppointments = Random.nextBoolean(),
                    rating = (35..50).random() / 10f,
                    companies = simulatedCompanies,
                    hasCompanyProfile = simulatedCompanies.isNotEmpty(),
                    description = "Especialista con amplia trayectoria en la zona de Tucumán.",
                    createdAt = System.currentTimeMillis(),
                    photoUrl = "https://picsum.photos/seed/$id/200/200",
                    bannerImageUrl = "https://picsum.photos/seed/b_$id/800/400",
                    galleryImages = List(Random.nextInt(3, 6)) { "https://picsum.photos/seed/gal_${id}_$it/600/400" }
                )

                providerListToInsert.add(provider)
            }
            
            try {
                providerListToInsert.chunked(25).forEach { chunk ->
                    chunk.forEach { providerRepository.saveProviderProfile(it) }
                    delay(150) 
                }
                Toast.makeText(application, "Sembrado Tucumán PRO completado ($providerCount prestadores).", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(application, "Error en siembra: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================================================================
    // --- LÓGICA INTERNA DE CONSTRUCCIÓN ---
    // ==========================================================================================

    private suspend fun selectProviderForSimulation(myId: String): Provider? {
        val all = providerRepository.allProviders.first()
        if (all.isEmpty()) return null

        val maverick = all.find { it.id == "1001" }
        if (maverick != null) return maverick

        val activeIds = chatRepository.getActiveChatIds(myId).first()
        return all.find { activeIds.contains(it.id) } ?: all.random()
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
            isRead = false
        )
        chatRepository.sendMessage(message)
    }

    /**
     * 🔥 CREADOR DE PRESUPUESTOS (ADAPTADO EXACTAMENTE A TUS DATA CLASSES)
     */
    private fun createProfessionalDesglosadoBudget(clientId: String, provider: Provider, tenderId: String?, forceCategory: String? = null): BudgetEntity {

        val randomValue = Random.nextDouble()
        val priceMultiplier = when {
            randomValue < PRICE_RIDICULOUS_CHANCE -> Random.nextDouble(2.5, 4.5)
            randomValue > (1.0 - PRICE_CHEAP_CHANCE) -> Random.nextDouble(0.4, 0.7)
            else -> Random.nextDouble(0.9, 1.2)
        }

        val category = forceCategory ?: provider.categories.firstOrNull() ?: "General"

        // 1. MATERIALES (BudgetItem usa unitPrice y quantity)
        val items = mutableListOf<BudgetItem>()
        
        when(category) {
            "Electricidad" -> {
                items.add(BudgetItem(description = "Cable Unipolar 2.5mm (Normalizado)", quantity = 1, unitPrice = 45000.0 * priceMultiplier))
                items.add(BudgetItem(description = "Térmica Sica 2x20A", quantity = 2, unitPrice = 8500.0 * priceMultiplier))
            }
            "Plomería" -> {
                items.add(BudgetItem(description = "Kit Termofusión Agua Fría/Caliente", quantity = 1, unitPrice = 15000.0 * priceMultiplier))
                items.add(BudgetItem(description = "Grifería Monocomando Premium", quantity = 1, unitPrice = 85000.0 * priceMultiplier))
            }
            else -> {
                items.add(BudgetItem(description = "Kit de Insumos Técnicos Cat.A", quantity = 1, unitPrice = 15000.0 * priceMultiplier))
                items.add(BudgetItem(description = "Componentes de Repuesto Original", quantity = 2, unitPrice = 4500.0 * priceMultiplier))
            }
        }
        
        val itemsTotal = items.sumOf { it.unitPrice * it.quantity }

        // 2. SERVICIOS (BudgetService usa solo total)
        val services = listOf(
            BudgetService(
                code = "SRV-01",
                description = "Mano de Obra Especializada",
                total = 25000.0 * priceMultiplier
            ),
            BudgetService(
                code = "SRV-02",
                description = "Configuración y Testing de Sistemas",
                total = 12000.0 * priceMultiplier
            )
        )
        val servicesTotal = services.sumOf { it.total }

        // 3. HONORARIOS (BudgetProfessionalFee usa solo total)
        val fees = listOf(
            BudgetProfessionalFee(
                code = "FEE-01",
                description = "Dirección Técnica y Certificación",
                total = 10000.0 * priceMultiplier
            )
        )
        val feesTotal = fees.sumOf { it.total }

        // 4. IMPUESTOS (BudgetTax usa amount)
        val subtotalValue = itemsTotal + servicesTotal + feesTotal
        val calculatedTax = subtotalValue * 0.21
        val taxes = listOf(
            BudgetTax(
                description = "IVA Inscrito (21%)",
                amount = calculatedTax
            )
        )
        val taxesTotal = taxes.sumOf { it.amount }

        val grandTotalValue = subtotalValue + taxesTotal

        // Configuramos características adicionales aleatorias
        val paymentMethod = listOf("Transferencia / Efectivo", "Tarjetas (3 Cuotas sin interés)", "Efectivo 10% OFF").random()
        val warranty = listOf("Garantía de 3 meses", "Garantía oficial de 1 año", "Sin garantía extendida").random()
        val execution = listOf("Aproximadamente 2 días", "Ejecución inmediata", "Requiere 1 semana de planificación").random()

        return BudgetEntity(
            budgetId = "SIM-${UUID.randomUUID().toString().take(6).uppercase()}",
            clientId = clientId,
            providerId = provider.id,
            tenderId = tenderId,
            category = category,
            providerName = provider.displayName,
            providerCompanyName = provider.companies.firstOrNull()?.name,
            providerPhotoUrl = provider.photoUrl,

            // Asignación a las listas
            items = items,
            services = services,
            professionalFees = fees,
            miscExpenses = emptyList(), // Queda vacío en esta simulación
            taxes = taxes,
            imageUrls = emptyList(),

            // Totales
            subtotal = subtotalValue,
            taxAmount = taxesTotal,
            discountAmount = 0.0,
            grandTotal = grandTotalValue,

            // Características comerciales
            validityDays = if (priceMultiplier < 0.7) 3 else 15,
            notes = if(priceMultiplier > 2.0) "Precio Premium VIP. Incluye repuestos importados y prioridad de urgencia." else "Presupuesto estándar. Sujeto a disponibilidad de agenda.",
            paymentMethods = paymentMethod,
            warrantyInfo = warranty,
            executionTime = execution,

            status = BudgetStatus.PENDIENTE,
            dateTimestamp = System.currentTimeMillis()
        )
    }
}
