package com.phoenix.companionforcodblackops7.feature.masterybadge.data.local

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Realm entity for tracking mastery badge completion progress locally
 *
 * Each instance represents one checkbox state for a specific badge/mode/weapon combination
 * Composite key format: "{weaponId}_{badgeLevel}_{mode}" (e.g., "1_badge_1_multiplayer")
 */
class MasteryBadgeProgressEntity : RealmObject {
    @PrimaryKey
    var id: String = "" // Composite: "{weaponId}_{badgeLevel}_{mode}"
    var weaponId: Int = 0
    var badgeLevel: String = ""
    var mode: String = ""
    var isCompleted: Boolean = false
}
