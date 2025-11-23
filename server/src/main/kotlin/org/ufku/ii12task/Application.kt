package org.ufku.ii12task

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport

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