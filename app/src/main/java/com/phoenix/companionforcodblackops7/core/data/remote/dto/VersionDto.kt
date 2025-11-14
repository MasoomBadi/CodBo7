package com.phoenix.companionforcodblackops7.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TableVersionDto(
    val version: Int = 0,
    @SerialName("schema_version")
    val schemaVersion: Int = 0
)

@Serializable
data class VersionResponseDto(
    val icons: TableVersionDto,
    val operators: TableVersionDto,
    @SerialName("data_versions")
    val dataVersions: TableVersionDto? = null
)
