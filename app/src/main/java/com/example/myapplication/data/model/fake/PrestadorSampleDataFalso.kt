package com.example.myapplication.data.model.fake

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.data.local.*
import com.example.myapplication.data.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

/**
 * --- GENERADOR DE DATOS DE PRUEBA MAESTRO PRO ULTRA ---
 * [ACTUALIZADO] Reducción de cantidad de prestadores y enfoque 100% en San Miguel de Tucumán (CP 4000).
 */
object PrestadorSampleDataFalso {

    const val CLIENT_ID = "user_demo_66"

    // =========================================
    // 📚 SECCIÓN: DICCIONARIOS DE DATOS REALES
    // =========================================
    private val NOMBRES = listOf("Juan", "Pedro", "María", "Ana", "Carlos", "Lucía", "Diego", "Elena", "Roberto", "Sonia", "Facundo", "Martina", "Gonzalo", "Paola")
    private val APELLIDOS = listOf("García", "Rodríguez", "López", "Martínez", "Sánchez", "Pérez", "Gómez", "Díaz", "Álvarez", "Nanterne", "Romero", "Sosa", "Torres")
    
    // Lista de calles reales de San Miguel de Tucumán proporcionada por el usuario
    private val CALLES = listOf("San Martin", "Catamarca", "Salta", "Jujuy", "Santiago del Estero", "9 de julio", "Congreso", "Crisostomo Alvarez", "Av. Sarmiento", "Av. Mitre")
    
    private val BARRIOS = listOf("Barrio Norte", "Barrio Sur", "Centro", "Yerba Buena", "Villa Luján", "Ciudadela")
    
    private val TITULOS_PROFESIONALES = listOf(
        "Técnico Matriculado", "Ingeniero Especialista", "Maestro Mayor de Obras", 
        "Especialista Senior", "Certificado Oficial", "Consultor Técnico"
    )

    private val LICITACIONES_POR_CATEGORIA = mapOf(
        "Informatica" to listOf(
            "Reparación de Notebook" to "La pantalla parpadea y calienta mucho al usar programas de diseño.",
            "Instalación de Red WiFi" to "Necesito extender la señal a un segundo piso en oficina comercial.",
            "Limpieza de Software" to "Mi PC está muy lenta y aparecen anuncios solos. Posible virus."
        ),
        "Electricidad" to listOf(
            "Recableado Completo" to "Casa antigua, saltan las térmicas seguido. Necesito cambiar cables.",
            "Instalación de Aire Acondicionado" to "Instalar split de 3000 frigorías con línea independiente.",
            "Cambio de Tablero" to "Reemplazar tapones antiguos por disyuntor y térmicas nuevas."
        ),
        "Plomería" to listOf(
            "Pérdida en Termotanque" to "Gotea por la base, necesito ver si tiene arreglo o cambio.",
            "Destape de Cañería" to "Cocina obstruida, ya probé con productos y no funciona.",
            "Instalación de Grifería" to "Cambiar juego de baño completo por uno tipo monocomando."
        ),
        "Hogar" to listOf(
            "Pintura de Living" to "Pintar paredes y techo, aprox 30m2. Incluye enduído de grietas.",
            "Arreglo de Persiana" to "Se cortó la cinta y quedó trabada arriba.",
            "Armado de Mueble" to "Necesito armar un ropero de 6 puertas comprado en caja."
        )
    )

    fun generateAll(realCategories: List<CategoryEntity>): DataSeedBundle {
        val providers = generateProviders(realCategories)
        val tenders = generateTenders(realCategories, providers)
        val calendarEvents = generateCalendarEvents(providers)

        return DataSeedBundle(
            providers = providers,
            tenders = tenders,
            budgets = emptyList(),
            messages = emptyList(),
            calendarEvents = calendarEvents
        )
    }

