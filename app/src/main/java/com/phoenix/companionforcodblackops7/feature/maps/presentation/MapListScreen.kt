package com.phoenix.companionforcodblackops7.feature.maps.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import coil.compose.rememberAsyncImagePainter
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapListScreen(
    onNavigateBack: () -> Unit,
    onMapClick: (GameMap) -> Unit,
    viewModel: MapListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "borderGlow")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TACTICAL MAPS",
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
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MapListUiState.Loading -> {
                    LoadingContent()
                }
                is MapListUiState.Success -> {
                    MapListContent(
                        maps = state.maps,
                        borderGlow = borderGlow,
                        onMapClick = onMapClick
                    )
                }
                is MapListUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.retry() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MapListContent(
    maps: List<GameMap>,
    borderGlow: Float,
    onMapClick: (GameMap) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrollable Map List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(maps) { map ->
                MapCard(
                    map = map,
                    borderGlow = borderGlow,
                    onClick = { onMapClick(map) }
                )
            }

            // Scroll indicator spacer
            item {
                Spacer(modifier = Modifier.height(8.dp))
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
private fun MapCard(
    map: GameMap,
    borderGlow: Float,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = borderGlow),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = borderGlow)
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image
            Image(
                painter = rememberAsyncImagePainter(
                    model = "http://codbo7.masoombadi.top${map.coverImageUrl}"
                ),
                contentDescription = "${map.displayName} cover",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 200f
                        )
                    )
            )

            // Map name at bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Accent line
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiary,
                            shape = MaterialTheme.shapes.small
                        )
                )

                // Map display name
                Text(
                    text = map.displayName.uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White
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
            LoadingIndicator(
                progress = { 0.5f },
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading maps...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error Loading Maps",
                style = MaterialTheme.typography.titleLarge.copy(
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
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Retry")
            }
        }
    }
}
