package org.example.project
import kotlinx.serialization.Serializable

@Serializable
data class GoogleBooksResponse(
    val items: List<Volume>? = null
)
