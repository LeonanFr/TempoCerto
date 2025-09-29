package com.app.tempocerto.di

import com.app.tempocerto.data.network.ApiUserRepository
import com.app.tempocerto.data.network.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient =
        HttpClient(Android) {
            install(ContentNegotiation) { json() }
        }

    @Provides
    @Singleton
    fun provideUserRepository(client: HttpClient): UserRepository =
        ApiUserRepository(client, baseUrl = "PLACEHOLDER")
}
