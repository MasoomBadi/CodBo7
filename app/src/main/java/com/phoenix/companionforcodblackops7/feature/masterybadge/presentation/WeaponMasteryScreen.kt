package com.phoenix.companionforcodblackops7.feature.masterybadge.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.MasteryBadge

private val BadgeColor = Color(0xFFFFB300) // Gold color for mastery badges

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeaponMasteryScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeaponMasteryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        when (val state = uiState) {
                            is WeaponMasteryUiState.Success -> {
                                Column {
                                    Text(
                                        text = state.weaponName.uppercase(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                    )
                                    Text(
                                        text = state.weaponCategory.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BadgeColor
                                    )
                                }
                            }
                            else -> Text("Loading...")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Progress section
                if (uiState is WeaponMasteryUiState.Success) {
                    val state = uiState as WeaponMasteryUiState.Success
                    val progress = if (state.totalCount > 0) {
                        state.completedCount.toFloat() / state.totalCount
                    } else 0f

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "MASTERY PROGRESS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = BadgeColor
                                    )
                                    Text(
                                        text = "${state.completedCount} / ${state.totalCount} badges unlocked",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BadgeColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = BadgeColor,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = BadgeColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (val state = uiState) {
            is WeaponMasteryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is WeaponMasteryUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No badges found for this weapon",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is WeaponMasteryUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Mode tabs (dynamically loaded from data)
                    if (state.availableModes.size > 1) {
                        ModeTabs(
                            modes = state.availableModes,
                            selectedMode = state.selectedMode,
                            onModeSelected = { viewModel.selectMode(it) }
                        )
                    }

                    // Badges list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.badges,
                            key = { it.id }
                        ) { badge ->
                            BadgeCard(
                                badge = badge,
                                onToggle = { viewModel.toggleBadge(badge) }
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
        }
    }
}

/**
 * Mode tabs component (Multiplayer / Zombies / etc.)
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModeTabs(
    modes: List<String>,
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        PrimaryTabRow(
            selectedTabIndex = modes.indexOf(selectedMode).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = BadgeColor
        ) {
            modes.forEach { mode ->
                val isSelected = mode == selectedMode
                Tab(
                    selected = isSelected,
                    onClick = { onModeSelected(mode) },
                    text = {
                        Text(
                            text = mode.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    selectedContentColor = BadgeColor,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Badge card component with checkbox
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BadgeCard(
    badge: MasteryBadge,
    onToggle: () -> Unit
) {
    val context = LocalContext.current

    // Glow animation for completed badges
    val infiniteTransition = rememberInfiniteTransition(label = "badgeGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderColor = when {
        badge.isLocked -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        badge.isCompleted -> BadgeColor.copy(alpha = glowAlpha * 0.8f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    val cardColor = when {
        badge.isLocked -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f)
        badge.isCompleted -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
    }

    Card(
        onClick = if (badge.isLocked) {
            {
                // Show toast for locked badges
                Toast.makeText(
                    context,
                    "Complete previous badges first to unlock this one",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            onToggle
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (badge.isCompleted) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (badge.isCompleted) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = !badge.isLocked // Disable interaction for locked badges
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Badge icon with glow effect
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background glow for completed badges
                if (badge.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        BadgeColor.copy(alpha = 0.4f * glowAlpha),
                                        BadgeColor.copy(alpha = 0.1f * glowAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = when {
                        badge.isLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        badge.isCompleted -> BadgeColor.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge.sortOrder.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = when {
                                badge.isLocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                badge.isCompleted -> BadgeColor
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Badge info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatBadgeName(badge.badgeLevel),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = when {
                        badge.isLocked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        badge.isCompleted -> BadgeColor
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    }
                )

                Text(
                    text = if (badge.isLocked) {
                        "UNLOCK PREVIOUS BADGES FIRST"
                    } else {
                        "GET ${badge.killsRequired} KILLS"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        badge.isLocked -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Checkbox
            AnimatedVisibility(
                visible = badge.isCompleted,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    BadgeColor.copy(alpha = glowAlpha),
                                    BadgeColor.copy(alpha = 0.85f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Lock icon for locked badges, empty checkbox for unlocked/incomplete badges
            if (!badge.isCompleted) {
                if (badge.isLocked) {
                    // Lock icon for locked badges
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Locked",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    // Empty checkbox for unlocked but not completed badges
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        color = Color.Transparent,
                        modifier = Modifier.size(40.dp)
                    ) {
                        // Empty
                    }
                }
            }
        }
    }
}

/**
 * Format badge level name (badge_1 → Badge 1, mastery → Mastery)
 */
private fun formatBadgeName(badgeLevel: String): String {
    return when {
        badgeLevel.startsWith("badge_") -> {
            val number = badgeLevel.removePrefix("badge_")
            "BADGE $number"
        }
        else -> badgeLevel.uppercase()
    }
}
