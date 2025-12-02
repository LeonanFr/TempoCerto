package com.app.tempocerto.data.model

import com.squareup.moshi.Json

data class RegisterRequest(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "username") val username: String,
    @param:Json(name = "email") val email: String?,
    @param:Json(name = "phone") val phone: String?,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "otp_code") val otpCode: String?
)

data class ForgotPasswordRequest(
    @field:Json(name = "contact") val contact: String,
    @field:Json(name = "type") val type: String
)

data class ResetPasswordRequest(
    @field:Json(name = "contact") val contact: String,
    @field:Json(name = "type") val type: String,
    @field:Json(name = "code") val code: String,
    @param:Json(name = "new_password") val newPassword: String
)

data class SendOtpRequest(
    @field:Json(name = "contact") val contact: String,
    @field:Json(name = "type") val type: String,
    @field:Json(name = "purpose") val purpose : String = "REGISTER"
)

data class AccessRequestBody(
    @field:Json(name = "days") val days: Int
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
    @field:Json(name = "email") val email: String?,
    @field:Json(name = "phone") val phone: String?,
    @field:Json(name = "role") val role: String? = "user"
)

data class AccessRequestResponse(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "username") val username: String,
    @field:Json(name = "name") val name: String,
    @param:Json(name = "requested_days") val requestedDays: Int,
    @param:Json(name = "request_date") val requestDate: String
)

data class ApproveRequestInput(
    @field:Json(name = "days") val days: Int
)