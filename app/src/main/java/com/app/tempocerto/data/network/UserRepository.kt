package com.app.tempocerto.data.network

import com.app.tempocerto.data.model.AccessRequestResponse
import com.app.tempocerto.data.model.ApproveRequestInput
import com.app.tempocerto.data.model.ForgotPasswordRequest
import com.app.tempocerto.data.model.LoginRequest
import com.app.tempocerto.data.model.LoginResponse
import com.app.tempocerto.data.model.RegisterRequest
import com.app.tempocerto.data.model.ResetPasswordRequest
import com.app.tempocerto.data.model.SendOtpRequest
import com.app.tempocerto.data.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Usuário ou senha inválidos", e))
        }
    }

    suspend fun sendOtp(contact: String, type: String, purpose: String = "REGISTER"): Result<Unit> {
        return try {
            val request = SendOtpRequest(contact = contact, type = type, purpose = purpose)
            val response = apiService.sendOtp(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Erro desconhecido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        username: String,
        email: String,
        phone: String,
        password: String,
        otpCode: String
    ): Result<Unit> {
        return try {
            val request = RegisterRequest(
                name = name,
                username = username,
                email = email.ifBlank { null },
                phone = phone.ifBlank { null },
                password = password,
                otpCode = otpCode
            )
            val response = apiService.register(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Falha na conexão", e))
        }
    }

    suspend fun requestPasswordReset(contact: String, type: String): Result<Unit> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(contact, type))

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                 Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(contact: String, type: String, code: String, newPass: String): Result<Unit> {
        return try {
            val request = ResetPasswordRequest(contact, type, code, newPass)
            val response = apiService.resetPassword(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Não foi possível redefinir a senha"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMe(): Result<UserProfile> {
        return try {
            val userProfile = apiService.getMe()
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(Exception("Sessão inválida ou expirada", e))
        }
    }

    suspend fun getPendingRequests(): Result<List<AccessRequestResponse>> {
        return try {
            val list = apiService.getAccessRequests()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveRequest(requestId: Int, days: Int): Result<Unit> {
        return try {
            val response = apiService.approveRequest(requestId, ApproveRequestInput(days))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erro: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
