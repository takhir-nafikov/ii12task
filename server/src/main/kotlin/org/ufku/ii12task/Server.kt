package org.ufku.ii12task

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

fun configureServer(): Server {
    // Base URL for the Weather API
    val baseUrl = "https://api4.binance.com"

    // Create an HTTP client with a default request configuration and JSON content negotiation
    val httpClient = HttpClient(CIO) {
        defaultRequest {
            url(baseUrl)
            headers {
                append("Accept", "application/json")
//                append("User-Agent", "WeatherApiClient/1.0")
            }
            contentType(ContentType.Application.Json)
        }
        // Install content negotiation plugin for JSON serialization/deserialization
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                },
            )
        }
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

    // Create the MCP Server instance with a basic implementation
    val server = Server(
        Implementation(
            name = "ticker", // Tool name is "weather"
            version = "1.0.0", // Version of the implementation
        ),
        ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = true)),
        ),
    ) {
        "This SSE server provides prompts and resources via Server-Sent Events."
    }


    server.addTool(
        name = "top_rated_ticker",
        description = """
            SCAM binance top N info
        """.trimIndent(),
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("count") {
                    put("type", "number")
                }
            },
            required = listOf("count"),
        ),
    ) { request ->
        val count = request.arguments?.get("count")?.jsonPrimitive?.intOrNull ?: return@addTool CallToolResult(
            content = listOf(TextContent("The 'count' parameter is required.")),
        )

        val tickers = httpClient.getTickers(count)

        CallToolResult(content = tickers.map { TextContent(it) })
    }

    server.addTool(
        name = "save_to_file",
        description = """
            save SCAM binance top N info
        """.trimIndent(),
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("data") {
                    put("type", "string")
                }
            },
            required = listOf("data"),
        ),
    ) { request ->
        val data = request.arguments?.get("data")?.jsonPrimitive?.content ?: return@addTool CallToolResult(
            content = listOf(TextContent("The 'data' parameter is required.")),
        )

        val flag = saveToFile(data)

        CallToolResult(
            content = if (flag) {
                listOf(TextContent("Успешно сохранили"))
            } else {
                listOf(TextContent("Неуспешно сохранили"))
            }
        )
    }

    return server
}

