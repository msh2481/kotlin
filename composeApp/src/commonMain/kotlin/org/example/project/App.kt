package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.ui.tooling.preview.Preview

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.consumeEach


private val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
}

fun booksChannel(): Channel<String> = Channel<String>().apply {
    CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            try {
                val books: List<String> = client.get("http://localhost:8080/books").body()
                books.forEach { send(it) }
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }
}

fun authorsChannel(): Channel<String> = Channel<String>().apply {
    CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            try {
                val authors: List<String> = client.get("http://localhost:8080/authors").body()
                authors.forEach { send(it) }
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }
}


class ItemsRepository {
    fun getBooksChannel() = booksChannel()
    fun getAuthorsChannel() = authorsChannel()
}

@Composable
@Preview
fun App() {
    val repository = remember { ItemsRepository() }

    MaterialTheme {
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Books", "Authors")

        Column {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> ItemList(itemsChannel = repository.getBooksChannel())
                1 -> ItemList(itemsChannel = repository.getAuthorsChannel())
            }
        }
    }
}

@Composable
fun ItemList(itemsChannel: Channel<String>) {
    val listState = rememberLazyListState()
    val items = remember { mutableStateListOf<String>() }

    LaunchedEffect(itemsChannel) {
        itemsChannel.consumeEach { newItem ->
            items.add(newItem)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(items) { item ->
            ItemCard(text = item)
        }

        item {
            if (listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size >= items.size - 5) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ItemCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = 4.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp)
        )
    }
}
