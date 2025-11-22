package com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model

/**
 * Represents a category of camos within a mode (e.g., Military, Special, Mastery)
 */
data class CamoCategory(
    val name: String,              // military, special, mastery, prestige1, etc.
    val displayName: String,       // Military, Special, Mastery, Prestige 1, etc.
    val categoryOrder: Int,        // Order within mode (1, 2, 3...)
    val camos: List<Camo>,
    val isLocked: Boolean = false  // Locked until previous category complete
) {
    val completedCount: Int
        get() = camos.count { it.isCompleted }

    val totalCount: Int
        get() = camos.size

    val isComplete: Boolean
        get() = completedCount == totalCount && totalCount > 0
}
