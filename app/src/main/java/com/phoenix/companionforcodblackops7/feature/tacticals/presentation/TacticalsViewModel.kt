package com.phoenix.companionforcodblackops7.feature.tacticals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.tacticals.domain.model.Tactical
import com.phoenix.companionforcodblackops7.feature.tacticals.domain.repository.TacticalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Tacticals screen
 */
sealed class TacticalsUiState {
    data object Loading : TacticalsUiState()
    data class Success(val tacticals: List<Tactical>) : TacticalsUiState()
    data class Error(val message: String) : TacticalsUiState()
}

/**
 * ViewModel for managing Tacticals UI state
 */
@HiltViewModel
class TacticalsViewModel @Inject constructor(
    private val tacticalsRepository: TacticalsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TacticalsUiState>(TacticalsUiState.Loading)
    val uiState: StateFlow<TacticalsUiState> = _uiState.asStateFlow()

    init {
        loadTacticals()
    }

    /**
     * Load all tacticals from the repository
     */
    fun loadTacticals() {
        viewModelScope.launch {
            _uiState.value = TacticalsUiState.Loading
            tacticalsRepository.getAllTacticals()
                .catch { exception ->
                    Timber.e(exception, "Failed to load tacticals")
                    _uiState.value = TacticalsUiState.Error(
                        exception.message ?: "Failed to load tacticals"
                    )
                }
                .collect { tacticals ->
                    _uiState.value = if (tacticals.isEmpty()) {
                        TacticalsUiState.Error("No tacticals available")
                    } else {
                        TacticalsUiState.Success(tacticals)
                    }
                }
        }
    }
}
