package com.ibrahimethemsen.guguk

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class GugukMockInterceptor(private val gugukMockDataManager: GugukMockDataManager) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url

        val baseUrl = gugukMockDataManager.getBaseUrl()
        val localServerUrl = gugukMockDataManager.getLocalServerUrl()
        val currentMockEndpoints = gugukMockDataManager.getMockEndpoints()

        if (baseUrl.isBlank() || currentMockEndpoints.isEmpty()) {
            println("MockInterceptor: Base URL boş veya mock edilecek endpoint yok. Orijinal istek: ${originalRequest.url}")
            return chain.proceed(originalRequest)
        }

        val endpointPath = if (requestUrl.toString().startsWith(baseUrl)) {
            val pathWithoutBase = requestUrl.toString().substring(baseUrl.length)
            if (pathWithoutBase.startsWith("/")) pathWithoutBase else "/$pathWithoutBase"
        } else {
            println("MockInterceptor: İstek (${requestUrl}) beklenen baseUrl ($baseUrl) ile başlamıyor. Orijinal istek gönderiliyor.")
            return chain.proceed(originalRequest)
        }

        val shouldMock = currentMockEndpoints.any { definedMockEndpoint ->
            endpointPath == definedMockEndpoint || endpointPath.startsWith("$definedMockEndpoint/")
        }

        return if (shouldMock) {
            val mockRequest = createMockRequest(originalRequest, localServerUrl)
            try {
                println("MockInterceptor: Mock isteği gönderiliyor: ${mockRequest.url} (Orijinal: ${originalRequest.url})")
                chain.proceed(mockRequest)
            } catch (e: Exception) {
                println("MockInterceptor: Mock server'a bağlanılamadı (${mockRequest.url}), orijinal request gönderiliyor: ${e.message}")
                chain.proceed(originalRequest)
            }
        } else {
            println("MockInterceptor: Endpoint ($endpointPath) mock listesinde değil. Orijinal istek: ${originalRequest.url}")
            chain.proceed(originalRequest)
        }
    }

    private fun createMockRequest(originalRequest: Request, localServerUrl: String): Request {
        val originalUrl = originalRequest.url
        val parsedLocalServerUrl = localServerUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid localServerUrl: $localServerUrl")

        val newPathSegments = originalUrl.pathSegments
        val newQuery = originalUrl.query
        val mockUrlBuilder = parsedLocalServerUrl.newBuilder()

        val combinedPathSegments = parsedLocalServerUrl.pathSegments.filter { it.isNotEmpty() } + newPathSegments
        mockUrlBuilder.encodedPath("/")
        combinedPathSegments.forEach { segment ->
            if (segment.isNotEmpty()) {
                mockUrlBuilder.addPathSegment(segment)
            }
        }

        mockUrlBuilder.query(newQuery)
        val mockUrl = mockUrlBuilder.build()

        return originalRequest.newBuilder()
            .url(mockUrl)
            .build()
    }
}

fun OkHttpClient.Builder.addGugukInterceptor(
    baseUrl: String,
    localServerUrl: String = "http://10.0.2.2:8080/",
    mockEndpoints: Set<String> = emptySet()
): OkHttpClient.Builder {
    val interceptor = GugukMockDataManager.getInstance().initialize(baseUrl, localServerUrl, mockEndpoints)
    return this.addInterceptor(interceptor)
}