package com.phoenix.companionforcodblackops7.core.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.core.data.local.entity.TableMetadata
import com.phoenix.companionforcodblackops7.core.data.remote.api.Bo7ApiService
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val apiService: Bo7ApiService,
    private val realm: Realm
) {

    suspend fun isFirstLaunch(): Boolean = withContext(Dispatchers.IO) {
        val metadata = realm.query<TableMetadata>().find()
        metadata.isEmpty()
    }

    suspend fun performInitialSync(
        onProgress: (String) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress("Fetching schema...")
            val schemaResponse = apiService.getAllSchemas()

            if (!schemaResponse.success) {
                return@withContext Result.failure(Exception("Failed to fetch schema"))
            }

            onProgress("Fetching all data...")
            val dataResponse = apiService.getAllData()

            if (!dataResponse.success) {
                return@withContext Result.failure(Exception("Failed to fetch data"))
            }

            onProgress("Saving to database...")
            val versionResponse = apiService.getVersions()

            realm.write {
                dataResponse.data.forEach { (tableName, tableData) ->
                    when (tableData) {
                        is JsonArray -> {
                            tableData.forEach { item ->
                                if (item is JsonObject) {
                                    saveEntity(tableName, item)
                                }
                            }
                        }
                        else -> Timber.w("Unexpected data format for table: $tableName")
                    }
                }

                val versionData = versionResponse.data
                saveTableMetadata("operators", versionData.operators.version, versionData.operators.schemaVersion)
                saveTableMetadata("icons", versionData.icons.version, versionData.icons.schemaVersion)
            }

            onProgress("Sync complete!")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Initial sync failed")
            Result.failure(e)
        }
    }

    private fun Realm.WriteTransaction.saveEntity(tableName: String, jsonObject: JsonObject) {
        val primaryKey = jsonObject["id"]?.toString() ?: return
        val entityId = "${tableName}_$primaryKey"

        val entity = DynamicEntity().apply {
            id = entityId
            this.tableName = tableName

            jsonObject.forEach { (key, value) ->
                data[key] = jsonToRealmAny(value)
            }
        }

        copyToRealm(entity)
    }

    private fun Realm.WriteTransaction.saveTableMetadata(
        tableName: String,
        version: Int,
        schemaVersion: Int
    ) {
        val metadata = TableMetadata().apply {
            this.tableName = tableName
            this.version = version
            this.schemaVersion = schemaVersion
            this.lastSynced = System.currentTimeMillis()
        }
        copyToRealm(metadata)
    }

    private fun jsonToRealmAny(value: kotlinx.serialization.json.JsonElement): RealmAny? {
        return when (value) {
            is JsonPrimitive -> {
                when {
                    value.isString -> RealmAny.create(value.content)
                    value.intOrNull != null -> RealmAny.create(value.intOrNull!!)
                    value.longOrNull != null -> RealmAny.create(value.longOrNull!!)
                    value.doubleOrNull != null -> RealmAny.create(value.doubleOrNull!!)
                    value.booleanOrNull != null -> RealmAny.create(value.booleanOrNull!!)
                    else -> null
                }
            }
            else -> null
        }
    }
}
