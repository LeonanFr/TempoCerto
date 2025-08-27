package com.app.tempocerto.data.model

enum class SoureParameters {
    AirHumidity,
    AtmosphericPressure,
    AirTemperature,
    BatteryVoltage,
    InternalTemperature,
    Precipitation,
    WindDirection,
    WaterLevel,
    WindSpeedMax,
    WindSpeedMean,
    WindSpeedMin,
    WaterTemperature;

    fun toListString(): String {
        return "Lista de ${this.toString()}"
    }

    override fun toString(): String {
        return when (this) {
            AirHumidity -> "Umidade do Ar"
            AtmosphericPressure -> "Pressão Atmosférica"
            AirTemperature -> "Temperatura do Ar"
            BatteryVoltage -> "Tensão da Bateria"
            InternalTemperature -> "Temperatura Interna"
            Precipitation -> "Precipitação"
            WindDirection -> "Direção do Vento"
            WaterLevel -> "Nível da Água"
            WindSpeedMax -> "Vento (Máximo)"
            WindSpeedMean -> "Vento (Médio)"
            WindSpeedMin -> "Vento (Mínimo)"
            WaterTemperature -> "Temperatura da Água"
        }
    }
}