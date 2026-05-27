package com.example.myapplication.presentation.features.home

import com.example.myapplication.data.model.ProviderDisplayModel

data class ProviderWithDistance(
    val service: ProviderDisplayModel,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val lat: Double,
    val lon: Double
)

data class FastFilterState(
    val isOnline: Boolean = false,
    val isSubscribed: Boolean = false,
    val is24h: Boolean = false,
    val isLocal: Boolean = false
)










