package com.phoenix.companionforcodblackops7.core.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TableVersionDto(
    val version: Int = 0,
    val schemaVersion: Int = 0
)

/**
 * Dynamic version response that supports any table names from the API.
 * The API returns a map directly: {"icons": {...}, "operators": {...}, "mods": {...}}
 * This is a typealias for the Map to make the code more readable.
 */
typealias VersionResponseDto = Map<String, TableVersionDto>
