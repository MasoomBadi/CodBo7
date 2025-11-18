package com.phoenix.companionforcodblackops7.feature.perks.domain.model

/**
 * Domain model for a Perk in multiplayer/campaign mode
 *
 * @property id Unique identifier
 * @property name Internal name (e.g., 'gung_ho')
 * @property displayName User-facing name (e.g., 'Gung Ho')
 * @property slot Perk slot (1, 2, or 3)
 * @property category Combat specialty (enforcer, recon, strategist)
 * @property categoryColor Color associated with category (red, blue, green)
 * @property unlockLevel Level required to unlock (0 for default)
 * @property unlockLabel User-facing unlock label (e.g., 'Level 18', 'Default')
 * @property description Detailed description of the perk's effects
 * @property iconUrl Path to the perk's icon image
 * @property sortOrder Order for sorting perks in lists
 */
data class Perk(
    val id: Int,
    val name: String,
    val displayName: String,
    val slot: Int,
    val category: String,
    val categoryColor: String,
    val unlockLevel: Int,
    val unlockLabel: String,
    val description: String,
    val iconUrl: String,
    val sortOrder: Int
) {
    /**
     * Returns the category color as a Compose Color
     */
    fun getCategoryColorValue(): androidx.compose.ui.graphics.Color {
        return when (categoryColor.lowercase()) {
            "red" -> androidx.compose.ui.graphics.Color(0xFFE53935) // Enforcer Red
            "blue" -> androidx.compose.ui.graphics.Color(0xFF1E88E5) // Recon Blue
            "green" -> androidx.compose.ui.graphics.Color(0xFF43A047) // Strategist Green
            else -> androidx.compose.ui.graphics.Color(0xFFF96800) // Default Orange
        }
    }

    /**
     * Returns true if this perk is unlocked by default
     */
    fun isDefault(): Boolean = unlockLevel == 0
}
