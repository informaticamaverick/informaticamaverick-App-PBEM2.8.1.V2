package com.example.myapplication.core.data.local

import androidx.room.TypeConverter
import com.example.myapplication.core.domain.model.*
import com.example.myapplication.core.data.local.entity.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * --- CONVERSORES DE TIPOS (ROOM) ---
 * [ELITE v5.1]: Soporte para AddressUnico y estructuras jerárquicas cliente/prestador.
 */
class Converters {
    private val gson = Gson()

    // --- 1. LISTAS DE TEXTO ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- 2. ADDRESS UNICO ---
    @TypeConverter
    fun fromAddressUnico(value: AddressUnico?): String? = gson.toJson(value)

    @TypeConverter
    fun toAddressUnico(value: String?): AddressUnico? = gson.fromJson(value, AddressUnico::class.java)

    @TypeConverter
    fun fromAddressUnicoList(value: List<AddressUnico>?): String = gson.toJson(value ?: emptyList<AddressUnico>())

    @TypeConverter
    fun toAddressUnicoList(value: String?): List<AddressUnico> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<AddressUnico>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- 3. MODELOS DE USUARIO / CLIENTE ---
    @TypeConverter
    fun fromCompanyClientList(value: List<CompanyClient>?): String = gson.toJson(value ?: emptyList<CompanyClient>())

    @TypeConverter
    fun toCompanyClientList(value: String?): List<CompanyClient> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<CompanyClient>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- 4. MODELOS DE PRESTADOR ---
    @TypeConverter
    fun fromCompanyProviderList(value: List<CompanyProvider>?): String = gson.toJson(value ?: emptyList<CompanyProvider>())

    @TypeConverter
    fun toCompanyProviderList(value: String?): List<CompanyProvider> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<CompanyProvider>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- 5. MODELOS DE PRESUPUESTO ---
    @TypeConverter
    fun fromBudgetItemList(value: List<BudgetItem>?): String = gson.toJson(value ?: emptyList<BudgetItem>())

    @TypeConverter
    fun toBudgetItemList(value: String?): List<BudgetItem> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetItem>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromBudgetServiceList(value: List<BudgetService>?): String = gson.toJson(value ?: emptyList<BudgetService>())

    @TypeConverter
    fun toBudgetServiceList(value: String?): List<BudgetService> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetService>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromBudgetFeeList(value: List<BudgetProfessionalFee>?): String = gson.toJson(value ?: emptyList<BudgetProfessionalFee>())

    @TypeConverter
    fun toBudgetFeeList(value: String?): List<BudgetProfessionalFee> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetProfessionalFee>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromBudgetMiscList(value: List<BudgetMiscExpense>?): String = gson.toJson(value ?: emptyList<BudgetMiscExpense>())

    @TypeConverter
    fun toBudgetMiscList(value: String?): List<BudgetMiscExpense> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetMiscExpense>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromBudgetTaxList(value: List<BudgetTax>?): String = gson.toJson(value ?: emptyList<BudgetTax>())

    @TypeConverter
    fun toBudgetTaxList(value: String?): List<BudgetTax> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetTax>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- 6. ENUMS ---
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

    @TypeConverter
    fun fromBudgetStatus(value: BudgetStatus): String = value.name

    @TypeConverter
    fun toBudgetStatus(value: String): BudgetStatus = enumValueOf<BudgetStatus>(value)
}
