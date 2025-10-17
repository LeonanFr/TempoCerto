package com.app.tempocerto.data.network

import com.app.tempocerto.data.model.LoginRequest
import com.app.tempocerto.data.model.LoginResponse
import com.app.tempocerto.data.model.RegisterRequest
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

    suspend fun register(name: String, username: String, email: String, password: String): Result<Unit> {
        return try {
            val request = RegisterRequest(name, username, email, password)
            val response = apiService.register(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Falha ao conectar com o servidor", e))
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
}