    // =========================================
    // 👤 SECCIÓN: GENERACIÓN DE PRESTADORES
    // =========================================
    private fun generateProviders(realCategories: List<CategoryEntity>): List<ProviderEntity> {
        val providers = mutableListOf<ProviderEntity>()
        providers.add(generateMaverickProvider())

        realCategories.forEach { category ->
            // [REQUERIMIENTO] Reducido de 1 a 2 por categoría
            val countPerCategory = Random.nextInt(1, 3) 
            repeat(countPerCategory) {
                providers.add(generateRandomProvider(category.name, realCategories))
            }
        }
        return providers
    }

    private fun generateRandomProvider(category: String, allCats: List<CategoryEntity>): ProviderEntity {
        val nombre = NOMBRES.random()
        val apellido = APELLIDOS.random()
        val id = "P-${UUID.randomUUID().toString().take(8)}"
        val isVerified = Random.nextFloat() > 0.2f
        val hasCompany = isVerified && Random.nextFloat() < 0.65f
        
        val myCategories = mutableSetOf(category)
        repeat(Random.nextInt(1, 3)) { myCategories.add(allCats.random().name) }

        val mainEmail = "${nombre.lowercase()}.${apellido.lowercase()}@maverickpro.com"
        val dni = Random.nextInt(25000000, 45000000).toString()
        
        // Coordenadas centradas en S.M. de Tucumán
        val baseLat = -26.82414 
        val baseLon = -65.22260
        val lat = baseLat + (Random.nextDouble() - 0.5) * 0.03
        val lon = baseLon + (Random.nextDouble() - 0.5) * 0.03

        val address = AddressProvider(
            calle = CALLES.random(), 
            numero = Random.nextInt(10, 2500).toString(), 
            localidad = "San Miguel de Tucumán",
            provincia = "Tucumán",
            pais = "Argentina",
            codigoPostal = "4000",
            latitude = lat,
            longitude = lon
        )

        return ProviderEntity(
            id = id,
            email = mainEmail,
            emails = listOf(mainEmail, "contacto.${nombre.lowercase()}@gmail.com"),
            displayName = "$nombre $apellido",
            name = nombre,
            lastName = apellido,
            phoneNumber = "+54 9 381 ${Random.nextInt(4000000, 6999999)}",
            additionalPhones = listOf("+54 9 381 ${Random.nextInt(1000000, 9999999)}"),
            address = address,
            addresses = listOf(address, address.copy(id = UUID.randomUUID().toString(), calle = CALLES.random(), numero = "${Random.nextInt(10, 2000)}")),
            isVerified = isVerified,
            isSubscribed = Random.nextBoolean(),
            works24h = Random.nextBoolean(),
            doesService = true,
            doesHomeVisits = Random.nextBoolean(),
            hasPhysicalLocation = Random.nextBoolean(),
            categories = myCategories.toList(),
            rating = 3.8f + (Random.nextFloat() * 1.2f),
            titulo = TITULOS_PROFESIONALES.random(),
            cuilCuit = "20-$dni-${Random.nextInt(0, 9)}",
            workingHours = "Lunes a Viernes: 09:00 a 13:00 y 17:00 a 21:00 hs",
            hasCompanyProfile = hasCompany,
            // [REQUERIMIENTO] Se generan hasta 3 empresas de manera aleatoria
            companies = if (hasCompany) List(Random.nextInt(1, 4)) { generateRandomCompany(category, allCats) } else emptyList(),
            createdAt = System.currentTimeMillis() - (Random.nextLong(1000000, 100000000)),
            photoUrl = "https://picsum.photos/seed/$id/200/200",
            bannerImageUrl = "https://picsum.photos/seed/b_$id/800/400",
            galleryImages = List(Random.nextInt(2, 5)) { "https://picsum.photos/seed/gal_${id}_$it/600/400" },
            description = "Profesional especializado en ${myCategories.joinToString(", ")} con amplia trayectoria en Tucumán."
        )
    }

