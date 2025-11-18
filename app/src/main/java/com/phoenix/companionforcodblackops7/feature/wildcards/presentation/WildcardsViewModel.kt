package com.phoenix.companionforcodblackops7.feature.wildcards.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.wildcards.domain.model.Wildcard
import com.phoenix.companionforcodblackops7.feature.wildcards.domain.repository.WildcardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Wildcards screen
 */
sealed class WildcardsUiState {
    data object Loading : WildcardsUiState()
    data class Success(val wildcards: List<Wildcard>) : WildcardsUiState()
    data class Error(val message: String) : WildcardsUiState()
}

/**
 * ViewModel for managing Wildcards UI state
 */
@HiltViewModel
class WildcardsViewModel @Inject constructor(
    private val wildcardsRepository: WildcardsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WildcardsUiState>(WildcardsUiState.Loading)
    val uiState: StateFlow<WildcardsUiState> = _uiState.asStateFlow()

    init {
        loadWildcards()
    }

    /**
     * Load all wildcards from the repository
     */
    fun loadWildcards() {
        viewModelScope.launch {
            _uiState.value = WildcardsUiState.Loading
            wildcardsRepository.getAllWildcards()
                .catch { exception ->
                    Timber.e(exception, "Failed to load wildcards")
                    _uiState.value = WildcardsUiState.Error(
                        exception.message ?: "Failed to load wildcards"
                    )
                }
                .collect { wildcards ->
                    _uiState.value = if (wildcards.isEmpty()) {
                        WildcardsUiState.Error("No wildcards available")
                    } else {
                        WildcardsUiState.Success(wildcards)
                    }
                }
        }
    }
}
