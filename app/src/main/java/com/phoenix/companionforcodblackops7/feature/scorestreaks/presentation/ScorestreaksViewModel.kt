package com.phoenix.companionforcodblackops7.feature.scorestreaks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.scorestreaks.domain.model.Scorestreak
import com.phoenix.companionforcodblackops7.feature.scorestreaks.domain.repository.ScorestreaksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Scorestreaks screen
 */
sealed class ScorestreaksUiState {
    data object Loading : ScorestreaksUiState()
    data class Success(val scorestreaks: List<Scorestreak>) : ScorestreaksUiState()
    data class Error(val message: String) : ScorestreaksUiState()
}

/**
 * ViewModel for managing Scorestreaks UI state
 */
@HiltViewModel
class ScorestreaksViewModel @Inject constructor(
    private val scorestreaksRepository: ScorestreaksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScorestreaksUiState>(ScorestreaksUiState.Loading)
    val uiState: StateFlow<ScorestreaksUiState> = _uiState.asStateFlow()

    init {
        loadScorestreaks()
    }

    /**
     * Load all scorestreaks from the repository
     */
    fun loadScorestreaks() {
        viewModelScope.launch {
            _uiState.value = ScorestreaksUiState.Loading
            scorestreaksRepository.getAllScorestreaks()
                .catch { exception ->
                    Timber.e(exception, "Failed to load scorestreaks")
                    _uiState.value = ScorestreaksUiState.Error(
                        exception.message ?: "Failed to load scorestreaks"
                    )
                }
                .collect { scorestreaks ->
                    _uiState.value = if (scorestreaks.isEmpty()) {
                        ScorestreaksUiState.Error("No scorestreaks available")
                    } else {
                        ScorestreaksUiState.Success(scorestreaks)
                    }
                }
        }
    }
}
