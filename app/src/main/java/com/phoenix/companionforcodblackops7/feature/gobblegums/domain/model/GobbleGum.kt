package com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Rarity levels for GobbleGums with associated colors and essence values
 */
enum class GobbleGumRarity(val displayName: String, val essenceValue: Int) {
    RARE("Rare", 500),
    EPIC("Epic", 1250),
    LEGENDARY("Legendary", 3000),
    ULTRA("Ultra", 5000),
    WHIMSICAL("Whimsical", 100);

    /**
     * Get the color for this rarity
     */
    fun getColor(): Color {
        return when (this) {
            RARE -> Color(0xFF2196F3) // Blue
            EPIC -> Color(0xFF9C27B0) // Purple
            LEGENDARY -> Color(0xFFFF6F00) // Orange
            ULTRA -> Color(0xFFE53935) // Red
            WHIMSICAL -> Color(0xFFFF1744) // Rainbow pink (will use gradient)
        }
    }

    /**
     * Get gradient colors for Whimsical rarity
     */
    fun getGradientColors(): List<Color> {
        return when (this) {
            WHIMSICAL -> listOf(
                Color(0xFFFF1744), // Pink
                Color(0xFFE040FB), // Purple
                Color(0xFF448AFF), // Blue
                Color(0xFF00E676), // Green
                Color(0xFFFFC107)  // Yellow
            )
            else -> listOf(getColor(), getColor())
        }
    }

    companion object {
        fun fromString(value: String): GobbleGumRarity {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: RARE
        }
    }
}

/**
 * Pattern types for GobbleGums
 */
enum class GobbleGumPattern(val displayName: String) {
    INSTANT("Instant"),
    TIMED("Timed"),
    CONDITIONAL("Conditional");

    companion object {
        fun fromString(value: String): GobbleGumPattern {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: INSTANT
        }
    }
}

/**
 * Gum type classification
 */
enum class GumType(val displayName: String) {
    NEW("New"),
    RETURNING("Returning");

    companion object {
        fun fromString(value: String): GumType {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: NEW
        }
    }
}

/**
 * Domain model for GobbleGum
 */
data class GobbleGum(
    val id: Int,
    val name: String,
    val rarity: GobbleGumRarity,
    val color: String,
    val essenceValue: Int,
    val pattern: GobbleGumPattern,
    val activationType: String,
    val zombiesEffect: String,
    val doa4Effect: String?,
    val duration: String?,
    val shortDescription: String,
    val gumType: GumType,
    val iconUrl: String,
    val recyclable: Boolean,
    val synergy: String?,
    val tags: String?,
    val sortOrder: Int,
    val tips: List<GobbleGumTip> = emptyList()
) {
    /**
     * Get accent color based on rarity
     */
    fun getAccentColor(): Color = rarity.getColor()

    /**
     * Get gradient colors (for Whimsical or solid for others)
     */
    fun getGradientColors(): List<Color> = rarity.getGradientColors()

    /**
     * Check if this is a Whimsical rarity (needs rainbow gradient)
     */
    fun isWhimsical(): Boolean = rarity == GobbleGumRarity.WHIMSICAL

    /**
     * Parse synergy into list of gobblegum names
     */
    fun getSynergyList(): List<String> {
        return synergy?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    /**
     * Parse tags into list
     */
    fun getTagsList(): List<String> {
        return tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    /**
     * Check if has synergy
     */
    fun hasSynergy(): Boolean = !synergy.isNullOrBlank()

    /**
     * Check if has tags
     */
    fun hasTags(): Boolean = !tags.isNullOrBlank()

    /**
     * Check if has DOA4 effect
     */
    fun hasDoa4Effect(): Boolean = !doa4Effect.isNullOrBlank()

    /**
     * Check if has duration
     */
    fun hasDuration(): Boolean = !duration.isNullOrBlank()

    /**
     * Get formatted duration text
     */
    fun getDurationText(): String = duration ?: "N/A"

    /**
     * Check if matches a tag (for filtering)
     */
    fun matchesTag(tag: String): Boolean {
        return getTagsList().any { it.equals(tag, ignoreCase = true) }
    }
}
