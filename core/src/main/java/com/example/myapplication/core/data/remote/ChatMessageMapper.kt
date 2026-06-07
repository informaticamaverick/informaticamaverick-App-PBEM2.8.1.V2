package com.example.myapplication.core.data.remote

import android.content.Context
import com.example.myapplication.core.data.local.Converters
import com.example.myapplication.core.data.local.entity.*
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.core.ChatIdHelper
import com.google.firebase.database.DataSnapshot
import org.json.JSONObject
import java.util.UUID

/**
 * --- CHAT MESSAGE MAPPER (COMPARTIDO) ---
 * Centraliza la conversión de DataSnapshot (Firebase Realtime Database) a MessageEntity (Room).
 * Maneja la compatibilidad entre los esquemas de la App Cliente y App Prestador.
 */
object ChatMessageMapper {

    /**
     * 🔥 [ELITE SSOT] Parsea un presupuesto desde el JSON embebido en el mensaje de chat.
     * Permite que el Administrador de Presupuestos funcione offline.
     * Soporta tanto el JSON estructurado moderno como el formato legacy (Pipes/Semicolons).
     */
    fun parseBudgetFromJson(
        jsonString: String,
        budgetId: String,
        providerId: String,
        clientId: String
    ): BudgetEntity? {
        return try {
            val json = JSONObject(jsonString)
            val converters = Converters()

            // 1. Extraer los bloques (pueden ser JSON Array o String Legacy)
            val itemsRaw = json.optString("items")
            val servicesRaw = json.optString("servicios")
            val feesRaw = json.optString("honorarios")
            val miscRaw = json.optString("gastos")
            val taxesRaw = json.optString("impuestosJ")

            // Funciones locales de parsing para centralizar lógica que antes estaba en la UI
            fun parseDynamicItems(raw: String): List<BudgetItem> {
                if (raw.isBlank()) return emptyList()
                if (raw.startsWith("[")) return converters.toBudgetItemList(raw)
                return raw.split("|").mapNotNull { line ->
                    val parts = line.split(";")
                    if (parts.size >= 4) BudgetItem(
                        code = parts[0], description = parts[1],
                        quantity = parts[2].toIntOrNull() ?: 1,
                        unitPrice = parts[3].toDoubleOrNull() ?: 0.0,
                        taxPercentage = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                        discountPercentage = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                    ) else null
                }
            }

            fun parseDynamicServices(raw: String): List<BudgetService> {
                if (raw.isBlank()) return emptyList()
                if (raw.startsWith("[")) return converters.toBudgetServiceList(raw)
                return raw.split("|").mapNotNull { line ->
                    val parts = line.split(";")
                    if (parts.size >= 2) BudgetService(
                        code = parts[0], description = parts[1],
                        total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                    ) else null
                }
            }

            fun parseDynamicFees(raw: String): List<BudgetProfessionalFee> {
                if (raw.isBlank()) return emptyList()
                if (raw.startsWith("[")) return converters.toBudgetFeeList(raw)
                return raw.split("|").mapNotNull { line ->
                    val parts = line.split(";")
                    if (parts.size >= 2) BudgetProfessionalFee(
                        code = parts[0], description = parts[1],
                        total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                    ) else null
                }
            }

            fun parseDynamicMisc(raw: String): List<BudgetMiscExpense> {
                if (raw.isBlank()) return emptyList()
                if (raw.startsWith("[")) return converters.toBudgetMiscList(raw)
                return raw.split("|").mapNotNull { line ->
                    val parts = line.split(";")
                    if (parts.size >= 2) BudgetMiscExpense(
                        description = parts[0],
                        amount = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                    ) else null
                }
            }

            fun parseDynamicTaxes(raw: String): List<BudgetTax> {
                if (raw.isBlank()) return emptyList()
                if (raw.startsWith("[")) return converters.toBudgetTaxList(raw)
                return raw.split("|").mapNotNull { line ->
                    val parts = line.split(";")
                    if (parts.size >= 2) BudgetTax(
                        description = parts[0],
                        amount = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                    ) else null
                }
            }

            BudgetEntity(
                budgetId = budgetId,
                clientId = clientId,
                providerId = providerId,
                providerName = "Prestador", // Se enriquecerá con Room
                providerCompanyName = json.optString("companyName").takeIf { it.isNotBlank() },
                category = json.optString("categorias"),
                items = parseDynamicItems(itemsRaw),
                services = parseDynamicServices(servicesRaw),
                professionalFees = parseDynamicFees(feesRaw),
                miscExpenses = parseDynamicMisc(miscRaw),
                taxes = parseDynamicTaxes(taxesRaw),
                subtotal = json.optDouble("subtotal", 0.0),
                taxAmount = json.optDouble("impuestos", 0.0),
                grandTotal = json.optDouble("total", 0.0),
                validityDays = json.optInt("validezDays", 7),
                notes = "Presupuesto Nro: ${json.optString("numero")}\n${json.optString("notas")}",
                status = BudgetStatus.PENDIENTE
            )
        } catch (e: Exception) {
            android.util.Log.e("ChatMessageMapper", "❌ [BUDGET_PARSE_ERROR] ${e.message}")
            null
        }
    }

