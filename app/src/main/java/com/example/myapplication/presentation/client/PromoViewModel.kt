package com.example.myapplication.presentation.client

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.presentation.components.AccordionBanner
import com.example.myapplication.presentation.components.BannerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * --- PROMO VIEWMODEL (EL ORQUESTADOR DE PUBLICIDAD UNIFICADO) ---
 * Centraliza toda la lógica de anuncios, promociones y destacados de la app.
 * Encargado del "trabajo sucio" de mezcla, filtrado táctico e inyección de publicidad.
 */
@HiltViewModel
class PromoViewModel @Inject constructor() : ViewModel() {

    // ======================================================================================
    // --- 1. ESTADOS DE CONTROL DE FILTROS ---
    // ======================================================================================
    private val _activeFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeFilters: StateFlow<Set<String>> = _activeFilters.asStateFlow()

    /**
     * Aplica los filtros seleccionados desde la UI.
     */
    fun updateFilters(filters: Set<String>) {
        _activeFilters.value = filters
    }

    /**
     * Limpia todos los filtros activos.
     */
    fun clearFilters() {
        _activeFilters.value = emptySet()
    }

    // ======================================================================================
    // --- 2. ORQUESTACIÓN DE BANNERS (LÓGICA ESTABLE) ---
    // ======================================================================================

    /**
     * Genera la lista de banners orquestada.
     * [ESTABILIDAD]: Utiliza seeds para Random para evitar el efecto "loco" en el carrusel.
     */
    fun getHomeBanners(
        categories: List<CategoryEntity>,
        services: List<ServiceDisplayModel>
    ): Flow<List<AccordionBanner>> = _activeFilters.map { filters ->
        val bannerList = mutableListOf<AccordionBanner>()

        // A. MAPEADO DE NOVEDADES
        categories.filter { it.isNew }.take(5).forEach { cat ->
            bannerList.add(AccordionBanner(
                id = "cat_${cat.name}",
                title = cat.name,
                subtitle = "🚀 EXPLORA LO NUEVO",
                icon = cat.icon,
                color = Color(CategoryVisuals.getColorFor(cat.superCategory)),
                type = BannerType.NEW_CATEGORY,
                originalCategory = cat,
                isNew = true
            ))
        }

        // B. MAPEADO DE PROMOCIONES
        services.filter { it.isSubscribed }.take(5).forEach { service ->
            bannerList.add(AccordionBanner(
                id = "promo_${service.id}", 
                title = if (service.doesProduct) "Oferta Producto" else "Oferta Especial", 
                subtitle = "Servicio destacado de ${service.title}", 
                icon = if (service.doesProduct) "🛍️" else "🔥", 
                color = Color(0xFFE91E63), 
                type = if (service.doesProduct) BannerType.PRODUCT_SALE else BannerType.PROMO, 
                discount = (15..45).random(Random(service.id.hashCode().toLong())), // Seed estable
                service = service 
            ))
        }

        // C. MAPEADO DE ADS
        bannerList.add(AccordionBanner(
            id = "ad_google_phantom", 
            title = "Anuncio Patrocinado", 
            subtitle = "Descubre más en Google Ads", 
            icon = "🌐", 
            color = Color.DarkGray, 
            type = BannerType.GOOGLE_AD,
            imageUrl = "https://picsum.photos/seed/google/600/300"
        ))

        // D. FILTRADO TÁCTICO
        val filteredList = if (filters.isEmpty()) bannerList 
        else bannerList.filter { banner ->
            if (banner.type == BannerType.GOOGLE_AD) true 
            else {
                val isNovedad = banner.type == BannerType.NEW_CATEGORY || banner.type == BannerType.NEW_PROVIDER
                val isPromo = banner.type == BannerType.PROMO || banner.discount != null
                val isProd = banner.type == BannerType.PRODUCT_SALE
                val isServ = banner.type == BannerType.SERVICE_SALE

                (filters.contains("NOVEDADES") && isNovedad) ||
                (filters.contains("PROMOCIONES") && isPromo) ||
                (filters.contains("PRODUCTOS") && isProd) ||
                (filters.contains("SERVICIOS") && isServ)
            }
        }

        // E. MEZCLA ESTABLE (Evita cambios bruscos en recomposición)
        inyectarPublicidad(filteredList.sortedBy { it.id }) 
    }

    /**
     * Lógica de inyección de publicidad (1 Ad cada 2 contenidos).
     */
    private fun inyectarPublicidad(items: List<AccordionBanner>): List<AccordionBanner> {
        val ads = items.filter { it.type == BannerType.GOOGLE_AD }
        val content = items.filter { it.type != BannerType.GOOGLE_AD }
        if (ads.isEmpty() || content.isEmpty()) return items

        val result = mutableListOf<AccordionBanner>()
        var adIdx = 0
        content.forEachIndexed { index, item ->
            result.add(item)
            if ((index + 1) % 2 == 0) {
                result.add(ads[adIdx % ads.size])
                adIdx++
            }
        }
        return result
    }
}
