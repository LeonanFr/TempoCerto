package com.app.tempocerto.data.model

enum class CurucaParameters {
    Temperature,
    Salinity,
    Pressure;

    fun toListString(): String {
        return when (this) {
            Temperature -> "Lista de Temperatura"
            Salinity -> "Lista de Salinidade"
            Pressure -> "Lista de Pressão"
        }
    }

    override fun toString(): String {
        return when (this) {
            Temperature -> "Temperatura"
            Salinity -> "Salinidade"
            Pressure -> "Pressão"
        }
    }

}