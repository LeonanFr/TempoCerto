package com.app.tempocerto.data.model

import com.squareup.moshi.Json

data class RegisterRequest(
    @field:Json(name = "name") val name: String,
    @field:Json(name = "username") val username: String,
    @field:Json(name = "email") val email: String,
    @field:Json(name = "password") val password: String
)

data class LoginRequest(
    @field:Json(name = "username") val username: String,
    @field:Json(name = "password") val password: String
)

data class LoginResponse(
    @field:Json(name = "token") val token: String
)

data class UserProfile(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "username") val username: String,
    @field:Json(name = "email") val email: String
)
