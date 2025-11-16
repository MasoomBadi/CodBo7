package com.phoenix.companionforcodblackops7.feature.maps.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapLayer
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.MapMarker
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
    val visibleLayerIds: Set<String> = emptySet(),
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
            _uiState.value = _uiState.value.copy(
                map = map,
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
                        layers.forEach { layer ->
                            Timber.d("Layer: id=${layer.id}, key=${layer.layerKey}, name=${layer.layerName}, parent=${layer.parentLayerId}")
                        }
                        markers.forEach { marker ->
                            Timber.d("Marker: id=${marker.id}, name=${marker.name}, category=${marker.category}, coords=(${marker.coordX}, ${marker.coordY})")
                        }

                        val defaultVisibleLayers = layers
                            .filter { it.isDefaultVisible }
                            .map { it.id }
                            .toSet()

                        Timber.d("Default visible layers: $defaultVisibleLayers")

                        _uiState.value = _uiState.value.copy(
                            layers = layers,
                            markers = markers,
                            visibleLayerIds = defaultVisibleLayers,
                            isLoading = false,
                            error = null
                        )
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

    fun toggleLayerVisibility(layerId: String) {
        val currentVisible = _uiState.value.visibleLayerIds
        val layers = _uiState.value.layers

        val isCurrentlyVisible = layerId in currentVisible

        if (isCurrentlyVisible) {
            val childLayerIds = layers
                .filter { it.parentLayerId == layerId }
                .map { it.id }
                .toSet()

            _uiState.value = _uiState.value.copy(
                visibleLayerIds = currentVisible - layerId - childLayerIds
            )
        } else {
            _uiState.value = _uiState.value.copy(
                visibleLayerIds = currentVisible + layerId
            )
        }
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