    private fun generateRandomCompany(category: String, allCats: List<CategoryEntity>): CompanyProvider {
        val companyName = "Empresa ${APELLIDOS.random()} & Asociados"
        val compId = UUID.randomUUID().toString().take(6)
        return CompanyProvider(
            id = "C-$compId",
            name = companyName,
            razonSocial = "$companyName S.R.L.",
            cuit = "30-${Random.nextInt(10000000, 99999999)}-${Random.nextInt(0, 9)}",
            isVerified = true,
            description = "Líderes en soluciones integrales de $category y rubros afines en el NOA.",
            categories = listOf(category, allCats.random().name),
            photoUrl = "https://picsum.photos/seed/c_$compId/200/200",
            bannerImageUrl = "https://picsum.photos/seed/cb_$compId/800/400",
            // [REQUERIMIENTO] Cada empresa tiene de 1 a 3 sucursales
            branches = List(Random.nextInt(1, 4)) { bIdx ->
                BranchProvider(
                    id = "B-$compId-$bIdx",
                    name = if (bIdx == 0) "Casa Central Tucumán" else "Sucursal ${BARRIOS.random()}",
                    address = AddressProvider(
                        calle = CALLES.random(), 
                        numero = "${Random.nextInt(100, 2500)}", 
                        localidad = "San Miguel de Tucumán",
                        provincia = "Tucumán",
                        codigoPostal = "4000",
                        latitude = -26.82414 + (Random.nextDouble() - 0.5) * 0.04,
                        longitude = -65.22260 + (Random.nextDouble() - 0.5) * 0.04
                    ),
                    workingHours = "08:30 a 13:00 y 17:00 a 21:00 hs",
                    doesService = true,
                    doesShipping = Random.nextBoolean(),
                    hasPhysicalLocation = true,
                    works24h = Random.nextFloat() > 0.7f,
                    employees = List(Random.nextInt(1, 4)) { eIdx ->
                        val eNombre = NOMBRES.random()
                        val eApellido = APELLIDOS.random()
                        EmployeeProvider(
                            id = "E-$compId-$bIdx-$eIdx",
                            name = eNombre,
                            lastName = eApellido,
                            position = if (eIdx == 0) "Gerente de Operaciones" else "Técnico Especialista",
                            detail = "Especialista certificado con atención personalizada en Tucumán.",
                            photoUrl = "https://picsum.photos/seed/emp_$eIdx${Random.nextInt()}/200/200"
                        )
                    },
                    galleryImages = List(Random.nextInt(2, 4)) { "https://picsum.photos/seed/br_${compId}_${bIdx}_$it/400/300" }
                )
            }
        )
    }

    fun generateMaverickProvider(): ProviderEntity {
        val mainAddress = AddressProvider(
            calle = "San Martín", 
            numero = "450", 
            localidad = "San Miguel de Tucumán", 
            provincia = "Tucumán", 
            codigoPostal = "4000",
            latitude = -26.82414,
            longitude = -65.22260
        )
        return ProviderEntity(
            id = "1001",
            email = "MAVERICKINFORMATICA@maverick.com",
            emails = listOf("MAVERICKINFORMATICA@maverick.com", "soporte@maverick.com"),
            displayName = "Maverick Informática",
            name = "Maximiliano",
            lastName = "Nanterne",
            phoneNumber = "+54 9 381 1234567",
            address = mainAddress,
            addresses = listOf(mainAddress),
            isVerified = true,
            isSubscribed = true,
            works24h = true,
            categories = listOf("Informatica", "Desarrollo Móvil", "Seguridad"),
            rating = 5.0f,
            titulo = "Ingeniero de Software & Tech Lead",
            cuilCuit = "20-30405060-7",
            workingHours = "Lunes a Viernes: 09:00 a 21:00 hs",
            hasCompanyProfile = true,
            companies = listOf(
                CompanyProvider(
                    id = "C-MAVERICK",
                    name = "Maverick Tech S.A.",
                    razonSocial = "Maverick Soluciones Digitales S.R.L.",
                    cuit = "30-12345678-9",
                    description = "Innovación tecnológica al servicio de Tucumán.",
                    categories = listOf("Consultoría IT", "Software"),
                    isVerified = true,
                    branches = listOf(
                        BranchProvider(
                            id = "B-MAV-1",
                            name = "Sede Central Barrio Sur",
                            address = AddressProvider(
                                calle = "Lavalle", 
                                numero = "1500", 
                                localidad = "San Miguel de Tucumán", 
                                provincia = "Tucumán", 
                                codigoPostal = "4000",
                                latitude = -26.832,
                                longitude = -65.225
                            ),
                            workingHours = "08:00 a 20:00 hs",
                            doesService = true,
                            works24h = true,
                            hasPhysicalLocation = true,
                            employees = listOf(
                                EmployeeProvider(id = "E-MAV-1", name = "Maximiliano", lastName = "Nanterne", position = "CEO", detail = "Tech Lead con 10 años de experiencia."),
                                EmployeeProvider(id = "E-MAV-2", name = "Ana", lastName = "Gómez", position = "Líder de Soporte", detail = "Especialista en atención al cliente.")
                            ),
                            galleryImages = listOf("https://picsum.photos/seed/m1/400/300", "https://picsum.photos/seed/m2/400/300")
                        )
                    )
                )
            ),
            galleryImages = listOf("https://picsum.photos/seed/g1/400/300", "https://picsum.photos/seed/g2/400/300"),
            createdAt = System.currentTimeMillis(),
            photoUrl = "https://picsum.photos/seed/maverick/200/200",
            bannerImageUrl = "https://picsum.photos/seed/maverick_banner/800/400",
            description = "Expertos en desarrollo de software, seguridad informática y soluciones móviles corporativas."
        )
    }

