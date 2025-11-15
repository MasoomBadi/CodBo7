package com.phoenix.companionforcodblackops7.feature.checklist.data.local

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ChecklistItemEntity : RealmObject {
    @PrimaryKey
    var id: String = ""
    var category: String = ""
    var isUnlocked: Boolean = false
}
