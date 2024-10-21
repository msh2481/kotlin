package org.example.project
import kotlinx.serialization.Serializable

@Serializable
data class Volume(
    val volumeInfo: VolumeInfo
)