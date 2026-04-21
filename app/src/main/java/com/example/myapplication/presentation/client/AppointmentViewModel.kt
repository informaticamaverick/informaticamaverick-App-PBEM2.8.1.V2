package com.example.myapplication.presentation.client

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Appointment
import com.example.myapplication.data.model.AppointmentStatus
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository,
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    fun solicitarCita(
        provider: Provider,
        date: String,
        time: String,
        service: String,
        notes: String
    ) {
        val clientId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateLong = try {
                sdf.parse(date)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }

            val appointment = Appointment(
                clientId = clientId,
                provider = provider.uid,
                clientName = auth.currentUser?.displayName ?: "Cliente",
                providerName = provider.displayName,
                requestDate = dateLong,
                requestTime = time,
                notes = notes,
                status = AppointmentStatus.PENDING
            )

            repository.createAppointment(appointment)
        }
    }

    fun respondToProviderAppointment(
        chatId: String,
        messageId: String,
        appointmentId: String?,
        accept: Boolean,
        date: String? = null,
        time: String? = null,
        title: String? = null,
        providerName: String? = null,
        providerPhotoUrl: String? = null
    ) {
        val newStatus = if (accept) "ACCEPTED" else "REJECTED"
        val appointmentStatus = if (accept) AppointmentStatus.ACCEPTED else AppointmentStatus.REJECTED

        viewModelScope.launch {
            // 1. Actualizar en Realtime Database (para que el prestador vea el cambio)
            try {
                database.reference
                    .child("chats")
                    .child(chatId)
                    .child("messages")
                    .child(messageId)
                    .child("appointmentStatus")
                    .setValue(newStatus)
            } catch (e: Exception) {
                Log.e("AppointmentViewModel", "Error updating RTDB: ${e.message}")
            }

            // 2. Actualizar en Firestore si tenemos el ID de la cita
            if (appointmentId != null) {
                repository.updateAppointmentStatus(appointmentId, appointmentStatus)
                
                // Si se acepta y hay nueva fecha/hora (propuesta por prestador), actualizarla
                if (accept && date != null && time != null) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dateLong = try { sdf.parse(date)?.time ?: 0L } catch(e: Exception) { 0L }
                    if (dateLong != 0L) {
                        repository.updateAppointmentDateTime(appointmentId, dateLong, time)
                    }
                }
            }
        }
    }
}
