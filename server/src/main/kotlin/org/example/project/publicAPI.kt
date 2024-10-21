package org.example.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext

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

fun queriesInf(): Flow<String> = flow {
    var idx = 1
    while (true) {
        emit(idxToQuery(idx))
        idx += 1
    }
}

fun booksInf(): Flow<BookInfo> = flow {
    queriesInf().collect { query ->
        fetchBooks(query).asFlow().collect {
            emit(it)
        }
    }
}

suspend fun bookInfTwoFlows(): Pair<Flow<BookInfo>, Flow<BookInfo>> {
    val sharedFlow = booksInf().shareIn(
        scope = CoroutineScope(coroutineContext),
        started = SharingStarted.Lazily,
        replay = 0,
    )
    return sharedFlow.map { it } to sharedFlow.map { it }
}