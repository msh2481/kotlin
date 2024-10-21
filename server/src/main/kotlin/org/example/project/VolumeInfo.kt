package org.example.project
import kotlinx.serialization.Serializable

@Serializable
data class VolumeInfo(
    val title: String,
    val authors: List<String>? = null
)