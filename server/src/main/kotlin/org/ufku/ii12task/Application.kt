package org.ufku.ii12task

import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

fun main() {
    embeddedServer(CIO, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowNonSimpleContentTypes = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(SSE)

    val s1 = configureServer()
    val s2 = configureServer2()

    routing {
        get("/test") {
            call.respondText("Server is running")
        }

        route("/server1") {
            sse {
                println("SSE connection established for server1")
                val transport = SseServerTransport("/server1", this)
                val session = s1.createSession(transport = transport)
                println("Server1 session created")
            }
        }

        route("/server2") {
            sse {
                println("SSE connection established for server2")
                val transport2 = SseServerTransport("/server2", this)
                val session2 = s2.createSession(transport = transport2)
                println("Server2 session created")
            }
        }
    }
}