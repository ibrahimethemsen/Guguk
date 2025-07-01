package com.ibrahimethemsen.guguk

class MockDataManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: MockDataManager? = null

        fun getInstance(): MockDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MockDataManager().also { INSTANCE = it }
            }
        }
    }

    private var mockInterceptor: MockInterceptor? = null
    private var isEnabled = false

    fun initialize(
        localServerUrl: String = "http://localhost:8080",
        mockEndpoints: Set<String> = emptySet()
    ): MockInterceptor {
        mockInterceptor = MockInterceptor(localServerUrl, mockEndpoints)
        isEnabled = true
        return mockInterceptor!!
    }

    fun addMockEndpoint(endpoint: String) {
        mockInterceptor?.addMockEndpoint(endpoint)
    }

    fun removeMockEndpoint(endpoint: String) {
        mockInterceptor?.removeMockEndpoint(endpoint)
    }

    fun clearMockEndpoints() {
        mockInterceptor?.clearMockEndpoints()
    }

    fun getMockEndpoints(): Set<String> {
        return mockInterceptor?.getMockEndpoints() ?: emptySet()
    }

    fun enable() {
        isEnabled = true
    }

    fun disable() {
        isEnabled = false
    }

    fun isEnabled(): Boolean {
        return isEnabled
    }
}