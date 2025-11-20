package com.phoenix.companionforcodblackops7.feature.prestige.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Classic Prestige Information Screen
 * Shows the prestige progression system in Black Ops 7
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PrestigeInfoScreen(
    onNavigateBack: () -> Unit
) {
    val accentColor = Color(0xFFFFB300) // Gold for prestige
    val masterColor = Color(0xFF9C27B0) // Purple for Prestige Master

    val infiniteTransition = rememberInfiniteTransition(label = "prestigeGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CLASSIC PRESTIGE",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable content
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Section
                item {
                    HeroSection(accentColor, glowAlpha)
                }

                // Overview Stats
                item {
                    OverviewStats(accentColor, masterColor)
                }

                // XP Sources Section
                item {
                    XPSourcesSection(accentColor)
                }

                // How Prestige Works
                item {
                    HowPrestigeWorksSection(accentColor)
                }

                // Prestige Rewards
                item {
                    PrestigeRewardsSection(accentColor)
                }

                // Prestige Master Info
                item {
                    PrestigeMasterSection(masterColor, glowAlpha)
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

@Composable
private fun HeroSection(
    accentColor: Color,
    glowAlpha: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = glowAlpha * 0.7f),
                        accentColor.copy(alpha = glowAlpha * 0.7f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large icon with glow
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.35f * glowAlpha),
                                    accentColor.copy(alpha = 0.15f * glowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = accentColor
                )
            }

            Text(
                text = "CLASSIC PRESTIGE SYSTEM",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Progress through Military Ranks and unlock exclusive rewards",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OverviewStats(
    accentColor: Color,
    masterColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = accentColor
            )
            Text(
                text = "OVERVIEW",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = accentColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "MILITARY LEVELS",
                value = "1-55",
                icon = Icons.Filled.Star,
                color = accentColor,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "PRESTIGE LEVELS",
                value = "10",
                icon = Icons.Filled.WorkspacePremium,
                color = accentColor,
                modifier = Modifier.weight(1f)
            )
        }

        StatCard(
            title = "PRESTIGE MASTER",
            value = "Level 1000",
            icon = Icons.Filled.MilitaryTech,
            color = masterColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = color
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun XPSourcesSection(accentColor: Color) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Gamepad,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = accentColor
            )
            Text(
                text = "EARN XP IN THESE MODES",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = accentColor
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val modes = listOf(
                    "Multiplayer" to Icons.Filled.People,
                    "Zombies" to Icons.Filled.Person,
                    "Co-Op Campaign" to Icons.Filled.Groups,
                    "Dead Ops Arcade 4" to Icons.Filled.SportsEsports,
                    "Warzone (Season 1)" to Icons.Filled.Public
                )

                modes.forEach { (mode, icon) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = accentColor
                        )
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HowPrestigeWorksSection(accentColor: Color) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(accentColor, MaterialTheme.shapes.small)
            )
            Text(
                text = "HOW IT WORKS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = accentColor
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrestigeStep(
                    number = 1,
                    title = "Progress Through Military Levels",
                    description = "Earn XP to level up from Level 1 to Level 55",
                    color = accentColor
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                )

                PrestigeStep(
                    number = 2,
                    title = "Enter Prestige at Level 55",
                    description = "Choose to enter Prestige and reset your level",
                    color = accentColor
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                )

                PrestigeStep(
                    number = 3,
                    title = "Repeat 10 Times",
                    description = "Complete all 10 Prestige levels to reach Prestige Master",
                    color = accentColor
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                )

                PrestigeStep(
                    number = 4,
                    title = "Prestige Master Journey",
                    description = "Continue to Level 1000 in Prestige Master",
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
private fun PrestigeStep(
    number: Int,
    title: String,
    description: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = color.copy(alpha = 0.2f),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = color
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PrestigeRewardsSection(accentColor: Color) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CardGiftcard,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = accentColor
            )
            Text(
                text = "REWARDS PER PRESTIGE",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = accentColor
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val rewards = listOf(
                    "Level Reset" to Icons.Filled.Refresh,
                    "Permanent Unlock Token" to Icons.Filled.Lock,
                    "Prestige Icon" to Icons.Filled.EmojiEvents,
                    "Exclusive Skins" to Icons.Filled.Face,
                    "Weapon Blueprints" to Icons.Filled.Build,
                    "Cosmetic Items" to Icons.Filled.Palette
                )

                rewards.forEach { (reward, icon) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = accentColor
                        )
                        Text(
                            text = reward,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrestigeMasterSection(
    masterColor: Color,
    glowAlpha: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        masterColor.copy(alpha = glowAlpha * 0.6f),
                        masterColor.copy(alpha = 0.3f),
                        masterColor.copy(alpha = glowAlpha * 0.6f)
                    )
                ),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    masterColor.copy(alpha = 0.3f),
                                    masterColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MilitaryTech,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = masterColor
                    )
                }

                Column {
                    Text(
                        text = "PRESTIGE MASTER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = masterColor
                    )
                    Text(
                        text = "The Ultimate Challenge",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )

            Text(
                text = "After completing all 10 Prestige levels, you'll enter Prestige Master and continue your journey to Level 1000. This is the ultimate test of dedication and skill!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                color = masterColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = masterColor
                    )
                    Column {
                        Text(
                            text = "LEVEL 1000",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = masterColor
                        )
                        Text(
                            text = "Maximum Rank",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
