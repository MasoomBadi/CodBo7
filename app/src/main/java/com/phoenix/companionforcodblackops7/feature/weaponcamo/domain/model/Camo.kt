package com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model

data class Camo(
    val id: Int,
    val name: String,
    val displayName: String,
    val category: String,          // military, special, mastery, prestige1, etc.
    val mode: String,              // campaign, multiplayer, zombie, prestige
    val camoUrl: String,
    val sortOrder: Int,            // Order within category
    val categoryOrder: Int,        // Order of category within mode
    val isCompleted: Boolean = false,
    val isLocked: Boolean = true,
    val criteriaCount: Int = 0,
    val completedCriteriaCount: Int = 0
)
