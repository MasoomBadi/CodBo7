package com.phoenix.companionforcodblackops7.feature.gamemodes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.gamemodes.domain.model.GameMode
import com.phoenix.companionforcodblackops7.feature.gamemodes.domain.repository.GameModesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class GameModesUiState {
    data object Loading : GameModesUiState()
    data class Success(val gameModes: List<GameMode>) : GameModesUiState()
    data class Error(val message: String) : GameModesUiState()
}

@HiltViewModel
class GameModesViewModel @Inject constructor(
    private val gameModesRepository: GameModesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameModesUiState>(GameModesUiState.Loading)
    val uiState: StateFlow<GameModesUiState> = _uiState.asStateFlow()

    init {
        loadGameModes()
    }

    private fun loadGameModes() {
        viewModelScope.launch {
            _uiState.value = GameModesUiState.Loading
            gameModesRepository.getAllGameModes()
                .catch { e ->
                    Timber.e(e, "Error loading game modes")
                    _uiState.value = GameModesUiState.Error(
                        e.message ?: "Failed to load game modes"
                    )
                }
                .collect { gameModes ->
                    Timber.d("Loaded ${gameModes.size} game modes")
                    _uiState.value = GameModesUiState.Success(gameModes)
                }
        }
    }

    fun retry() {
        loadGameModes()
    }
}
