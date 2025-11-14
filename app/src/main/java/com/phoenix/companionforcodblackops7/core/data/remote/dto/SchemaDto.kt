package com.phoenix.companionforcodblackops7.core.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class FieldDto(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val key: String? = null,
    val default: String? = null,
    val extra: String? = null
)

@Serializable
data class TableSchemaDto(
    val table: String,
    val fields: List<FieldDto>
)

typealias SchemaAllResponseDto = Map<String, TableSchemaDto>