    /**
     * 🔥 [ELITE ONDEMAND] Parsea solo lo esencial para la burbuja de chat.
     */
    fun parseBudgetSummaryFromJson(jsonString: String, budgetId: String): BudgetEntity? {
        return try {
            val json = JSONObject(jsonString)
            BudgetEntity(
                budgetId = budgetId,
                clientId = "", 
                providerId = "",
                providerName = "Prestador",
                providerCompanyName = null,
                category = json.optString("categorias"),
                items = emptyList(), 
                services = emptyList(),
                grandTotal = json.optDouble("total", 0.0),
                validityDays = json.optInt("validezDays", 7),
                status = BudgetStatus.PENDIENTE
            )
        } catch (e: Exception) {
            null
        }
    }

    fun fromDataSnapshot(
        snapshot: DataSnapshot, 
        chatId: String, 
        context: Context? = null,
        myUserId: String = "" 
    ): MessageEntity? {
        return try {
            val senderIdFromSnap = snapshot.child("senderId").getValue(String::class.java) ?: ""
            if (senderIdFromSnap.isBlank()) return null
            
            val effectiveMyUserId = myUserId.ifBlank { 
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "" 
            }

            // Resolución de IDs
            val id = snapshot.child("id").getValue(String::class.java)
                ?: snapshot.child("messageId").getValue(String::class.java)
                ?: snapshot.key
                ?: UUID.randomUUID().toString()

            val typeStr = snapshot.child("type").getValue(String::class.java)
                ?: snapshot.child("messageType").getValue(String::class.java)
                ?: "TEXT"
            val type = try { MessageType.valueOf(typeStr) } catch (e: Exception) { MessageType.TEXT }

            var finalImageUrl = snapshot.child("imageUrl").getValue(String::class.java)
            var finalContent = snapshot.child("content").getValue(String::class.java)
                ?: snapshot.child("text").getValue(String::class.java)
                ?: ""
            
            // 🔥 [ELITE v8.0] Verificación de Presupuesto Embebido
            if (type == MessageType.BUDGET && finalContent.isBlank()) {
                finalContent = "[Presupuesto]"
            }

            // Multimedia Base64 a Archivo Local
            if (context != null && (type == MessageType.IMAGE || type == MessageType.AUDIO) && finalContent.length > 500) {
                val prefix = if (type == MessageType.IMAGE) "IMG_" else "AUD_"
                val ext = if (type == MessageType.IMAGE) ".webp" else ".m4a"
                val localPath = ImageUtils.saveBase64ToFile(context, finalContent, id, prefix, ext)
                if (localPath != null) {
                    finalImageUrl = localPath
                    finalContent = if (type == MessageType.IMAGE) "[Imagen]" else "[Audio]"
                }
            }

            val chatIdFromSnap = snapshot.child("chatId").getValue(String::class.java)
                ?: snapshot.child("conversationId").getValue(String::class.java)
                ?: chatId

            // 🔥 [ELITE v8.1] SYMMETRIC ROOM TAGGING (SSOT)
            // Extraemos los contextos basándonos en el ROL del usuario local.
            // 1. Extraer contextos del ID como fallback de seguridad
            val (cMy_FB, bMy_FB, _) = ChatIdHelper.extractMyContext(chatIdFromSnap, effectiveMyUserId)
            val (cOther_FB, bOther_FB, _) = ChatIdHelper.extractOtherContext(chatIdFromSnap, effectiveMyUserId)

            // 2. Prioridad a los tags explícitos del mensaje (v8+)
            val isMeSender = senderIdFromSnap == effectiveMyUserId

            val finalLocalB = snapshot.child(if (isMeSender) "senderBranchId" else "receiverBranchId").getValue(String::class.java) ?: bMy_FB
            val finalLocalC = snapshot.child(if (isMeSender) "senderCompanyId" else "receiverCompanyId").getValue(String::class.java) ?: cMy_FB
            val finalRemoteB = snapshot.child(if (isMeSender) "receiverBranchId" else "senderBranchId").getValue(String::class.java) ?: bOther_FB
            val finalRemoteC = snapshot.child(if (isMeSender) "receiverCompanyId" else "senderCompanyId").getValue(String::class.java) ?: cOther_FB

            MessageEntity(
                id = id,
                chatId = chatIdFromSnap,
                senderId = senderIdFromSnap,
                receiverId = snapshot.child("receiverId").getValue(String::class.java) ?: "",
                type = type,
                content = finalContent,
                imageUrl = finalImageUrl,
                thumbnailBase64 = snapshot.child("thumbnailBase64").getValue(String::class.java),
                latitude = snapshot.child("latitude").getValue(Double::class.java),
                longitude = snapshot.child("longitude").getValue(Double::class.java),
                locationAddress = snapshot.child("locationAddress").getValue(String::class.java)
                    ?: snapshot.child("budgetRequestClientAddress").getValue(String::class.java),
                durationSeconds = snapshot.child("durationSeconds").getValue(Int::class.java),
                relatedId = snapshot.child("relatedId").getValue(String::class.java)
                    ?: snapshot.child("budgetId").getValue(String::class.java)
                    ?: snapshot.child("tenderId").getValue(String::class.java),

                budgetDataJson = snapshot.child("budgetDataJson").getValue(String::class.java),
                rejectionReason = snapshot.child("rejectionReason").getValue(String::class.java),
                budgetRequestDescription = snapshot.child("budgetRequestDescription").getValue(String::class.java)
                    ?: snapshot.child("problem").getValue(String::class.java),
                budgetRequestClientAddress = snapshot.child("budgetRequestClientAddress").getValue(String::class.java)
                    ?: snapshot.child("locationAddress").getValue(String::class.java),

                appointmentDate = snapshot.child("appointmentDate").getValue(String::class.java)
                    ?: snapshot.child("date").getValue(String::class.java),
                appointmentTime = snapshot.child("appointmentTime").getValue(String::class.java)
                    ?: snapshot.child("time").getValue(String::class.java),
                appointmentStatus = snapshot.child("appointmentStatus").getValue(String::class.java),
                appointmentType = snapshot.child("appointmentType").getValue(String::class.java),
                providerAddress = snapshot.child("providerAddress").getValue(String::class.java),
                
                companyId = snapshot.child("companyId").getValue(String::class.java),
                branchId = snapshot.child("branchId").getValue(String::class.java),
                categoryId = snapshot.child("categoryId").getValue(String::class.java),

                // Tags de Identidad Simétrica (Para Firebase)
                senderBranchId = snapshot.child("senderBranchId").getValue(String::class.java) ?: (if(isMeSender) bMy_FB else bOther_FB),
                senderCompanyId = snapshot.child("senderCompanyId").getValue(String::class.java) ?: (if(isMeSender) cMy_FB else cOther_FB),
                receiverBranchId = snapshot.child("receiverBranchId").getValue(String::class.java) ?: (if(isMeSender) bOther_FB else bMy_FB),
                receiverCompanyId = snapshot.child("receiverCompanyId").getValue(String::class.java) ?: (if(isMeSender) cOther_FB else cMy_FB),

                // 🔥 [ELITE v8.1] SOBERANÍA LOCAL
                localBranchId = finalLocalB,
                localCompanyId = finalLocalC,
                remoteBranchId = finalRemoteB,
                remoteCompanyId = finalRemoteC,

                calendarStartDate = snapshot.child("calendarStartDate").getValue(String::class.java),
                calendarEndDate = snapshot.child("calendarEndDate").getValue(String::class.java),
                availabilityJson = snapshot.child("availabilityJson").getValue(String::class.java),
                bookedSlotsJson = snapshot.child("bookedSlotsJson").getValue(String::class.java),
                calendarInviteMessageId = snapshot.child("calendarInviteMessageId").getValue(String::class.java),

                receiptService = snapshot.child("receiptService").getValue(String::class.java),
                receiptProviderName = snapshot.child("receiptProviderName").getValue(String::class.java),
                receiptIsTechnician = snapshot.child("receiptIsTechnician").getValue(Boolean::class.java),
                receiptProfession = snapshot.child("receiptProfession").getValue(String::class.java),
                receiptAddress = snapshot.child("receiptAddress").getValue(String::class.java)
                    ?: snapshot.child("providerAddress").getValue(String::class.java),
                receiptCode = snapshot.child("receiptCode").getValue(String::class.java),
                receiptPrioritizeCompany = snapshot.child("receiptPrioritizeCompany").getValue(Boolean::class.java),

                imageLocalPath = finalImageUrl?.takeIf { it.startsWith("/") },
                audioLocalPath = if (type == MessageType.AUDIO && finalImageUrl?.startsWith("/") == true) finalImageUrl else null,

                replyToId = snapshot.child("replyToId").getValue(String::class.java),
                replyToContent = snapshot.child("replyToContent").getValue(String::class.java),
                replyToSenderName = snapshot.child("replyToSenderName").getValue(String::class.java),

                timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis(),
                status = snapshot.child("status").getValue(String::class.java) ?: "SENT",
                isRead = snapshot.child("isRead").getValue(Boolean::class.java) ?: false,
                isDelivered = snapshot.child("isDelivered").getValue(Boolean::class.java) ?: false,
                isSynced = true
            )
        } catch (e: Exception) {
            null
        }
    }
}