    private fun generateTenders(realCategories: List<CategoryEntity>, providers: List<ProviderEntity>): List<TenderEntity> {
        val tenders = mutableListOf<TenderEntity>()
        
        listOf("ABIERTA", "CERRADA", "ADJUDICADA", "CANCELADA").forEach { status ->
            repeat(2) {
                val categoryName = LICITACIONES_POR_CATEGORIA.keys.random()
                val data = LICITACIONES_POR_CATEGORIA[categoryName]?.random()!!
                
                val provider = if (status == "ADJUDICADA") providers.random() else null
                
                tenders.add(
                    TenderEntity(
                        tenderId = "T-${UUID.randomUUID().toString().take(6)}",
                        title = data.first,
                        isActive = status == "ABIERTA",
                        clientId = CLIENT_ID,
                        description = data.second,
                        category = categoryName,
                        status = status,
                        dateTimestamp = System.currentTimeMillis() - (Random.nextLong(1, 10) * 86400000L),
                        awardedProviderId = provider?.id,
                        awardedProviderName = provider?.displayName,
                        budgetCount = Random.nextInt(1, 8),
                        locationAddress = CALLES.random() + " " + Random.nextInt(100, 2000),
                        locationLocality = "San Miguel de Tucumán"
                    )
                )
            }
        }
        return tenders
    }

    private fun generateCalendarEvents(providers: List<ProviderEntity>): List<CalendarEventEntity> {
        val events = mutableListOf<CalendarEventEntity>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val baseTime = System.currentTimeMillis()
        
        providers.shuffled().take(12).forEach { provider ->
            val daysOffset = Random.nextInt(-1, 4)
            events.add(
                CalendarEventEntity(
                    id = UUID.randomUUID().toString(),
                    date = dateFormat.format(Date(baseTime + (daysOffset * 86400000L))),
                    time = "${Random.nextInt(9, 19)}:00",
                    type = listOf(EventType.VISIT, EventType.APPOINTMENT).random(),
                    title = "Servicio Técnico: ${provider.categories.firstOrNull() ?: "General"}",
                    provider = provider.displayName,
                    providerId = provider.id,
                    address = provider.address?.fullString() ?: "Domicilio del Cliente",
                    status = VisitStatus.CONFIRMED,
                    providerPhotoUrl = provider.photoUrl
                )
            )
        }
        return events
    }
}

data class DataSeedBundle(
    val providers: List<ProviderEntity>,
    val tenders: List<TenderEntity>,
    val budgets: List<BudgetEntity>,
    val messages: List<MessageEntity>,
    val calendarEvents: List<CalendarEventEntity>
)
