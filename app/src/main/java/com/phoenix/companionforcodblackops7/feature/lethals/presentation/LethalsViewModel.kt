package com.phoenix.companionforcodblackops7.feature.lethals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.lethals.domain.model.Lethal
import com.phoenix.companionforcodblackops7.feature.lethals.domain.repository.LethalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for Lethals screen
 */
sealed class LethalsUiState {
    data object Loading : LethalsUiState()
    data class Success(val lethals: List<Lethal>) : LethalsUiState()
    data class Error(val message: String) : LethalsUiState()
}

/**
 * ViewModel for managing Lethals UI state
 */
@HiltViewModel
class LethalsViewModel @Inject constructor(
    private val lethalsRepository: LethalsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LethalsUiState>(LethalsUiState.Loading)
    val uiState: StateFlow<LethalsUiState> = _uiState.asStateFlow()

    init {
        loadLethals()
    }

    /**
     * Load all lethals from the repository
     */
    fun loadLethals() {
        viewModelScope.launch {
            _uiState.value = LethalsUiState.Loading
            lethalsRepository.getAllLethals()
                .catch { exception ->
                    Timber.e(exception, "Failed to load lethals")
                    _uiState.value = LethalsUiState.Error(
                        exception.message ?: "Failed to load lethals"
                    )
                }
                .collect { lethals ->
                    _uiState.value = if (lethals.isEmpty()) {
                        LethalsUiState.Error("No lethals available")
                    } else {
                        LethalsUiState.Success(lethals)
                    }
                }
        }
    }
}
