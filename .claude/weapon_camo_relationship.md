# Weapon-Camo Relationship Documentation

## Overview

Each weapon has **54 total camos** distributed as:
- **51 Common Camos** - Shared by ALL 29 weapons
- **3 Unique Prestige Camos** - Specific to each weapon

## Camo Distribution Breakdown

### Common Camos (51) - Shared by ALL weapons

These camos are **NOT** in the `weapon_camo` junction table. They are directly queried from the `camo` table based on category.

| Category | Count | Distribution | IDs |
|----------|-------|--------------|-----|
| `military` | 27 | 9 per mode (campaign, multiplayer, zombie) | Various |
| `special` | 9 | 3 per mode (campaign, multiplayer, zombie) | Various |
| `mastery` | 12 | 4 per mode (campaign, multiplayer, zombie) | Various |
| `prestigem1` | 1 | Single shared camo | 51 |
| `prestigem2` | 1 | Single shared camo | 52 |
| `prestigem3` | 1 | Single shared camo | 53 |
| **Total** | **51** | | |

**Query to get common camos:**
```sql
SELECT * FROM camo
WHERE category IN ('military', 'special', 'mastery', 'prestigem1', 'prestigem2', 'prestigem3')
ORDER BY sort_order;
```

### Unique Prestige Camos (3) - Specific to Each Weapon

These camos **ARE** in the `weapon_camo` junction table. Each weapon has 3 unique prestige camos.

| Category | Count | Description |
|----------|-------|-------------|
| `prestige1` | 1 | Unique Prestige 1 camo for this weapon |
| `prestige2` | 1 | Unique Prestige 2 camo for this weapon |
| `prestigem` | 1 | Unique Prestige Master camo for this weapon |
| **Total** | **3** | |

**Query to get unique camos for weapon_id = 1:**
```sql
SELECT c.* FROM camo c
INNER JOIN weapon_camo wc ON c.id = wc.camo_id
WHERE wc.weapon_id = 1
ORDER BY c.sort_order;
```

## Junction Table Schema

### Table: `weapon_camo`

