package com.phoenix.companionforcodblackops7.feature.maps.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.unit.IntOffset
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
                layerControls = uiState.layerControls,
                visibleControlIds = uiState.visibleControlIds,
                expandedParentIds = uiState.expandedParentIds,
                onToggleControl = { controlId -> viewModel.toggleLayerVisibility(controlId) },
                onToggleExpansion = { parentId -> viewModel.toggleParentExpansion(parentId) },
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
                                                layerControls = uiState.layerControls,
                                                visibleControlIds = uiState.visibleControlIds,
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
                                    layerControls = uiState.layerControls,
                                    visibleControlIds = uiState.visibleControlIds,
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
    layerControls: List<LayerControl>,
    visibleControlIds: Set<String>,
    canvasSize: IntSize
) {
    // Get visible marker categories from controls
    val visibleMarkerCategories = layerControls
        .filter { it.id in visibleControlIds && it.markerCategory != null }
        .mapNotNull { it.markerCategory }
        .toSet()

    // Get visible layer keys from controls
    val visibleLayerKeys = layerControls
        .filter { it.id in visibleControlIds && it.layerKey != null }
        .mapNotNull { it.layerKey }
        .toSet()

    // Filter layers
    val visibleLayers = layers.filter { it.layerKey in visibleLayerKeys }

    // Filter markers by category
    val visibleMarkers = markers.filter { it.category in visibleMarkerCategories }

    LaunchedEffect(visibleLayers.size, visibleMarkers.size, canvasSize) {
        Timber.d("MapCanvas: ${visibleLayers.size} visible layers, ${visibleMarkers.size} visible markers (from ${visibleMarkerCategories.size} categories), canvas size: $canvasSize")
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
                            .offset {
                                IntOffset(
                                    x = markerPosition.x.toInt(),
                                    y = markerPosition.y.toInt()
                                )
                            }
                            .size(32.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "http://codbo7.masoombadi.top${marker.iconUrl}"
                            ),
                            contentDescription = marker.name,
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
    if (canvasSize.width == 0 || canvasSize.height == 0) {
        return Offset.Zero
    }

    val mapWidth = (mapBounds.northeastX - mapBounds.southwestX).toFloat()
    val mapHeight = (mapBounds.northeastY - mapBounds.southwestY).toFloat()

    // Normalize coordinates (0.0 to 1.0)
    // Coordinates are from webscript, already using top-left origin like Compose
    val normalizedX = (marker.coordX - mapBounds.southwestX).toFloat() / mapWidth
    val normalizedY = (marker.coordY - mapBounds.southwestY).toFloat() / mapHeight

    // Calculate the aspect ratio of the map and canvas
    val mapAspectRatio = mapWidth / mapHeight
    val canvasAspectRatio = canvasSize.width.toFloat() / canvasSize.height.toFloat()

    // Determine the actual image bounds within the canvas (accounting for ContentScale.Fit)
    val imageWidth: Float
    val imageHeight: Float
    val imageOffsetX: Float
    val imageOffsetY: Float

    if (canvasAspectRatio > mapAspectRatio) {
        // Canvas is wider - image will have letterboxing on left/right
        imageHeight = canvasSize.height.toFloat()
        imageWidth = imageHeight * mapAspectRatio
        imageOffsetX = (canvasSize.width - imageWidth) / 2f
        imageOffsetY = 0f
    } else {
        // Canvas is taller - image will have pillarboxing on top/bottom
        imageWidth = canvasSize.width.toFloat()
        imageHeight = imageWidth / mapAspectRatio
        imageOffsetX = 0f
        imageOffsetY = (canvasSize.height - imageHeight) / 2f
    }

    // Calculate marker position within the actual image bounds
    val markerX = imageOffsetX + (normalizedX * imageWidth) - 16f
    val markerY = imageOffsetY + (normalizedY * imageHeight) - 16f

    Timber.d("Marker '${marker.name}': coords=(${marker.coordX},${marker.coordY}), normalized=($normalizedX,$normalizedY), position=($markerX,$markerY)")

    return Offset(
        x = markerX,
        y = markerY
    )
}

private fun findMarkerAtPosition(
    tapOffset: Offset,
    markers: List<MapMarker>,
    layerControls: List<LayerControl>,
    visibleControlIds: Set<String>,
    canvasSize: IntSize,
    mapBounds: com.phoenix.companionforcodblackops7.feature.maps.domain.model.Bounds,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): MapMarker? {
    val adjustedTapX = (tapOffset.x - offsetX) / scale
    val adjustedTapY = (tapOffset.y - offsetY) / scale

    // Get visible marker categories from controls
    val visibleMarkerCategories = layerControls
        .filter { it.id in visibleControlIds && it.markerCategory != null }
        .mapNotNull { it.markerCategory }
        .toSet()

    // Filter markers by category
    return markers
        .filter { it.category in visibleMarkerCategories }
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

@Composable
private fun ParentControlItem(
    control: LayerControl,
    isVisible: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onExpandToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isVisible) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandToggle() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.ExpandMore
                    } else {
                        Icons.AutoMirrored.Filled.ChevronRight
                    },
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isVisible) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = control.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isVisible) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Checkbox(
                checked = isVisible,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@Composable
private fun ChildControlItem(
    control: LayerControl,
    isVisible: Boolean,
    onToggle: () -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isEnabled -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f)
                isVisible -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = isEnabled) { if (isEnabled) onToggle() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .background(
                            if (isEnabled && isVisible) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            },
                            shape = MaterialTheme.shapes.small
                        )
                )

                Text(
                    text = control.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = when {
                        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        isVisible -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Checkbox(
                checked = isVisible,
                onCheckedChange = { onToggle() },
                enabled = isEnabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.secondary,
                    uncheckedColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledCheckedColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    disabledUncheckedColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LayerControlDrawer(
    layerControls: List<LayerControl>,
    visibleControlIds: Set<String>,
    expandedParentIds: Set<String>,
    onToggleControl: (String) -> Unit,
    onToggleExpansion: (String) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(340.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(28.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "MAP LAYERS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val parentControls = layerControls.filter { it.parentId == null }

                parentControls.forEach { parent ->
                    item {
                        ParentControlItem(
                            control = parent,
                            isVisible = parent.id in visibleControlIds,
                            isExpanded = parent.id in expandedParentIds,
                            onToggle = { onToggleControl(parent.id) },
                            onExpandToggle = { onToggleExpansion(parent.id) }
                        )
                    }

                    val childControls = layerControls.filter { it.parentId == parent.id }
                    if (parent.id in expandedParentIds && childControls.isNotEmpty()) {
                        childControls.forEach { child ->
                            item {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    ChildControlItem(
                                        control = child,
                                        isVisible = child.id in visibleControlIds,
                                        onToggle = { onToggleControl(child.id) },
                                        isEnabled = parent.id in visibleControlIds
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
