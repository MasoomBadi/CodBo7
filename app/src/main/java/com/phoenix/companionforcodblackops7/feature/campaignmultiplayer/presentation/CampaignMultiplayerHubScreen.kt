package com.phoenix.companionforcodblackops7.feature.campaignmultiplayer.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Hub screen for Campaign/Multiplayer content
 * Shows different categories like Perks, Weapons, Equipment, etc.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CampaignMultiplayerHubScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPerks: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CAMPAIGN/MULTIPLAYER",
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
                    titleContentColor = Color(0xFF00BCD4)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "LOADOUT TOOLKIT",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFF00BCD4)
                    )
                    Text(
                        text = "Master your arsenal with comprehensive data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Perks Card - Active
            item {
                HubCategoryCard(
                    title = "PERKS",
                    subtitle = "Combat Specialties & Abilities",
                    icon = Icons.Filled.Star,
                    accentColor = Color(0xFFAB47BC), // Purple
                    isAvailable = true,
                    onClick = onNavigateToPerks
                )
            }

            // Weapons Card - Coming Soon
            item {
                HubCategoryCard(
                    title = "WEAPONS",
                    subtitle = "Arsenal & Attachments",
                    icon = Icons.Filled.Info,
                    accentColor = Color(0xFFFF6F00), // Deep Orange
                    isAvailable = false,
                    onClick = { }
                )
            }

            // Equipment Card - Coming Soon
            item {
                HubCategoryCard(
                    title = "EQUIPMENT",
                    subtitle = "Lethal & Tactical Gear",
                    icon = Icons.Filled.Warning,
                    accentColor = Color(0xFFE53935), // Red
                    isAvailable = false,
                    onClick = { }
                )
            }

            // Scorestreaks Card - Coming Soon
            item {
                HubCategoryCard(
                    title = "SCORESTREAKS",
                    subtitle = "Killstreak Rewards",
                    icon = Icons.Filled.AccountBox,
                    accentColor = Color(0xFF1E88E5), // Blue
                    isAvailable = false,
                    onClick = { }
                )
            }

            // Field Upgrades Card - Coming Soon
            item {
                HubCategoryCard(
                    title = "FIELD UPGRADES",
                    subtitle = "Deployable Abilities",
                    icon = Icons.Filled.Build,
                    accentColor = Color(0xFF43A047), // Green
                    isAvailable = false,
                    onClick = { }
                )
            }

            // Wildcards Card - Coming Soon
            item {
                HubCategoryCard(
                    title = "WILDCARDS",
                    subtitle = "Loadout Modifiers",
                    icon = Icons.Filled.Send,
                    accentColor = Color(0xFFFDD835), // Yellow
                    isAvailable = false,
                    onClick = { }
                )
            }

            // Banner Ad Space
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(12.dp)
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

/**
 * Category card for hub screen with glow animation
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HubCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isAvailable: Boolean,
    onClick: () -> Unit
) {
    // Animated glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderColor = if (isAvailable) accentColor.copy(alpha = glowAlpha) else Color.Gray.copy(alpha = 0.3f)
    val contentAlpha = if (isAvailable) 1f else 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                if (isAvailable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.3f * contentAlpha),
                                accentColor.copy(alpha = 0.1f * contentAlpha)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = accentColor.copy(alpha = contentAlpha)
                )
            }

            // Right side - Text content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = accentColor.copy(alpha = contentAlpha)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
                if (!isAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "COMING SOON",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.Gray
                    )
                }
            }

            // Arrow indicator (only for available items)
            if (isAvailable) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = accentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
