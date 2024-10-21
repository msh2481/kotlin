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

class ItemsRepository {
    private val flows = (loadMoreItems(0) to loadMoreItems(0))//bookInfTwoFlows()

    fun getTab1Flow() = flows.first //.map { it.title }
    fun getTab2Flow() = flows.second //.map { it.author }
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
                0 -> ItemList(itemsFlow = repository.getTab1Flow())
                1 -> ItemList(itemsFlow = repository.getTab2Flow())
            }
        }
    }
}

@Composable
fun ItemList(itemsFlow: Flow<String>) {
    val listState = rememberLazyListState()
    val items = remember { mutableStateListOf<String>() }

    LaunchedEffect(itemsFlow) {
        itemsFlow.collect { newItem ->
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

fun loadMoreItems(startIndex: Int): Flow<String> = flow {
    var currentIndex = startIndex
    while (true) {
        emit(currentIndex.toString())
        currentIndex++
        kotlinx.coroutines.delay(100) // Simulate network delay
    }
}
