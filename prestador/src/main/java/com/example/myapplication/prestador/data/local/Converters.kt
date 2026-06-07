package com.example.myapplication.prestador.data.local

import androidx.room.TypeConverter
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.domain.model.BranchProvider
import com.example.myapplication.core.domain.model.EmployeeProvider
import com.example.myapplication.core.domain.model.CompanyClient
import com.example.myapplication.core.domain.model.MessageType
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
    fun fromAddressUnico(value: AddressUnico?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAddressUnico(value: String?): AddressUnico? {
        return gson.fromJson(value, AddressUnico::class.java)
    }

    @TypeConverter
    fun fromAddressUnicoList(value: List<AddressUnico>?): String {
        return gson.toJson(value ?: emptyList<AddressUnico>())
    }

    @TypeConverter
    fun toAddressUnicoList(value: String?): List<AddressUnico> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<AddressUnico>>() {}.type
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

    // --- SECCIÓN: MODELOS DE USUARIO / CLIENTE ---
    @TypeConverter
    fun fromCompanyClientList(value: List<CompanyClient>?): String {
        return gson.toJson(value ?: emptyList<CompanyClient>())
    }

    @TypeConverter
    fun toCompanyClientList(value: String?): List<CompanyClient> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<CompanyClient>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- SECCIÓN: ENUMS COMPARTIDOS ---
    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return try { MessageType.valueOf(value) } catch (e: Exception) { MessageType.TEXT }
    }
}
