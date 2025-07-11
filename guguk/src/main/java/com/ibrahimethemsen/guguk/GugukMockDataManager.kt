package com.ibrahimethemsen.guguk

class GugukMockDataManager private constructor() {

    companion object Companion {
        @Volatile
        private var INSTANCE: GugukMockDataManager? = null

        fun getInstance(): GugukMockDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GugukMockDataManager().also { INSTANCE = it }
            }
        }
    }

    private var _Guguk_mockInterceptor: GugukMockInterceptor? = null
    val gugukMockInterceptor: GugukMockInterceptor?
        get() = _Guguk_mockInterceptor

    private val currentMockEndpoints = mutableSetOf<String>()
    private var currentBaseUrl: String = ""
    private var currentLocalServerUrl: String = "http://10.0.2.2:8080/"

    fun initialize(
        baseUrl: String,
        localServerUrl: String = "http://10.0.2.2:8080/",
        initialMockEndpoints: Set<String> = emptySet()
    ): GugukMockInterceptor {
        currentBaseUrl = baseUrl
        currentLocalServerUrl = localServerUrl
        currentMockEndpoints.clear()
        initialMockEndpoints.forEach { addMockEndpointInternal(it) }

        _Guguk_mockInterceptor = GugukMockInterceptor(this)
        return _Guguk_mockInterceptor!!
    }

    private fun addMockEndpointInternal(endpoint: String) {
        val formattedEndpoint = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        currentMockEndpoints.add(formattedEndpoint)
    }

    fun addMockEndpoint(endpoint: String) {
        addMockEndpointInternal(endpoint)
    }

    fun removeMockEndpoint(endpoint: String) {
        val formattedEndpoint = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        currentMockEndpoints.remove(formattedEndpoint)
    }

    fun clearMockEndpoints() {
        currentMockEndpoints.clear()
    }

    fun getMockEndpoints(): Set<String> {
        return currentMockEndpoints.toSet()
    }

    fun getBaseUrl(): String = currentBaseUrl
    fun getLocalServerUrl(): String = currentLocalServerUrl
}
