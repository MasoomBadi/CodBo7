# Camo Table Schema Reference

This document contains the database schema for the `camo` table, which will be used as a foreign key reference for weapon camo relationships.

## Table: `camo`

### SQL Schema
```sql
CREATE TABLE `camo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `display_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` ENUM('military','special','mastery','prestige1','prestige2','prestigem','prestigem1','prestigem2','prestigem3') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `mode` ENUM('campaign','multiplayer','zombie','prestige') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `camo_url` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_order` INT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Field Definitions

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | INT | NOT NULL, AUTO_INCREMENT, PRIMARY KEY | Unique identifier for each camo |
| `name` | VARCHAR(100) | NOT NULL | Internal name of the camo |
| `display_name` | VARCHAR(100) | NOT NULL | User-facing display name |
| `category` | ENUM | NOT NULL | Camo category (see categories below) |
| `mode` | ENUM | NOT NULL | Game mode for the camo (see modes below) |
| `camo_url` | VARCHAR(255) | NOT NULL | Path to camo image (relative to base URL) |
| `sort_order` | INT | NOT NULL | Display order for sorting |

### Category Enum Values
1. `military` - Military camos
2. `special` - Special camos
3. `mastery` - Mastery camos
4. `prestige1` - Prestige 1 camos
5. `prestige2` - Prestige 2 camos
6. `prestigem` - Prestige Master camos
7. `prestigem1` - Prestige Master 1 camos
8. `prestigem2` - Prestige Master 2 camos
9. `prestigem3` - Prestige Master 3 camos

### Mode Enum Values
1. `campaign` - Campaign mode camos
2. `multiplayer` - Multiplayer mode camos
3. `zombie` - Zombie mode camos
4. `prestige` - Prestige camos

## Usage Notes

- This table will be used as a foreign key reference for weapon-camo relationships
- The `camo_url` field contains the relative path to the camo image
- Use `sort_order` to display camos in the intended sequence
- When fetching from Realm's DynamicEntity, the table name will be `"camo"`

## Domain Model Structure (Kotlin)

When implementing the Camo feature, the domain model should look like:

```kotlin
data class Camo(
    val id: Int,
    val name: String,
    val displayName: String,
    val category: CamoCategory,
    val mode: CamoMode,
    val camoUrl: String,
    val sortOrder: Int
)

enum class CamoCategory(val displayName: String) {
    MILITARY("Military"),
    SPECIAL("Special"),
    MASTERY("Mastery"),
    PRESTIGE1("Prestige 1"),
    PRESTIGE2("Prestige 2"),
    PRESTIGE_MASTER("Prestige Master"),
    PRESTIGE_MASTER_1("Prestige Master 1"),
    PRESTIGE_MASTER_2("Prestige Master 2"),
    PRESTIGE_MASTER_3("Prestige Master 3");

    companion object {
        fun fromString(value: String): CamoCategory {
            return when (value.lowercase()) {
                "military" -> MILITARY
                "special" -> SPECIAL
                "mastery" -> MASTERY
                "prestige1" -> PRESTIGE1
                "prestige2" -> PRESTIGE2
                "prestigem" -> PRESTIGE_MASTER
                "prestigem1" -> PRESTIGE_MASTER_1
                "prestigem2" -> PRESTIGE_MASTER_2
                "prestigem3" -> PRESTIGE_MASTER_3
                else -> MILITARY
            }
        }
    }
}

enum class CamoMode(val displayName: String) {
    CAMPAIGN("Campaign"),
    MULTIPLAYER("Multiplayer"),
    ZOMBIE("Zombie"),
    PRESTIGE("Prestige");

    companion object {
        fun fromString(value: String): CamoMode {
            return when (value.lowercase()) {
                "campaign" -> CAMPAIGN
                "multiplayer" -> MULTIPLAYER
                "zombie" -> ZOMBIE
                "prestige" -> PRESTIGE
                else -> MULTIPLAYER
            }
        }
    }
}
```

## Future Implementation

When implementing weapon camos, there will likely be a junction/relationship table (e.g., `weapon_camo`) that connects:
- `weapons_mp.id` → foreign key to weapon
- `camo.id` → foreign key to this camo table
- Additional fields for unlock requirements, challenges, etc.
