package com.phoenix.companionforcodblackops7.core.data.repository

import com.phoenix.companionforcodblackops7.core.data.local.entity.DynamicEntity
import com.phoenix.companionforcodblackops7.core.data.local.entity.TableMetadata
import com.phoenix.companionforcodblackops7.core.data.local.preferences.PreferencesManager
import com.phoenix.companionforcodblackops7.core.data.remote.api.Bo7ApiService
import com.phoenix.companionforcodblackops7.core.data.remote.dto.TableVersionDto
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmAny
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
    private val realm: Realm,
    private val preferencesManager: PreferencesManager
) {

    suspend fun performFreshSync(
        onProgress: (String) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress("Clearing existing data...")
            realm.write {
                delete(query<DynamicEntity>())
                delete(query<TableMetadata>())
            }

            onProgress("Fetching schema...")
            val schemaResponse = apiService.getAllSchemas()

            if (!schemaResponse.success) {
                preferencesManager.setIsSyncComplete(false)
                return@withContext Result.failure(Exception("Failed to fetch schema"))
            }

            onProgress("Fetching all data...")
            val dataResponse = apiService.getAllData()

            if (!dataResponse.success) {
                preferencesManager.setIsSyncComplete(false)
                return@withContext Result.failure(Exception("Failed to fetch data"))
            }

            onProgress("Saving to database...")
            val versionResponse = apiService.getVersions()

            if (!versionResponse.success) {
                preferencesManager.setIsSyncComplete(false)
                return@withContext Result.failure(Exception("Failed to fetch versions"))
            }

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

                // Dynamically save metadata for all tables from version response
                versionResponse.data.getAllTables().forEach { (tableName, versionInfo) ->
                    saveTableMetadata(tableName, versionInfo.version, versionInfo.schemaVersion)
                }
            }

            preferencesManager.setIsSyncComplete(true)
            onProgress("Sync complete!")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Fresh sync failed")
            preferencesManager.setIsSyncComplete(false)
            Result.failure(e)
        }
    }

    suspend fun checkAndSync(
        onProgress: (String) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val isSyncComplete = preferencesManager.isSyncComplete.first()

            if (!isSyncComplete) {
                return@withContext performFreshSync(onProgress)
            }

            onProgress("Checking for updates...")
            val versionResponse = apiService.getVersions()

            if (!versionResponse.success) {
                return@withContext Result.failure(Exception("Failed to fetch version info"))
            }

            // Dynamically get all tables from the API response
            val remoteVersions = versionResponse.data.getAllTables()

            val localMetadata = realm.query<TableMetadata>().find()
            val localVersionMap = localMetadata.associateBy { it.tableName }

            val tablesToSync = mutableListOf<TableSyncInfo>()

            remoteVersions.forEach { (tableName, remoteVersion) ->
                val localVersion = localVersionMap[tableName]

                when {
                    localVersion == null -> {
                        tablesToSync.add(
                            TableSyncInfo(
                                tableName = tableName,
                                syncType = SyncType.NEW_TABLE,
                                remoteVersion = remoteVersion
                            )
                        )
                    }
                    localVersion.schemaVersion != remoteVersion.schemaVersion &&
                    localVersion.version != remoteVersion.version -> {
                        tablesToSync.add(
                            TableSyncInfo(
                                tableName = tableName,
                                syncType = SyncType.BOTH_MISMATCH,
                                remoteVersion = remoteVersion
                            )
                        )
                    }
                    localVersion.schemaVersion != remoteVersion.schemaVersion -> {
                        tablesToSync.add(
                            TableSyncInfo(
                                tableName = tableName,
                                syncType = SyncType.SCHEMA_MISMATCH,
                                remoteVersion = remoteVersion
                            )
                        )
                    }
                    localVersion.version != remoteVersion.version -> {
                        tablesToSync.add(
                            TableSyncInfo(
                                tableName = tableName,
                                syncType = SyncType.VERSION_MISMATCH,
                                remoteVersion = remoteVersion
                            )
                        )
                    }
                }
            }

            if (tablesToSync.isEmpty()) {
                onProgress("All data is up to date")
                return@withContext Result.success(Unit)
            }

            preferencesManager.setIsSyncComplete(false)

            tablesToSync.forEach { syncInfo ->
                when (syncInfo.syncType) {
                    SyncType.NEW_TABLE -> {
                        onProgress("Adding new table: ${syncInfo.tableName}")
                        syncNewTable(syncInfo.tableName, syncInfo.remoteVersion)
                    }
                    SyncType.SCHEMA_MISMATCH -> {
                        onProgress("Updating schema: ${syncInfo.tableName}")
                        syncSchemaChange(syncInfo.tableName, syncInfo.remoteVersion)
                    }
                    SyncType.VERSION_MISMATCH -> {
                        onProgress("Updating data: ${syncInfo.tableName}")
                        syncDataOnly(syncInfo.tableName, syncInfo.remoteVersion)
                    }
                    SyncType.BOTH_MISMATCH -> {
                        onProgress("Updating schema & data: ${syncInfo.tableName}")
                        syncSchemaChange(syncInfo.tableName, syncInfo.remoteVersion)
                    }
                }
            }

            preferencesManager.setIsSyncComplete(true)
            onProgress("Sync complete!")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sync check failed")
            preferencesManager.setIsSyncComplete(false)
            Result.failure(e)
        }
    }

    private suspend fun syncNewTable(tableName: String, versionInfo: TableVersionDto) {
        val schemaResponse = apiService.getTableSchema(tableName)
        if (!schemaResponse.success) {
            throw Exception("Failed to fetch schema for $tableName")
        }

        val dataResponse = apiService.getTableData(tableName)
        if (!dataResponse.success) {
            throw Exception("Failed to fetch data for $tableName")
        }

        realm.write {
            dataResponse.data.forEach { item ->
                if (item is JsonObject) {
                    saveEntity(tableName, item)
                }
            }
            saveTableMetadata(tableName, versionInfo.version, versionInfo.schemaVersion)
        }
    }

    private suspend fun syncSchemaChange(tableName: String, versionInfo: TableVersionDto) {
        val schemaResponse = apiService.getTableSchema(tableName)
        if (!schemaResponse.success) {
            throw Exception("Failed to fetch schema for $tableName")
        }

        realm.write {
            delete(query<DynamicEntity>("tableName == $0", tableName))
            delete(query<TableMetadata>("tableName == $0", tableName))
        }

        val dataResponse = apiService.getTableData(tableName)
        if (!dataResponse.success) {
            throw Exception("Failed to fetch data for $tableName")
        }

        realm.write {
            dataResponse.data.forEach { item ->
                if (item is JsonObject) {
                    saveEntity(tableName, item)
                }
            }
            saveTableMetadata(tableName, versionInfo.version, versionInfo.schemaVersion)
        }
    }

    private suspend fun syncDataOnly(tableName: String, versionInfo: TableVersionDto) {
        realm.write {
            delete(query<DynamicEntity>("tableName == $0", tableName))
        }

        val dataResponse = apiService.getTableData(tableName)
        if (!dataResponse.success) {
            throw Exception("Failed to fetch data for $tableName")
        }

        realm.write {
            dataResponse.data.forEach { item ->
                if (item is JsonObject) {
                    saveEntity(tableName, item)
                }
            }

            val existingMetadata = query<TableMetadata>("tableName == $0", tableName).first().find()
            existingMetadata?.let {
                it.version = versionInfo.version
                it.lastSynced = System.currentTimeMillis()
            }
        }
    }

    private fun MutableRealm.saveEntity(tableName: String, jsonObject: JsonObject) {
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

    private fun MutableRealm.saveTableMetadata(
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

    private data class TableSyncInfo(
        val tableName: String,
        val syncType: SyncType,
        val remoteVersion: TableVersionDto
    )

    private enum class SyncType {
        NEW_TABLE,
        SCHEMA_MISMATCH,
        VERSION_MISMATCH,
        BOTH_MISMATCH
    }
}
