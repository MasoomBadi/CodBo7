package com.phoenix.companionforcodblackops7.feature.maps.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapMarker
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapViewerScreen(
    map: GameMap,
    onNavigateBack: () -> Unit,
    viewModel: MapViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(map) {
        viewModel.loadMap(map)
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)

        val maxOffsetX = (canvasSize.width * (scale - 1) / 2f).coerceAtLeast(0f)
        val maxOffsetY = (canvasSize.height * (scale - 1) / 2f).coerceAtLeast(0f)

        offsetX = (offsetX + panChange.x).coerceIn(-maxOffsetX, maxOffsetX)
        offsetY = (offsetY + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            LayerControlDrawer(
                layers = uiState.layers,
                visibleLayerIds = uiState.visibleLayerIds,
                onToggleLayer = { layerId -> viewModel.toggleLayerVisibility(layerId) },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = map.displayName.uppercase(),
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
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Toggle Layers",
                                tint = MaterialTheme.colorScheme.primary
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
                when {
                    uiState.isLoading -> {
                        LoadingContent()
                    }
                    uiState.error != null -> {
                        ErrorContent(message = uiState.error!!)
                    }
                    uiState.map != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .onSizeChanged { canvasSize = it }
                                    .transformable(state = transformableState)
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures { tapOffset ->
                                            val marker = findMarkerAtPosition(
                                                tapOffset = tapOffset,
                                                markers = uiState.markers,
                                                layers = uiState.layers,
                                                visibleLayerIds = uiState.visibleLayerIds,
                                                canvasSize = canvasSize,
                                                mapBounds = map.bounds,
                                                scale = scale,
                                                offsetX = offsetX,
                                                offsetY = offsetY
                                            )
                                            viewModel.selectMarker(marker)
                                        }
                                    }
                            ) {
                                MapCanvas(
                                    map = map,
                                    layers = uiState.layers,
                                    markers = uiState.markers,
                                    visibleLayerIds = uiState.visibleLayerIds,
                                    canvasSize = canvasSize
                                )
                            }

                            AnimatedVisibility(
                                visible = uiState.selectedMarker != null,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                uiState.selectedMarker?.let { marker ->
                                    MarkerDetailCard(
                                        marker = marker,
                                        onDismiss = { viewModel.selectMarker(null) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapCanvas(
    map: GameMap,
    layers: List<com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapLayer>,
    markers: List<MapMarker>,
    visibleLayerIds: Set<String>,
    canvasSize: IntSize
) {
    val visibleLayers = layers.filter { it.id in visibleLayerIds }

    // Create a map of layer keys to layer IDs for visible layers
    val visibleLayerKeyToId = layers
        .filter { it.id in visibleLayerIds }
        .associate { it.layerKey to it.id }

    // Match markers to layers using category -> layerKey relationship
    val visibleMarkers = markers.filter { marker ->
        when {
            layers.isEmpty() -> true
            marker.category.isEmpty() -> true
            visibleLayerKeyToId.keys.any { it.contains(marker.markerType) || marker.category.contains(it) } -> true
            else -> false
        }
    }

    LaunchedEffect(visibleLayers.size, visibleMarkers.size, canvasSize) {
        Timber.d("MapCanvas: ${visibleLayers.size} visible layers, ${visibleMarkers.size} visible markers, canvas size: $canvasSize")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(
                model = "http://codbo7.masoombadi.top${map.baseImageUrl}"
            ),
            contentDescription = "${map.displayName} base map",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        visibleLayers.forEach { layer ->
            Image(
                painter = rememberAsyncImagePainter(
                    model = "http://codbo7.masoombadi.top${layer.imageUrl}"
                ),
                contentDescription = layer.layerName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                alpha = 0.7f
            )
        }

        visibleMarkers.forEach { marker ->
                if (canvasSize.width > 0 && canvasSize.height > 0) {
                    val markerPosition = calculateMarkerPosition(
                        marker = marker,
                        canvasSize = canvasSize,
                        mapBounds = map.bounds
                    )

                    Box(
                        modifier = Modifier
                            .offset(
                                x = markerPosition.x.dp,
                                y = markerPosition.y.dp
                            )
                            .size(32.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "http://codbo7.masoombadi.top${marker.iconUrl}"
                            ),
                            contentDescription = marker.label,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
    }
}

private fun calculateMarkerPosition(
    marker: MapMarker,
    canvasSize: IntSize,
    mapBounds: com.phoenix.companionforcodblackops7.feature.maps.domain.model.Bounds
): Offset {
    val mapWidth = mapBounds.northeastX - mapBounds.southwestX
    val mapHeight = mapBounds.northeastY - mapBounds.southwestY

    val normalizedX = (marker.coordX - mapBounds.southwestX).toFloat() / mapWidth
    val normalizedY = (marker.coordY - mapBounds.southwestY).toFloat() / mapHeight

    return Offset(
        x = normalizedX * canvasSize.width - 16f,
        y = normalizedY * canvasSize.height - 16f
    )
}

private fun findMarkerAtPosition(
    tapOffset: Offset,
    markers: List<MapMarker>,
    layers: List<com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapLayer>,
    visibleLayerIds: Set<String>,
    canvasSize: IntSize,
    mapBounds: com.phoenix.companionforcodblackops7.feature.maps.domain.model.Bounds,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): MapMarker? {
    val adjustedTapX = (tapOffset.x - offsetX) / scale
    val adjustedTapY = (tapOffset.y - offsetY) / scale

    // Create a map of layer keys to layer IDs for visible layers
    val visibleLayerKeyToId = layers
        .filter { it.id in visibleLayerIds }
        .associate { it.layerKey to it.id }

    // Match markers to layers using category -> layerKey relationship
    return markers
        .filter { marker ->
            when {
                layers.isEmpty() -> true
                marker.category.isEmpty() -> true
                visibleLayerKeyToId.keys.any { it.contains(marker.markerType) || marker.category.contains(it) } -> true
                else -> false
            }
        }
        .firstOrNull { marker ->
            val markerPos = calculateMarkerPosition(marker, canvasSize, mapBounds)
            val distance = kotlin.math.sqrt(
                (adjustedTapX - markerPos.x).let { it * it } +
                (adjustedTapY - markerPos.y).let { it * it }
            )
            distance < 32f
        }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MarkerDetailCard(
    marker: MapMarker,
    onDismiss: () -> Unit
) {
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = "http://codbo7.masoombadi.top${marker.iconUrl}"
                        ),
                        contentDescription = marker.name,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = marker.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = marker.markerType.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }


            Text(
                text = "Tap to close",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LayerControlDrawer(
    layers: List<com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapLayer>,
    visibleLayerIds: Set<String>,
    onToggleLayer: (String) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(320.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiary,
                            shape = MaterialTheme.shapes.small
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "MAP LAYERS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val parentLayers = layers.filter { it.parentLayerId == null }

                if (parentLayers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No layers available for this map",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                parentLayers.forEach { parent ->
                    item {
                        LayerToggleItem(
                            layer = parent,
                            isVisible = parent.id in visibleLayerIds,
                            onToggle = { onToggleLayer(parent.id) },
                            isIndented = false,
                            isEnabled = true
                        )
                    }

                    val childLayers = layers.filter { it.parentLayerId == parent.id }
                    childLayers.forEach { child ->
                        item {
                            LayerToggleItem(
                                layer = child,
                                isVisible = child.id in visibleLayerIds,
                                onToggle = { onToggleLayer(child.id) },
                                isIndented = true,
                                isEnabled = parent.id in visibleLayerIds
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerToggleItem(
    layer: com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapLayer,
    isVisible: Boolean,
    onToggle: () -> Unit,
    isIndented: Boolean = false,
    isEnabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isIndented) 24.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isEnabled -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f)
                isVisible -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        onClick = { if (isEnabled) onToggle() },
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = layer.layerName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = when {
                        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        isVisible -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = layer.layerType.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = when {
                        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        isVisible -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !isEnabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            isVisible -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isVisible && isEnabled) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Layer visible",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
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
                text = "Loading map...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
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
                text = "Error Loading Map",
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
        }
    }
}
