package com.phoenix.companionforcodblackops7.core.data.local.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class TableMetadata : RealmObject {
    @PrimaryKey
    var tableName: String = ""
    var version: Int = 0
    var schemaVersion: Int = 0
    var lastSynced: Long = 0
}
