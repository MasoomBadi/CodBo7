package com.phoenix.companionforcodblackops7.feature.masterybadge.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.BadgeProgress
import android.widget.Toast

private val ACCENT_COLOR = Color(0xFFFFB300) // Gold color for mastery badges

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeaponMasteryScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeaponMasteryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMode by remember { mutableStateOf("mp") } // "mp" or "zm"
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        when (val state = uiState) {
                            is WeaponMasteryUiState.Success -> {
                                Text(
                                    text = state.progress.weaponName.uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                )
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
                        titleContentColor = ACCENT_COLOR
                    )
                )

                // Progress section
                if (uiState is WeaponMasteryUiState.Success) {
                    val state = uiState as WeaponMasteryUiState.Success
                    val progress = state.progress.percentage / 100f

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Weapon category badge
                            Surface(
                                color = ACCENT_COLOR.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = state.progress.weaponCategory.uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = ACCENT_COLOR,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${state.progress.unlockedBadgesCount}/${state.progress.totalBadges} BADGES UNLOCKED",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${state.progress.percentage.toInt()}%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ACCENT_COLOR
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ACCENT_COLOR,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // Mode tabs
                if (uiState is WeaponMasteryUiState.Success) {
                    ModeTabRow(
                        selectedMode = selectedMode,
                        onModeSelected = { selectedMode = it }
                    )
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
            is WeaponMasteryUiState.Success -> {
                // Select badges based on selected mode
                val badges = if (selectedMode == "mp") state.progress.mpBadges else state.progress.zmBadges
                val currentKills = if (selectedMode == "mp") state.progress.mpKills else state.progress.zmKills

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Badge cards for selected mode
                        badges.forEach { badgeProgress ->
                            item(key = badgeProgress.badge.id) {
                                BadgeCard(
                                    badgeProgress = badgeProgress,
                                    onToggle = {
                                        if (badgeProgress.isLocked) {
                                            Toast.makeText(
                                                context,
                                                "Complete previous badges first",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            // Toggle badge by setting kills to required amount or 0
                                            val newKills = if (badgeProgress.isUnlocked) 0 else badgeProgress.requiredKills
                                            if (selectedMode == "mp") {
                                                viewModel.updateMpKills(newKills)
                                            } else {
                                                viewModel.updateZmKills(newKills)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Fixed Banner Ad Space
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
            is WeaponMasteryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeTabRow(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = if (selectedMode == "mp") 0 else 1,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = ACCENT_COLOR,
        modifier = modifier
    ) {
        Tab(
            selected = selectedMode == "mp",
            onClick = { onModeSelected("mp") },
            text = {
                Text(
                    text = "MULTIPLAYER",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        )
        Tab(
            selected = selectedMode == "zm",
            onClick = { onModeSelected("zm") },
            text = {
                Text(
                    text = "ZOMBIE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BadgeCard(
    badgeProgress: BadgeProgress,
    onToggle: () -> Unit
) {
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

    val borderColor = if (badgeProgress.isUnlocked) {
        ACCENT_COLOR.copy(alpha = glowAlpha * 0.7f)
    } else if (badgeProgress.isLocked) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Card(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (badgeProgress.isUnlocked) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (badgeProgress.isUnlocked) {
                MaterialTheme.colorScheme.surface
            } else if (badgeProgress.isLocked) {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (badgeProgress.isUnlocked) 4.dp else 0.dp),
        enabled = !badgeProgress.isLocked
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge icon
            Surface(
                shape = CircleShape,
                color = if (badgeProgress.isUnlocked) {
                    ACCENT_COLOR
                } else if (badgeProgress.isLocked) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (badgeProgress.isUnlocked) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Unlocked",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    } else if (badgeProgress.isLocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Not unlocked",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Badge description
            Text(
                text = "GET ${badgeProgress.requiredKills} KILLS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                ),
                color = if (badgeProgress.isUnlocked) {
                    ACCENT_COLOR
                } else if (badgeProgress.isLocked) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                },
                modifier = Modifier.weight(1f)
            )

            // Checkbox
            Checkbox(
                checked = badgeProgress.isUnlocked,
                onCheckedChange = { onToggle() },
                enabled = !badgeProgress.isLocked,
                colors = CheckboxDefaults.colors(
                    checkedColor = ACCENT_COLOR,
                    uncheckedColor = if (badgeProgress.isLocked) {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            )
        }
    }
}
