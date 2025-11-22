package com.phoenix.companionforcodblackops7.feature.weapons.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.companionforcodblackops7.feature.masterybadge.domain.repository.MasteryBadgeRepository
import com.phoenix.companionforcodblackops7.feature.weapons.domain.repository.WeaponsRepository
import com.phoenix.companionforcodblackops7.feature.weapons.presentation.model.WeaponWithBadges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WeaponsViewModel @Inject constructor(
    private val weaponsRepository: WeaponsRepository,
    private val masteryBadgeRepository: MasteryBadgeRepository
) : ViewModel() {

    val weaponsByCategory: StateFlow<Map<String, List<WeaponWithBadges>>> = combine(
        weaponsRepository.getAllWeapons(),
        // Observe all badge changes to trigger refresh when any badge is toggled
        masteryBadgeRepository.observeAllBadgeChanges()
    ) { weapons, _ ->
        weapons.map { weapon ->
            val (completed, total) = try {
                masteryBadgeRepository.getBadgeProgress(weapon.id)
            } catch (e: Exception) {
                0 to 0
            }
            WeaponWithBadges(
                weapon = weapon,
                completedBadges = completed,
                totalBadges = total
            )
        }.groupBy { it.weapon.category }
            .toSortedMap(compareBy { it })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
}
