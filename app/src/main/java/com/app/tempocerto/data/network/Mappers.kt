package com.app.tempocerto.data.network

import com.app.tempocerto.data.model.CurucaLog
import com.app.tempocerto.data.model.SoureLog
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun Map<String, Any>.getDouble(key: String): Double? {
    val value = this[key]
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
        else -> null
    }
}

fun Map<String, Any>.getInt(key: String): Int? {
    val value = this[key]
    return when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }
}

inline fun <reified T> Map<String, Any>.getAs(key: String): T? {
    return this[key] as? T
}

fun Map<String, Any>.toSoureLog(): SoureLog {
    val dateTimeString = getAs<String>("date")
    val dateTime = try {
        if (dateTimeString != null) {
            ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } else null
    } catch (_: Exception) {
        null
    }

    return SoureLog(
        dateTime = dateTime,
        airHumidity = getDouble("air_humid")?.toFloat(),
        airPressure = getDouble("air_pressure")?.toFloat(),
        airTemperature = getDouble("air_temperature")?.toFloat(),
        precipitation = getDouble("precipitation")?.toFloat(),
        windDirection = getInt("wind_direction"),
        waterLevel = getInt("water_level"),
        windSpeedMax = getDouble("wind_speed_max")?.toFloat(),
        windSpeedMean = getDouble("wind_speed_mean")?.toFloat(),
        windSpeedMin = getDouble("wind_speed_min")?.toFloat(),
        waterTemperature = getDouble("water_temperature")?.toFloat()
    )
}

fun Map<String, Any>.toCurucaLog(): CurucaLog {
    val dateTimeString = getAs<String>("date")
    val dateTime = try {
        if (dateTimeString != null) {
            ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } else null
    } catch (_: Exception) {
        null
    }

    return CurucaLog(
        dateTime = dateTime,
        temperature = getDouble("temperature")?.toFloat(),
        pressure = getDouble("pressure")?.toFloat(),
        salinity = getDouble("salinity")?.toFloat()
    )
}
