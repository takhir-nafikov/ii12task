package org.ufku.ii12task

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.kotlinx.json.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Duration

class Client2 {
    private val mcp: Client = Client(clientInfo = Implementation(name = "mcp-client-cli2", version = "1.0.0"))
    val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                },
            )
        }
        install(SSE)
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        install(HttpTimeout) {
            // не ограничивать длительность запроса (полезно для stream=true)
            requestTimeoutMillis = 100_000L
            // дать время на TCP/TLS рукопожатие
            connectTimeoutMillis = 30_000
        }
        engine {
            // запас на уровень движка
            requestTimeout = 0 // 0 = бесконечно для CIO
            endpoint {
                connectTimeout = 30_000
                keepAliveTime = 30_000
                maxConnectionsPerRoute = 100
                pipelineMaxSize = 20
            }
        }
    }

    suspend fun connect() {
        val transport = SseClientTransport(
            client = http,
            urlString = "http://localhost:8080/server2",
            reconnectionTime = Duration.INFINITE
        )
        transport.start()
        mcp.connect(transport)
    }

    suspend fun readTool(): String {
        val answer = withContext(Dispatchers.IO) {
            mcp.callTool(
                name = "reader_md",
                arguments = mapOf(),
            )?.content?.map {
                (it as? TextContent)?.text ?: ""
            }
        }
        return answer?.getOrNull(0) ?: "что то не так"
    }
}