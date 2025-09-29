package com.app.tempocerto.data.network
import com.app.tempocerto.data.model.ApiResponse
import com.app.tempocerto.data.model.LoginRequest
import com.app.tempocerto.data.model.RegisterRequest
import com.app.tempocerto.data.model.UserProfile
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiUserRepository(
    private val client: HttpClient,
    private val baseUrl: String
) : UserRepository {

    override suspend fun login(email: String, password: String): Result<UserProfile> {
        return try {
            val response: ApiResponse<UserProfile> = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Erro desconhecido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<UserProfile> {
        return try {
            val response: ApiResponse<UserProfile> = client.post("$baseUrl/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, password, name))
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Erro desconhecido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfile(token: String): Result<UserProfile> {
        return try {
            val response: ApiResponse<UserProfile> = client.get("$baseUrl/profile") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Erro desconhecido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProfile(token: String, profile: UserProfile): Result<Unit> {
        return try {
            val response: ApiResponse<Unit> = client.put("$baseUrl/profile") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(profile)
            }.body()

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error ?: "Erro desconhecido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
