package com.phoenix.companionforcodblackops7.core.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TableVersionDto(
    val version: Int = 0,
    val schemaVersion: Int = 0
)

/**
 * Dynamic version response that supports any table names from the API.
 * Uses a Map to handle tables dynamically (icons, operators, mods, etc.)
 */
@Serializable
data class VersionResponseDto(
    // This will contain all table versions as a map: tableName -> TableVersionDto
    // e.g., {"icons": {...}, "operators": {...}, "mods": {...}}
    private val data: Map<String, TableVersionDto> = emptyMap()
) {
    // Provide easy access to all tables
    fun getAllTables(): Map<String, TableVersionDto> = data

    // Provide convenient accessor for specific tables if needed
    fun getTableVersion(tableName: String): TableVersionDto? = data[tableName]
}
