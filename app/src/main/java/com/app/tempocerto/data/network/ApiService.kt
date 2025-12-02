package com.app.tempocerto.data.network

import com.app.tempocerto.data.model.AccessRequestBody
import com.app.tempocerto.data.model.AccessRequestResponse
import com.app.tempocerto.data.model.ApproveRequestInput
import com.app.tempocerto.data.model.ForgotPasswordRequest
import com.app.tempocerto.data.model.LoginRequest
import com.app.tempocerto.data.model.LoginResponse
import com.app.tempocerto.data.model.RegisterRequest
import com.app.tempocerto.data.model.ResetPasswordRequest
import com.app.tempocerto.data.model.SendOtpRequest
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

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<Unit>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    @POST("request-access")
    suspend fun requestAccess(@Body body: AccessRequestBody): Response<Unit>

    @GET("me")
    suspend fun getMe(): UserProfile

    @GET("admin/requests")
    suspend fun getAccessRequests(): List<AccessRequestResponse>

    @POST("admin/approve")
    suspend fun approveRequest(
        @Query("id") id: Int,
        @Body body: ApproveRequestInput
    ): Response<Unit>
}