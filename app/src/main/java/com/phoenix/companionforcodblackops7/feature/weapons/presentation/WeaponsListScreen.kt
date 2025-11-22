package com.phoenix.companionforcodblackops7.feature.weapons.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.feature.weapons.presentation.model.WeaponWithBadges

private const val BASE_URL = "http://codbo7.masoombadi.top"

/**
 * Weapons List Screen - Displays all multiplayer weapons with category filters
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeaponsListScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeaponsViewModel = hiltViewModel()
) {
    val weaponsByCategory by viewModel.weaponsByCategory.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) } // Dynamic String category
    var expandedWeaponId by remember { mutableStateOf<Int?>(null) }
    val accentColor = Color(0xFF00BCD4) // Cyan

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WEAPONS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = accentColor
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        WeaponsContent(
            weaponsByCategory = weaponsByCategory,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            expandedWeaponId = expandedWeaponId,
            onToggleExpanded = { weaponId ->
                expandedWeaponId = if (expandedWeaponId == weaponId) null else weaponId
            },
            accentColor = accentColor,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

/**
 * Main content with category filters and weapon cards
 */
@Composable
private fun WeaponsContent(
    weaponsByCategory: Map<String, List<WeaponWithBadges>>, // Dynamic String categories
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    expandedWeaponId: Int?,
    onToggleExpanded: (Int) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Category filter chips
        CategoryFilterRow(
            categories = weaponsByCategory.keys.toList(),
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
            accentColor = accentColor
        )

        // Filtered weapons list
        val filteredWeapons = if (selectedCategory == null) {
            weaponsByCategory.values.flatten()
        } else {
            weaponsByCategory[selectedCategory] ?: emptyList()
        }

        // Weapon cards
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = filteredWeapons,
                key = { it.weapon.id }
            ) { weaponWithBadges ->
                ExpandableWeaponCard(
                    weaponWithBadges = weaponWithBadges,
                    isExpanded = expandedWeaponId == weaponWithBadges.weapon.id,
                    onToggleExpanded = { onToggleExpanded(weaponWithBadges.weapon.id) },
                    accentColor = accentColor
                )
            }
        }

        // Fixed Banner Ad Space at Bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Banner Ad Space (320x90)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

/**
 * Horizontal scrolling category filter chips
 */
@Composable
private fun CategoryFilterRow(
    categories: List<String>, // Dynamic String categories
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" filter chip
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = {
                Text(
                    text = "ALL",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = accentColor.copy(alpha = 0.3f),
                selectedLabelColor = accentColor
            )
        )

        // Category filter chips
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = formatCategoryDisplayName(category).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor.copy(alpha = 0.3f),
                    selectedLabelColor = accentColor
                )
            )
        }
    }
}

/**
 * Expandable weapon card with big image
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpandableWeaponCard(
    weaponWithBadges: WeaponWithBadges,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    accentColor: Color
) {
    val weapon = weaponWithBadges.weapon
    val infiniteTransition = rememberInfiniteTransition(label = "weaponGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Card(
        onClick = onToggleExpanded,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = accentColor.copy(alpha = glowAlpha * 0.7f),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with big weapon image
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Big weapon image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.2f),
                                    accentColor.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                radius = 800f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "$BASE_URL${weapon.iconUrl}",
                        contentDescription = weapon.displayName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }

                // Weapon name and expand icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = weapon.displayName.uppercase(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = accentColor,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Category and Type badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = weapon.categoryDisplayName.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = accentColor
                        )
                    }
                }

                // Weapon type badge
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = weapon.weaponTypeDisplayName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                // Mastery badge count
                if (weaponWithBadges.totalBadges > 0) {
                    val badgeColor = Color(0xFFFFB300) // Gold color for mastery badges
                    Surface(
                        color = if (weaponWithBadges.isFullyCompleted) {
                            badgeColor.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (weaponWithBadges.isFullyCompleted) badgeColor
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                            )
                            Text(
                                text = "MASTERY ${weaponWithBadges.badgeProgressText}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (weaponWithBadges.isFullyCompleted) {
                                    badgeColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            // Expandable details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = accentColor.copy(alpha = 0.3f)
                    )

                    // Stats section
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "STATS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = accentColor
                            )

                            // Max Level
                            WeaponStatRow(
                                label = "Max Level",
                                value = weapon.maxLevel.toString(),
                                accentColor = accentColor
                            )

                            if (weapon.fireModes.isNotEmpty()) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                                // Fire Modes
                                WeaponStatRow(
                                    label = "Fire Modes",
                                    value = weapon.fireModes,
                                    accentColor = accentColor
                                )
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )

                            // Unlock criteria
                            WeaponStatRow(
                                label = "Unlock",
                                value = weapon.unlockLabel,
                                accentColor = accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Weapon stat row for expandable section
 */
@Composable
private fun WeaponStatRow(
    label: String,
    value: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = accentColor
        )
    }
}

/**
 * Format category string to display name for UI
 * Matches backend formatting
 */
private fun formatCategoryDisplayName(category: String): String {
    return when (category.uppercase().replace(" ", "_")) {
        "ASSAULT_RIFLE", "ASSAULT RIFLE", "ASSAULT_RIFLES" -> "Assault Rifles"
        "SMG", "SMGS" -> "SMGs"
        "SHOTGUN", "SHOTGUNS" -> "Shotguns"
        "LMG", "LMGS" -> "LMGs"
        "MARKSMAN", "MARKSMAN_RIFLE", "MARKSMAN RIFLE", "MARKSMAN_RIFLES" -> "Marksman Rifles"
        "SNIPER", "SNIPER_RIFLE", "SNIPER RIFLE", "SNIPER_RIFLES" -> "Sniper Rifles"
        "PISTOL", "PISTOLS" -> "Pistols"
        "LAUNCHER", "LAUNCHERS" -> "Launchers"
        "MELEE" -> "Melee"
        else -> category.replace("_", " ").capitalize()
    }
}
