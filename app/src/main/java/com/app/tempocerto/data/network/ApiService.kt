package com.app.tempocerto.data.network

import com.app.tempocerto.data.model.AccessRequestBody
import com.app.tempocerto.data.model.LoginRequest
import com.app.tempocerto.data.model.LoginResponse
import com.app.tempocerto.data.model.RegisterRequest
import com.app.tempocerto.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("logs/{station_name}/latest")
    suspend fun getLatestLog(
        @Path("station_name") stationName: String
    ): Map<String, Any>?

    @GET("logs/{station_name}")
    suspend fun getLogsForDate(
        @Path("station_name") stationName: String,
        @Query("date") date: String
    ): List<Map<String, Any>>

    @GET("logs/{station_name}")
    suspend fun getLogsForDateRange(
        @Path("station_name") stationName: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): List<Map<String, Any>>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("request-access")
    suspend fun requestAccess(@Body body: AccessRequestBody): Response<Unit>

    @GET("me")
    suspend fun getMe(): UserProfile
}