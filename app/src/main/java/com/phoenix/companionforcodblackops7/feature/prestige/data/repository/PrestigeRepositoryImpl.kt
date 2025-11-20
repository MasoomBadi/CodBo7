package com.phoenix.companionforcodblackops7.feature.prestige.data.repository

import com.phoenix.companionforcodblackops7.feature.prestige.domain.model.PrestigeItem
import com.phoenix.companionforcodblackops7.feature.prestige.domain.model.PrestigeType
import com.phoenix.companionforcodblackops7.feature.prestige.domain.repository.PrestigeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class PrestigeRepositoryImpl @Inject constructor() : PrestigeRepository {

    override fun getAllPrestigeItems(): Flow<List<PrestigeItem>> {
        val items = mutableListOf<PrestigeItem>()

        // Military Levels 1-55
        for (level in 1..55) {
            items.add(
                PrestigeItem(
                    id = "military_$level",
                    name = "Military Rank $level",
                    type = PrestigeType.MILITARY,
                    level = level,
                    description = "Reach Military Rank $level"
                )
            )
        }

        // Prestige 1-10
        for (prestige in 1..10) {
            items.add(
                PrestigeItem(
                    id = "prestige_$prestige",
                    name = "Prestige $prestige",
                    type = PrestigeType.PRESTIGE,
                    level = prestige,
                    description = "Complete Prestige $prestige"
                )
            )
        }

        // Prestige Master Milestones
        val masterMilestones = listOf(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)
        masterMilestones.forEach { milestone ->
            items.add(
                PrestigeItem(
                    id = "master_$milestone",
                    name = "Prestige Master $milestone",
                    type = PrestigeType.PRESTIGE_MASTER,
                    level = milestone,
                    description = "Reach Prestige Master Level $milestone"
                )
            )
        }

        return flowOf(items)
    }
}
