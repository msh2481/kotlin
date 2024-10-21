package org.example.project
import kotlinx.serialization.Serializable

@Serializable
data class BookInfo(
    val title: String,
    val authors: List<String>
)
