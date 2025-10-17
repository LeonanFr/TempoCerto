package com.app.tempocerto.data.model

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class CurucaLog(
    val dateTime: ZonedDateTime? = null,
    val temperature: Float? = null,
    val salinity: Float? = null,
    val pressure: Float? = null
) {
    fun toMap(): Map<String, String> {
        val notAvailable = "N/A"
        return mapOf(
            "Temperatura" to (temperature?.let { "%.2f °C".format(it) } ?: notAvailable),
            "Salinidade" to (salinity?.let { "%.2f PSU".format(it) } ?: notAvailable),
            "Pressão" to (pressure?.let { "%.2f hPa".format(it) } ?: notAvailable)
        )
    }

    fun getLocalDate(): LocalDate? {
        return dateTime?.toLocalDate()
    }

    fun getTime(): String {
        return dateTime?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: "--:--:--"
    }

    fun getValue(parameter: CurucaParameters): String {
        val notAvailable = "N/A"
        return when (parameter) {
            CurucaParameters.Temperature -> temperature?.let { "%.2f °C".format(it) } ?: notAvailable
            CurucaParameters.Salinity -> salinity?.let { "%.2f PSU".format(it) } ?: notAvailable
            CurucaParameters.Pressure -> pressure?.let { "%.2f hPa".format(it) } ?: notAvailable
        }
    }

    fun getRawValue(parameter: CurucaParameters): Float? {
        return when (parameter) {
            CurucaParameters.Temperature -> temperature
            CurucaParameters.Salinity -> salinity
            CurucaParameters.Pressure -> pressure
        }
    }
}