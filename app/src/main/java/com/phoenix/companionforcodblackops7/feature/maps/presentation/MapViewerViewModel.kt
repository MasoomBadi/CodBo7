package com.phoenix.companionforcodblackops7.feature.maps.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapLayer
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapMarker
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapTile
import com.phoenix.companionforcodblackops7.feature.maps.domain.repository.MapsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class MapViewerState(
    val map: GameMap? = null,
    val layers: List<MapLayer> = emptyList(),
    val markers: List<MapMarker> = emptyList(),
    val tiles: List<MapTile> = emptyList(),
    val currentZoomLevel: Int = 1,
    val isTiledMap: Boolean = false,
    val layerControls: List<LayerControl> = emptyList(),
    val visibleControlIds: Set<String> = emptySet(),
    val expandedParentIds: Set<String> = emptySet(),
    val selectedMarker: MapMarker? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

@HiltViewModel
class MapViewerViewModel @Inject constructor(
    private val mapsRepository: MapsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapViewerState())
    val uiState: StateFlow<MapViewerState> = _uiState.asStateFlow()

    fun loadMap(map: GameMap) {
        viewModelScope.launch {
            // Detect if this is a tiled map based on type
            val isTiled = map.type == "zombie_big"

            _uiState.value = _uiState.value.copy(
                map = map,
                isTiledMap = isTiled,
                isLoading = true,
                error = null
            )

            try {
                combine(
                    mapsRepository.getLayersForMap(map.id),
                    mapsRepository.getMarkersForMap(map.id)
                ) { layers, markers ->
                    Pair(layers, markers)
                }
                    .catch { error ->
                        Timber.e(error, "Failed to load map data")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load map data"
                        )
                    }
                    .collect { (layers, markers) ->
                        Timber.d("Loaded ${layers.size} layers and ${markers.size} markers for map ${map.id}")

                        // Build hierarchical layer controls
                        val controls = buildLayerControls(layers, markers)

                        // Default: all controls visible
                        val defaultVisible = controls.map { it.id }.toSet()

                        // Default: all parents expanded
                        val allParents = controls.filter { it.parentId == null }.map { it.id }.toSet()

                        Timber.d("Built ${controls.size} layer controls, all visible by default")

                        _uiState.value = _uiState.value.copy(
                            layers = layers,
                            markers = markers,
                            layerControls = controls,
                            visibleControlIds = defaultVisible,
                            expandedParentIds = allParents,
                            isLoading = false,
                            error = null
                        )

                        // If tiled map, load initial tiles for zoom level 1
                        if (isTiled) {
                            loadTilesForZoomLevel(1)
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load map data")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load map data"
                )
            }
        }
    }

    fun loadTilesForZoomLevel(zoomLevel: Int) {
        val mapId = _uiState.value.map?.id ?: return

        viewModelScope.launch {
            try {
                mapsRepository.getTilesForMap(mapId, zoomLevel)
                    .catch { error ->
                        Timber.e(error, "Failed to load tiles for zoom $zoomLevel")
                    }
                    .collect { tiles ->
                        Timber.d("Loaded ${tiles.size} tiles for zoom level $zoomLevel")
                        _uiState.value = _uiState.value.copy(
                            tiles = tiles,
                            currentZoomLevel = zoomLevel
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load tiles for zoom $zoomLevel")
            }
        }
    }

    fun updateZoomLevel(scale: Float) {
        if (!_uiState.value.isTiledMap) return

        val newZoomLevel = calculateZoomLevel(scale)
        if (newZoomLevel != _uiState.value.currentZoomLevel) {
            loadTilesForZoomLevel(newZoomLevel)
        }
    }

    private fun calculateZoomLevel(scale: Float): Int {
        return when {
            scale < 1.0f -> 1
            scale < 1.5f -> 2
            scale < 2.5f -> 3
            scale < 4.0f -> 4
            else -> 5
        }
    }

    fun toggleParentExpansion(parentId: String) {
        val currentExpanded = _uiState.value.expandedParentIds
        _uiState.value = _uiState.value.copy(
            expandedParentIds = if (parentId in currentExpanded) {
                currentExpanded - parentId
            } else {
                currentExpanded + parentId
            }
        )
    }

    fun toggleLayerVisibility(controlId: String) {
        val currentVisible = _uiState.value.visibleControlIds
        val controls = _uiState.value.layerControls

        val isCurrentlyVisible = controlId in currentVisible

        if (isCurrentlyVisible) {
            // Turn off this control and all its children
            val childControlIds = controls
                .filter { it.parentId == controlId }
                .map { it.id }
                .toSet()

            _uiState.value = _uiState.value.copy(
                visibleControlIds = currentVisible - controlId - childControlIds
            )
        } else {
            // Turn on this control
            _uiState.value = _uiState.value.copy(
                visibleControlIds = currentVisible + controlId
            )
        }
    }

    private fun buildLayerControls(layers: List<MapLayer>, markers: List<MapMarker>): List<LayerControl> {
        val controls = mutableListOf<LayerControl>()

        // Main Spawn Location (no parent)
        controls.add(LayerControl(
            id = "spawn",
            displayName = "Main Spawn Location",
            type = LayerControlType.MARKER_CATEGORY,
            markerCategory = "multiplayer_mainSpawnLocation"
        ))

        // POI parent
        controls.add(LayerControl(
            id = "poi",
            displayName = "POI",
            type = LayerControlType.MARKER_CATEGORY
        ))
        // POI Label Primary child
        controls.add(LayerControl(
            id = "poi_label",
            displayName = "POI Label Primary",
            type = LayerControlType.MARKER_CATEGORY,
            parentId = "poi",
            markerCategory = "poiLabel"
        ))

        // Objective parent
        controls.add(LayerControl(
            id = "objective",
            displayName = "Objective",
            type = LayerControlType.MARKER_CATEGORY
        ))
        // Objective children
        controls.add(LayerControl(
            id = "obj_dom",
            displayName = "Domination",
            type = LayerControlType.MARKER_CATEGORY,
            parentId = "objective",
            markerCategory = "multiplayer_objective_domination"
        ))
        controls.add(LayerControl(
            id = "obj_hp",
            displayName = "Hardpoint",
            type = LayerControlType.MARKER_CATEGORY,
            parentId = "objective",
            markerCategory = "multiplayer_objective_hardpoint"
        ))
        controls.add(LayerControl(
            id = "obj_snd",
            displayName = "Search and Destroy",
            type = LayerControlType.MARKER_CATEGORY,
            parentId = "objective",
            markerCategory = "multiplayer_objective_searchAndDestroy"
        ))

        // Layers parent
        controls.add(LayerControl(
            id = "layers",
            displayName = "Layers",
            type = LayerControlType.MAP_LAYER
        ))
        // Add actual map layers as children
        layers.forEach { layer ->
            controls.add(LayerControl(
                id = "layer_${layer.id}",
                displayName = layer.layerName,
                type = LayerControlType.MAP_LAYER,
                parentId = "layers",
                layerKey = layer.layerKey
            ))
        }

        return controls
    }

    fun selectMarker(marker: MapMarker?) {
        _uiState.value = _uiState.value.copy(selectedMarker = marker)
    }

    fun updateTransform(scale: Float, offsetX: Float, offsetY: Float) {
        _uiState.value = _uiState.value.copy(
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY
        )
    }
}
