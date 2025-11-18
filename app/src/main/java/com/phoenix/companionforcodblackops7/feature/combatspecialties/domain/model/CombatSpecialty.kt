package com.phoenix.companionforcodblackops7.feature.combatspecialties.domain.model

/**
 * Domain model for a Combat Specialty in multiplayer mode
 *
 * @property id Unique specialty identifier
 * @property name URL-friendly specialty name (e.g., 'enforcer', 'scout')
 * @property displayName Human-readable specialty name (e.g., 'Enforcer', 'Scout')
 * @property specialtyType Type: 'core' (single color) or 'hybrid' (mixed colors)
 * @property categoryColor Color: 'red', 'blue', 'green', or 'hybrid'
 * @property requiredPerks Human-readable perk requirement
 * @property perkCombination Structured perk formula (e.g., 'red:3', 'red:2,blue:1')
 * @property effectDescription Specialty effect and abilities
 * @property iconUrl Path to specialty icon image
 * @property sortOrder Display order
 */
data class CombatSpecialty(
    val id: Int,
    val name: String,
    val displayName: String,
    val specialtyType: String,
    val categoryColor: String,
    val requiredPerks: String,
    val perkCombination: String,
    val effectDescription: String,
    val iconUrl: String,
    val sortOrder: Int
) {
    /**
     * Returns true if this is a core specialty (single color)
     */
    fun isCoreSpecialty(): Boolean = specialtyType.equals("core", ignoreCase = true)

    /**
     * Returns true if this is a hybrid specialty (mixed colors)
     */
    fun isHybridSpecialty(): Boolean = specialtyType.equals("hybrid", ignoreCase = true)

    /**
     * Returns the category color as a Compose Color
     */
    fun getCategoryColorValue(): androidx.compose.ui.graphics.Color {
        return when (categoryColor.lowercase()) {
            "red" -> androidx.compose.ui.graphics.Color(0xFFE53935) // Enforcer Red
            "blue" -> androidx.compose.ui.graphics.Color(0xFF1E88E5) // Recon Blue
            "green" -> androidx.compose.ui.graphics.Color(0xFF43A047) // Strategist Green
            "hybrid" -> androidx.compose.ui.graphics.Color(0xFFEC407A) // Hybrid Pink
            else -> androidx.compose.ui.graphics.Color(0xFFF96800) // Default Orange
        }
    }

    /**
     * Returns a formatted type label for display
     */
    fun getTypeLabel(): String {
        return when (specialtyType.lowercase()) {
            "core" -> "CORE"
            "hybrid" -> "HYBRID"
            else -> specialtyType.uppercase()
        }
    }
}
