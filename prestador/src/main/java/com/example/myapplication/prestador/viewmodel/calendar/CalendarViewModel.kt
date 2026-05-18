package com.example.myapplication.prestador.viewmodel.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.dao.BookedAppointmentDao
import com.example.myapplication.prestador.data.local.entity.BookedAppointmentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val bookedAppointmentDao: BookedAppointmentDao
) : ViewModel() {

    val allAppointments: StateFlow<List<BookedAppointmentEntity>> = bookedAppointmentDao.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cancelAppointment(id: String) {
        viewModelScope.launch {
            bookedAppointmentDao.updateStatus(id, "CANCELLED")
        }
    }

    fun rescheduleAppointment(id: String) {
        viewModelScope.launch {
            bookedAppointmentDao.updateStatus(id, "RESCHEDULED")
        }
    }

    fun completeAppointment(id: String) {
        viewModelScope.launch {
            bookedAppointmentDao.updateStatus(id, "COMPLETED")
        }
    }
}