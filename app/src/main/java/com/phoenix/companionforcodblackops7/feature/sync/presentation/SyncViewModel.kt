package com.phoenix.companionforcodblackops7.feature.sync.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.core.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SyncUiState {
    data object Idle : SyncUiState
    data class Loading(val message: String) : SyncUiState
    data object Success : SyncUiState
    data class Error(val error: String) : SyncUiState
}

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    fun startSync() {
        viewModelScope.launch {
            _uiState.value = SyncUiState.Loading("Initializing...")

            syncRepository.checkAndSync { progress ->
                _uiState.value = SyncUiState.Loading(progress)
            }.onSuccess {
                _uiState.value = SyncUiState.Success
            }.onFailure { error ->
                _uiState.value = SyncUiState.Error(
                    error.message ?: "Unknown error occurred"
                )
            }
        }
    }
}
