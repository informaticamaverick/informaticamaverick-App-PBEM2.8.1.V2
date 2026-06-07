package com.example.myapplication.core.domain.model

/**
 * --- MODELO DE DOMINIO: Provider (PERFIL DEL PRESTADOR) ---
 * Este objeto representa al profesional que ofrece servicios en la plataforma.
 * Es utilizado tanto por el cliente (para ver a quién contrata) como por el 
 * propio prestador (para gestionar su perfil).
 */
data class Provider(
    val uid: String,

    // --- MULTIMEDIA (Ley #2 y #3) ---
    val photoUrl: String? = null,
    val profileThumbnail: String? = null, // [NUEVO] Imagen pequeña (Base64) para carga instantánea

    // --- DATOS DEL PRESTADOR ---
    val displayName: String,
    val name: String,
    val lastName: String,
    val matricula: String? = null,
    val profesion: String? = null,
    val cuilCuit: String? = null, // DNI o CUIT/CUIL
    val description: String = "",

    // --- DATOS DE CONTACTO ---
    val email: String, // Email principal (Google/Registro)
    val emails: List<String> = emptyList(), // Lista de emails adicionales
    val phoneNumber: String,

    // --- UBICACIONES ---
    val addresses: List<AddressUnico> = emptyList(), // Una o más direcciones
    val address: AddressUnico? = null, // Dirección principal

    // --- ESTRUCTURA EMPRESARIAL ---
    val companies: List<CompanyProvider> = emptyList(), // Max 3 empresas

    // --- CONFIGURACIÓN DE SERVICIOS (FLAGS) ---
    val doesService: Boolean = false,
    val doesProduct: Boolean = false,
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val trabajaConOtros: Boolean = false,

    // --- ESTADOS ---
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val isFavorite: Boolean = false,
    val priorizarEmpresa: Boolean = false,

    // --- METADATOS Y VALORACIÓN ---
    val rating: Float = 0f,
    val workingHours: String = "",
    val categories: List<String> = emptyList(),
    val serviceType: String? = null, // [LEY #4] TECHNICAL, PRODUCT, etc.
    val lastSyncTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    val id: String get() = uid

    // --- ALIASES DE COMPATIBILIDAD UI ---
    val apellido: String get() = lastName
    val imageUrl: String? get() = photoUrl
    val verificado: Boolean get() = isVerified
    val hasCompanyProfile: Boolean get() = companies.isNotEmpty()
    val vaDomicilio: Boolean get() = doesHomeVisits
    val suscripto: Boolean get() = isSubscribed
    val favorito: Boolean get() = isFavorite
    val tieneEmpresa: Boolean get() = companies.isNotEmpty()
    val nombreEmpresa: String? get() = companies.firstOrNull()?.name
    val direccionEmpresa: String? get() = companies.firstOrNull()?.branches?.firstOrNull()?.address?.fullString()
    val cuitEmpresa: String? get() = companies.firstOrNull()?.cuit
    val turnosEnLocal: Boolean get() = hasPhysicalLocation
    val direccionLocal: String? get() = address?.fullString()
    val phone: String get() = phoneNumber
    val dniCuit: String? get() = cuilCuit
    val tieneMatricula: Boolean get() = !matricula.isNullOrBlank()
    val envios: Boolean get() = doesShipping
    val atencionUrgencias: Boolean get() = works24h
    val horarioLocal: String get() = workingHours

    // 🔥 [LEY #1] Cálculo de Disponibilidad SSOT
    val statusText: String get() = calculateAvailability().first
    val statusColor: Long get() = calculateAvailability().second

    private fun calculateAvailability(): Pair<String, Long> {
        if (workingHours.isBlank()) return "sin Horarios" to 0xFF9E9E9E
        return try {
            val now = java.util.Calendar.getInstance()
            val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(java.util.Calendar.MINUTE)
            val currentTimeMinutes = currentHour * 60 + currentMinute

            val parts = workingHours.split("-")
            if (parts.size >= 2) {
                val startParts = parts[0].trim().split(":")
                val endParts = parts[1].trim().split(":")
                val startMinutes = startParts[0].toInt() * 60 + startParts[1].take(2).toInt()
                val endMinutes = endParts[0].toInt() * 60 + endParts[1].take(2).toInt()
                
                // Rango de tarde (ejemplo Maverick)
                val start2Minutes = 16 * 60
                val end2Minutes = 20 * 60

                val isOpen = (currentTimeMinutes in startMinutes..endMinutes) || 
                             (currentTimeMinutes in start2Minutes..end2Minutes)
                
                if (isOpen) "ABIERTO AHORA ✅" to 0xFF4ADE80 else "CERRADO AHORA 🔴" to 0xFFF87171
            } else "Horario Especial" to 0xFFFBBF24
        } catch (e: Exception) {
            "Horario Especial" to 0xFFFBBF24
        }
    }
}

/**
 * Extensión para convertir el modelo de dominio a la entidad de persistencia (SSOT).
 */
fun Provider.toEntity(): com.example.myapplication.core.data.local.entity.ProviderEntity {
    return com.example.myapplication.core.data.local.entity.ProviderEntity.fromDomain(this)
}