```sql
CREATE TABLE `weapon_camo` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `weapon_id` INT NOT NULL,
  `camo_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `weapon_camo_unique` (`weapon_id`, `camo_id`),
  KEY `weapon_id` (`weapon_id`),
  KEY `camo_id` (`camo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Purpose:** Links weapons to their unique prestige camos (prestige1, prestige2, prestigem)

**Constraints:**
- `weapon_id` references `weapons_mp.id`
- `camo_id` references `camo.id`
- UNIQUE constraint ensures no duplicate weapon-camo pairs

## Complete Query: Get ALL 54 Camos for a Weapon

```sql
-- Get all 54 camos for weapon_id = 1 (51 common + 3 unique)
SELECT * FROM camo
WHERE category IN ('military', 'special', 'mastery', 'prestigem1', 'prestigem2', 'prestigem3')

UNION

SELECT c.* FROM camo c
INNER JOIN weapon_camo wc ON c.id = wc.camo_id
WHERE wc.weapon_id = 1

ORDER BY sort_order;
```

## Kotlin Domain Models

### WeaponCamo Data Class

```kotlin
data class WeaponCamo(
    val id: Int,
    val weaponId: Int,
    val camo: Camo,
    val isUnlocked: Boolean = false
)
```

### Camo Grouping

For UI display, camos should be grouped by category and mode:

```kotlin
data class WeaponCamoProgress(
    val weaponId: Int,
    val weaponName: String,
    val commonCamos: List<Camo>, // 51 camos
    val uniqueCamos: List<Camo>,  // 3 camos
    val totalCamos: Int = 54,
    val unlockedCount: Int,
    val percentage: Float
)

data class CamosByMode(
    val mode: CamoMode,
    val military: List<Camo>,   // 9 per mode
    val special: List<Camo>,    // 3 per mode
    val mastery: List<Camo>,    // 4 per mode
    val prestigeMaster: List<Camo> // prestigem1, prestigem2, prestigem3 (only for multiplayer mode)
)
```

## Implementation Plan

### Phase 1: Data Layer

1. **Create domain models:**
   - `WeaponCamo` - combines weapon, camo, unlock status
   - `WeaponCamoProgress` - tracks progress per weapon
   - `CamosByMode` - groups camos for display

2. **Create repository:**
   - `WeaponCamoRepository` interface
   - `WeaponCamoRepositoryImpl` implementation
   - Fetch common camos (51) from `camo` table
   - Fetch unique camos (3) from `weapon_camo` junction table
   - Combine both queries to get all 54 camos
   - Store unlock status in DataStore or local Realm

3. **Use cases:**
   - `GetWeaponCamosUseCase` - fetch all camos for a weapon
   - `ToggleCamoUnlockUseCase` - mark camo as unlocked/locked
   - `GetWeaponCamoProgressUseCase` - calculate progress percentage

### Phase 2: Presentation Layer

1. **ViewModel:**
   - `WeaponCamoViewModel` - manages state and business logic
   - State: loading, success (with camo groups), error
   - Track expanded weapon/category states

2. **UI Design Options:**

   **Option A: Weapon Detail Screen** (Tap weapon → see its camos)
   - Add "View Camos" button to expanded weapon card in WeaponsListScreen
   - Navigate to WeaponCamoDetailScreen
   - Show 54 camos grouped by:
     - Mode tabs (Campaign, Multiplayer, Zombie)
     - Categories (Military, Special, Mastery, Prestige)
   - Tap camo to toggle unlock status

   **Option B: Camo Tracker in Collection Tracker** (Like existing checklist)
   - Add "Weapon Camos" category to ChecklistCategory enum
   - Show all 29 weapons in CategoryChecklistScreen
   - Tap weapon → see its 54 camos
   - Similar to existing prestige tracker

3. **UI Components:**
   - Camo grid/list with unlock toggle
   - Progress indicators (X/54 unlocked)
   - Category filters/tabs
   - Mode filters (Campaign, Multiplayer, Zombie)

### Phase 3: Integration

1. Add camo button to WeaponsListScreen expandable card
2. Set up navigation in MainActivity
3. Configure Hilt DI modules
4. Implement unlock persistence (DataStore)

## Storage Strategy for Unlock Status

Since the camo data comes from the server but unlock status is local user data:

```kotlin
// DataStore key format: "weapon_camo_unlock_{weaponId}_{camoId}"
// Value: Boolean (true = unlocked, false = locked)

class WeaponCamoPreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    fun isWeaponCamoUnlocked(weaponId: Int, camoId: Int): Flow<Boolean>
    suspend fun setWeaponCamoUnlocked(weaponId: Int, camoId: Int, unlocked: Boolean)
    fun getWeaponCamoProgress(weaponId: Int): Flow<Pair<Int, Int>> // (unlocked, total)
}
```

## SQL Reference Queries

```sql
-- Get all common camos (51)
SELECT * FROM camo
WHERE category IN ('military', 'special', 'mastery', 'prestigem1', 'prestigem2', 'prestigem3')
ORDER BY mode, category, sort_order;

-- Get unique prestige camos for weapon (3)
SELECT c.* FROM camo c
INNER JOIN weapon_camo wc ON c.id = wc.camo_id
WHERE wc.weapon_id = :weaponId
ORDER BY c.sort_order;

-- Get all 54 camos for weapon
SELECT * FROM camo
WHERE category IN ('military', 'special', 'mastery', 'prestigem1', 'prestigem2', 'prestigem3')
UNION
SELECT c.* FROM camo c
INNER JOIN weapon_camo wc ON c.id = wc.camo_id
WHERE wc.weapon_id = :weaponId
ORDER BY sort_order;

-- Count common camos by category
SELECT category, mode, COUNT(*) as count
FROM camo
WHERE category IN ('military', 'special', 'mastery', 'prestigem1', 'prestigem2', 'prestigem3')
GROUP BY category, mode;
```

## Notes

- Common camos (51) are shared → fetch once and reuse for all weapons
- Unique camos (3) are weapon-specific → fetch per weapon from junction table
- Total: 54 camos per weapon
- 29 weapons × 54 camos = 1,566 total weapon-camo combinations
- But only 51 common + (29 weapons × 3 unique) = 51 + 87 = 138 actual camo records in database
