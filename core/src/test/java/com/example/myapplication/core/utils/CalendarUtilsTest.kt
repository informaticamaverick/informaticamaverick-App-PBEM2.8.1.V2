package com.example.myapplication.core.utilidades

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.*

class CalendarUtilsTest {

    @Test
    fun testConvertToUtc_Formats() {
        val date1 = "2024-06-25"
        val time1 = "10:00"
        val ts1 = CalendarUtils.convertToUtc(date1, time1)
        
        val date2 = "25/06/2024"
        val time2 = "10:00hs"
        val ts2 = CalendarUtils.convertToUtc(date2, time2)
        
        // Ambos deberían representar el mismo momento en UTC
        assertNotEquals("Timestamp should not be 0", 0L, ts1)
        assertEquals("Different formats should yield same timestamp", ts1, ts2)
    }

    @Test
    fun testConvertToUtc_TimezoneIsolation() {
        // Este test verifica que no haya desfase por zona horaria local
        val date = "2024-01-01"
        val time = "00:00"
        val ts = CalendarUtils.convertToUtc(date, time)
        
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = ts
        
        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH))
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
    }
}






























