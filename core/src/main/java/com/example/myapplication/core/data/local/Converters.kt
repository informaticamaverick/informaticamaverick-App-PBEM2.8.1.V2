package com.example.myapplication.core.data.local

import androidx.room.TypeConverter
import com.example.myapplication.core.data.local.entity.*
import com.example.myapplication.core.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * --- CONVERSORES DE TIPOS (ROOM) ---
 * Esta clase le enseña a Room cómo guardar objetos complejos (listas, enums, clases personalizadas)
 * convirtiéndolos a texto JSON y viceversa. Es fundamental para que el módulo compartido
 * pueda persistir estructuras como presupuestos y perfiles de usuario.
 */
class Converters {
    private val gson = Gson()

    // --- 1. LISTAS DE TEXTO (STRING) ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- 2. MODELOS DE USUARIO Y CLIENTE ---
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

    @TypeConverter
    fun fromAddressClientList(value: List<AddressClient>?): String {
        return gson.toJson(value ?: emptyList<AddressClient>())
    }

    @TypeConverter
    fun toAddressClientList(value: String?): List<AddressClient> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<AddressClient>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- 3. MODELOS DE PRESTADOR ---
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

    // --- 4. MODELOS DE PRESUPUESTO ---
    @TypeConverter
    fun fromBudgetItemList(value: List<BudgetItem>?): String {
        return gson.toJson(value ?: emptyList<BudgetItem>())
    }

    @TypeConverter
    fun toBudgetItemList(value: String?): List<BudgetItem> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetItem>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromBudgetServiceList(value: List<BudgetService>?): String {
        return gson.toJson(value ?: emptyList<BudgetService>())
    }

    @TypeConverter
    fun toBudgetServiceList(value: String?): List<BudgetService> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetService>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromBudgetFeeList(value: List<BudgetProfessionalFee>?): String {
        return gson.toJson(value ?: emptyList<BudgetProfessionalFee>())
    }

    @TypeConverter
    fun toBudgetFeeList(value: String?): List<BudgetProfessionalFee> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetProfessionalFee>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromBudgetMiscList(value: List<BudgetMiscExpense>?): String {
        return gson.toJson(value ?: emptyList<BudgetMiscExpense>())
    }

    @TypeConverter
    fun toBudgetMiscList(value: String?): List<BudgetMiscExpense> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetMiscExpense>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromBudgetTaxList(value: List<BudgetTax>?): String {
        return gson.toJson(value ?: emptyList<BudgetTax>())
    }

    @TypeConverter
    fun toBudgetTaxList(value: String?): List<BudgetTax> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetTax>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- 5. ENUMS ---
    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return try { MessageType.valueOf(value) } catch (e: Exception) { MessageType.TEXT }
    }

    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = enumValueOf<EventType>(value)

    @TypeConverter
    fun fromVisitStatus(value: VisitStatus): String = value.name

    @TypeConverter
    fun toVisitStatus(value: String): VisitStatus = enumValueOf<VisitStatus>(value)
}
