package com.phoenix.companionforcodblackops7.feature.combatspecialties.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.feature.combatspecialties.domain.model.CombatSpecialty

/**
 * Combat Specialties list screen showing all specialties organized by type
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CombatSpecialtiesListScreen(
    onNavigateBack: () -> Unit,
    viewModel: CombatSpecialtiesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "COMBAT SPECIALTIES",
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
                    titleContentColor = Color(0xFFEC407A) // Pink
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is CombatSpecialtiesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            is CombatSpecialtiesUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { viewModel.loadCombatSpecialties() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is CombatSpecialtiesUiState.Success -> {
                CombatSpecialtiesContent(
                    specialties = state.specialties,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

/**
 * Main content showing combat specialties organized by type
 */
@Composable
private fun CombatSpecialtiesContent(
    specialties: List<CombatSpecialty>,
    modifier: Modifier = Modifier
) {
    val specialtiesGroupedByType = specialties.groupBy { it.specialtyType.lowercase() }

    Column(
        modifier = modifier
    ) {
        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SPECIALTY BONUSES",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFFEC407A)
                    )
                    Text(
                        text = "Unlock powerful bonuses with perk combinations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Core Specialties Section
            specialtiesGroupedByType["core"]?.let { coreSpecialties ->
                item {
                    TypeSectionHeader(
                        title = "CORE SPECIALTIES",
                        subtitle = "Three perks of the same color",
                        color = Color(0xFFEC407A)
                    )
                }

                items(coreSpecialties) { specialty ->
                    CombatSpecialtyCard(
                        specialty = specialty,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Hybrid Specialties Section
            specialtiesGroupedByType["hybrid"]?.let { hybridSpecialties ->
                item {
                    TypeSectionHeader(
                        title = "HYBRID SPECIALTIES",
                        subtitle = "Mix two colors for unique combinations",
                        color = Color(0xFFEC407A)
                    )
                }

                items(hybridSpecialties) { specialty ->
                    CombatSpecialtyCard(
                        specialty = specialty,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Scroll indicator spacer
            item {
                Spacer(modifier = Modifier.height(8.dp))
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
 * Section header for specialty types
 */
@Composable
private fun TypeSectionHeader(
    title: String,
    subtitle: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                ),
                color = color
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/**
 * Individual combat specialty card with all information
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CombatSpecialtyCard(
    specialty: CombatSpecialty,
    modifier: Modifier = Modifier
) {
    // Animated glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "specialtyGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val categoryColor = specialty.getCategoryColorValue()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = categoryColor.copy(alpha = glowAlpha * 0.5f),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Icon and Name with Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    categoryColor.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "http://codbo7.masoombadi.top${specialty.iconUrl}",
                        contentDescription = specialty.displayName,
                        modifier = Modifier.size(70.dp)
                    )
                }

                // Name and Type
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = specialty.displayName.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = categoryColor
                    )

                    // Type Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.15f),
                        contentColor = categoryColor
                    ) {
                        Text(
                            text = specialty.getTypeLabel(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Required Perks Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(categoryColor)
                    )
                    Text(
                        text = "REQUIREMENTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = specialty.requiredPerks,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = categoryColor
                )
            }

            // Effect Description Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(categoryColor)
                    )
                    Text(
                        text = "SPECIALTY EFFECT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = specialty.effectDescription,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Color indicator bar at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                categoryColor,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
