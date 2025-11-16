package com.phoenix.companionforcodblackops7.feature.maps.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.maps.domain.model.GameMap
import com.phoenix.companionforcodblackops7.feature.maps.domain.repository.MapsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface MapListUiState {
    data object Loading : MapListUiState
    data class Success(val maps: List<GameMap>) : MapListUiState
    data class Error(val message: String) : MapListUiState
}

@HiltViewModel
class MapListViewModel @Inject constructor(
    private val mapsRepository: MapsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapListUiState>(MapListUiState.Loading)
    val uiState: StateFlow<MapListUiState> = _uiState.asStateFlow()

    init {
        loadMaps()
    }

    private fun loadMaps() {
        viewModelScope.launch {
            mapsRepository.getAllMaps()
                .catch { error ->
                    Timber.e(error, "Failed to load maps")
                    _uiState.value = MapListUiState.Error(
                        error.message ?: "Failed to load maps"
                    )
                }
                .collect { maps ->
                    _uiState.value = MapListUiState.Success(maps)
                }
        }
    }

    fun retry() {
        _uiState.value = MapListUiState.Loading
        loadMaps()
    }
}
