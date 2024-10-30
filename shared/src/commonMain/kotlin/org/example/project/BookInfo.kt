package org.example.project

@kotlinx.serialization.Serializable
data class BookInfo(
    val title: String,
    val authors: List<String>
)
