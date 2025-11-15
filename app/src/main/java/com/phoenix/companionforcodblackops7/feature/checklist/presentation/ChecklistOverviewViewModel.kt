package com.phoenix.companionforcodblackops7.feature.checklist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.checklist.domain.model.ChecklistProgress
import com.phoenix.companionforcodblackops7.feature.checklist.domain.repository.ChecklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ChecklistOverviewUiState {
    data object Loading : ChecklistOverviewUiState
    data class Success(val progress: ChecklistProgress) : ChecklistOverviewUiState
}

@HiltViewModel
class ChecklistOverviewViewModel @Inject constructor(
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

    val uiState: StateFlow<ChecklistOverviewUiState> = checklistRepository.getProgress()
        .map { progress ->
            ChecklistOverviewUiState.Success(progress)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChecklistOverviewUiState.Loading
        )
}
