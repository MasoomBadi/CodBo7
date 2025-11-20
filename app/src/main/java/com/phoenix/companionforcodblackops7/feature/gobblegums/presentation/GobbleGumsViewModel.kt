package com.phoenix.companionforcodblackops7.feature.gobblegums.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.model.GobbleGum
import com.phoenix.companionforcodblackops7.feature.gobblegums.domain.repository.GobbleGumsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for GobbleGums list screen
 */
sealed class GobbleGumsUiState {
    data object Loading : GobbleGumsUiState()
    data class Success(
        val gobblegums: List<GobbleGum>,
        val allTags: List<String>,
        val selectedTag: String? = null,
        val filteredGobblegums: List<GobbleGum> = gobblegums
    ) : GobbleGumsUiState()
    data class Error(val message: String) : GobbleGumsUiState()
}

/**
 * ViewModel for GobbleGums (Zombie mode)
 */
@HiltViewModel
class GobbleGumsViewModel @Inject constructor(
    private val repository: GobbleGumsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GobbleGumsUiState>(GobbleGumsUiState.Loading)
    val uiState: StateFlow<GobbleGumsUiState> = _uiState.asStateFlow()

    init {
        loadGobbleGums()
    }

    private fun loadGobbleGums() {
        viewModelScope.launch {
            repository.getGobbleGums()
                .catch { e ->
                    Timber.e(e, "Error loading gobblegums")
                    _uiState.value = GobbleGumsUiState.Error(
                        e.message ?: "Failed to load gobblegums"
                    )
                }
                .collect { gobblegums ->
                    val allTags = extractAllTags(gobblegums)
                    _uiState.value = GobbleGumsUiState.Success(
                        gobblegums = gobblegums,
                        allTags = allTags,
                        selectedTag = null,
                        filteredGobblegums = gobblegums
                    )
                }
        }
    }

    /**
     * Extract all unique tags from gobblegums
     */
    private fun extractAllTags(gobblegums: List<GobbleGum>): List<String> {
        return gobblegums
            .flatMap { it.getTagsList() }
            .distinct()
            .sorted()
    }

    /**
     * Filter gobblegums by tag
     */
    fun filterByTag(tag: String?) {
        val currentState = _uiState.value
        if (currentState is GobbleGumsUiState.Success) {
            val filtered = if (tag == null) {
                currentState.gobblegums
            } else {
                currentState.gobblegums.filter { it.matchesTag(tag) }
            }

            _uiState.value = currentState.copy(
                selectedTag = tag,
                filteredGobblegums = filtered
            )
        }
    }

    /**
     * Clear tag filter
     */
    fun clearFilter() {
        filterByTag(null)
    }
}
