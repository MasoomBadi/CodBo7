package com.phoenix.companionforcodblackops7.feature.weapons.domain.model

/**
 * Weapon model - Completely dynamic
 * category and weaponType come from database - no hardcoded enums
 */
data class Weapon(
    val id: Int,
    val name: String,
    val displayName: String,
    val category: String, // Dynamic from database (was WeaponCategory enum)
    val categoryDisplayName: String, // Display name for category
    val weaponType: String, // Dynamic from database (was WeaponType enum)
    val weaponTypeDisplayName: String, // Display name for type
    val unlockCriteria: String,
    val unlockLevel: Int,
    val unlockLabel: String,
    val maxLevel: Int,
    val fireModes: String,
    val iconUrl: String,
    val sortOrder: Int
)
