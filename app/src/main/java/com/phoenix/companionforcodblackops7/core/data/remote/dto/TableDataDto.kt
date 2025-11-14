package com.phoenix.companionforcodblackops7.core.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

/**
 * DTO for table data response from /api/data/{table} endpoint.
 * The API wraps the array in an object with table name and data array.
 */
@Serializable
data class TableDataDto(
    val table: String,
    val data: JsonArray
)
