package org.example.project

@kotlinx.serialization.Serializable
data class VolumeInfo(
    val title: String,
    val authors: List<String>? = null
)