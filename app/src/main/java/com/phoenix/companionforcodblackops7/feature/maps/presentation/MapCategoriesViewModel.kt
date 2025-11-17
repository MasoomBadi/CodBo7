package com.phoenix.companionforcodblackops7.feature.maps.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.maps.domain.repository.MapsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class MapCategoriesUiState {
    data object Loading : MapCategoriesUiState()
    data class Success(val categories: List<String>) : MapCategoriesUiState()
    data class Error(val message: String) : MapCategoriesUiState()
}

@HiltViewModel
class MapCategoriesViewModel @Inject constructor(
    private val mapsRepository: MapsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapCategoriesUiState>(MapCategoriesUiState.Loading)
    val uiState: StateFlow<MapCategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = MapCategoriesUiState.Loading
            mapsRepository.getAllMaps()
                .catch { e ->
                    Timber.e(e, "Error loading map categories")
                    _uiState.value = MapCategoriesUiState.Error(
                        e.message ?: "Failed to load map categories"
                    )
                }
                .collect { maps ->
                    // Extract unique categories by removing "_big" suffix
                    val categories = maps
                        .map { it.type.removeSuffix("_big") }
                        .distinct()
                        .sorted()

                    Timber.d("Loaded ${categories.size} categories: $categories")
                    _uiState.value = MapCategoriesUiState.Success(categories)
                }
        }
    }

    fun retry() {
        loadCategories()
    }
}
