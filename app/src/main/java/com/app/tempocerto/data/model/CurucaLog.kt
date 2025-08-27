package com.app.tempocerto.data.model

import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CurucaLog(
    val date: String = "",
    val temperature: Float = 0f,
    val salinity: Float = 0f,
    val pressure: Float = 0f
) {

    private val parsedDateTime: LocalDateTime? by lazy {
        try {
            if (date.isBlank()) {
                null
            } else {
                LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        } catch (e: Exception) {
            Log.e("CurucaLog", "Falha CRÍTICA ao converter a string de data: '$date'", e)
            null
        }
    }

    fun toMap(): Map<String, String> {
        return mapOf(
            "Temperatura" to "%.2f °C".format(temperature),
            "Salinidade" to "%.2f PSU".format(salinity),
            "Pressão" to "%.2f hPa".format(pressure)
        )
    }

    fun getLocalDate(): LocalDate? {
        return parsedDateTime?.toLocalDate()
    }

    fun getTime(): String {
        return parsedDateTime?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: ""
    }

    fun getValue(parameter: CurucaParameters): String {
        return when (parameter) {
            CurucaParameters.Temperature -> "%.2f °C".format(temperature)
            CurucaParameters.Salinity -> "%.2f PSU".format(salinity)
            CurucaParameters.Pressure -> "%.2f hPa".format(pressure)
        }
    }

    fun getRawValue(parameter: CurucaParameters): Float {
        return when (parameter) {
            CurucaParameters.Temperature -> temperature
            CurucaParameters.Salinity -> salinity
            CurucaParameters.Pressure -> pressure
        }
    }
}