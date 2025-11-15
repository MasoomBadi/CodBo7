package com.phoenix.companionforcodblackops7.feature.operators.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phoenix.companionforcodblackops7.feature.operators.domain.model.Operator

private const val BASE_URL = "http://codbo7.masoombadi.top"

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun OperatorDetailsScreen(
    operator: Operator,
    iconMap: Map<String, String>,
    onNavigateBack: () -> Unit
) {
    val operatorImageUrl = if (operator.imageUrl.isNotEmpty()) {
        "$BASE_URL${operator.imageUrl}"
    } else null
    val divisionIconUrl = iconMap[operator.division.lowercase()]?.let { "$BASE_URL$it" }
    val zombieIconUrl = iconMap["zombie"]?.let { "$BASE_URL$it" }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = operator.shortName.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        modifier = Modifier.basicMarquee()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_revert),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero image section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    if (operatorImageUrl != null) {
                        AsyncImage(
                            model = operatorImageUrl,
                            contentDescription = operator.shortName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                )
                        )
                    }

                    // Badges at top right
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Division badge
                        if (divisionIconUrl != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = MaterialTheme.shapes.medium,
                                tonalElevation = 6.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    AsyncImage(
                                        model = divisionIconUrl,
                                        contentDescription = operator.division,
                                        modifier = Modifier.size(24.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = operator.division.uppercase(),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Zombie badge
                        if (operator.zombiePlayable && zombieIconUrl != null) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.tertiary,
                                tonalElevation = 6.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    AsyncImage(
                                        model = zombieIconUrl,
                                        contentDescription = "Zombie playable",
                                        modifier = Modifier.size(32.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }

                // Info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Name section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = operator.shortName.uppercase(),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            text = operator.fullName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.basicMarquee()
                        )
                    }

                    // Nationality
                    InfoCard(
                        title = "NATIONALITY",
                        content = operator.nationality
                    )

                    // Description
                    if (operator.description.isNotEmpty()) {
                        InfoCard(
                            title = "ABOUT",
                            content = operator.description
                        )
                    }

                    // Unlock criteria
                    if (operator.unlockCriteria.isNotEmpty()) {
                        InfoCard(
                            title = "UNLOCK REQUIREMENT",
                            content = operator.unlockCriteria,
                            accent = true
                        )
                    }
                }
            }

            // Banner Ad Space
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InfoCard(
    title: String,
    content: String,
    accent: Boolean = false
) {
    Surface(
        color = if (accent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = if (accent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    letterSpacing = 0.3.sp,
                    lineHeight = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OperatorsScreen(
    onNavigateBack: () -> Unit,
    onOperatorClick: (Operator, Map<String, String>) -> Unit,
    viewModel: OperatorsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Top App Bar
        OperatorsTopBar(onNavigateBack = onNavigateBack)

        // Content based on UI state
        Box(
            modifier = Modifier.weight(1f)
        ) {
            when (val state = uiState) {
                is OperatorsUiState.Loading -> {
                    LoadingContent()
                }
                is OperatorsUiState.Empty -> {
                    EmptyContent(onRetry = { viewModel.retry() })
                }
                is OperatorsUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.retry() }
                    )
                }
                is OperatorsUiState.Success -> {
                    OperatorsGrid(
                        operators = state.operators,
                        iconMap = state.iconMap,
                        onOperatorClick = { operator ->
                            onOperatorClick(operator, state.iconMap)
                        }
                    )
                }
            }
        }

        // Banner Ad Space at Bottom
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "Loading operators...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyContent(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "No Operators Found",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "The operators data hasn't been synced yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Error Loading Operators",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun OperatorsGrid(
    operators: List<Operator>,
    iconMap: Map<String, String>,
    onOperatorClick: (Operator) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(operators, key = { it.id }) { operator ->
            OperatorCard(
                operator = operator,
                iconMap = iconMap,
                onClick = { onOperatorClick(operator) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun OperatorsTopBar(onNavigateBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "OPERATORS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
private fun OperatorCard(
    operator: Operator,
    iconMap: Map<String, String>,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_${operator.id}")

    // Subtle shimmer animation for premium feel
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    // Subtle glow pulse for accent border
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Get URLs
    val divisionIconUrl = iconMap[operator.division.lowercase()]?.let { "$BASE_URL$it" }
    val zombieIconUrl = iconMap["zombie"]?.let { "$BASE_URL$it" }
    val operatorImageUrl = if (operator.imageUrl.isNotEmpty()) {
        "$BASE_URL${operator.imageUrl}"
    } else null

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Operator image background
            if (operatorImageUrl != null) {
                AsyncImage(
                    model = operatorImageUrl,
                    contentDescription = operator.shortName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Sophisticated gradient scrim - darker at edges, lighter in middle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.9f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Subtle primary color overlay for brand consistency
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                )
            } else {
                // Fallback: Dynamic gradient using theme colors
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        )
                )
            }

            // Subtle shimmer overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(shimmerAlpha)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Content with proper Material 3 spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top badges row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Division badge - Material 3 Surface with elevation
                    if (divisionIconUrl != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                            shape = MaterialTheme.shapes.small,
                            tonalElevation = 3.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                AsyncImage(
                                    model = divisionIconUrl,
                                    contentDescription = operator.division,
                                    modifier = Modifier.size(18.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = operator.division.uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Zombie badge - Bright, prominent with proper elevation
                    if (operator.zombiePlayable && zombieIconUrl != null) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.tertiary,
                            tonalElevation = 4.dp,
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                AsyncImage(
                                    model = zombieIconUrl,
                                    contentDescription = "Zombie playable",
                                    modifier = Modifier.size(28.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                // Bottom info with enhanced hierarchy
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Operator name - Bold, expressive typography with marquee
                    Text(
                        text = operator.shortName.uppercase(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )

                    // Full name - Supporting detail
                    Text(
                        text = operator.fullName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Nationality with subtle styling
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                        )
                        Text(
                            text = operator.nationality,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.4.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Subtle accent border with glow effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(glowAlpha * 0.5f)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                Color.Transparent,
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    )
            )
        }
    }
}
