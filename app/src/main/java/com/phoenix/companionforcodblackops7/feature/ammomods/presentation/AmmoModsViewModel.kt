package com.phoenix.companionforcodblackops7.feature.ammomods.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.ammomods.domain.model.AmmoMod
import com.phoenix.companionforcodblackops7.feature.ammomods.domain.repository.AmmoModsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Ammo Mods screen
 */
sealed class AmmoModsUiState {
    data object Loading : AmmoModsUiState()
    data class Success(val ammoMods: List<AmmoMod>) : AmmoModsUiState()
    data class Error(val message: String) : AmmoModsUiState()
}

/**
 * ViewModel for managing Ammo Mods UI state
 */
@HiltViewModel
class AmmoModsViewModel @Inject constructor(
    private val ammoModsRepository: AmmoModsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AmmoModsUiState>(AmmoModsUiState.Loading)
    val uiState: StateFlow<AmmoModsUiState> = _uiState.asStateFlow()

    init {
        loadAmmoMods()
    }

    /**
     * Load all ammo mods from the repository
     */
    fun loadAmmoMods() {
        viewModelScope.launch {
            _uiState.value = AmmoModsUiState.Loading
            ammoModsRepository.getAllAmmoMods()
                .catch { exception ->
                    Timber.e(exception, "Failed to load ammo mods")
                    _uiState.value = AmmoModsUiState.Error(
                        exception.message ?: "Failed to load ammo mods"
                    )
                }
                .collect { ammoMods ->
                    _uiState.value = if (ammoMods.isEmpty()) {
                        AmmoModsUiState.Error("No ammo mods available")
                    } else {
                        AmmoModsUiState.Success(ammoMods)
                    }
                }
        }
    }
}
