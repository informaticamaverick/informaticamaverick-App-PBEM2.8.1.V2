package com.example.myapplication.prestador.data.local

import androidx.room.TypeConverter
import com.example.myapplication.core.domain.model.AddressProvider
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.domain.model.BranchProvider
import com.example.myapplication.core.domain.model.EmployeeProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * --- CONVERTERS PARA LA APP PRESTADOR (REPLICADO) ---
 * Maneja la serialización de objetos complejos para Room.
 */
class Converters {
    private val gson = Gson()

    // --- SECCIÓN: LISTAS SIMPLES (String) ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- SECCIÓN: MODELOS DE DIRECCIÓN ---
    @TypeConverter
    fun fromAddressProvider(value: AddressProvider?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAddressProvider(value: String?): AddressProvider? {
        return gson.fromJson(value, AddressProvider::class.java)
    }

    @TypeConverter
    fun fromAddressProviderList(value: List<AddressProvider>?): String {
        return gson.toJson(value ?: emptyList<AddressProvider>())
    }

    @TypeConverter
    fun toAddressProviderList(value: String?): List<AddressProvider> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<AddressProvider>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- SECCIÓN: MODELOS DE EMPRESA ---
    @TypeConverter
    fun fromCompanyProviderList(value: List<CompanyProvider>?): String {
        return gson.toJson(value ?: emptyList<CompanyProvider>())
    }

    @TypeConverter
    fun toCompanyProviderList(value: String?): List<CompanyProvider> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<CompanyProvider>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
