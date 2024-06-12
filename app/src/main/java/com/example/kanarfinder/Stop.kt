package com.example.kanarfinder

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Stop(
    val lineNumber: String? = null,
    val stopName: String? = null,
    val timestamp: Long? = null
) {
    fun getFormattedTimestamp(): String {
        val offsetTime = timestamp?.let { it + 2 * 60 * 60 * 1000 } // Dodaj 2 godziny (2 * 60 minut * 60 sekund * 1000 milisekund)
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return offsetTime?.let { dateFormat.format(Date(it)) } ?: "Unknown time"
    }
}
