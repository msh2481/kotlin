package org.example.project

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

lateinit var titles: Channel<String>
lateinit var authors: Channel<String>

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }

    titles = titlesChannel()
    authors = authorsChannel()


    routing {
        get("/") {
            call.respondText("Hi!", contentType = io.ktor.http.ContentType.Text.Plain)
        }

        get("/books") {
            runBlocking {
                val currentTitles = mutableListOf<String>()
                repeat(20) {
                    currentTitles.add(titles.receive())
                }
                call.respond(currentTitles)
            }
        }

        get("/authors") {
            runBlocking() {
                val currentAuthors = mutableListOf<String>()
                repeat(20) {
                    currentAuthors.add(authors.receive())
                }
                call.respond(currentAuthors)
            }
        }
    }
}