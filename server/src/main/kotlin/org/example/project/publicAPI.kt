package org.example.project

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.internal.ChannelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

const val apiKey = "AIzaSyCBK0mcT7iHUC9qiQU4vBkHpk-bbeNtlZU"

suspend fun fetchBooks(query: String): List<BookInfo> {
    // Initialize Ktor HTTP client with JSON support
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    return try {
        // Make GET request to Google Books API
        val response: GoogleBooksResponse = client.get("https://www.googleapis.com/books/v1/volumes") {
            parameter("q", query)
            parameter("key", apiKey)
        }.body()

        // Extract book titles and authors
        response.items?.mapNotNull { volume ->
            val title = volume.volumeInfo.title
            val authors = volume.volumeInfo.authors
            if (!authors.isNullOrEmpty()) {
                BookInfo(title = title, authors = authors)
            } else {
                null
            }
        } ?: emptyList()
    }
    catch (e: Exception) {
        println("Error fetching books: ${e.message}")
        client.close()
        emptyList()
    } finally {
        client.close()
    }
}

fun idxToQuery(idx: Int): String {
    val chars = CharArray(26) { 'a' + it }
    var num = idx
    val sb = StringBuilder()
    while (num > 0) {
        sb.append(chars[num % 26])
        num /= 26
    }
    sb.reverse()
    return sb.toString()
}

fun queriesInf(): Channel<String> = runBlocking {
    val channel = Channel<String>()
    CoroutineScope(Dispatchers.Default).launch {
        var idx = 1
        while (true) {
            channel.send(idxToQuery(idx))
            idx += 1
        }
    }
    channel
}

 fun titlesChannel(): Channel<String> = runBlocking {
     val input = queriesInf()
     val output = Channel<String>()
     CoroutineScope(Dispatchers.Default).launch {
         while (true) {
             val query = input.receive()
             val books = fetchBooks(query)
             books.forEach { output.send(it.title) }
         }
     }
     output
}

fun authorsChannel(): Channel<String> = runBlocking {
    val input = queriesInf()
    val output = Channel<String>()
    CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            val query = input.receive()
            val books = fetchBooks(query)
            books.forEach { output.send(it.authors.first()) }
        }
    }
    output
}
