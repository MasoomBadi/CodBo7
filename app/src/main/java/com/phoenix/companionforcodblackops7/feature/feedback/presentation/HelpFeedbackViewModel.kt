package com.phoenix.companionforcodblackops7.feature.feedback.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.feedback.domain.repository.FeedbackRepository
import com.phoenix.companionforcodblackops7.feature.feedback.domain.repository.FeedbackResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val feedbackText: String = "",
    val isSubmitting: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class HelpFeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun onFeedbackTextChange(text: String) {
        _uiState.update { it.copy(feedbackText = text) }
    }

    fun submitFeedback() {
        val currentText = _uiState.value.feedbackText
        if (currentText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            when (val result = feedbackRepository.submitFeedback(currentText)) {
                is FeedbackResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            feedbackText = "",
                            showSuccessDialog = true
                        )
                    }
                }
                is FeedbackResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showErrorDialog = true,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }

    fun dismissErrorDialog() {
        _uiState.update { it.copy(showErrorDialog = false) }
    }
}
