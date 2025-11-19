package com.phoenix.companionforcodblackops7.feature.powerups.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.powerups.domain.model.PowerUp
import com.phoenix.companionforcodblackops7.feature.powerups.domain.repository.PowerUpsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Power-Ups screen
 */
sealed class PowerUpsUiState {
    data object Loading : PowerUpsUiState()
    data class Success(
        val powerUps: List<PowerUp>,
        val expandedId: Int? = null
    ) : PowerUpsUiState()
    data class Error(val message: String) : PowerUpsUiState()
}

/**
 * ViewModel for Power-Ups (Zombie mode) screen
 */
@HiltViewModel
class PowerUpsViewModel @Inject constructor(
    private val repository: PowerUpsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PowerUpsUiState>(PowerUpsUiState.Loading)
    val uiState: StateFlow<PowerUpsUiState> = _uiState.asStateFlow()

    init {
        loadPowerUps()
    }

    private fun loadPowerUps() {
        viewModelScope.launch {
            repository.getPowerUps()
                .catch { e ->
                    Timber.e(e, "Error loading power-ups")
                    _uiState.value = PowerUpsUiState.Error(
                        e.message ?: "Failed to load power-ups"
                    )
                }
                .collect { powerUps ->
                    _uiState.value = PowerUpsUiState.Success(powerUps = powerUps)
                }
        }
    }

    /**
     * Toggle expansion state of a power-up card
     */
    fun toggleExpanded(powerUpId: Int) {
        val currentState = _uiState.value
        if (currentState is PowerUpsUiState.Success) {
            _uiState.value = currentState.copy(
                expandedId = if (currentState.expandedId == powerUpId) null else powerUpId
            )
        }
    }
}
