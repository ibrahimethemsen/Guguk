package com.ibrahimethemsen.sample.data.di

import com.ibrahimethemsen.guguk.addGugukInterceptor
import com.ibrahimethemsen.sample.data.service.QuoteService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val BASE_URL = "https://api.quotable.io/"
@[Module InstallIn(SingletonComponent::class)]
object NetworkModule {

    @[Provides Singleton]
    fun provideJson(): Json {
        return Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @[Provides Singleton]
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): QuoteService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(QuoteService::class.java)
    }

    @Provides
    @Singleton
    fun provideGugukOkHttpClient(): OkHttpClient {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {

                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }

            builder.addGugukInterceptor(
                baseUrl = BASE_URL,
                localServerUrl = "https://10.0.2.2:8443/",
                mockEndpoints = setOf("")
            )

            return builder.build()
        } catch (e: Exception) {
            throw RuntimeException("Unable to create insecure OkHttpClient", e)
        }
    }
}

