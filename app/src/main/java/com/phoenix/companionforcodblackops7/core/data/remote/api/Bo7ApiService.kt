package com.phoenix.companionforcodblackops7.core.data.remote.api

import com.phoenix.companionforcodblackops7.core.data.remote.dto.ApiResponse
import com.phoenix.companionforcodblackops7.core.data.remote.dto.SchemaAllResponseDto
import com.phoenix.companionforcodblackops7.core.data.remote.dto.TableSchemaDto
import com.phoenix.companionforcodblackops7.core.data.remote.dto.VersionResponseDto
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Path

interface Bo7ApiService {

    @GET("api/versions")
    suspend fun getVersions(): ApiResponse<VersionResponseDto>

    @GET("api/schema/all")
    suspend fun getAllSchemas(): ApiResponse<SchemaAllResponseDto>

    @GET("api/schema/{table}")
    suspend fun getTableSchema(@Path("table") tableName: String): ApiResponse<TableSchemaDto>

    @GET("api/data/all")
    suspend fun getAllData(): ApiResponse<JsonObject>

    @GET("api/data/{table}")
    suspend fun getTableData(@Path("table") tableName: String): ApiResponse<JsonArray>
}
