package com.app.tempocerto.data.network

import com.app.tempocerto.data.model.CurucaLog
import com.app.tempocerto.data.model.SoureLog
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

inline fun <reified T> Map<String, Any>.getAs(key: String): T? {
    return this[key] as? T
}

fun Map<String, Any>.toSoureLog(): SoureLog {
    val dateTimeString = getAs<String>("date")
    val dateTime = try {
        ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    } catch (_: Exception) {
        null
    }

    return SoureLog(
        dateTime = dateTime,
        airHumidity = getAs<Double>("air_humid")?.toFloat(),
        airPressure = getAs<Double>("air_pressure")?.toFloat(),
        airTemperature = getAs<Double>("air_temperature")?.toFloat(),
        precipitation = getAs<Double>("precipitation")?.toFloat(),
        windDirection = getAs<Double>("wind_direction")?.toInt(),
        waterLevel = getAs<Double>("water_level")?.toInt(),
        windSpeedMax = getAs<Double>("wind_speed_max")?.toFloat(),
        windSpeedMean = getAs<Double>("wind_speed_mean")?.toFloat(),
        windSpeedMin = getAs<Double>("wind_speed_min")?.toFloat(),
        waterTemperature = getAs<Double>("water_temperature")?.toFloat()
    )
}

fun Map<String, Any>.toCurucaLog(): CurucaLog {
    val dateTimeString = getAs<String>("date")
    val dateTime = try {
        ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    } catch (_: Exception) {
        null
    }

    return CurucaLog(
        dateTime = dateTime,
        temperature = getAs<Double>("temperature")?.toFloat(),
        pressure = getAs<Double>("pressure")?.toFloat(),
        salinity = getAs<Double>("salinity")?.toFloat()
    )
}
