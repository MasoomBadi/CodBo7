package com.phoenix.companionforcodblackops7.feature.masterybadge.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.model.MasteryBadge
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository.MasteryBadgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Weapon Mastery Screen
 * Manages badge data and completion state for a specific weapon
 */
@HiltViewModel
class WeaponMasteryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MasteryBadgeRepository
) : ViewModel() {

    // Extract navigation arguments
    private val weaponId: Int = savedStateHandle.get<String>("weaponId")?.toIntOrNull() ?: 0
    private val weaponName: String = savedStateHandle.get<String>("weaponName") ?: ""
    private val weaponCategory: String = savedStateHandle.get<String>("weaponCategory") ?: ""

    // Selected mode tab
    private val _selectedMode = MutableStateFlow("multiplayer")
    val selectedMode: StateFlow<String> = _selectedMode.asStateFlow()

    // All badges from database with completion status
    private val badgesFlow = repository.getBadgesForWeapon(weaponId)

    // UI state combining badges and selected mode
    val uiState: StateFlow<WeaponMasteryUiState> = combine(
        badgesFlow,
        _selectedMode
    ) { allBadges, selectedMode ->
        if (allBadges.isEmpty()) {
            WeaponMasteryUiState.Empty
        } else {
            // Get available modes dynamically from data
            val availableModes = allBadges.map { it.mode }.distinct().sorted()

            // Filter badges by selected mode
            val badgesForMode = allBadges.filter { it.mode == selectedMode }

            // Calculate progress
            val completedCount = allBadges.count { it.isCompleted }
            val totalCount = allBadges.size

            WeaponMasteryUiState.Success(
                weaponId = weaponId,
                weaponName = weaponName,
                weaponCategory = weaponCategory,
                availableModes = availableModes,
                selectedMode = selectedMode,
                badges = badgesForMode,
                completedCount = completedCount,
                totalCount = totalCount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeaponMasteryUiState.Loading
    )

    /**
     * Switch between mode tabs (multiplayer/zombies/etc.)
     */
    fun selectMode(mode: String) {
        _selectedMode.value = mode
        Timber.d("Selected mode: $mode for weapon $weaponId")
    }

    /**
     * Toggle badge completion checkbox
     * Only allows toggling if badge is not locked (previous badges are completed)
     */
    fun toggleBadge(badge: MasteryBadge) {
        // Prevent toggling locked badges
        if (badge.isLocked) {
            Timber.d("Cannot toggle locked badge: ${badge.badgeLevel} / ${badge.mode}")
            return
        }

        viewModelScope.launch {
            try {
                repository.toggleBadgeCompletion(
                    weaponId = badge.weaponId,
                    badgeLevel = badge.badgeLevel,
                    mode = badge.mode
                )
                Timber.d("Toggled badge: ${badge.badgeLevel} / ${badge.mode}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle badge")
            }
        }
    }
}

/**
 * UI state sealed class for weapon mastery screen
 */
sealed class WeaponMasteryUiState {
    data object Loading : WeaponMasteryUiState()
    data object Empty : WeaponMasteryUiState()
    data class Success(
        val weaponId: Int,
        val weaponName: String,
        val weaponCategory: String,
        val availableModes: List<String>, // Dynamically loaded (e.g., ["multiplayer", "zombies"])
        val selectedMode: String,
        val badges: List<MasteryBadge>, // Badges for selected mode, sorted by sort_order
        val completedCount: Int,
        val totalCount: Int
    ) : WeaponMasteryUiState()
}
