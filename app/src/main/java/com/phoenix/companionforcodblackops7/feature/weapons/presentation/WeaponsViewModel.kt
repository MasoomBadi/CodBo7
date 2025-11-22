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
    ) { weapons, badgeChangeTimestamp ->
        timber.log.Timber.d("WeaponsViewModel: Badge change detected at $badgeChangeTimestamp, refreshing ${weapons.size} weapons")
        weapons.map { weapon ->
            val (completed, total) = try {
                masteryBadgeRepository.getBadgeProgress(weapon.id)
            } catch (e: Exception) {
                0 to 0
            }
            timber.log.Timber.d("WeaponsViewModel: Weapon ${weapon.id} (${weapon.displayName}): $completed/$total badges")
            WeaponWithBadges(
                weapon = weapon,
                completedBadges = completed,
                totalBadges = total
            )
        }.groupBy { it.weapon.category }
            .toSortedMap(compareBy { it })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily, // Keep flow alive even when screen not visible
        initialValue = emptyMap()
    )
}
