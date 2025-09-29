package com.app.tempocerto.data.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val location: String? = null,
    val preferences: Preferences = Preferences(),
    val history: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val alerts: List<AlertConfig> = emptyList()
)

data class Preferences(
    val tide: Boolean = false,
    val salinity: Boolean = false
)

data class AlertConfig(
    val type: String = "",
    val enabled: Boolean = false
)
