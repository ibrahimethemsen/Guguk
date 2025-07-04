package com.ibrahimethemsen.guguk

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MockInterceptor(
    private val localServerUrl: String = "http://localhost:8080",
    private val mockEndpoints: Set<String> = emptySet()
) : Interceptor {

    private val mockEndpointSet = mutableSetOf<String>()

    init {
        mockEndpointSet.addAll(mockEndpoints)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url.toString()

        // Endpoint'in mock edilip edilmeyeceğini kontrol et
        val shouldMock = mockEndpointSet.any { endpoint ->
            originalUrl.contains(endpoint) || originalRequest.url.encodedPath.contains(endpoint)
        }

        return if (shouldMock) {
            val mockRequest = createMockRequest(originalRequest)
            try {
                chain.proceed(mockRequest)
            } catch (e: Exception) {
                // Local server'a bağlanamıyorsa orijinal request'i gönder
                println("Mock server'a bağlanılamadı, orijinal request gönderiliyor: ${e.message}")
                chain.proceed(originalRequest)
            }
        } else {
            // Orijinal request'i gönder
            chain.proceed(originalRequest)
        }
    }

    private fun createMockRequest(originalRequest: Request): Request {
        val originalUrl = originalRequest.url
        val mockUrl = originalUrl.newBuilder()
            .scheme("http")
            .host("localhost")
            .port(8080)
            .build()

        return originalRequest.newBuilder()
            .url(mockUrl)
            .build()
    }

    // Runtime'da yeni endpoint eklemek için
    fun addMockEndpoint(endpoint: String) {
        mockEndpointSet.add(endpoint)
    }

    // Runtime'da endpoint kaldırmak için
    fun removeMockEndpoint(endpoint: String) {
        mockEndpointSet.remove(endpoint)
    }

    // Tüm mock endpoint'leri temizlemek için
    fun clearMockEndpoints() {
        mockEndpointSet.clear()
    }

    // Mevcut mock endpoint'leri görmek için
    fun getMockEndpoints(): Set<String> {
        return mockEndpointSet.toSet()
    }
}

fun OkHttpClient.Builder.addMockInterceptor(
    localServerUrl: String = "http://localhost:8080",
    mockEndpoints: Set<String> = emptySet()
): OkHttpClient.Builder {
    val mockManager = MockDataManager.getInstance()
    val interceptor = mockManager.initialize(localServerUrl, mockEndpoints)
    return this.addInterceptor(interceptor)
}