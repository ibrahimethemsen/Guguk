package com.ibrahimethemsen.guguk

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.certificates.saveToFile
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.routing.application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class MockResponse(

    val body: String,
    val statusCode: Int = 200
)

object MockServerState {
    // Ana map: Path (String) -> İkincil Map (HttpMethod -> MockResponse)
    private val responsesByPathAndMethod =
        ConcurrentHashMap<String, ConcurrentHashMap<HttpMethod, MockResponse>>()

    /**
     * Belirli bir path ve HTTP metodu için mock yanıtını ve durum kodunu ayarlar veya günceller.
     * @param path Endpoint path'i (örn: "/users")
     * @param method HTTP metodu (örn: HttpMethod.Get, HttpMethod.Post)
     * @param responseJson Bu isteğe döndürülecek JSON yanıtı.
     * @param statusCode Bu yanıt için HTTP durum kodu.
     */
    fun setResponse(
        path: String,
        method: HttpMethod,
        responseJson: String,
        statusCode: Int = 200
    ) {
        val mockResponse = MockResponse(body = responseJson, statusCode = statusCode)
        responsesByPathAndMethod.computeIfAbsent(path) { ConcurrentHashMap() }[method] =
            mockResponse
    }

    /**
     * Belirli bir path ve HTTP metodu için tanımlanmış mock yanıtını (body ve durum kodu) getirir.
     * @param path Endpoint path'i
     * @param method HTTP metodu
     * @return Tanımlıysa MockResponse nesnesini, yoksa null döner.
     */
    fun getResponse(path: String, method: HttpMethod): MockResponse? {
        return responsesByPathAndMethod[path]?.get(method)
    }
}

fun startLocalServer(
    httpPort: Int,
    httpsPort: Int
) {
    embeddedServer(
        Netty,
        applicationEnvironment { log = LoggerFactory.getLogger("ktor.application") },
        {
            envConfig(httpPort, httpsPort)
        },
        module = Application::configureRouting
    ).start(wait = true)
}

private fun ApplicationEngine.Configuration.envConfig(
    httpPort: Int,
    httpsPort: Int
) {
    val keyStoreFile = File("build/keystore.jks")
    val keyStore = buildKeyStore {
        certificate("gugukMock") {
            password = "guguk"
            domains = listOf("127.0.0.1", "0.0.0.0", "localhost", "10.0.2.2")
        }
    }
    keyStore.saveToFile(keyStoreFile, "guguk1453")
    connector {
        port = httpPort
    }
    sslConnector(
        keyStore = keyStore,
        keyAlias = "gugukMock",
        keyStorePassword = { "guguk1453".toCharArray() },
        privateKeyPassword = { "guguk".toCharArray() }) {
        port = httpsPort
        keyStorePath = keyStoreFile
    }
}

fun Application.configureRouting() {
    routing {
        route("{...}") { // Herhangi bir path'i yakala
            handle {
                val requestedPath = call.request.uri
                val requestedMethod: HttpMethod = call.request.httpMethod

                application.log.info("Yakalandı ${requestedMethod.value} $requestedPath")

                val mockResponseData =
                    MockServerState.getResponse(requestedPath, requestedMethod) // Değişiklik burada

                if (mockResponseData != null) {
                    application.log.info("Found mock response for ${requestedMethod.value} $requestedPath")
                    try {
                        if (mockResponseData.body.isNotBlank()) {
                            Json.parseToJsonElement(mockResponseData.body) // JSON'u doğrula
                        }
                        call.respondText(
                            mockResponseData.body,
                            ContentType.Application.Json,
                            status = HttpStatusCode.fromValue(mockResponseData.statusCode) // Durum kodunu buradan alıyoruz
                        )
                    } catch (e: SerializationException) {
                        application.log.error("Gecersiz JSON ${requestedMethod.value} $requestedPath: ${e.message}")
                        call.respondText(
                            " Server'a ulasamadı ${requestedMethod.value} $requestedPath. Invalid JSON in mock data.",
                            status = HttpStatusCode.InternalServerError // Mock verideki JSON bozuksa sunucu hatası
                        )
                    }
                } else {
                    val message = "Mock data yok ${requestedMethod.value} $requestedPath"
                    application.log.info(message)
                    call.respondText(
                        message,
                        ContentType.Text.Plain,
                        HttpStatusCode.NotFound
                    )
                }
            }
        }
    }
}