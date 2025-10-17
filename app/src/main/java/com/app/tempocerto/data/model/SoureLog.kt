package com.app.tempocerto.data.model

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SoureLog(
    val dateTime: ZonedDateTime? = null,
    val airHumidity: Float? = null,
    val airPressure: Float? = null,
    val airTemperature: Float? = null,
    val precipitation: Float? = null,
    val windDirection: Int? = null,
    val waterLevel: Int? = null,
    val windSpeedMax: Float? = null,
    val windSpeedMean: Float? = null,
    val windSpeedMin: Float? = null,
    val waterTemperature: Float? = null
) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "Umidade do Ar" to getValue(SoureParameters.AirHumidity),
            "Pressão Atmosférica" to getValue(SoureParameters.AtmosphericPressure),
            "Temperatura do Ar" to getValue(SoureParameters.AirTemperature),
            "Nível da Água" to getValue(SoureParameters.WaterLevel),
            "Temperatura da Água" to getValue(SoureParameters.WaterTemperature),
            "Precipitação" to getValue(SoureParameters.Precipitation),
            "Vento (Médio)" to getValue(SoureParameters.WindSpeedMean),
            "Vento (Máximo)" to getValue(SoureParameters.WindSpeedMax),
            "Direção do Vento" to getValue(SoureParameters.WindDirection)
        )
    }

    fun getLocalDate(): LocalDate? {
        return dateTime?.toLocalDate()
    }

    fun getTime(): String {
        return dateTime?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: "--:--:--"
    }

    fun getRawValue(parameter: SoureParameters): Float? {
        return when (parameter) {
            SoureParameters.AirHumidity -> airHumidity
            SoureParameters.AtmosphericPressure -> airPressure
            SoureParameters.AirTemperature -> airTemperature
            SoureParameters.Precipitation -> precipitation
            SoureParameters.WindDirection -> windDirection?.toFloat()
            SoureParameters.WaterLevel -> waterLevel?.toFloat()
            SoureParameters.WindSpeedMax -> windSpeedMax
            SoureParameters.WindSpeedMean -> windSpeedMean
            SoureParameters.WindSpeedMin -> windSpeedMin
            SoureParameters.WaterTemperature -> waterTemperature
        }
    }

    fun getValue(parameter: SoureParameters): String {
        val rawValue = getRawValue(parameter)
        val locale = Locale.US

        when {
            rawValue == null -> return "N/D"
            rawValue < 0 -> return "Defeito"
            rawValue == 0f -> return "Sem Coleta"
        }

        return when (parameter) {
            SoureParameters.AirHumidity -> "%.1f %%".format(locale, rawValue)
            SoureParameters.AtmosphericPressure -> "%.2f hPa".format(locale, rawValue)
            SoureParameters.AirTemperature -> "%.2f °C".format(locale, rawValue)
            SoureParameters.Precipitation -> "%.1f mm".format(locale, rawValue)
            SoureParameters.WindDirection -> "${rawValue.toInt()} °"
            SoureParameters.WaterLevel -> "${rawValue.toInt()} cm"
            SoureParameters.WindSpeedMax -> "%.2f m/s".format(locale, rawValue)
            SoureParameters.WindSpeedMean -> "%.2f m/s".format(locale, rawValue)
            SoureParameters.WindSpeedMin -> "%.2f m/s".format(locale, rawValue)
            SoureParameters.WaterTemperature -> "%.2f °C".format(locale, rawValue)
        }
    }
}