package com.example.myapplication.presentation.features.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.presentation.components.AccordionBanner
import com.example.myapplication.presentation.components.BannerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * --- PROMO VIEWMODEL (EL ORQUESTADOR DE PUBLICIDAD UNIFICADO) ---
 * Centraliza toda la lógica de anuncios, promociones y destacados de la app.
 */
@HiltViewModel
class PromoViewModel @Inject constructor() : ViewModel() {

    private val _activeFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeFilters: StateFlow<Set<String>> = _activeFilters.asStateFlow()

    fun updateFilters(filters: Set<String>) {
        _activeFilters.value = filters
    }

    fun clearFilters() {
        _activeFilters.value = emptySet()
    }

    fun generateHomeBanners(
        categories: List<CategoryEntity>,
        services: List<ProviderDisplayModel>,
        filters: Set<String> = _activeFilters.value
    ): List<AccordionBanner> {
        val bannerList = mutableListOf<AccordionBanner>()

        // A. MAPEADO DE NOVEDADES
        categories.filter { it.isNew }.take(4).forEach { cat ->
            bannerList.add(AccordionBanner(
                id = "cat_${cat.name}",
                title = cat.name,
                subtitle = "🚀 NUEVA CATEGORÍA",
                icon = cat.icon,
                color = Color(CategoryVisuals.getColorFor(cat.superCategory)),
                type = BannerType.NEW_CATEGORY,
                originalCategory = cat,
                isNew = true
            ))
        }

        // B. MAPEADO DE PROMOCIONES
        services.filter { it.isSubscribed }.take(6).forEach { service ->
            val stableSeed = Random(service.id.hashCode().toLong())
            val discount = if (stableSeed.nextBoolean()) (10..50).random(stableSeed) else null
           /**
            bannerList.add(AccordionBanner(
                id = "promo_${service.id}", 
                title = service.title, 
                subtitle = if (discount != null) "¡OFERTA IMPERDIBLE!" else "SERVICIO DESTACADO", 
               // icon = if (service.doesProduct) "🛍️" else "🛠️",
                color = if (discount != null) Color(0xFFE91E63) else Color(0xFF2197F5), 
               // type = if (service.doesProduct) BannerType.PRODUCT_SALE else BannerType.PROMO,
               // discount = discount,
              //  service = service
            ))
            */
        }

        // C. MAPEADO DE ADS
        bannerList.add(AccordionBanner(
            id = "ad_maverick_premium", 
            title = "Maverick Premium", 
            subtitle = "Sube de nivel tu negocio hoy", 
            icon = "💎", 
            color = Color(0xFFFFD700),
            type = BannerType.GOOGLE_AD,
            imageUrl = "https://picsum.photos/seed/maverick/600/300"
        ))

        // D. FILTRADO
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

        return inyectarPublicidad(filteredList.sortedBy { it.id })
    }

    fun getHomeBanners(
        categories: List<CategoryEntity>,
        services: List<ProviderDisplayModel>
    ): Flow<List<AccordionBanner>> = _activeFilters.map { filters ->
        generateHomeBanners(categories, services, filters)
    }

    private fun inyectarPublicidad(items: List<AccordionBanner>): List<AccordionBanner> {
        val ads = items.filter { it.type == BannerType.GOOGLE_AD }
        val content = items.filter { it.type != BannerType.GOOGLE_AD }
        if (ads.isEmpty() || content.isEmpty()) return items

        val result = mutableListOf<AccordionBanner>()
        var adIdx = 0
        content.forEachIndexed { index, item ->
            result.add(item)
            if ((index + 1) % 2 == 0) {
                val adBase = ads[adIdx % ads.size]
                result.add(adBase.copy(id = "${adBase.id}_$index"))
                adIdx++
            }
        }
        return result
    }
}

