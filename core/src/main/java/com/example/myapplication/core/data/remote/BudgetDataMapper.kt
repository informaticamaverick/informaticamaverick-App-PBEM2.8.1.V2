package com.example.myapplication.core.data.remote

import android.util.Log
import com.example.myapplication.core.data.local.entity.*
import com.google.firebase.firestore.DocumentSnapshot

/**
 * --- BUDGET & TENDER DATA MAPPER (COMPARTIDO) ---
 * Centraliza la conversión de Presupuestos y Licitaciones desde Firestore.
 * Garantiza la integridad matemática de los cálculos comerciales en ambas apps.
 */
object BudgetDataMapper {

    /**
     * Mapea un presupuesto (Budget) desde Firestore.
     */
    fun fromFirestoreBudget(doc: DocumentSnapshot): BudgetEntity? {
        if (!doc.exists()) return null
        return try {
            val data = doc.data ?: return null

            // Mapeo de Items con seguridad de tipos
            val itemsRaw = data["items"] as? List<*> ?: emptyList<Any>()
            val mappedItems = itemsRaw.mapNotNull { it as? Map<*, *> }.map { i ->
                BudgetItem(
                    code = i["code"] as? String ?: "",
                    description = i["description"] as? String ?: "",
                    quantity = (i["quantity"] as? Number)?.toInt() ?: 1,
                    unitPrice = (i["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                    taxPercentage = (i["taxPercentage"] as? Number)?.toDouble() ?: 0.0,
                    discountPercentage = (i["discountPercentage"] as? Number)?.toDouble() ?: 0.0
                )
            }

            // Mapeo de Servicios
            val servicesRaw = data["services"] as? List<*> ?: emptyList<Any>()
            val mappedServices = servicesRaw.mapNotNull { it as? Map<*, *> }.map { s ->
                BudgetService(
                    code = s["code"] as? String ?: "",
                    description = s["description"] as? String ?: "",
                    total = (s["total"] as? Number)?.toDouble() ?: 0.0
                )
            }

            // Mapeo de Honorarios
            val feesRaw = data["professionalFees"] as? List<*> ?: emptyList<Any>()
            val mappedFees = feesRaw.mapNotNull { it as? Map<*, *> }.map { f ->
                BudgetProfessionalFee(
                    code = f["code"] as? String ?: "",
                    description = f["description"] as? String ?: "",
                    total = (f["total"] as? Number)?.toDouble() ?: 0.0
                )
            }

            // Mapeo de Gastos Varios
            val miscRaw = data["miscExpenses"] as? List<*> ?: emptyList<Any>()
            val mappedMisc = miscRaw.mapNotNull { it as? Map<*, *> }.map { m ->
                BudgetMiscExpense(
                    description = m["description"] as? String ?: "",
                    amount = (m["amount"] as? Number)?.toDouble() ?: 0.0
                )
            }

            // Mapeo de Impuestos
            val taxesRaw = data["taxes"] as? List<*> ?: emptyList<Any>()
            val mappedTaxes = taxesRaw.mapNotNull { it as? Map<*, *> }.map { t ->
                BudgetTax(
                    description = t["description"] as? String ?: "",
                    amount = (t["amount"] as? Number)?.toDouble() ?: 0.0
                )
            }

            BudgetEntity(
                budgetId = doc.id,
                clientId = data["clientId"] as? String ?: "",
                providerId = data["providerId"] as? String ?: "",
                tenderId = data["tenderId"] as? String,
                category = data["category"] as? String,
                providerName = data["providerName"] as? String ?: "Prestador",
                providerCompanyName = data["providerCompanyName"] as? String,
                providerPhotoUrl = data["providerPhotoUrl"] as? String,
                providerThumbnail = data["providerThumbnail"] as? String ?: data["thumbnailBase64"] as? String, // [ELITE v5.4]
                items = mappedItems,
                services = mappedServices,
                professionalFees = mappedFees,
                miscExpenses = mappedMisc,
                taxes = mappedTaxes,
                imageUrls = (data["imageUrls"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                subtotal = (data["subtotal"] as? Number)?.toDouble() ?: 0.0,
                taxAmount = (data["taxAmount"] as? Number)?.toDouble() ?: 0.0,
                discountAmount = (data["discountAmount"] as? Number)?.toDouble() ?: 0.0,
                grandTotal = (data["grandTotal"] as? Number)?.toDouble() ?: 0.0,
                validityDays = (data["validityDays"] as? Number)?.toInt() ?: 7,
                notes = data["notes"] as? String,
                paymentMethods = data["paymentMethods"] as? String,
                warrantyInfo = data["warrantyInfo"] as? String,
                executionTime = data["executionTime"] as? String,
                status = try { BudgetStatus.valueOf(data["status"] as? String ?: "PENDIENTE") } catch(e: Exception) { BudgetStatus.PENDIENTE },
                isRead = data["isRead"] as? Boolean ?: false,
                dateTimestamp = (data["dateTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("BudgetDataMapper", "Error mapeando Presupuesto ${doc.id}: ${e.message}")
            null
        }
    }

    /**
     * Mapea una licitación (Tender) desde Firestore.
     */
    fun fromFirestoreTender(doc: DocumentSnapshot): TenderEntity? {
        if (!doc.exists()) return null
        return try {
            val data = doc.data ?: return null
            TenderEntity(
                tenderId = doc.id,
                title = data["title"] as? String ?: "",
                clientId = data["clientId"] as? String ?: "",
                description = data["description"] as? String ?: "",
                category = data["category"] as? String ?: "",
                status = data["status"] as? String ?: "ABIERTA",
                isActive = data["isActive"] as? Boolean ?: true,
                dateTimestamp = (data["dateTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                startDate = (data["startDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                endDate = (data["endDate"] as? Number)?.toLong() ?: 0L,
                budgetCount = (data["budgetCount"] as? Number)?.toInt() ?: 0,
                cancellationDate = (data["cancellationDate"] as? Number)?.toLong(),
                awardedProviderId = data["awardedProviderId"] as? String,
                awardedProviderName = data["awardedProviderName"] as? String,
                awardedBudgetId = data["awardedBudgetId"] as? String,
                awardedProviderPhotoUrl = data["awardedProviderPhotoUrl"] as? String,
                awardedProviderThumbnail = data["awardedProviderThumbnail"] as? String ?: data["awardedThumbnailBase64"] as? String, // [ELITE v5.4]
                requiresVisit = data["requiresVisit"] as? Boolean ?: false,
                requiresPaymentMethod = data["requiresPaymentMethod"] as? Boolean ?: false,
                requiresWorkGuarantee = data["requiresWorkGuarantee"] as? Boolean ?: false,
                requiresProviderDoc = data["requiresProviderDoc"] as? Boolean ?: false,
                locationAddress = data["locationAddress"] as? String,
                locationNumber = data["locationNumber"] as? String,
                locationLocality = data["locationLocality"] as? String,
                locationPostalCode = data["locationPostalCode"] as? String,
                locationType = data["locationType"] as? String,
                clientDisplayName = data["clientDisplayName"] as? String,
                companyName = data["companyName"] as? String,
                branchName = data["branchName"] as? String,
                expiresAt = (data["expiresAt"] as? Number)?.toLong(),
                matchKey = data["matchKey"] as? String,
                imageUrls = (data["imageUrls"] as? List<*>)?.map { it.toString() } ?: emptyList()
            )
        } catch (e: Exception) {
            Log.e("BudgetDataMapper", "Error mapeando Licitación ${doc.id}: ${e.message}")
            null
        }
    }
}
