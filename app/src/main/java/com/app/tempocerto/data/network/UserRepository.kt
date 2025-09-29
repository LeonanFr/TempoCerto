package com.app.tempocerto.data.network

import com.app.tempocerto.data.model.UserProfile

interface UserRepository {
    suspend fun login(email: String, password: String): Result<UserProfile>
    suspend fun register(email: String, password: String, name: String): Result<UserProfile>
    suspend fun getProfile(token: String): Result<UserProfile>
    suspend fun saveProfile(token: String, profile: UserProfile): Result<Unit>
}
