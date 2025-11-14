package com.phoenix.companionforcodblackops7.core.data.local.entity

import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.types.RealmAny
import io.realm.kotlin.types.RealmDictionary
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class DynamicEntity : RealmObject {
    @PrimaryKey
    var id: String = ""
    var tableName: String = ""
    var data: RealmDictionary<RealmAny?> = realmDictionaryOf()
}
