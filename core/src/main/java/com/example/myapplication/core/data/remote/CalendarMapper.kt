package com.example.myapplication.core.data.remote

import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- CALENDAR MAPPER (COMPARTIDO) ---
 * Centraliza la lógica de parseo y generación de slots para turnos y visitas técnicas.
 * Sigue la política de "Cero Lógica en UI" y asegura consistencia entre Cliente y Prestador.
 */
object CalendarMapper {

    data class DayAvailability(
        val date: Date,
        val startTime: String, // "HH:mm"
        val endTime: String,   // "HH:mm"
        val slotDurationMinutes: Int
    )

    data class TimeSlot(
        val time: String, // "HH:mm"
        val isOccupied: Boolean
    )

    fun parseAvailabilityJson(json: String): List<DayAvailability> {
        val list = mutableListOf<DayAvailability>()
        try {
            val array = JSONArray(json)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val dateStr = obj.getString("date")
                val startTimeStr = obj.getString("startTime")
                val endTimeStr = obj.getString("endTime")
                val duration = obj.getInt("durationMinutes")
                
                val date = dateFormatter.parse(dateStr)
                if (date != null) {
                    list.add(DayAvailability(
                        date = date,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        slotDurationMinutes = duration
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedBy { it.date }
    }

    fun parseBookedSlotsJson(json: String): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val dateStr = obj.getString("date")
                val timeStr = obj.getString("time")
                list.add(dateStr to timeStr)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun generateSlotsFromAvailability(avail: DayAvailability, booked: List<Pair<String, String>>): List<TimeSlot> {
        val slots = mutableListOf<TimeSlot>()
        val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateKey = dateSdf.format(avail.date)
        
        val duration = if (avail.slotDurationMinutes > 0) avail.slotDurationMinutes else 60

        try {
            var current = timeSdf.parse(avail.startTime)
            val end = timeSdf.parse(avail.endTime)
            
            if (current != null && end != null) {
                val calendar = Calendar.getInstance()
                if (duration <= 0) return emptyList()

                while (true) {
                    val currentTime = current!!
                    calendar.time = currentTime
                    val next = Calendar.getInstance().apply {
                        time = currentTime
                        add(Calendar.MINUTE, duration)
                    }.time
                    
                    if (next.after(end)) break
                    
                    val currentTimeStr = timeSdf.format(current)
                    val isOccupied = booked.any { it.first == dateKey && it.second == currentTimeStr }
                    slots.add(TimeSlot(time = currentTimeStr, isOccupied = isOccupied))
                    current = next
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return slots
    }
}
