package com.app.tempocerto.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SoureLog(
    val dt: String = "",
    val ah: Float = 0f,
    val ap: Float = 0f,
    val at: Float = 0f,
    val bv: Float = 0f,
    val it: Float = 0f,
    val p: Float = 0f,
    val wd: Int = 0,
    val wl: Int = 0,
    val wsma: Float = 0f,
    val wsme: Float = 0f,
    val wsmi: Float = 0f,
    val wt: Float = 0f
) {
    private val parsedDateTime: LocalDateTime? = try {
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m:s")
        LocalDateTime.parse(dt, formatter)
    } catch (_: Exception) {
        null
    }

    fun toMap(): Map<String, String> {
        val locale = Locale.US
        return mapOf(
            "Umidade do Ar" to "%.1f %%".format(locale, ah),
            "Pressão Atmosférica" to "%.2f hPa".format(locale, ap),
            "Temperatura do Ar" to "%.2f °C".format(locale, at),
            "Nível da Água" to "$wl cm",
            "Temperatura da Água" to "%.2f °C".format(locale, wt),
            "Precipitação" to "%.1f mm".format(locale, p),
            "Vento (Médio)" to "%.2f m/s".format(locale, wsme),
            "Vento (Máximo)" to "%.2f m/s".format(locale, wsma),
            "Direção do Vento" to "$wd °",
            "Tensão da Bateria" to "%.2f V".format(locale, bv)
        )
    }

    fun getLocalDate(): LocalDate? {
        return parsedDateTime?.toLocalDate()
    }

    fun getTime(): String {
        return parsedDateTime?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: ""
    }



    fun getRawValue(parameter: SoureParameters): Float {
        return when (parameter) {
            SoureParameters.AirHumidity -> ah
            SoureParameters.AtmosphericPressure -> ap
            SoureParameters.AirTemperature -> at
            SoureParameters.BatteryVoltage -> bv
            SoureParameters.InternalTemperature -> it
            SoureParameters.Precipitation -> p
            SoureParameters.WindDirection -> wd.toFloat()
            SoureParameters.WaterLevel -> wl.toFloat()
            SoureParameters.WindSpeedMax -> wsma
            SoureParameters.WindSpeedMean -> wsme
            SoureParameters.WindSpeedMin -> wsmi
            SoureParameters.WaterTemperature -> wt
        }
    }

    fun getValue(parameter: SoureParameters): String {
        val locale = Locale.US
        return when (parameter) {
            SoureParameters.AirHumidity -> "%.1f %%".format(locale, ah)
            SoureParameters.AtmosphericPressure -> "%.2f hPa".format(locale, ap)
            SoureParameters.AirTemperature -> "%.2f °C".format(locale, at)
            SoureParameters.BatteryVoltage -> "%.2f V".format(locale, bv)
            SoureParameters.InternalTemperature -> "%.2f °C".format(locale, it)
            SoureParameters.Precipitation -> "%.1f mm".format(locale, p)
            SoureParameters.WindDirection -> "$wd °"
            SoureParameters.WaterLevel -> "$wl cm"
            SoureParameters.WindSpeedMax -> "%.2f m/s".format(locale, wsma)
            SoureParameters.WindSpeedMean -> "%.2f m/s".format(locale, wsme)
            SoureParameters.WindSpeedMin -> "%.2f m/s".format(locale, wsmi)
            SoureParameters.WaterTemperature -> "%.2f °C".format(locale, wt)
        }
    }
}