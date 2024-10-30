package org.example.project

@kotlinx.serialization.Serializable
data class GoogleBooksResponse(
    val items: List<Volume>? = null
)
